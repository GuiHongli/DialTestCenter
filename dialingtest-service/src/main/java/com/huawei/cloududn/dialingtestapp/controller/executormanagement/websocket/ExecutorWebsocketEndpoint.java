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
 * V4版本：支持JSON信令（Text）和二进制分片（Binary）混合模式
 *
 * <p>Acts as the single gateway for agent registration, heartbeat and task reporting.</p>
 *
 * @author g00940940
 * @since 2025-11-14
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
     * 接收JSON信令（V4新增）
     *
     * @param message JSON消息字符串
     * @param session WebSocket会话
     */
    @OnMessage
    public void onMessage(String message, Session session) {
        if (message == null || message.isEmpty()) {
            logger.warn("Received empty JSON message, sessionId={}", session.getId());
            return;
        }
        try {
            logger.debug("Received JSON message, sessionId={}, size={} chars",
                    session.getId(), message.length());
            dispatcher.dispatch(message, session);
        } catch (Exception e) {
            logger.error("Failed to parse JSON message, sessionId={}", session.getId(), e);
        }
    }

    /**
     * 接收二进制分片（V4：用于文件传输）
     *
     * @param message 二进制分片数据
     * @param session WebSocket会话
     */
    @OnMessage
    public void onMessage(ByteBuffer message, Session session) {
        if (message == null || message.remaining() == 0) {
            logger.warn("Received empty binary chunk, sessionId={}", session.getId());
            return;
        }
        try {
            logger.debug("Received binary chunk, sessionId={}, size={} bytes",
                    session.getId(), message.remaining());
            dispatcher.dispatch(message, session);
        } catch (Exception e) {
            logger.error("Failed to process binary chunk, sessionId={}", session.getId(), e);
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
     * 发送JSON信令（V4新增）
     *
     * @param sessionId 目标会话ID
     * @param jsonMessage JSON消息字符串
     */
    public void sendText(String sessionId, String jsonMessage) {
        Session session = sessionRegistry.getSession(sessionId);
        if (session != null && session.isOpen()) {
            try {
                session.getBasicRemote().sendText(jsonMessage);
                logger.debug("JSON message sent successfully, sessionId={}, size={} chars",
                        sessionId, jsonMessage.length());
            } catch (IOException e) {
                logger.error("Failed to send JSON message, sessionId={}", sessionId, e);
            }
        } else {
            logger.warn("Session not found or closed, sessionId={}", sessionId);
        }
    }

    /**
     * 发送二进制分片（V4：用于文件传输）
     *
     * @param sessionId 目标会话ID
     * @param buffer 二进制数据
     */
    public void sendBinary(String sessionId, ByteBuffer buffer) {
        Session session = sessionRegistry.getSession(sessionId);
        if (session != null && session.isOpen()) {
            try {
                session.getBasicRemote().sendBinary(buffer);
                logger.debug("Binary chunk sent successfully, sessionId={}, size={} bytes",
                        sessionId, buffer.remaining());
            } catch (IOException e) {
                logger.error("Failed to send binary chunk, sessionId={}", sessionId, e);
            }
        } else {
            logger.warn("Session not found or closed, sessionId={}", sessionId);
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
