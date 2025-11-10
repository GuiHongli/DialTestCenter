/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.cloududn.dialingtest.service.taskmanagement.orchestration.action;

import com.huawei.cloududn.dialingtest.service.taskmanagement.dto.TaskContext;
import com.huawei.cloududn.dialingtest.service.taskmanagement.orchestration.impl.FullReleaseTaskImpl;
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
 * 全量发布动作单元测试
 *
 * @author g00940940
 * @since 2025-11-09
 */
@RunWith(MockitoJUnitRunner.class)
public class FullReleaseActionTest {
    @Mock
    private FullReleaseTaskImpl fullReleaseTask;

    @Mock
    private StateContext<TaskState, TaskEvent> stateContext;

    @InjectMocks
    private FullReleaseAction action;

    @Test
    public void testExecute_Success() {
        TaskContext taskContext = new TaskContext();
        taskContext.setStep(TaskState.START_FULL_RELEASE);
        DefaultExtendedState extState = new DefaultExtendedState();
        extState.getVariables().put("TASK_CONTEXT", taskContext);
        Mockito.when(stateContext.getExtendedState()).thenReturn(extState);
        Mockito.when(fullReleaseTask.start(Mockito.any(TaskContext.class))).thenReturn("job-release-202");
        action.execute(stateContext);
        assertEquals("job-release-202", taskContext.getData().get("async_job_id"));
    }

    @Test
    public void testExecute_MissingContext() {
        DefaultExtendedState extState = new DefaultExtendedState();
        Mockito.when(stateContext.getExtendedState()).thenReturn(extState);
        action.execute(stateContext);
        Mockito.verify(fullReleaseTask, Mockito.never()).start(Mockito.any(TaskContext.class));
    }
}

