/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.cloududn.dialingtest.service.taskmanagement.orchestration.config;

import com.huawei.cloududn.dialingtest.service.taskmanagement.orchestration.action.TrainModelAction;
import com.huawei.cloududn.dialingtest.service.taskmanagement.orchestration.action.ValidationAction;
import com.huawei.cloududn.dialingtest.service.taskmanagement.orchestration.action.ReplayAction;
import com.huawei.cloududn.dialingtest.service.taskmanagement.orchestration.action.GrayValidationAction;
import com.huawei.cloududn.dialingtest.service.taskmanagement.orchestration.action.FullReleaseAction;
import com.huawei.cloududn.dialingtest.service.taskmanagement.orchestration.state.TaskEvent;
import com.huawei.cloududn.dialingtest.service.taskmanagement.orchestration.state.TaskState;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.StateMachineBuilder;

/**
 * 简化版状态机配置：为每个主任务按需创建独立状态机实例。
 *
 * <p>说明：使用编程式构建，保持依赖轻量。转换规则按设计文档5.4表实现。</p>
 *
 * @author g00940940
 * @since 2025-10-24
 */
@Configuration
@EnableStateMachine(name = "taskStateMachine")
public class StateMachineConfig {
    private static final Logger logger = LoggerFactory.getLogger(StateMachineConfig.class);
    @Autowired
    private StateMachineListener stateMachineListener;
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

    @Bean
    public StateMachine<TaskState, TaskEvent> createMachine() throws Exception {
        StateMachineBuilder.Builder<TaskState, TaskEvent> builder = StateMachineBuilder.builder();
        builder.configureConfiguration().withConfiguration().listener(stateMachineListener);

        builder.configureStates().withStates()
            .initial(TaskState.START_VALIDATION)
            .state(TaskState.START_VALIDATION, validationAction, null)
            .state(TaskState.START_TRAINING_DIALING, validationAction, null)
            .state(TaskState.START_MODEL_TRAIN, trainModelAction, null)
            .state(TaskState.START_MODEL_REPLAY, replayAction, null)
            .state(TaskState.START_GRAY_VALIDATION, grayValidationAction, null)
            .state(TaskState.START_FULL_REPLAY, replayAction, null)
            .state(TaskState.START_WHITELIST, validationAction, null)
            .state(TaskState.START_FULL_RELEASE, fullReleaseAction, null)
            .end(TaskState.FINAL);

        builder.configureTransitions()
            // START_VALIDATION
            .withExternal().source(TaskState.START_VALIDATION).target(TaskState.FINAL).event(TaskEvent.TASK_SUCCESS).and()
            .withExternal().source(TaskState.START_VALIDATION).target(TaskState.START_TRAINING_DIALING).event(TaskEvent.TASK_FAILED).and()
            // START_TRAINING_DIALING
            .withExternal().source(TaskState.START_TRAINING_DIALING).target(TaskState.START_MODEL_TRAIN).event(TaskEvent.TASK_SUCCESS).and()
            .withExternal().source(TaskState.START_TRAINING_DIALING).target(TaskState.START_TRAINING_DIALING).event(TaskEvent.TASK_FAILED).and()
            // START_MODEL_TRAIN
            .withExternal().source(TaskState.START_MODEL_TRAIN).target(TaskState.START_MODEL_REPLAY).event(TaskEvent.TASK_SUCCESS).and()
            .withExternal().source(TaskState.START_MODEL_TRAIN).target(TaskState.FINAL).event(TaskEvent.TASK_FAILED).and()
            // START_MODEL_REPLAY
            .withExternal().source(TaskState.START_MODEL_REPLAY).target(TaskState.START_GRAY_VALIDATION).event(TaskEvent.TASK_SUCCESS).and()
            .withExternal().source(TaskState.START_MODEL_REPLAY).target(TaskState.FINAL).event(TaskEvent.TASK_FAILED).and()
            // START_GRAY_VALIDATION
            .withExternal().source(TaskState.START_GRAY_VALIDATION).target(TaskState.START_FULL_REPLAY).event(TaskEvent.TASK_SUCCESS).and()
            .withExternal().source(TaskState.START_GRAY_VALIDATION).target(TaskState.FINAL).event(TaskEvent.TASK_FAILED).and()
            // START_FULL_REPLAY
            .withExternal().source(TaskState.START_FULL_REPLAY).target(TaskState.START_WHITELIST).event(TaskEvent.TASK_SUCCESS).and()
            .withExternal().source(TaskState.START_FULL_REPLAY).target(TaskState.FINAL).event(TaskEvent.TASK_FAILED).and()
            // START_WHITELIST
            .withExternal().source(TaskState.START_WHITELIST).target(TaskState.START_FULL_RELEASE).event(TaskEvent.TASK_SUCCESS).and()
            .withExternal().source(TaskState.START_WHITELIST).target(TaskState.FINAL).event(TaskEvent.TASK_FAILED).and()
            // START_FULL_RELEASE
            .withExternal().source(TaskState.START_FULL_RELEASE).target(TaskState.FINAL).event(TaskEvent.TASK_SUCCESS).and()
            .withExternal().source(TaskState.START_FULL_RELEASE).target(TaskState.FINAL).event(TaskEvent.TASK_FAILED);

        StateMachine<TaskState, TaskEvent> machine = builder.build();
        logger.info("StateMachine built (not started), initial: {}", machine.getState() == null ? null : machine.getState().getId());
        return machine;
    }

    /**
     * 为指定初始状态创建新的状态机实例，用于基于任务上下文恢复。
     */
    public StateMachine<TaskState, TaskEvent> build(TaskState initial) throws Exception {
        StateMachineBuilder.Builder<TaskState, TaskEvent> builder = StateMachineBuilder.builder();
        builder.configureConfiguration().withConfiguration().listener(stateMachineListener);

        builder.configureStates().withStates()
            .initial(initial == null ? TaskState.START_VALIDATION : initial)
            .state(TaskState.START_VALIDATION, validationAction, null)
            .state(TaskState.START_TRAINING_DIALING, validationAction, null)
            .state(TaskState.START_MODEL_TRAIN, trainModelAction, null)
            .state(TaskState.START_MODEL_REPLAY, replayAction, null)
            .state(TaskState.START_GRAY_VALIDATION, grayValidationAction, null)
            .state(TaskState.START_FULL_REPLAY, replayAction, null)
            .state(TaskState.START_WHITELIST, validationAction, null)
            .state(TaskState.START_FULL_RELEASE, fullReleaseAction, null)
            .end(TaskState.FINAL);

        builder.configureTransitions()
            .withExternal().source(TaskState.START_VALIDATION).target(TaskState.FINAL).event(TaskEvent.TASK_SUCCESS).and()
            .withExternal().source(TaskState.START_VALIDATION).target(TaskState.START_TRAINING_DIALING).event(TaskEvent.TASK_FAILED).and()
            .withExternal().source(TaskState.START_TRAINING_DIALING).target(TaskState.START_MODEL_TRAIN).event(TaskEvent.TASK_SUCCESS).and()
            .withExternal().source(TaskState.START_TRAINING_DIALING).target(TaskState.START_TRAINING_DIALING).event(TaskEvent.TASK_FAILED).and()
            .withExternal().source(TaskState.START_MODEL_TRAIN).target(TaskState.START_MODEL_REPLAY).event(TaskEvent.TASK_SUCCESS).and()
            .withExternal().source(TaskState.START_MODEL_TRAIN).target(TaskState.FINAL).event(TaskEvent.TASK_FAILED).and()
            .withExternal().source(TaskState.START_MODEL_REPLAY).target(TaskState.START_GRAY_VALIDATION).event(TaskEvent.TASK_SUCCESS).and()
            .withExternal().source(TaskState.START_MODEL_REPLAY).target(TaskState.FINAL).event(TaskEvent.TASK_FAILED).and()
            .withExternal().source(TaskState.START_GRAY_VALIDATION).target(TaskState.START_FULL_REPLAY).event(TaskEvent.TASK_SUCCESS).and()
            .withExternal().source(TaskState.START_GRAY_VALIDATION).target(TaskState.FINAL).event(TaskEvent.TASK_FAILED).and()
            .withExternal().source(TaskState.START_FULL_REPLAY).target(TaskState.START_WHITELIST).event(TaskEvent.TASK_SUCCESS).and()
            .withExternal().source(TaskState.START_FULL_REPLAY).target(TaskState.FINAL).event(TaskEvent.TASK_FAILED).and()
            .withExternal().source(TaskState.START_WHITELIST).target(TaskState.START_FULL_RELEASE).event(TaskEvent.TASK_SUCCESS).and()
            .withExternal().source(TaskState.START_WHITELIST).target(TaskState.FINAL).event(TaskEvent.TASK_FAILED).and()
            .withExternal().source(TaskState.START_FULL_RELEASE).target(TaskState.FINAL).event(TaskEvent.TASK_SUCCESS).and()
            .withExternal().source(TaskState.START_FULL_RELEASE).target(TaskState.FINAL).event(TaskEvent.TASK_FAILED);

        StateMachine<TaskState, TaskEvent> machine = builder.build();
        return machine;
    }
}


