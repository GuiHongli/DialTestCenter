/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2025. All rights reserved.
 */

package com.huawei.cloududn.dialingtestapp.controller.executormanagement.websocket.dto;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * TaskStopRequestDto UT test
 *
 * @author g00940940
 * @since 2025-11-18
 */
public class TaskStopRequestDtoTest {
    @Test
    public void testTaskStopRequestDto_AllMethods_Success() {
        TaskStopRequestDto dto1 = new TaskStopRequestDto();
        assertNotNull(dto1);
        
        TaskStopRequestDto dto2 = new TaskStopRequestDto(123456L, 1001, "test_script", "1.0");
        dto2.setToken(123456L);
        dto2.setTaskId(1001);
        dto2.setScriptName("test_script");
        dto2.setVersion("1.0");
        
        assertEquals(123456L, dto2.getToken());
        assertEquals(1001, dto2.getTaskId());
        assertEquals("test_script", dto2.getScriptName());
        assertEquals("1.0", dto2.getVersion());
        assertNotNull(dto2.toString());
    }
}

