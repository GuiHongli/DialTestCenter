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
 * ScriptUpdateAckDto UT test
 *
 * @author g00940940
 * @since 2025-11-18
 */
public class ScriptUpdateAckDtoTest {
    @Test
    public void testScriptUpdateAckDto_AllMethods_Success() {
        ScriptUpdateAckDto dto1 = new ScriptUpdateAckDto();
        assertNotNull(dto1);
        
        ScriptUpdateAckDto dto2 = new ScriptUpdateAckDto(123456L, "test_script", "1.0", 0);
        dto2.setToken(123456L);
        dto2.setScriptName("test_script");
        dto2.setVersion("1.0");
        dto2.setState(0);
        
        assertEquals(123456L, dto2.getToken());
        assertEquals("test_script", dto2.getScriptName());
        assertEquals("1.0", dto2.getVersion());
        assertEquals(0, dto2.getState());
        assertTrue(dto2.isSuccess());
        
        dto2.setState(1);
        assertFalse(dto2.isSuccess());
        assertNotNull(dto2.toString());
    }
}

