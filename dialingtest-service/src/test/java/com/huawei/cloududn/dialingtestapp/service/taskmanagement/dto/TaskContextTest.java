/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.cloududn.dialingtestapp.service.taskmanagement.dto;

import com.huawei.cloududn.dialingtestapp.service.taskmanagement.orchestration.state.TaskState;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * 任务上下文单元测试
 *
 * @author g00940940
 * @since 2025-11-09
 */
public class TaskContextTest {
    @Test
    public void testSetAndGetStep() {
        TaskContext context = new TaskContext();
        context.setStep(TaskState.START_VALIDATION);
        assertEquals(TaskState.START_VALIDATION, context.getStep());
    }

    @Test
    public void testGetData_NotNull() {
        TaskContext context = new TaskContext();
        assertNotNull(context.getData());
    }

    @Test
    public void testGetData_IsEmpty() {
        TaskContext context = new TaskContext();
        assertTrue(context.getData().isEmpty());
    }

    @Test
    public void testGetData_CanPutAndGet() {
        TaskContext context = new TaskContext();
        context.getData().put("key1", "value1");
        context.getData().put("async_job_id", "job-123");
        assertEquals("value1", context.getData().get("key1"));
        assertEquals("job-123", context.getData().get("async_job_id"));
        assertEquals(2, context.getData().size());
    }

    @Test
    public void testDefaultState_IsNull() {
        TaskContext context = new TaskContext();
        assertNull(context.getStep());
    }
}

