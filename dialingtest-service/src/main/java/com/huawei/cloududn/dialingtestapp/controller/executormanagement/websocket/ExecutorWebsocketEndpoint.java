/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2025. All rights reserved.
 */

package com.huawei.cloududn.dialingtestapp.controller.executormanagement.websocket;

import com.huawei.cloududn.dialingtestapp.service.executormanagement.ExecutorMgmtService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.ByteBuffer;

import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

/**
 * WebSocket endpoint for agent connections.
 * V3版本：支持TLV二进制协议
 *
 * <p>Acts as the single gateway for agent registration, heartbeat and task reporting.</p>
 *
 * @author g00940940
 * @since 2025-11-11
 */
@Component
@ServerEndpoint("/ws/executor")
public class ExecutorWebsocketEndpoint {

    private static final Logger logger = LoggerFactory.getLogger(ExecutorWebsocketEndpoint.class);

    private static WebSocketSessionRegistry sessionRegistry;

    private static WssMessageDispatcher dispatcher;

    private static ExecutorMgmtService executorMgmtService;

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

    /**
     * 连接建立事件
     *
     * @param session WebSocket会话
     */
    @OnOpen
    public void onOpen(Session session) {
        sessionRegistry.addSession(session);
        logger.info("Agent connected, sessionId={}", session.getId());
    }

    /**
     * 接收二进制消息（V3版本）
     * V3变更：从String消息改为ByteBuffer二进制消息
     *
     * @param message 二进制消息缓冲区
     * @param session WebSocket会话
     */
    @OnMessage
    public void onMessage(ByteBuffer message, Session session) {
        if (message == null || message.remaining() == 0) {
            logger.warn("Received empty binary message, sessionId={}", session.getId());
            return;
        }
        try {
            logger.debug("Received binary WebSocket message, sessionId={}, size={} bytes",
                    session.getId(), message.remaining());
            dispatcher.dispatch(message, session);
        } catch (IllegalArgumentException e) {
            logger.error("Failed to parse TLV message, sessionId={}", session.getId(), e);
        }
    }

    /**
     * 连接关闭事件
     *
     * @param session WebSocket会话
     * @param reason  关闭原因
     */
    @OnClose
    public void onClose(Session session, CloseReason reason) {
        sessionRegistry.removeSession(session.getId());
        logger.info("Agent disconnected, sessionId={}, code={}", session.getId(), reason.getReasonPhrase());
        executorMgmtService.handleExecutorDisconnect(session.getId());
    }

    /**
     * 错误事件
     *
     * @param session   WebSocket会话
     * @param throwable 异常信息
     */
    @OnError
    public void onError(Session session, Throwable throwable) {
        logger.error("WebSocket error, sessionId={}", session.getId(), throwable);
    }

    /**
     * Send binary TLV message to a specific session.
     * V3版本：使用二进制格式发送
     *
     * @param sessionId target session id
     * @param buffer    TLV binary buffer
     * @throws IOException when send fails
     */
    public void sendBinary(String sessionId, ByteBuffer buffer) throws IOException {
        logger.debug("Sending binary WebSocket message, sessionId={}, size={} bytes",
                sessionId, buffer.remaining());
        sessionRegistry.sendBinary(sessionId, buffer);
        logger.debug("Binary WebSocket message sent successfully, sessionId={}", sessionId);
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
