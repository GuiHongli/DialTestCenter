/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.cloududn.dialingtestapp.service.taskmanagement.orchestration.action;

import com.huawei.cloududn.dialingtestapp.service.taskmanagement.dto.TaskContext;
import com.huawei.cloududn.dialingtestapp.service.taskmanagement.orchestration.impl.DialingTestTaskImpl;
import com.huawei.cloududn.dialingtestapp.service.taskmanagement.orchestration.state.TaskState;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

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
    @InjectMocks
    private ValidationAction action;

    @Test
    public void testExecute_Success() {
        TaskContext taskContext = new TaskContext();
        taskContext.setStep(TaskState.START_VALIDATION);
        Mockito.when(dialingTestTask.start(Mockito.any(TaskContext.class))).thenReturn("job-dial-123");
        action.execute(taskContext);
        assertEquals("job-dial-123", taskContext.getData().get("async_job_id"));
    }

    @Test
    public void testExecute_NullContext() {
        action.execute(null);
        Mockito.verify(dialingTestTask, Mockito.never()).start(Mockito.any(TaskContext.class));
    }
}

