/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.cloududn.dialingtest.service.taskmanagement.orchestration.state;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * 任务状态枚举单元测试
 *
 * @author g00940940
 * @since 2025-11-09
 */
public class TaskStateTest {
    @Test
    public void testValues_Contains9States() {
        TaskState[] states = TaskState.values();
        assertEquals(9, states.length);
    }

    @Test
    public void testValueOf_StartValidation() {
        TaskState state = TaskState.valueOf("START_VALIDATION");
        assertEquals(TaskState.START_VALIDATION, state);
    }

    @Test
    public void testValueOf_Final() {
        TaskState state = TaskState.valueOf("FINAL");
        assertEquals(TaskState.FINAL, state);
    }

    @Test
    public void testEnumValues_AllPresent() {
        assertNotNull(TaskState.START_VALIDATION);
        assertNotNull(TaskState.START_TRAINING_DIALING);
        assertNotNull(TaskState.START_MODEL_TRAIN);
        assertNotNull(TaskState.START_MODEL_REPLAY);
        assertNotNull(TaskState.START_GRAY_VALIDATION);
        assertNotNull(TaskState.START_FULL_REPLAY);
        assertNotNull(TaskState.START_WHITELIST);
        assertNotNull(TaskState.START_FULL_RELEASE);
        assertNotNull(TaskState.FINAL);
    }
}

