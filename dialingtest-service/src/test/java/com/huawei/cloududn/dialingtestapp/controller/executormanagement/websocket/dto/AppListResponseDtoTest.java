/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2025. All rights reserved.
 */

package com.huawei.cloududn.dialingtestapp.controller.executormanagement.websocket.dto;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * AppListResponseDto UT test
 *
 * @author g00940940
 * @since 2025-11-18
 */
public class AppListResponseDtoTest {
    @Test
    public void testAppListResponseDto_AllMethods_Success() {
        AppListResponseDto dto1 = new AppListResponseDto();
        assertNotNull(dto1);
        assertNotNull(dto1.getAppList());
        
        List<AppItemDto> appList = new ArrayList<>();
        AppListResponseDto dto2 = new AppListResponseDto(123456L, "device001", 0, appList);
        dto2.setToken(123456L);
        dto2.setSerialNo("device001");
        dto2.setState(0);
        dto2.setAppList(appList);
        
        assertEquals(123456L, dto2.getToken());
        assertEquals("device001", dto2.getSerialNo());
        assertEquals(0, dto2.getState());
        assertNotNull(dto2.getAppList());
        assertTrue(dto2.isSuccess());
        
        dto2.setState(1);
        assertFalse(dto2.isSuccess());
        assertNotNull(dto2.toString());
        
        AppListResponseDto dto3 = new AppListResponseDto(123456L, "device001", 0, null);
        assertNotNull(dto3.getAppList());
    }
}

