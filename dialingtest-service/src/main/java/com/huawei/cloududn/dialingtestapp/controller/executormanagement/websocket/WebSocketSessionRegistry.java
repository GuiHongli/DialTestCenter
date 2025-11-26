/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2025. All rights reserved.
 */

package com.huawei.cloududn.dialingtestapp.controller.executormanagement.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.websocket.Session;

/**
 * Session registry for WebSocket connections.
 * V3版本：支持二进制消息发送
 *
 * <p>Provides thread-safe session storage and binary message sending.</p>
 *
 * @author g00940940
 * @since 2025-11-11
 */
@Component
public class WebSocketSessionRegistry {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketSessionRegistry.class);

    private final Map<String, Session> sessionMap = new ConcurrentHashMap<>();

    /**
     * Add a new session to the registry.
     *
     * @param session WebSocket session
     */
    public void addSession(Session session) {
        if (session == null) {
            logger.warn("Attempted to add null Session");
            return;
        }
        sessionMap.put(session.getId(), session);
        logger.debug("Session added, sessionId={}", session.getId());
    }

    /**
     * Remove a session by id.
     *
     * @param sessionId session id
     */
    public void removeSession(String sessionId) {
        if (sessionId == null) {
            logger.warn("Attempted to remove session with null id");
        } else {
            sessionMap.remove(sessionId);
            logger.debug("Session removed, sessionId={}", sessionId);
        }
    }

    /**
     * Get session by id.
     *
     * @param sessionId session id
     * @return Session or null
     */
    public Session getSession(String sessionId) {
        return sessionMap.get(sessionId);
    }

    /**
     * Send a text message to the given session id.
     *
     * @param sessionId target session id
     * @param message   text message
     */
    public void sendText(String sessionId, String message) {
        Session session = sessionMap.get(sessionId);
        if (session == null) {
            logger.warn("Session not found for sessionId={}", sessionId);
            return;
        }
        if (!session.isOpen()) {
            logger.warn("Session is closed, sessionId={}", sessionId);
            return;
        }
        try {
            session.getBasicRemote().sendText(message);
        } catch (IOException e) {
            logger.error("Failed to send text message to sessionId={}", sessionId, e);
        }
    }

    /**
     * Send a binary TLV message to the given session id.
     * V3版本：使用二进制格式发送
     *
     * @param sessionId target session id
     * @param buffer    TLV binary buffer
     */
    public void sendBinary(String sessionId, ByteBuffer buffer) {
        Session session = sessionMap.get(sessionId);
        if (session == null) {
            logger.warn("Session not found for sessionId={}", sessionId);
            return;
        }
        if (!session.isOpen()) {
            logger.warn("Session is closed, sessionId={}", sessionId);
            return;
        }
        try {
            session.getBasicRemote().sendBinary(buffer);
        } catch (IOException e) {
            logger.error("Failed to send binary message to sessionId={}", sessionId, e);
        }
    }
}
