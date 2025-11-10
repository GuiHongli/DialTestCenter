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

import java.io.IOException;

import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

/**
 * WebSocket endpoint for agent connections.
 *
 * <p>Acts as the single gateway for agent registration, heartbeat and task reporting.</p>
 *
 * @author g00940940
 * @since 2025-11-04
 */
@Component
@ServerEndpoint("/wss/agents")
public class ExecutorWebsocketEndpoint {

    private static final Logger logger = LoggerFactory.getLogger(ExecutorWebsocketEndpoint.class);

    private static WebSocketSessionRegistry sessionRegistry;

    private static WssMessageDispatcher dispatcher;

    private static ExecutorMgmtService executorMgmtService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public void setSessionRegistry(WebSocketSessionRegistry registry) {
        ExecutorWebsocketEndpoint.sessionRegistry = registry;
    }

    @Autowired
    public void setDispatcher(WssMessageDispatcher disp) {
        ExecutorWebsocketEndpoint.dispatcher = disp;
    }

    @Autowired
    public void setExecutorMgmtService(ExecutorMgmtService service) {
        ExecutorWebsocketEndpoint.executorMgmtService = service;
    }

    @OnOpen
    public void onOpen(Session session) {
        sessionRegistry.addSession(session);
        logger.info("Agent connected, sessionId={}", session.getId());
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        if (message == null || message.trim().isEmpty()) {
            logger.warn("Received empty message, sessionId={}", session.getId());
            return;
        }
        try {
            JsonNode node = objectMapper.readTree(message);
            String messageType = node.has("message_type") ? node.get("message_type").asText() : "";
            logger.debug("Received WebSocket message, sessionId={}, messageType={}", session.getId(), messageType);
            JsonNode dataNode = node.get("data");
            if (dataNode == null) {
                dataNode = objectMapper.createObjectNode();
            }
            dispatcher.dispatch(messageType, dataNode, session);
        } catch (IOException e) {
            logger.error("Failed to parse WebSocket message, sessionId={}", session.getId(), e);
        }
    }

    @OnClose
    public void onClose(Session session, CloseReason reason) {
        sessionRegistry.removeSession(session.getId());
        logger.info("Agent disconnected, sessionId={}, code={}", session.getId(), reason.getReasonPhrase());
        executorMgmtService.handleExecutorDisconnect(session.getId());
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        logger.error("WebSocket error, sessionId={}", session.getId(), throwable);
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
     * @return Session or null
     */
    public Session getSession(String sessionId) {
        return sessionRegistry.getSession(sessionId);
    }
}
