/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2025. All rights reserved.
 */

package com.huawei.cloududn.dialingtest.controller.executormanagement.websocket;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.huawei.cloududn.dialingtest.controller.executormanagement.websocket.dto.WssMessage;
import com.huawei.cloududn.dialingtest.service.executormanagement.ExecutorMgmtService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket endpoint for agent connections.
 *
 * <p>Acts as the single gateway for agent registration, heartbeat and task reporting.</p>
 *
 * @author g00940940
 * @since 2025-11-04
 */
@Component
public class ExecutorWebsocketEndpoint extends TextWebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(ExecutorWebsocketEndpoint.class);

    @Autowired
    private WebSocketSessionRegistry sessionRegistry;

    @Autowired
    private WssMessageDispatcher dispatcher;

    @Autowired
    private ExecutorMgmtService executorMgmtService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessionRegistry.addSession(session);
        logger.info("Agent connected, sessionId={}", session.getId());
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        String payload = message.getPayload();
        if (payload == null || payload.trim().isEmpty()) {
            logger.warn("Received empty message, sessionId={}", session.getId());
            return;
        }
        try {
            JsonNode node = objectMapper.readTree(payload);
            String messageType = node.has("message_type") ? node.get("message_type").asText() : "";
            logger.debug("Received WebSocket message, sessionId={}, messageType={}", session.getId(), messageType);
            JsonNode dataNode = node.get("data");
            if (dataNode == null) {
                dataNode = objectMapper.createObjectNode();
            }
            dispatcher.dispatch(messageType, dataNode, session);
        } catch (IOException e) {
            logger.error("Failed to parse WebSocket message, sessionId={}", session.getId(), e);
            throw e;
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessionRegistry.removeSession(session.getId());
        logger.info("Agent disconnected, sessionId={}, code={}", session.getId(), status.getCode());
        executorMgmtService.handleExecutorDisconnect(session.getId());
    }

    /**
     * Send JSON message to a specific session.
     *
     * @param sessionId target session id
     * @param message   message wrapper
     * @throws IOException when send fails
     */
    public void sendMessage(String sessionId, WssMessage message) throws IOException {
        logger.debug("Sending WebSocket message, sessionId={}, messageType={}", sessionId, message.getMessage_type());
        try {
            sessionRegistry.sendMessage(sessionId, message);
            logger.debug("WebSocket message sent successfully, sessionId={}", sessionId);
        } catch (IOException e) {
            logger.error("Failed to send WebSocket message, sessionId={}, messageType={}", sessionId, message.getMessage_type(), e);
            throw e;
        }
    }

    /**
     * Get a session by id.
     *
     * @param sessionId session id
     * @return WebSocketSession or null
     */
    public WebSocketSession getSession(String sessionId) {
        return sessionRegistry.getSession(sessionId);
    }
}


