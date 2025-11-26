/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2025. All rights reserved.
 */

package com.huawei.cloududn.dialingtestapp.controller.executormanagement.websocket.dto;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * AppListQueryDto UT test
 *
 * @author g00940940
 * @since 2025-11-18
 */
public class AppListQueryDtoTest {
    @Test
    public void testAppListQueryDto_AllMethods_Success() {
        AppListQueryDto dto1 = new AppListQueryDto();
        assertNotNull(dto1);
        
        AppListQueryDto dto2 = new AppListQueryDto(123456L, "device001");
        dto2.setToken(123456L);
        dto2.setSerialNo("device001");
        
        assertEquals(123456L, dto2.getToken());
        assertEquals("device001", dto2.getSerialNo());
        assertNotNull(dto2.toString());
    }
}

