/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.cloududn.dialingtest.service.taskmanagement.orchestration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.huawei.cloududn.dialingtest.model.TaskEntity;
import com.huawei.cloududn.dialingtest.service.taskmanagement.TaskMgmtService;
import com.huawei.cloududn.dialingtest.service.taskmanagement.dto.TaskContext;
import com.huawei.cloududn.dialingtest.service.taskmanagement.orchestration.action.FullReleaseAction;
import com.huawei.cloududn.dialingtest.service.taskmanagement.orchestration.action.GrayValidationAction;
import com.huawei.cloududn.dialingtest.service.taskmanagement.orchestration.action.ReplayAction;
import com.huawei.cloududn.dialingtest.service.taskmanagement.orchestration.action.TrainModelAction;
import com.huawei.cloududn.dialingtest.service.taskmanagement.orchestration.action.ValidationAction;
import com.huawei.cloududn.dialingtest.service.taskmanagement.orchestration.listener.TaskStatePersistenceListener;
import com.huawei.cloududn.dialingtest.service.taskmanagement.orchestration.state.TaskEvent;
import com.huawei.cloududn.dialingtest.service.taskmanagement.orchestration.state.TaskState;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.annotation.PostConstruct;

/**
 * 任务编排器：负责恢复/驱动状态机，并在变更后持久化context.step。
 *
 * <p>为最小可运行演示版：仅提供根据结果驱动状态转换与持久化的能力。</p>
 *
 * @author g00940940
 * @since 2025-10-24
 */
@Service
public class TaskOrchestratorService {
    private static final Logger logger = LoggerFactory.getLogger(TaskOrchestratorService.class);
    @Autowired
    private TaskStateMachine taskStateMachine;
    @Autowired
    private TaskMgmtService taskMgmtService;
    @Autowired
    private ValidationAction validationAction;
    @Autowired
    private TrainModelAction trainModelAction;
    @Autowired
    private ReplayAction replayAction;
    @Autowired
    private GrayValidationAction grayValidationAction;
    @Autowired
    private FullReleaseAction fullReleaseAction;
    @Autowired
    private TaskStatePersistenceListener persistenceListener;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostConstruct
    public void init() {
        taskStateMachine.registerAction(TaskState.START_VALIDATION, validationAction);
        taskStateMachine.registerAction(TaskState.START_TRAINING_DIALING, validationAction);
        taskStateMachine.registerAction(TaskState.START_MODEL_TRAIN, trainModelAction);
        taskStateMachine.registerAction(TaskState.START_MODEL_REPLAY, replayAction);
        taskStateMachine.registerAction(TaskState.START_GRAY_VALIDATION, grayValidationAction);
        taskStateMachine.registerAction(TaskState.START_FULL_REPLAY, replayAction);
        taskStateMachine.registerAction(TaskState.START_WHITELIST, validationAction);
        taskStateMachine.registerAction(TaskState.START_FULL_RELEASE, fullReleaseAction);
        taskStateMachine.addListener(persistenceListener);
        logger.info("TaskOrchestratorService initialized with all actions and listeners");
    }

    public void sendResultEvent(Long mainTaskId, boolean success) {
        sendResultEvent(mainTaskId, success, null);
    }

    public void sendResultEvent(Long mainTaskId, boolean success, java.util.Map<String, Object> resultData) {
        TaskEntity task = taskMgmtService.findById(mainTaskId);
        if (task == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found: " + mainTaskId);
        } else {
            TaskContext ctx = readContext(task);
            TaskState currentState = ctx.getStep() == null ? TaskState.START_VALIDATION : ctx.getStep();
            String fingerprintPrefix = success ? "S:" : "F:";
            String resultTextForFp;
            try {
                resultTextForFp = (resultData == null || resultData.isEmpty()) ? "null" : objectMapper.writeValueAsString(resultData);
            } catch (JsonProcessingException e) {
                resultTextForFp = String.valueOf(resultData);
            }
            String newFingerprint = fingerprintPrefix + resultTextForFp;
            Object lastFpObj = ctx.getData().get("last_callback_fingerprint");
            if (lastFpObj instanceof String) {
                String lastFp = (String) lastFpObj;
                if (lastFp.equals(newFingerprint)) {
                    logger.info("Task {} duplicate callback ignored for state {} with fingerprint {}", mainTaskId, currentState, newFingerprint);
                    return;
                }
            } else {
                logger.debug("No previous fingerprint found, proceeding with state transition");
            }
            ctx.getData().put("taskId", mainTaskId);
            if (resultData != null) {
                ctx.getData().put("callback_result", resultData);
            } else {
                logger.debug("No result data provided for task: {}", mainTaskId);
            }
            ctx.getData().put("last_callback_fingerprint", newFingerprint);
            TaskEvent event = success ? TaskEvent.TASK_SUCCESS : TaskEvent.TASK_FAILED;
            TaskState newState = taskStateMachine.sendEvent(currentState, event, ctx);
            logger.info("Task {} sent event {} on {} -> {}", mainTaskId, event, currentState, newState);
            try {
                TaskEntity sub = new TaskEntity();
                sub.setCreator("CALLBACK");
                sub.setStatus(success ? "COMPLETED" : "FAILED");
                sub.setResult(success ? "SUCCESS" : "FAILED");
                if (resultData != null && !resultData.isEmpty()) {
                    try {
                        sub.setInput(objectMapper.writeValueAsString(resultData));
                    } catch (JsonProcessingException e) {
                        sub.setInput(String.valueOf(resultData));
                    }
                } else {
                    sub.setInput("{}");
                }
                com.fasterxml.jackson.databind.node.ObjectNode subCtx = objectMapper.createObjectNode();
                subCtx.put("from", "callback");
                subCtx.put("state_before", currentState == null ? null : currentState.name());
                subCtx.put("state_after", newState == null ? null : newState.name());
                subCtx.put("fingerprint", newFingerprint);
                sub.setContext(subCtx.toString());
                taskMgmtService.createSubTask(mainTaskId, mainTaskId, sub);
            } catch (Exception ex) {
                logger.warn("Create sub task record failed for mainTaskId={}", mainTaskId, ex);
            }
            if (newState == TaskState.FINAL) {
                String result = success ? "SUCCESS" : "FAILED";
                String resultText = null;
                if (resultData != null && !resultData.isEmpty()) {
                    try {
                        resultText = objectMapper.writeValueAsString(resultData);
                    } catch (JsonProcessingException e) {
                        resultText = String.valueOf(resultData);
                    }
                } else {
                    logger.debug("No result data for finalized task: {}", mainTaskId);
                }
                taskMgmtService.updateStatusAndContext(mainTaskId, success ? "COMPLETED" : "FAILED", result, toJson(ctx));
                if (!success && resultText != null) {
                    logger.warn("Task {} finalized with failure: {}", mainTaskId, resultText);
                } else {
                    logger.info("Task {} finalized successfully", mainTaskId);
                }
            } else {
                taskMgmtService.updateStatusAndContext(mainTaskId, "RUNNING", null, toJson(ctx));
            }
        }
    }

    /**
     * 停止任务执行
     *
     * @param mainTaskId 主任务ID
     */
    public void stopTask(Long mainTaskId) {
        logger.info("Stopping task: {}", mainTaskId);

        TaskEntity task = taskMgmtService.findById(mainTaskId);
        if (task == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found: " + mainTaskId);
        }

        TaskContext ctx = readContext(task);
        TaskState currentState = ctx.getStep() == null ? TaskState.START_VALIDATION : ctx.getStep();

        ctx.getData().put("taskId", mainTaskId);
        ctx.getData().put("stop_reason", "manual_stop");

        TaskState newState = taskStateMachine.sendEvent(currentState, TaskEvent.STOP, ctx);
        logger.info("Task {} sent STOP event on {} -> {}", mainTaskId, currentState, newState);

        // 更新任务状态为停止
        taskMgmtService.updateStatusAndContext(mainTaskId, "STOPPED", "STOPPED", toJson(ctx));

        logger.info("Task {} stopped successfully", mainTaskId);
    }

    private TaskContext readContext(TaskEntity task) {
        try {
            if (task.getContext() == null || task.getContext().trim().isEmpty()) {
                return new TaskContext();
            } else {
                return objectMapper.readValue(task.getContext(), TaskContext.class);
            }
        } catch (Exception e) {
            logger.warn("Parse context failed, using empty", e);
            return new TaskContext();
        }
    }

    private String toJson(TaskContext ctx) {
        try {
            return objectMapper.writeValueAsString(ctx);
        } catch (JsonProcessingException e) {
            logger.warn("Serialize context failed", e);
            return "{}";
        }
    }
}


