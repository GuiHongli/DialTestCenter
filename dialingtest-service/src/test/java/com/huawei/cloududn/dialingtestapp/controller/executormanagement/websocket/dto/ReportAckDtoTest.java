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
 * ReportAckDto UT test
 *
 * @author g00940940
 * @since 2025-11-18
 */
public class ReportAckDtoTest {
    @Test
    public void testReportAckDto_AllMethods_Success() {
        ReportAckDto dto1 = new ReportAckDto();
        assertNotNull(dto1);
        
        ReportAckDto dto2 = new ReportAckDto(123456L, 0);
        dto2.setToken(123456L);
        dto2.setState(0);
        
        assertEquals(123456L, dto2.getToken());
        assertEquals(0, dto2.getState());
        assertTrue(dto2.isSuccess());
        
        dto2.setState(1);
        assertFalse(dto2.isSuccess());
        assertNotNull(dto2.toString());
    }
}

