/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.cloududn.dialingtest.service.taskmanagement.orchestration;

import com.huawei.cloududn.dialingtest.model.TaskEntity;
import com.huawei.cloududn.dialingtest.service.taskmanagement.TaskMgmtService;
import com.huawei.cloududn.dialingtest.service.taskmanagement.orchestration.config.StateMachineConfig;
import com.huawei.cloududn.dialingtest.service.taskmanagement.orchestration.state.TaskEvent;
import com.huawei.cloududn.dialingtest.service.taskmanagement.orchestration.state.TaskState;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.state.State;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class TaskOrchestratorServiceTest {

    @Mock
    private StateMachine<TaskState, TaskEvent> machine;

    @Mock
    private StateMachineConfig stateMachineConfig;

    @Mock
    private TaskMgmtService taskMgmtService;

    @InjectMocks
    private TaskOrchestratorService orchestrator;

    @Test
    public void testSendResultEvent_Success() throws Exception {
        TaskEntity task = new TaskEntity();
        task.setId(1);
        task.setContext("{}\n");

        @SuppressWarnings("unchecked")
        State<TaskState, TaskEvent> state = (State<TaskState, TaskEvent>) Mockito.mock(State.class);
        Mockito.when(state.getId()).thenReturn(TaskState.START_VALIDATION);
        Mockito.when(machine.getState()).thenReturn(state);
        Mockito.when(taskMgmtService.findById(1L)).thenReturn(task);
        Mockito.when(stateMachineConfig.build(Mockito.any(TaskState.class))).thenReturn(machine);

        orchestrator.sendResultEvent(1L, true);
        Mockito.verify(machine).sendEvent(TaskEvent.TASK_SUCCESS);
        Mockito.verify(taskMgmtService).updateStatusAndContext(Mockito.eq(1L), Mockito.eq("RUNNING"), Mockito.any(), Mockito.anyString());
    }
}


