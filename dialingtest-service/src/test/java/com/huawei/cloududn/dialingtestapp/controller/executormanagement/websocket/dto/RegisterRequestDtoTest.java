/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2025. All rights reserved.
 */

package com.huawei.cloududn.dialingtestapp.controller.executormanagement.websocket.dto;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * RegisterRequestDto UT test
 *
 * @author g00940940
 * @since 2025-11-18
 */
public class RegisterRequestDtoTest {
    @Test
    public void testRegisterRequestDto_AllMethods_Success() {
        RegisterRequestDto dto1 = new RegisterRequestDto();
        assertNotNull(dto1);
        
        RegisterRequestDto dto2 = new RegisterRequestDto("hostname001");
        dto2.setHostname("hostname001");
        
        assertEquals("hostname001", dto2.getHostname());
        assertNotNull(dto2.toString());
    }
}

