/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2025. All rights reserved.
 */

package com.huawei.cloududn.dialingtestapp.controller.executormanagement.websocket.dto;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * ReportMsgDto UT test
 *
 * @author g00940940
 * @since 2025-11-18
 */
public class ReportMsgDtoTest {
    @Test
    public void testReportMsgDto_AllMethods_Success() {
        ReportMsgDto dto1 = new ReportMsgDto();
        assertNotNull(dto1);
        assertNotNull(dto1.getUeList());
        
        List<UeItemDto> ueList = new ArrayList<>();
        ReportMsgDto dto2 = new ReportMsgDto(123456L, "running", ueList);
        dto2.setToken(123456L);
        dto2.setState("running");
        dto2.setUeList(ueList);
        
        assertEquals(123456L, dto2.getToken());
        assertEquals("running", dto2.getState());
        assertNotNull(dto2.getUeList());
        assertNotNull(dto2.toString());
        
        ReportMsgDto dto3 = new ReportMsgDto(123456L, "running", null);
        assertNotNull(dto3.getUeList());
    }
}

