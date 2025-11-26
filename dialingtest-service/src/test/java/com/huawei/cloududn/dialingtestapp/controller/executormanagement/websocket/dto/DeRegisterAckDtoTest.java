/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2025. All rights reserved.
 */

package com.huawei.cloududn.dialingtestapp.controller.executormanagement.websocket.dto;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * DeRegisterAckDto UT test
 *
 * @author g00940940
 * @since 2025-11-18
 */
public class DeRegisterAckDtoTest {
    @Test
    public void testDeRegisterAckDto_AllMethods_Success() {
        DeRegisterAckDto dto = new DeRegisterAckDto(123456L, 0, "success");
        assertNotNull(dto);
        
        dto.setToken(123456L);
        dto.setResultCode(0);
        dto.setDescription("success");
        
        assertEquals(123456L, dto.getToken());
        assertEquals(0, dto.getResultCode());
        assertEquals("success", dto.getDescription());
    }
}

