/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2025. All rights reserved.
 */

package com.huawei.cloududn.dialingtestapp.controller.executormanagement.websocket.dto;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * ScriptUpdateNotifyDto UT test
 *
 * @author g00940940
 * @since 2025-11-18
 */
public class ScriptUpdateNotifyDtoTest {
    @Test
    public void testScriptUpdateNotifyDto_AllMethods_Success() {
        ScriptUpdateNotifyDto dto1 = new ScriptUpdateNotifyDto();
        assertNotNull(dto1);
        
        ScriptUpdateNotifyDto dto2 = new ScriptUpdateNotifyDto(123456L, "test_script", "1.0", 2048, "def456");
        dto2.setToken(123456L);
        dto2.setScriptName("test_script");
        dto2.setVersion("1.0");
        dto2.setFilelen(2048);
        dto2.setCrc("def456");
        
        assertEquals(123456L, dto2.getToken());
        assertEquals("test_script", dto2.getScriptName());
        assertEquals("1.0", dto2.getVersion());
        assertEquals(2048, dto2.getFilelen());
        assertEquals("def456", dto2.getCrc());
        assertNotNull(dto2.toString());
    }
}

