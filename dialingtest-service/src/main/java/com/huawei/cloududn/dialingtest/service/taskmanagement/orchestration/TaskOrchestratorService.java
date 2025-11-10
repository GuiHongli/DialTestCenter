/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.cloududn.dialingtest.service.taskmanagement.orchestration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.huawei.cloududn.dialingtest.dao.taskmanagement.TaskDao;
import com.huawei.cloududn.dialingtest.model.TaskEntity;
import com.huawei.cloududn.dialingtest.service.taskmanagement.TaskMgmtService;
import com.huawei.cloududn.dialingtest.service.taskmanagement.dto.TaskContext;
import com.huawei.cloududn.dialingtest.service.taskmanagement.orchestration.state.TaskEvent;
import com.huawei.cloududn.dialingtest.service.taskmanagement.orchestration.state.TaskState;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.statemachine.StateMachine;
import org.springframework.stereotype.Service;
import com.huawei.cloududn.dialingtest.service.taskmanagement.orchestration.config.StateMachineConfig;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

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
    private StateMachineConfig stateMachineConfig;

    @Autowired
    private TaskMgmtService taskMgmtService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public void sendResultEvent(Long mainTaskId, boolean success) {
        sendResultEvent(mainTaskId, success, null);
    }

    public void sendResultEvent(Long mainTaskId, boolean success, java.util.Map<String, Object> resultData) {
        TaskEntity task = taskMgmtService.findById(mainTaskId);
        if (task == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found: " + mainTaskId);
        } else {
            TaskContext ctx = readContext(task);
            TaskState initial = ctx.getStep() == null ? TaskState.START_VALIDATION : ctx.getStep();
            // 幂等性：相同的回调(status+resultData)不应重复推进状态
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
                    logger.info("Task {} duplicate callback ignored for state {} with fingerprint {}", mainTaskId, initial, newFingerprint);
                    return;
                }
            }
            StateMachine<TaskState, TaskEvent> machine;
            try {
                machine = stateMachineConfig.build(initial);
            } catch (Exception e) {
                throw new IllegalStateException("Failed to build state machine", e);
            }
            // 将上下文及主任务ID放入 ExtendedState，交由 Listener 持久化（为空时跳过以避免 NPE）
            if (machine.getExtendedState() != null && machine.getExtendedState().getVariables() != null) {
                machine.getExtendedState().getVariables().put("TASK_CONTEXT", ctx);
                machine.getExtendedState().getVariables().put("MAIN_TASK_ID", mainTaskId);
            }
            if (resultData != null) {
                ctx.getData().put("callback_result", resultData);
            }
            // 记录本次回调指纹供幂等判断
            ctx.getData().put("last_callback_fingerprint", newFingerprint);
            machine.start();
            TaskEvent event = success ? TaskEvent.TASK_SUCCESS : TaskEvent.TASK_FAILED;
            machine.sendEvent(event);
            TaskState current = machine.getState() == null ? null : machine.getState().getId();
            logger.info("Task {} send event {} on {} -> {} (persist by listener)", mainTaskId, event, initial, current);

            // Create a sub task record for this callback to visualize execution steps on UI
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
                }
                // 简要上下文，记录触发时机和指纹，便于排查
                com.fasterxml.jackson.databind.node.ObjectNode subCtx = objectMapper.createObjectNode();
                subCtx.put("from", "callback");
                subCtx.put("state_before", initial == null ? null : initial.name());
                subCtx.put("state_after", current == null ? null : current.name());
                subCtx.put("fingerprint", newFingerprint);
                sub.setContext(subCtx.toString());
                taskMgmtService.createSubTask(mainTaskId, mainTaskId, sub);
            } catch (Exception ex) {
                // 不影响主流程，记录告警
                logger.warn("Create sub task record failed for mainTaskId={}", mainTaskId, ex);
            }

            // 非 FINAL 时将状态更新为 RUNNING；到达 FINAL 时持久化最终状态
            if (current == TaskState.FINAL) {
                String result = success ? "SUCCESS" : "FAILED";
                String resultText = null;
                if (resultData != null && !resultData.isEmpty()) {
                    try {
                        resultText = objectMapper.writeValueAsString(resultData);
                    } catch (JsonProcessingException e) {
                        resultText = String.valueOf(resultData);
                    }
                }
                taskMgmtService.updateStatusAndContext(mainTaskId, success ? "COMPLETED" : "FAILED", result, toJson(ctx));
                if (!success && resultText != null) {
                    // 失败时将错误信息写入output字段（当前服务层无单独API，保持最简实现通过context+result表达）
                    logger.warn("Task {} finalized with failure: {}", mainTaskId, resultText);
                }
            } else {
                taskMgmtService.updateStatusAndContext(mainTaskId, "RUNNING", null, toJson(ctx));
            }
        }
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


