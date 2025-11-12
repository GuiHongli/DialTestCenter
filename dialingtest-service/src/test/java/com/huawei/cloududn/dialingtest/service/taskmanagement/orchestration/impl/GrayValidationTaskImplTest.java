/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.cloududn.dialingtest.service.taskmanagement.orchestration.impl;

import com.huawei.cloududn.dialingtest.service.taskmanagement.dto.TaskContext;
import com.huawei.cloududn.dialingtest.service.taskmanagement.orchestration.state.TaskState;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.*;

/**
 * 灰度验证任务实现单元测试
 *
 * @author g00940940
 * @since 2025-11-09
 */
@RunWith(MockitoJUnitRunner.class)
public class GrayValidationTaskImplTest {
    @InjectMocks
    private GrayValidationTaskImpl taskImpl;

    @Test
    public void testStart_ReturnsJobId() {
        TaskContext context = new TaskContext();
        context.setStep(TaskState.START_GRAY_VALIDATION);
        String jobId = taskImpl.start(context);
        assertNotNull(jobId);
        assertTrue(jobId.startsWith("job-gray-"));
    }

    @Test
    public void testStart_UniqueJobId() {
        TaskContext context1 = new TaskContext();
        TaskContext context2 = new TaskContext();
        String jobId1 = taskImpl.start(context1);
        String jobId2 = taskImpl.start(context2);
        assertNotEquals(jobId1, jobId2);
    }
}

