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
 * RegisterResultDto UT test
 *
 * @author g00940940
 * @since 2025-11-18
 */
public class RegisterResultDtoTest {
    @Test
    public void testRegisterResultDto_AllMethods_Success() {
        RegisterResultDto dto1 = new RegisterResultDto();
        assertNotNull(dto1);
        
        RegisterResultDto dto2 = new RegisterResultDto(0, "success");
        assertEquals(0, dto2.getResult());
        assertEquals("success", dto2.getDescription());
        assertTrue(dto2.isSuccess());
        assertTrue(dto2.getSuccess());
        
        RegisterResultDto dto3 = new RegisterResultDto(1, "failed", 123456L);
        dto3.setResult(1);
        dto3.setDescription("failed");
        dto3.setToken(123456L);
        
        assertEquals(1, dto3.getResult());
        assertEquals("failed", dto3.getDescription());
        assertEquals(Long.valueOf(123456L), dto3.getToken());
        assertFalse(dto3.isSuccess());
        assertFalse(dto3.getSuccess());
        assertNotNull(dto3.toString());
    }
}

