/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2025. All rights reserved.
 */

package com.huawei.cloududn.dialingtestapp.controller.executormanagement.websocket.dto;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * AppItemDto UT test
 *
 * @author g00940940
 * @since 2025-11-18
 */
public class AppItemDtoTest {
    @Test
    public void testAppItemDto_AllMethods_Success() {
        AppItemDto dto1 = new AppItemDto();
        assertNotNull(dto1);
        
        AppItemDto dto2 = new AppItemDto("com.test.app", "TestApp", "1.0.0");
        dto2.setPackageName("com.test.app");
        dto2.setName("TestApp");
        dto2.setVersion("1.0.0");
        
        assertEquals("com.test.app", dto2.getPackageName());
        assertEquals("TestApp", dto2.getName());
        assertEquals("1.0.0", dto2.getVersion());
        assertNotNull(dto2.toString());
    }
}

