/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2025. All rights reserved.
 */

package com.huawei.cloududn.dialingtest.service.executormanagement;

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

    public void bind(String sessionId, String executorName) {
        sessionToExecutor.put(sessionId, executorName);
        executorToSession.put(executorName, sessionId);
    }

    public void unbind(String sessionId) {
        String executor = sessionToExecutor.remove(sessionId);
        if (executor != null) {
            executorToSession.remove(executor);
        } else {
            // no-op
        }
    }

    public String getExecutorName(String sessionId) {
        return sessionToExecutor.get(sessionId);
    }

    public String getSessionId(String executorName) {
        return executorToSession.get(executorName);
    }
}


