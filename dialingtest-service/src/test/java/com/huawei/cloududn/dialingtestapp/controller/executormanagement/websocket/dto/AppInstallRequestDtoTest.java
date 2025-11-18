/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2025. All rights reserved.
 */

package com.huawei.cloududn.dialingtestapp.controller.executormanagement.websocket.dto;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * AppInstallRequestDto UT test
 *
 * @author g00940940
 * @since 2025-11-18
 */
public class AppInstallRequestDtoTest {
    @Test
    public void testAppInstallRequestDto_AllMethods_Success() {
        AppInstallRequestDto dto1 = new AppInstallRequestDto();
        assertNotNull(dto1);
        
        AppInstallRequestDto dto2 = new AppInstallRequestDto(123456L, "device001", 1001, 
            "TestApp", 1024, "apk", "abc123");
        dto2.setToken(123456L);
        dto2.setSerialNo("device001");
        dto2.setTaskId(1001);
        dto2.setAppName("TestApp");
        dto2.setFilelen(1024);
        dto2.setFiletype("apk");
        dto2.setCrc("abc123");
        
        assertEquals(123456L, dto2.getToken());
        assertEquals("device001", dto2.getSerialNo());
        assertEquals(1001, dto2.getTaskId());
        assertEquals("TestApp", dto2.getAppName());
        assertEquals(Integer.valueOf(1024), dto2.getFilelen());
        assertEquals("apk", dto2.getFiletype());
        assertEquals("abc123", dto2.getCrc());
        assertNotNull(dto2.toString());
    }
}

