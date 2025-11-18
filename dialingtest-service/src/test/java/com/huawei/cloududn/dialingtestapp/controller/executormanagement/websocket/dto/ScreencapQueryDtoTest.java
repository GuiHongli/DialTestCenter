/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2025. All rights reserved.
 */

package com.huawei.cloududn.dialingtestapp.controller.executormanagement.websocket.dto;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * ScreencapQueryDto UT test
 *
 * @author g00940940
 * @since 2025-11-18
 */
public class ScreencapQueryDtoTest {
    @Test
    public void testScreencapQueryDto_AllMethods_Success() {
        ScreencapQueryDto dto1 = new ScreencapQueryDto();
        assertNotNull(dto1);
        
        ScreencapQueryDto dto2 = new ScreencapQueryDto(123456L, "device001");
        dto2.setToken(123456L);
        dto2.setSerialNo("device001");
        
        assertEquals(123456L, dto2.getToken());
        assertEquals("device001", dto2.getSerialNo());
        assertNotNull(dto2.toString());
    }
}

