/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.cloududn.dialingtest.service.taskmanagement.orchestration.action;

import com.huawei.cloududn.dialingtest.service.taskmanagement.dto.TaskContext;
import com.huawei.cloududn.dialingtest.service.taskmanagement.orchestration.impl.DialingTestTaskImpl;
import com.huawei.cloududn.dialingtest.service.taskmanagement.orchestration.state.TaskEvent;
import com.huawei.cloududn.dialingtest.service.taskmanagement.orchestration.state.TaskState;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.support.DefaultExtendedState;

import static org.junit.Assert.*;

/**
 * 验证动作单元测试
 *
 * @author g00940940
 * @since 2025-11-09
 */
@RunWith(MockitoJUnitRunner.class)
public class ValidationActionTest {
    @Mock
    private DialingTestTaskImpl dialingTestTask;

    @Mock
    private StateContext<TaskState, TaskEvent> stateContext;

    @InjectMocks
    private ValidationAction action;

    @Test
    public void testExecute_Success() {
        TaskContext taskContext = new TaskContext();
        taskContext.setStep(TaskState.START_VALIDATION);
        DefaultExtendedState extState = new DefaultExtendedState();
        extState.getVariables().put("TASK_CONTEXT", taskContext);
        Mockito.when(stateContext.getExtendedState()).thenReturn(extState);
        Mockito.when(dialingTestTask.start(Mockito.any(TaskContext.class))).thenReturn("job-dial-123");
        action.execute(stateContext);
        assertEquals("job-dial-123", taskContext.getData().get("async_job_id"));
    }

    @Test
    public void testExecute_MissingContext() {
        DefaultExtendedState extState = new DefaultExtendedState();
        Mockito.when(stateContext.getExtendedState()).thenReturn(extState);
        action.execute(stateContext);
        Mockito.verify(dialingTestTask, Mockito.never()).start(Mockito.any(TaskContext.class));
    }

    @Test
    public void testExecute_InvalidContextType() {
        DefaultExtendedState extState = new DefaultExtendedState();
        extState.getVariables().put("TASK_CONTEXT", "invalid");
        Mockito.when(stateContext.getExtendedState()).thenReturn(extState);
        action.execute(stateContext);
        Mockito.verify(dialingTestTask, Mockito.never()).start(Mockito.any(TaskContext.class));
    }
}

