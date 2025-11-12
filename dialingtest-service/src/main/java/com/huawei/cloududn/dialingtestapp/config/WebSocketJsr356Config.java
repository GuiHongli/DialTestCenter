/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2025. All rights reserved.
 */

package com.huawei.cloududn.dialingtestapp.config;

import com.huawei.cloududn.dialingtestapp.controller.executormanagement.websocket.ExecutorWebsocketEndpoint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import javax.servlet.ServletContext;
import javax.websocket.DeploymentException;
import javax.websocket.server.ServerContainer;
import javax.websocket.server.ServerEndpointConfig;

/**
 * JSR 356 WebSocket configuration for Executor Management module.
 * V3版本：支持二进制消息和大容量缓冲区配置
 *
 * <p>Manually registers WebSocket endpoint using JSR 356 API with custom buffer sizes.</p>
 *
 * @author g00940940
 * @since 2025-11-11
 */
@Configuration
public class WebSocketJsr356Config {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketJsr356Config.class);

    @Autowired
    private ServletContext servletContext;

    @Autowired
    private ExecutorWebsocketEndpoint executorWebsocketEndpoint;

    @Value("${websocket.text-buffer-size:2097152}")
    private int textBufferSize;

    @Value("${websocket.binary-buffer-size:10485760}")
    private int binaryBufferSize;

    @Value("${websocket.max-idle-timeout:300000}")
    private long maxIdleTimeout;

    @PostConstruct
    public void registerWebSocketEndpoint() {
        try {
            ServerContainer serverContainer = (ServerContainer) servletContext
                    .getAttribute(ServerContainer.class.getName());
            
            if (serverContainer == null) {
                logger.error("ServerContainer not found in ServletContext");
                throw new IllegalStateException("ServerContainer not available");
            }
            
            // Configure buffer sizes for V3 binary protocol
            serverContainer.setDefaultMaxTextMessageBufferSize(textBufferSize);
            serverContainer.setDefaultMaxBinaryMessageBufferSize(binaryBufferSize);
            serverContainer.setDefaultMaxSessionIdleTimeout(maxIdleTimeout);
            
            logger.info("WebSocket container configured: textBuffer={}KB, binaryBuffer={}KB, idleTimeout={}s",
                textBufferSize / 1024, binaryBufferSize / 1024, maxIdleTimeout / 1000);
            
            ServerEndpointConfig config = ServerEndpointConfig.Builder
                    .create(ExecutorWebsocketEndpoint.class, "/ws/executor")
                    .configurator(new SpringAwareEndpointConfigurator(executorWebsocketEndpoint))
                    .build();
            
            serverContainer.addEndpoint(config);
            logger.info("WebSocket endpoint registered successfully: /ws/executor (V3 binary protocol enabled)");
        } catch (DeploymentException e) {
            logger.error("Failed to register WebSocket endpoint", e);
            throw new IllegalStateException("Failed to register WebSocket endpoint", e);
        }
    }

    /**
     * Custom configurator to use Spring-managed endpoint instance.
     */
    private static class SpringAwareEndpointConfigurator extends ServerEndpointConfig.Configurator {
        private final ExecutorWebsocketEndpoint endpoint;

        public SpringAwareEndpointConfigurator(ExecutorWebsocketEndpoint endpoint) {
            this.endpoint = endpoint;
        }

        @Override
        public <T> T getEndpointInstance(Class<T> endpointClass) throws InstantiationException {
            return endpointClass.cast(endpoint);
        }
    }
}
