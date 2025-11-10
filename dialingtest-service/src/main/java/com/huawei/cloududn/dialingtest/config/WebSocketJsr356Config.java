/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2025. All rights reserved.
 */

package com.huawei.cloududn.dialingtest.config;

import com.huawei.cloududn.dialingtest.controller.executormanagement.websocket.ExecutorWebsocketEndpoint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import javax.servlet.ServletContext;
import javax.websocket.DeploymentException;
import javax.websocket.server.ServerContainer;
import javax.websocket.server.ServerEndpointConfig;

/**
 * JSR 356 WebSocket configuration for Executor Management module.
 *
 * <p>Manually registers WebSocket endpoint using JSR 356 API.</p>
 *
 * @author g00940940
 * @since 2025-11-10
 */
@Configuration
public class WebSocketJsr356Config {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketJsr356Config.class);

    @Autowired
    private ServletContext servletContext;

    @Autowired
    private ExecutorWebsocketEndpoint executorWebsocketEndpoint;

    @PostConstruct
    public void registerWebSocketEndpoint() {
        try {
            ServerContainer serverContainer = (ServerContainer) servletContext
                    .getAttribute(ServerContainer.class.getName());
            
            if (serverContainer == null) {
                logger.error("ServerContainer not found in ServletContext");
                throw new IllegalStateException("ServerContainer not available");
            }
            
            ServerEndpointConfig config = ServerEndpointConfig.Builder
                    .create(ExecutorWebsocketEndpoint.class, "/wss/agents")
                    .configurator(new SpringAwareEndpointConfigurator(executorWebsocketEndpoint))
                    .build();
            
            serverContainer.addEndpoint(config);
            logger.info("WebSocket endpoint registered successfully: /wss/agents");
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
