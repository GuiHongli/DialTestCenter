/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2025. All rights reserved.
 */

package com.huawei.cloududn.dialingtest.service.executormanagement.auth;

import com.huawei.cloududn.dialingtest.controller.executormanagement.websocket.codec.DtoTlvConverter;
import com.huawei.cloududn.dialingtest.controller.executormanagement.websocket.codec.TlvDecoder;
import com.huawei.cloududn.dialingtest.controller.executormanagement.websocket.dto.RegisterChallengeDto;
import com.huawei.cloududn.dialingtest.controller.executormanagement.websocket.dto.RegisterRequestDto;
import com.huawei.cloududn.dialingtest.controller.executormanagement.websocket.dto.RegisterResultDto;
import com.huawei.cloududn.dialingtest.dao.executormanagement.AgentUserDao;
import com.huawei.cloududn.dialingtest.dao.executormanagement.ExecutorDao;
import com.huawei.cloududn.dialingtest.model.AgentUser;
import com.huawei.cloududn.dialingtest.service.executormanagement.SessionBindingRegistry;
import com.huawei.cloududn.dialingtest.service.executormanagement.task.WssMessageSender;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import javax.websocket.Session;

/**
 * CHAP authentication and session binding service.
 * V3版本：实现四阶段CHAP认证 (0x01→0x02→0x03→0x04)
 *
 * <p>Four-stage CHAP authentication:</p>
 * <ul>
 *   <li>Stage 1: Register-Request (0x01) - Agent sends hostname</li>
 *   <li>Stage 2: Register-Challenge (0x02) - Server sends challenge</li>
 *   <li>Stage 3: Register-Response (0x03) - Agent sends username + response</li>
 *   <li>Stage 4: Register-Result (0x04) - Server sends token or error</li>
 * </ul>
 *
 * @author g00940940
 * @since 2025-11-11
 */
@Service
public class AuthSessionServiceV3 {

    private static final Logger logger = LoggerFactory.getLogger(AuthSessionServiceV3.class);

    private static final long CHALLENGE_TTL_MILLIS = 120_000L;
    private static final int CHALLENGE_SIZE_BYTES = 16;

    private final Map<String, PendingAuthContext> pendingMap = new ConcurrentHashMap<>();
    private final AtomicInteger challengeIdCounter = new AtomicInteger(1);

    @Autowired
    private WssMessageSender wssMessageSender;

    @Autowired
    private AgentUserDao agentUserDao;

    @Autowired
    private ExecutorDao executorDao;

    @Autowired
    private SessionBindingRegistry sessionBindingRegistry;

    /**
     * Handle Register-Request (0x01): generate and send challenge
     * 阶段1→2：接收注册请求，生成并发送挑战
     *
     * @param dto     RegisterRequest DTO
     * @param session WebSocket session
     */
    public void handleRegisterRequest(RegisterRequestDto dto, Session session) {
        String hostname = dto.getHostname();
        logger.info("Received Register-Request from sessionId={}, hostname={}", 
            session.getId(), hostname);
        
        // Generate challenge
        int challengeId = challengeIdCounter.getAndIncrement();
        byte[] challengeBytes = generateChallenge();
        
        // Store pending context
        pendingMap.put(session.getId(), 
            new PendingAuthContext(challengeId, hostname, challengeBytes, Instant.now()));
        
        // Send Register-Challenge (0x02)
        RegisterChallengeDto challengeDto = new RegisterChallengeDto(challengeId, challengeBytes);
        ByteBuffer buffer = DtoTlvConverter.encodeRegisterChallenge(challengeDto);
        wssMessageSender.sendBinary(session.getId(), buffer);
        
        logger.info("Sent Register-Challenge to sessionId={}, challengeId={}, hostname={}", 
            session.getId(), challengeId, hostname);
    }

    /**
     * Handle Register-Response (0x03): verify response and bind token
     * 阶段3→4：接收认证应答，验证后发送结果
     *
     * @param decoded Decoded TLV message
     * @param session WebSocket session
     */
    public void handleRegisterResponse(TlvDecoder.DecodedMessage decoded, Session session) {
        // Extract fields from TLV message
        int challengeId = decoded.getField(com.huawei.cloududn.dialingtest.controller.executormanagement.websocket.codec.FieldTag.CHALLENGE_ID).getAsInt();
        String username = decoded.getField(com.huawei.cloududn.dialingtest.controller.executormanagement.websocket.codec.FieldTag.USERNAME).getAsString();
        byte[] response = decoded.getField(com.huawei.cloududn.dialingtest.controller.executormanagement.websocket.codec.FieldTag.RESPONSE).getAsBytes();
        
        logger.info("Received Register-Response from sessionId={}, challengeId={}, username={}", 
            session.getId(), challengeId, username);
        
        // Verify pending context
        PendingAuthContext ctx = pendingMap.get(session.getId());
        if (ctx == null || isExpired(ctx)) {
            sendRegisterResult(session.getId(), 1, "Challenge expired or not found", null);
            logger.warn("Auth failed: challenge missing or expired, sessionId={}", session.getId());
            return;
        }
        
        if (ctx.challengeId != challengeId) {
            sendRegisterResult(session.getId(), 2, "Challenge ID mismatch", null);
            logger.warn("Auth failed: challenge ID mismatch, sessionId={}", session.getId());
            return;
        }
        
        // Query user from database
        AgentUser user = agentUserDao.findByUsername(username);
        if (user == null) {
            sendRegisterResult(session.getId(), 3, "User not found", null);
            logger.warn("Auth failed: user not found, username={}", username);
            return;
        }
        
        // Verify CHAP response
        byte[] expectedResponse = computeChapResponse(user.getPassword(), ctx.challengeBytes);
        if (!Arrays.equals(expectedResponse, response)) {
            sendRegisterResult(session.getId(), 4, "Authentication failed", null);
            logger.warn("Auth failed: incorrect response, username={}", username);
            return;
        }
        
        // Generate token (8 bytes)
        long token = generateToken();
        
        // Update executor database
        try {
            executorDao.saveOrUpdateExecutor(ctx.hostname, token, "ONLINE");
            logger.info("Executor registered successfully: hostname={}, token={}", ctx.hostname, token);
        } catch (IllegalArgumentException e) {
            sendRegisterResult(session.getId(), 5, "Database error: " + e.getMessage(), null);
            logger.error("Failed to update executor in database, hostname={}", ctx.hostname, e);
            return;
        }
        
        // Bind session to executor
        sessionBindingRegistry.bind(session.getId(), ctx.hostname, token);
        
        // Send Register-Result (0x04) - Success
        sendRegisterResult(session.getId(), 0, "Authentication successful", token);
        
        // Cleanup
        pendingMap.remove(session.getId());
        
        logger.info("Authentication successful: sessionId={}, hostname={}, username={}, token={}", 
            session.getId(), ctx.hostname, username, token);
    }

    /**
     * Send Register-Result (0x04)
     *
     * @param sessionId  session ID
     * @param resultCode result code (0=success, non-zero=failure)
     * @param description description
     * @param token      token (null if failed)
     */
    private void sendRegisterResult(String sessionId, int resultCode, String description, Long token) {
        RegisterResultDto resultDto = new RegisterResultDto(resultCode, description, token);
        ByteBuffer buffer = DtoTlvConverter.encodeRegisterResult(resultDto);
        wssMessageSender.sendBinary(sessionId, buffer);
    }

    /**
     * Generate random challenge (16 bytes)
     *
     * @return challenge bytes
     */
    private byte[] generateChallenge() {
        byte[] challenge = new byte[CHALLENGE_SIZE_BYTES];
        new SecureRandom().nextBytes(challenge);
        return challenge;
    }

    /**
     * Compute CHAP response: MD5(NTLM-Hash + Challenge)
     *
     * @param ntlmHash      NTLM hash stored in database
     * @param challengeBytes challenge bytes
     * @return MD5 response (16 bytes)
     */
    private byte[] computeChapResponse(String ntlmHash, byte[] challengeBytes) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(ntlmHash.getBytes());
            md5.update(challengeBytes);
            return md5.digest();
        } catch (Exception e) {
            logger.error("Failed to compute CHAP response", e);
            return new byte[16];
        }
    }

    /**
     * Generate 8-byte token
     *
     * @return token as long
     */
    private long generateToken() {
        byte[] bytes = new byte[8];
        new SecureRandom().nextBytes(bytes);
        long token = 0;
        for (int i = 0; i < 8; i++) {
            token = (token << 8) | (bytes[i] & 0xFF);
        }
        return token;
    }

    /**
     * Check if context is expired
     *
     * @param ctx context
     * @return true if expired
     */
    private boolean isExpired(PendingAuthContext ctx) {
        return Instant.now().toEpochMilli() - ctx.timestamp.toEpochMilli() > CHALLENGE_TTL_MILLIS;
    }

    /**
     * Pending authentication context
     */
    private static class PendingAuthContext {
        final int challengeId;
        final String hostname;
        final byte[] challengeBytes;
        final Instant timestamp;

        PendingAuthContext(int challengeId, String hostname, byte[] challengeBytes, Instant timestamp) {
            this.challengeId = challengeId;
            this.hostname = hostname;
            this.challengeBytes = challengeBytes;
            this.timestamp = timestamp;
        }
    }
}

