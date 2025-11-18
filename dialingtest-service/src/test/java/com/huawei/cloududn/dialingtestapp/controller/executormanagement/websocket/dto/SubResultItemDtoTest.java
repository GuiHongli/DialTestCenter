/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2025. All rights reserved.
 */

package com.huawei.cloududn.dialingtestapp.controller.executormanagement.websocket.dto;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * SubResultItemDto UT test
 *
 * @author g00940940
 * @since 2025-11-18
 */
public class SubResultItemDtoTest {
    @Test
    public void testSubResultItemDto_AllMethods_Success() {
        SubResultItemDto dto1 = new SubResultItemDto();
        assertNotNull(dto1);
        
        SubResultItemDto dto2 = new SubResultItemDto("device001", "Success", "test passed");
        dto2.setSerialNo("device001");
        dto2.setResult("Success");
        dto2.setBlock("block1");
        dto2.setDescription("test passed");
        
        assertEquals("device001", dto2.getSerialNo());
        assertEquals("Success", dto2.getResult());
        assertEquals("block1", dto2.getBlock());
        assertEquals("test passed", dto2.getDescription());
        assertNotNull(dto2.toString());
    }
}

