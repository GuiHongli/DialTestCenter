/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2025. All rights reserved.
 */

package com.huawei.cloududn.dialingtest.config;

import com.huawei.cloududn.dialingtest.controller.executormanagement.websocket.ExecutorWebsocketEndpoint;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * WebSocket configuration for Executor Management module.
 *
 * <p>Registers the `/wss/agents` endpoint for agent connections.</p>
 *
 * @author g00940940
 * @since 2025-11-04
 */
@Configuration
@EnableWebSocket
public class WebSocketExecutorConfig implements WebSocketConfigurer {

    @Autowired
    private ExecutorWebsocketEndpoint executorWebsocketEndpoint;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(executorWebsocketEndpoint, "/wss/agents")
                .setAllowedOrigins("*");
    }
}


