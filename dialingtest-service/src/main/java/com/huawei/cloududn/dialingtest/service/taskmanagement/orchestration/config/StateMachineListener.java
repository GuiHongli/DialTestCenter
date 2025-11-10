/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.cloududn.dialingtest.service.taskmanagement.orchestration.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.huawei.cloududn.dialingtest.service.taskmanagement.TaskMgmtService;
import com.huawei.cloududn.dialingtest.service.taskmanagement.dto.TaskContext;
import com.huawei.cloududn.dialingtest.service.taskmanagement.orchestration.state.TaskEvent;
import com.huawei.cloududn.dialingtest.service.taskmanagement.orchestration.state.TaskState;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;
import org.springframework.stereotype.Component;

/**
 * 状态机监听器：在状态变更时将 context.step 持久化到任务表。
 *
 * <p>依赖：需要在 ExtendedState 中放入 TASK_CONTEXT（TaskContext）与 MAIN_TASK_ID（Long）。</p>
 */
@Component
public class StateMachineListener extends StateMachineListenerAdapter<TaskState, TaskEvent> {
    private static final Logger logger = LoggerFactory.getLogger(StateMachineListener.class);

    @Autowired
    private TaskMgmtService taskMgmtService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void stateChanged(State<TaskState, TaskEvent> from, State<TaskState, TaskEvent> to) {
        if (to == null) {
            return;
        } else {
            logger.info("State changed: {} -> {}", from == null ? null : from.getId(), to.getId());
        }
    }
    @Override
    public void stateContext(StateContext<TaskState, TaskEvent> stateContext) {
        if (stateContext == null || stateContext.getStateMachine() == null) {
            return;
        } else {
            StateMachine<TaskState, TaskEvent> machine = stateContext.getStateMachine();
            Object ctxObj = machine.getExtendedState().getVariables().get("TASK_CONTEXT");
            Object idObj = machine.getExtendedState().getVariables().get("MAIN_TASK_ID");
            if (!(ctxObj instanceof TaskContext) || !(idObj instanceof Long)) {
                return;
            } else {
                TaskContext ctx = (TaskContext) ctxObj;
                TaskState current = machine.getState() == null ? null : machine.getState().getId();
                if (current == null) {
                    return;
                } else {
                    ctx.setStep(current);
                    String json;
                    try {
                        json = objectMapper.writeValueAsString(ctx);
                    } catch (JsonProcessingException e) {
                        logger.warn("Serialize context failed", e);
                        json = "{}";
                    }
                    taskMgmtService.updateStatusAndContext((Long) idObj, "RUNNING", null, json);
                    logger.info("Listener persisted state {} for task {}", current, idObj);
                }
            }
        }
    }
}


