/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2025. All rights reserved.
 */

package com.huawei.cloududn.dialingtestapp.controller.executormanagement.websocket.dto;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * DeRegisterRequestDto UT test
 *
 * @author g00940940
 * @since 2025-11-18
 */
public class DeRegisterRequestDtoTest {
    @Test
    public void testDeRegisterRequestDto_AllMethods_Success() {
        DeRegisterRequestDto dto1 = new DeRegisterRequestDto();
        assertNotNull(dto1);
        
        DeRegisterRequestDto dto2 = new DeRegisterRequestDto(123456L, "hostname001");
        dto2.setToken(123456L);
        dto2.setHostname("hostname001");
        
        assertEquals(123456L, dto2.getToken());
        assertEquals("hostname001", dto2.getHostname());
        assertNotNull(dto2.toString());
    }
}

