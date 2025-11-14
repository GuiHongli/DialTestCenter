/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2025. All rights reserved.
 */

package com.huawei.cloududn.dialingtestapp.service.executormanagement.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.huawei.cloududn.dialingtestapp.controller.executormanagement.websocket.dto.RegisterChallengeDto;
import com.huawei.cloududn.dialingtestapp.controller.executormanagement.websocket.dto.RegisterRequestDto;
import com.huawei.cloududn.dialingtestapp.controller.executormanagement.websocket.dto.RegisterResponseDto;
import com.huawei.cloududn.dialingtestapp.controller.executormanagement.websocket.dto.RegisterResultDto;
import com.huawei.cloududn.dialingtestapp.controller.executormanagement.websocket.dto.WssMessage;
import com.huawei.cloududn.dialingtestapp.dao.executormanagement.ExecutorDao;
import com.huawei.cloududn.dialingtest.model.DialUser;
import com.huawei.cloududn.dialingtestapp.service.DialUserService;
import com.huawei.cloududn.dialingtestapp.service.executormanagement.SessionBindingRegistry;
import com.huawei.cloududn.dialingtestapp.controller.executormanagement.websocket.flow.WssMessageSender;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;

import javax.websocket.Session;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Arrays;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * CHAP authentication and session binding service.
 *
 * <p>Handles register_request and register_auth messages.</p>
 *
 * @author g00940940
 * @since 2025-11-04
 */
@Service
public class AuthSessionService {

    private static final Logger logger = LoggerFactory.getLogger(AuthSessionService.class);

    private static final long CHALLENGE_TTL_MILLIS = 120_000L;

    private final Map<String, PendingAuthContext> pendingMap = new ConcurrentHashMap<>();

    @Autowired
    private WssMessageSender wssMessageSender;

    @Autowired
    private DialUserService dialUserService;

    @Autowired
    private ExecutorDao executorDao;

    @Autowired
    private SessionBindingRegistry registry;
    
    private final AtomicInteger challengeIdCounter = new AtomicInteger(1);

    /**
     * Handle register_request: generate and send challenge (Legacy JSON version).
     *
     * @param data    request data
     * @param session session
     * @deprecated Use {@link #handleRegisterRequest(RegisterRequestDto, Session)} for V3 TLV protocol
     */
    @Deprecated
    public void handleRegisterRequest(JsonNode data, Session session) {
        String executorName = getText(data, "name");
        String username = getText(data, "username");
        byte[] challengeBytes = generateChallenge();
        String challenge = Base64.getEncoder().encodeToString(challengeBytes);
        pendingMap.put(session.getId(), new PendingAuthContext(username, executorName, challenge, Instant.now()));
        ObjectNode resp = objectNode("challenge", challenge);
        send(session.getId(), new WssMessage("register_challenge", resp));
        logger.info("Sent register_challenge to sessionId={}, executor={}", session.getId(), executorName);
    }
    
    /**
     * Handle Register-Request (0x01): generate and send challenge (V3 TLV version).
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
        
        // Store pending context with empty username (will be provided in response)
        pendingMap.put(session.getId(), 
            new PendingAuthContext("", hostname, Base64.getEncoder().encodeToString(challengeBytes), Instant.now(), challengeId));
        
        // Send Register-Challenge (V4 JSON format)
        RegisterChallengeDto challengeDto = new RegisterChallengeDto(challengeId, 
                Base64.getEncoder().encodeToString(challengeBytes));
        wssMessageSender.sendJsonMessage(session.getId(), challengeDto);

        // DEBUG: Log challenge details
        logger.info("Sent Register-Challenge to sessionId={}, challengeId={}, hostname={}, challengeBytes.length={}",
            session.getId(), challengeId, hostname, challengeBytes.length);
        logger.debug("Challenge bytes (hex): {}", bytesToHexString(challengeBytes));
        logger.debug("Challenge Base64: {}", Base64.getEncoder().encodeToString(challengeBytes));
    }

    /**
     * Handle register_auth: verify response and bind token (Legacy JSON version).
     *
     * @param data    auth data
     * @param session session
     * @deprecated Use {@link #handleRegisterResponse(TlvDecoder.DecodedMessage, Session)} for V3 TLV protocol
     */
    @Deprecated
    public void handleRegisterAuth(JsonNode data, Session session) {
        PendingAuthContext ctx = pendingMap.get(session.getId());
        if (ctx == null || isExpired(ctx)) {
            sendAck(session.getId(), false, "challenge expired or not found", null);
            logger.warn("Auth failed: challenge missing, sessionId={}", session.getId());
            return;
        }
        String response = getText(data, "response");
        logger.debug("Querying dial user from database, username={}", ctx.username);
        DialUser user = dialUserService.findByUsername(ctx.username);
        if (user == null) {
            sendAck(session.getId(), false, "user not found", null);
            logger.warn("Auth failed: user not found, username={}", ctx.username);
            return;
        }
        String check = computeChapResponse(user.getPassword(), ctx.challenge);
        if (!check.equalsIgnoreCase(response)) {
            sendAck(session.getId(), false, "invalid response", null);
            logger.warn("Auth failed: invalid response, sessionId={}", session.getId());
            return;
        }
        String token = UUID.randomUUID().toString();
        logger.debug("Updating executor token and status, name={}", ctx.executorName);
        executorDao.updateTokenAndStatus(ctx.executorName, token, 1, Instant.now());
        registry.bind(session.getId(), ctx.executorName);
        sendAck(session.getId(), true, "", token);
        pendingMap.remove(session.getId());
        logger.info("Auth success, executor={}, sessionId={}", ctx.executorName, session.getId());
    }
    
    /**
     * Handle Register-Response (V4 JSON format).
     * V4版本：处理JSON格式的注册响应
     *
     * @param dto     Register Response DTO
     * @param session WebSocket session
     */
    public void handleRegisterResponse(RegisterResponseDto dto, Session session) {
        int challengeId = dto.getChallengeId();
        String username = dto.getUsername();
        String responseHex = dto.getResponse();
        
        logger.info("Received Register-Response from sessionId={}, challengeId={}, username={}, response={}",
                session.getId(), challengeId, username, responseHex);
        
        // Verify pending context
        PendingAuthContext ctx = pendingMap.get(session.getId());
        if (ctx == null || isExpired(ctx)) {
            sendRegisterResultV4(session.getId(), 1, "Challenge expired or not found", null);
            logger.warn("Auth failed: challenge missing or expired, sessionId={}", session.getId());
            return;
        }
        
        if (ctx.challengeId != challengeId) {
            sendRegisterResultV4(session.getId(), 2, "Challenge ID mismatch", null);
            logger.warn("Auth failed: challenge ID mismatch, sessionId={}", session.getId());
            return;
        }
        
        // Query user from dial_users table
        DialUser user = dialUserService.findByUsername(username);
        if (user == null) {
            sendRegisterResultV4(session.getId(), 3, "User not found", null);
            logger.warn("Auth failed: user not found, username={}", username);
            return;
        }
        
        // Verify CHAP response using NTLM Hash
        byte[] expectedResponse = computeChapResponseV3(user.getPassword(), ctx.challenge);
        String expectedHex = bytesToHexString(expectedResponse);
        
        logger.debug("Auth verification: username={}, expectedHex={}, actualHex={}",
                username, expectedHex, responseHex);
        
        if (!expectedHex.equalsIgnoreCase(responseHex)) {
            sendRegisterResultV4(session.getId(), 4, "Authentication failed", null);
            logger.warn("Auth failed: incorrect response, username={}", username);
            return;
        }
        
        // Generate token
        long token = generateTokenV3();
        
        // Update executor database
        try {
            executorDao.saveOrUpdateExecutor(ctx.executorName, token, "ONLINE");
            logger.info("Executor registered successfully: hostname={}, token={}", ctx.executorName, token);
        } catch (IllegalArgumentException e) {
            sendRegisterResultV4(session.getId(), 5, "Database error: " + e.getMessage(), null);
            logger.error("Failed to update executor in database, hostname={}", ctx.executorName, e);
            return;
        }
        
        // Bind session to executor
        registry.bind(session.getId(), ctx.executorName, token);
        
        // Send Register-Result - Success
        sendRegisterResultV4(session.getId(), 0, "Authentication successful", token);
        
        // Cleanup
        pendingMap.remove(session.getId());
        
        logger.info("Authentication successful: sessionId={}, hostname={}, username={}, token={}", 
                session.getId(), ctx.executorName, username, token);
    }

    /**
     * Handle Register-Response (0x03): verify response and bind token (V3 TLV version).
     * 阶段3→4：接收认证应答，验证后发送结果
     * 已废弃：V4版本使用JSON格式，参见上面的 handleRegisterResponse(RegisterResponseDto dto, Session session) 方法
     *
     * @param decoded Decoded TLV message
     * @param session WebSocket session
     */
    /*
    public void handleRegisterResponse(TlvDecoder.DecodedMessage decoded, Session session) {
        // Extract fields from TLV message
        int challengeId = decoded.getField(FieldTag.CHALLENGE_ID).getAsInt();
        String username = decoded.getField(FieldTag.USERNAME).getAsString();
        byte[] response = decoded.getField(FieldTag.RESPONSE).getAsBytes();

        logger.info("Received Register-Response from sessionId={}, challengeId={}, username={}, response.length={}",
            session.getId(), challengeId, username, response.length);
        logger.debug("Response bytes (hex): {}", bytesToHexString(response));
        
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
        
        // Query user from dial_users table via DialUserService
        DialUser user = dialUserService.findByUsername(username);
        if (user == null) {
            sendRegisterResult(session.getId(), 3, "User not found", null);
            logger.warn("Auth failed: user not found, username={}", username);
            return;
        }
        
        // Verify CHAP response using NTLM Hash from dial_users table
        byte[] expectedResponse = computeChapResponseV3(user.getPassword(), ctx.challenge);
        logger.debug("Auth verification: username={}, ntlmHash={}, challengeBase64={}",
            username, user.getPassword(), ctx.challenge);
        logger.debug("Expected response (hex): {}", bytesToHexString(expectedResponse));
        logger.debug("Actual response (hex): {}", bytesToHexString(response));

        if (!Arrays.equals(expectedResponse, response)) {
            sendRegisterResult(session.getId(), 4, "Authentication failed", null);
            logger.warn("Auth failed: incorrect response, username={}", username);
            return;
        }
        
        // Generate token (8 bytes)
        long token = generateTokenV3();
        
        // Update executor database
        try {
            executorDao.saveOrUpdateExecutor(ctx.executorName, token, "ONLINE");
            logger.info("Executor registered successfully: hostname={}, token={}", ctx.executorName, token);
        } catch (IllegalArgumentException e) {
            sendRegisterResult(session.getId(), 5, "Database error: " + e.getMessage(), null);
            logger.error("Failed to update executor in database, hostname={}", ctx.executorName, e);
            return;
        }
        
        // Bind session to executor
        registry.bind(session.getId(), ctx.executorName, token);
        
        // Send Register-Result (0x04) - Success
        sendRegisterResult(session.getId(), 0, "Authentication successful", token);
        
        // Cleanup
        pendingMap.remove(session.getId());
        
        logger.info("Authentication successful: sessionId={}, hostname={}, username={}, token={}", 
            session.getId(), ctx.executorName, username, token);
    }
    */

    private static String getText(JsonNode node, String field) {
        return node != null && node.has(field) ? node.get(field).asText("") : "";
    }

    private static byte[] generateChallenge() {
        byte[] bytes = new byte[16];
        new SecureRandom().nextBytes(bytes);
        return bytes;
    }

    private boolean isExpired(PendingAuthContext ctx) {
        return ctx.createdAt.plusMillis(CHALLENGE_TTL_MILLIS).isBefore(Instant.now());
    }

    private void sendAck(String sessionId, boolean success, String error, String token) {
        ObjectNode data = objectNode("status", success ? "success" : "failed");
        if (token != null) {
            data.put("token", token);
        }
        data.put("error_message", error);
        send(sessionId, new WssMessage("register_ack", data));
    }

    private ObjectNode objectNode(String key, String value) {
        ObjectNode node = new ObjectNode(com.fasterxml.jackson.databind.node.JsonNodeFactory.instance);
        node.put(key, value);
        return node;
    }

    /**
     * Compute CHAP response according to design specification.
     *
     * <p>Formula: Response = MD5(NTLM-Hash bytes + Challenge bytes)</p>
     * <p>NTLM-Hash is stored as hex string in database, needs to be decoded</p>
     * <p>Challenge is Base64 encoded string, needs to be decoded</p>
     *
     * @param ntlmHashHex NTLM Hash in hex string format
     * @param challengeBase64 Challenge in Base64 format
     * @return MD5 hex string (lowercase)
     */
    private String computeChapResponse(String ntlmHashHex, String challengeBase64) {
        try {
            // Decode NTLM Hash from hex string
            byte[] ntlmHashBytes = hexStringToBytes(ntlmHashHex);
            
            // Decode Challenge from Base64
            byte[] challengeBytes = Base64.getDecoder().decode(challengeBase64);
            
            // Concatenate NTLM Hash bytes + Challenge bytes
            byte[] combined = new byte[ntlmHashBytes.length + challengeBytes.length];
            System.arraycopy(ntlmHashBytes, 0, combined, 0, ntlmHashBytes.length);
            System.arraycopy(challengeBytes, 0, combined, ntlmHashBytes.length, challengeBytes.length);
            
            // Compute MD5
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(combined);
            
            // Convert to hex string
            return bytesToHexString(digest);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid NTLM Hash or Challenge format", e);
            throw new IllegalStateException("Invalid credential format", e);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("MD5 algorithm not available", e);
        }
    }
    
    /**
     * Convert hex string to byte array.
     *
     * @param hexString Hex string (e.g., "a1b2c3")
     * @return Byte array
     */
    private static byte[] hexStringToBytes(String hexString) {
        if (hexString == null || hexString.length() % 2 != 0) {
            throw new IllegalArgumentException("Invalid hex string: " + hexString);
        }
        byte[] bytes = new byte[hexString.length() / 2];
        for (int i = 0; i < hexString.length(); i += 2) {
            bytes[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4)
                    + Character.digit(hexString.charAt(i + 1), 16));
        }
        return bytes;
    }
    
    /**
     * Convert byte array to hex string.
     *
     * @param bytes Byte array
     * @return Hex string (lowercase)
     */
    private static String bytesToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(b & 0xff);
            if (hex.length() == 1) {
                sb.append('0');
            } else {
                // no-op
            }
            sb.append(hex);
        }
        return sb.toString();
    }

    private void send(String sessionId, WssMessage msg) {
        // Legacy JSON protocol is deprecated in V3; no-op for compatibility
        logger.warn("Legacy JSON send is deprecated and ignored, sessionId={}, messageType={}",
            sessionId, msg != null ? msg.getMessage_type() : "null");
    }

    /**
     * Send Register-Result (0x04).
     *
     * @param sessionId   session ID
     * @param resultCode  result code (0=success, non-zero=failure)
     * @param description description
     * @param token       token (null if failed)
     */
    /**
     * Send Register-Result (V4 JSON format).
     * V4版本：发送JSON格式的注册结果
     */
    private void sendRegisterResultV4(String sessionId, int resultCode, String description, Long token) {
        RegisterResultDto resultDto = new RegisterResultDto(resultCode, description, token);
        wssMessageSender.sendJsonMessage(sessionId, resultDto);
    }
    
    /**
     * Send Register-Result (V3 TLV format).
     * V3版本：发送TLV格式的注册结果
     * 已废弃：V4版本不再使用TLV格式
     */
    /*
    private void sendRegisterResult(String sessionId, int resultCode, String description, Long token) {
        RegisterResultDto resultDto = new RegisterResultDto(resultCode, description, token);
        ByteBuffer buffer = DtoTlvConverter.encodeRegisterResult(resultDto);
        wssMessageSender.sendBinary(sessionId, buffer);
    }
    */
    
    /**
     * Compute CHAP response for V3: MD5(NTLM-Hash + Challenge).
     *
     * @param ntlmHash       NTLM hash stored in database (hex string)
     * @param challengeBase64 challenge bytes (Base64 encoded)
     * @return MD5 response (16 bytes)
     */
    private byte[] computeChapResponseV3(String ntlmHash, String challengeBase64) {
        try {
            byte[] ntlmBytes = hexStringToBytes(ntlmHash);
            byte[] challengeBytes = Base64.getDecoder().decode(challengeBase64);

            logger.debug("CHAP calculation: ntlmBytes.length={}, challengeBytes.length={}", ntlmBytes.length, challengeBytes.length);
            logger.debug("NTLM bytes (hex): {}", bytesToHexString(ntlmBytes));
            logger.debug("Challenge bytes (hex): {}", bytesToHexString(challengeBytes));

            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(ntlmBytes);
            md5.update(challengeBytes);
            byte[] result = md5.digest();

            logger.debug("CHAP result (hex): {}", bytesToHexString(result));
            return result;
        } catch (NoSuchAlgorithmException e) {
            logger.error("Failed to compute CHAP response", e);
            return new byte[16];
        }
    }
    
    /**
     * Generate 8-byte token.
     *
     * @return token as long
     */
    private long generateTokenV3() {
        byte[] bytes = new byte[8];
        new SecureRandom().nextBytes(bytes);
        long token = 0;
        for (int i = 0; i < 8; i++) {
            token = (token << 8) | (bytes[i] & 0xFF);
        }
        return token;
    }
    
    private static class PendingAuthContext {
        private final String username;
        private final String executorName;
        private final String challenge;
        private final Instant createdAt;
        private final int challengeId;

        private PendingAuthContext(String username, String executorName, String challenge, Instant createdAt) {
            this(username, executorName, challenge, createdAt, 0);
        }
        
        private PendingAuthContext(String username, String executorName, String challenge, Instant createdAt, int challengeId) {
            this.username = username;
            this.executorName = executorName;
            this.challenge = challenge;
            this.createdAt = createdAt;
            this.challengeId = challengeId;
        }
    }
}



