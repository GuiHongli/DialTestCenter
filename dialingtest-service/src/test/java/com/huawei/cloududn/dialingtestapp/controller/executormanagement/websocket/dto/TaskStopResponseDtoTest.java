/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2025. All rights reserved.
 */

package com.huawei.cloududn.dialingtestapp.controller.executormanagement.websocket.dto;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * TaskStopResponseDto UT test
 *
 * @author g00940940
 * @since 2025-11-18
 */
public class TaskStopResponseDtoTest {
    @Test
    public void testTaskStopResponseDto_AllMethods_Success() {
        TaskStopResponseDto dto1 = new TaskStopResponseDto();
        assertNotNull(dto1);
        
        TaskStopResponseDto dto2 = new TaskStopResponseDto(123456L, 1001, 0);
        assertNotNull(dto2);
        
        TaskStopResponseDto dto3 = new TaskStopResponseDto(123456L, 1001, 0, "success");
        dto3.setToken(123456L);
        dto3.setTaskId(1001);
        dto3.setState(0);
        dto3.setDescription("success");
        
        assertEquals(123456L, dto3.getToken());
        assertEquals(1001, dto3.getTaskId());
        assertEquals(0, dto3.getState());
        assertEquals("success", dto3.getDescription());
        assertTrue(dto3.isSuccess());
        
        dto3.setState(1);
        assertFalse(dto3.isSuccess());
        assertNotNull(dto3.toString());
    }
}

