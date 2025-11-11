/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.cloududn.dialingtest.service.taskmanagement.orchestration.event;

import com.huawei.cloududn.dialingtest.service.executormanagement.task.TaskInterfaceService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 任务分发事件监听器，处理任务分发到执行机的逻辑
 *
 * @author g00940940
 * @since 2025-11-09
 */
@Component
public class TaskDispatchEventListener {
    private static final Logger logger = LoggerFactory.getLogger(TaskDispatchEventListener.class);

    @Autowired
    private TaskInterfaceService taskInterfaceService;

    /**
     * 处理任务分发事件 (V3 TLV版本)
     *
     * @param event 任务分发事件
     */
    @EventListener
    @Async
    public void handleTaskDispatch(TaskDispatchEvent event) {
        logger.info("Handling task dispatch event: taskId={}, sessionId={}", event.getTaskId(), event.getSessionId());
        try {
            // 从taskPayload中提取V3版本所需的信息
            if (event.getTaskPayload() instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> payload = (Map<String, Object>) event.getTaskPayload();

                String taskId = event.getTaskId();
                String scriptName = (String) payload.getOrDefault("script_name", "default_script.air");
                String version = (String) payload.getOrDefault("script_version", "1.0");
                String parameters = payload.getOrDefault("params", "{}").toString();

                taskInterfaceService.dispatchTaskToAgent(event.getSessionId(), taskId, scriptName, version, parameters);
                logger.info("Task dispatched successfully: taskId={}, scriptName={}, version={}", taskId, scriptName, version);
            } else {
                logger.error("Invalid task payload type: expected Map, got {}", event.getTaskPayload().getClass());
            }
        } catch (Exception e) {
            logger.error("Failed to dispatch task: taskId={}", event.getTaskId(), e);
        }
    }
}

