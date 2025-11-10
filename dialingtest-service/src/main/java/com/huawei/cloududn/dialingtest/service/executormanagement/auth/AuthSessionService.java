/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2025. All rights reserved.
 */

package com.huawei.cloududn.dialingtest.service.executormanagement.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.huawei.cloududn.dialingtest.controller.executormanagement.websocket.dto.WssMessage;
import com.huawei.cloududn.dialingtest.dao.executormanagement.AgentUserDao;
import com.huawei.cloududn.dialingtest.dao.executormanagement.ExecutorDao;
import com.huawei.cloududn.dialingtest.model.AgentUser;
import com.huawei.cloududn.dialingtest.service.executormanagement.task.WssMessageSender;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

import javax.websocket.Session;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

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
    private AgentUserDao agentUserDao;

    @Autowired
    private ExecutorDao executorDao;

    @Autowired
    private com.huawei.cloududn.dialingtest.service.executormanagement.SessionBindingRegistry registry;

    /**
     * Handle register_request: generate and send challenge.
     *
     * @param data    request data
     * @param session session
     */
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
     * Handle register_auth: verify response and bind token.
     *
     * @param data    auth data
     * @param session session
     */
    public void handleRegisterAuth(JsonNode data, Session session) {
        PendingAuthContext ctx = pendingMap.get(session.getId());
        if (ctx == null || isExpired(ctx)) {
            sendAck(session.getId(), false, "challenge expired or not found", null);
            logger.warn("Auth failed: challenge missing, sessionId={}", session.getId());
            return;
        }
        String response = getText(data, "response");
        logger.debug("Querying agent user from database, username={}", ctx.username);
        AgentUser user = agentUserDao.findByUsername(ctx.username);
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
        try {
            logger.debug("Sending auth message, sessionId={}, messageType={}", sessionId, msg.getMessage_type());
            wssMessageSender.send(sessionId, msg);
        } catch (Exception e) {
            logger.error("Failed to send auth message, sessionId={}, messageType={}", sessionId, msg.getMessage_type(), e);
        }
    }

    private static class PendingAuthContext {
        private final String username;
        private final String executorName;
        private final String challenge;
        private final Instant createdAt;

        private PendingAuthContext(String username, String executorName, String challenge, Instant createdAt) {
            this.username = username;
            this.executorName = executorName;
            this.challenge = challenge;
            this.createdAt = createdAt;
        }
    }
}


