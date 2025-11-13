/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.cloududn.dialingtest.service.taskmanagement.orchestration.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.huawei.cloududn.dialingtest.service.taskmanagement.TaskMgmtService;
import com.huawei.cloududn.dialingtest.service.taskmanagement.dto.TaskContext;
import com.huawei.cloududn.dialingtest.service.taskmanagement.orchestration.state.TaskState;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 任务状态持久化监听器，负责在状态变更时更新数据库
 *
 * @author g00940940
 * @since 2025-11-10
 */
@Component
public class TaskStatePersistenceListener implements TaskStateChangeListener {
    private static final Logger logger = LoggerFactory.getLogger(TaskStatePersistenceListener.class);
    @Autowired
    private TaskMgmtService taskMgmtService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void onStateChanged(TaskState from, TaskState to, TaskContext context) {
        if (to == null) {
            logger.warn("Target state is null, skipping persistence");
            return;
        } else if (context == null) {
            logger.warn("Context is null for state change: {} -> {}", from, to);
            return;
        } else {
            context.setStep(to);
            Long mainTaskId = getMainTaskId(context);
            if (mainTaskId == null) {
                logger.warn("Main task ID not found in context for state: {}", to);
                return;
            } else {
                String json;
                try {
                    json = objectMapper.writeValueAsString(context);
                } catch (JsonProcessingException e) {
                    logger.warn("Serialize context failed for task: {}", mainTaskId, e);
                    json = "{}";
                }
                taskMgmtService.updateStatusAndContext(mainTaskId, "RUNNING", null, json);
                logger.info("Persisted state {} for task {}", to, mainTaskId);
            }
        }
    }

    private Long getMainTaskId(TaskContext context) {
        Object taskIdObj = context.getData().get("taskId");
        if (taskIdObj instanceof Long) {
            return (Long) taskIdObj;
        } else if (taskIdObj instanceof Integer) {
            return ((Integer) taskIdObj).longValue();
        } else if (taskIdObj instanceof String) {
            try {
                return Long.parseLong((String) taskIdObj);
            } catch (NumberFormatException e) {
                logger.warn("Invalid taskId format: {}", taskIdObj);
                return null;
            }
        } else {
            return null;
        }
    }
}

