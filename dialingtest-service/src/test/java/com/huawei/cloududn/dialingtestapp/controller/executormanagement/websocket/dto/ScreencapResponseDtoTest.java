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
 * ScreencapResponseDto UT test
 *
 * @author g00940940
 * @since 2025-11-18
 */
public class ScreencapResponseDtoTest {
    @Test
    public void testScreencapResponseDto_AllMethods_Success() {
        ScreencapResponseDto dto1 = new ScreencapResponseDto();
        assertNotNull(dto1);
        
        ScreencapResponseDto dto2 = new ScreencapResponseDto(123456L, "device001", 0, "screen.png", 4096, "ghi789");
        dto2.setToken(123456L);
        dto2.setSerialNo("device001");
        dto2.setState(0);
        dto2.setFilename("screen.png");
        dto2.setFilelen(4096);
        dto2.setCrc("ghi789");
        
        assertEquals(123456L, dto2.getToken());
        assertEquals("device001", dto2.getSerialNo());
        assertEquals(0, dto2.getState());
        assertEquals("screen.png", dto2.getFilename());
        assertEquals(Integer.valueOf(4096), dto2.getFilelen());
        assertEquals("ghi789", dto2.getCrc());
        assertTrue(dto2.isSuccess());
        
        dto2.setState(1);
        assertFalse(dto2.isSuccess());
        assertNotNull(dto2.toString());
    }
}

