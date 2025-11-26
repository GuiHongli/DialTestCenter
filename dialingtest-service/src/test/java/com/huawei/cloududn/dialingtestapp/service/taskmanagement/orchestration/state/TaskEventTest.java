/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.cloududn.dialingtestapp.service.taskmanagement.orchestration.state;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * 任务事件枚举单元测试
 *
 * @author g00940940
 * @since 2025-11-09
 */
public class TaskEventTest {
    @Test
    public void testValues_Contains3Events() {
        TaskEvent[] events = TaskEvent.values();
        assertEquals(3, events.length);
    }

    @Test
    public void testValueOf_TaskSuccess() {
        TaskEvent event = TaskEvent.valueOf("TASK_SUCCESS");
        assertEquals(TaskEvent.TASK_SUCCESS, event);
    }

    @Test
    public void testValueOf_TaskFailed() {
        TaskEvent event = TaskEvent.valueOf("TASK_FAILED");
        assertEquals(TaskEvent.TASK_FAILED, event);
    }

    @Test
    public void testValueOf_Stop() {
        TaskEvent event = TaskEvent.valueOf("STOP");
        assertEquals(TaskEvent.STOP, event);
    }

    @Test
    public void testEnumValues_AllPresent() {
        assertNotNull(TaskEvent.TASK_SUCCESS);
        assertNotNull(TaskEvent.TASK_FAILED);
        assertNotNull(TaskEvent.STOP);
    }
}

