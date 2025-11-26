/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2025. All rights reserved.
 */

package com.huawei.cloududn.dialingtestapp.service.executormanagement;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple registry for binding sessionId to executor name.
 *
 * @author g00940940
 * @since 2025-11-04
 */
@Component
public class SessionBindingRegistry {

    private final Map<String, String> sessionToExecutor = new ConcurrentHashMap<>();
    private final Map<String, String> executorToSession = new ConcurrentHashMap<>();
    private final Map<String, Long> sessionToToken = new ConcurrentHashMap<>();

    public void bind(String sessionId, String executorName) {
        sessionToExecutor.put(sessionId, executorName);
        executorToSession.put(executorName, sessionId);
    }
    
    /**
     * Bind session to executor with token (V3 TLV protocol).
     *
     * @param sessionId    session ID
     * @param executorName executor name
     * @param token        8-byte token
     */
    public void bind(String sessionId, String executorName, long token) {
        sessionToExecutor.put(sessionId, executorName);
        executorToSession.put(executorName, sessionId);
        sessionToToken.put(sessionId, token);
    }

    public void unbind(String sessionId) {
        String executor = sessionToExecutor.remove(sessionId);
        if (executor != null) {
            executorToSession.remove(executor);
        } else {
            // no-op
        }
        sessionToToken.remove(sessionId);
    }

    public String getExecutorName(String sessionId) {
        return sessionToExecutor.get(sessionId);
    }

    public String getSessionId(String executorName) {
        return executorToSession.get(executorName);
    }
    
    /**
     * Get token by session ID (V3 TLV protocol).
     *
     * @param sessionId session ID
     * @return token or null
     */
    public Long getToken(String sessionId) {
        return sessionToToken.get(sessionId);
    }
}


