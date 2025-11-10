/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2025. All rights reserved.
 */

package com.huawei.cloududn.dialingtest.controller.executormanagement.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huawei.cloududn.dialingtest.controller.executormanagement.websocket.dto.WssMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Session registry for WebSocket connections.
 *
 * <p>Provides thread-safe session storage and message sending.</p>
 *
 * @author g00940940
 * @since 2025-11-06
 */
@Component
public class WebSocketSessionRegistry {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketSessionRegistry.class);

    private final Map<String, WebSocketSession> sessionMap = new ConcurrentHashMap<>();

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Add a new session to the registry.
     *
     * @param session WebSocket session
     */
    public void addSession(WebSocketSession session) {
        if (session == null) {
            logger.warn("Attempted to add null WebSocketSession");
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
            // no-op
        }
        sessionMap.remove(sessionId);
        logger.debug("Session removed, sessionId={}", sessionId);
    }

    /**
     * Get session by id.
     *
     * @param sessionId session id
     * @return WebSocketSession or null
     */
    public WebSocketSession getSession(String sessionId) {
        return sessionMap.get(sessionId);
    }

    /**
     * Send a JSON message to the given session id.
     *
     * @param sessionId target session id
     * @param message   message wrapper
     * @throws IOException when sending fails
     */
    public void sendMessage(String sessionId, WssMessage message) throws IOException {
        WebSocketSession session = sessionMap.get(sessionId);
        if (session == null) {
            logger.warn("Session not found for sessionId={}", sessionId);
            return;
        }
        String json = objectMapper.writeValueAsString(message);
        session.sendMessage(new TextMessage(json));
    }
}


