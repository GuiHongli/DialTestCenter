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
 * AppInstallResponseDto UT test
 *
 * @author g00940940
 * @since 2025-11-18
 */
public class AppInstallResponseDtoTest {
    @Test
    public void testAppInstallResponseDto_AllMethods_Success() {
        AppInstallResponseDto dto1 = new AppInstallResponseDto();
        assertNotNull(dto1);
        
        AppInstallResponseDto dto2 = new AppInstallResponseDto(123456L, "device001", 1001, 0);
        dto2.setToken(123456L);
        dto2.setSerialNo("device001");
        dto2.setTaskId(1001);
        dto2.setState(0);
        
        assertEquals(123456L, dto2.getToken());
        assertEquals("device001", dto2.getSerialNo());
        assertEquals(1001, dto2.getTaskId());
        assertEquals(0, dto2.getState());
        assertTrue(dto2.isSuccess());
        
        dto2.setState(1);
        assertFalse(dto2.isSuccess());
        assertNotNull(dto2.toString());
    }
}

