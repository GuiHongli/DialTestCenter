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
 * TaskStartRequestDto UT test
 *
 * @author g00940940
 * @since 2025-11-18
 */
public class TaskStartRequestDtoTest {
    @Test
    public void testTaskStartRequestDto_AllMethods_Success() {
        TaskStartRequestDto dto = new TaskStartRequestDto();
        assertNotNull(dto);
        assertNotNull(dto.getSerialNoList());
        
        dto.setToken(123456L);
        dto.setTaskId(1001);
        dto.setScriptName("test_script");
        dto.setVersion("1.0");
        dto.setProcType(1);
        dto.setParameters("--param1 value1");
        
        List<String> serialNos = new ArrayList<>();
        serialNos.add("device001");
        dto.setSerialNoList(serialNos);
        
        assertEquals(123456L, dto.getToken());
        assertEquals(1001, dto.getTaskId());
        assertEquals("test_script", dto.getScriptName());
        assertEquals("1.0", dto.getVersion());
        assertEquals(Integer.valueOf(1), dto.getProcType());
        assertEquals("--param1 value1", dto.getParameters());
        assertNotNull(dto.getSerialNoList());
        assertNotNull(dto.toString());
    }
}

