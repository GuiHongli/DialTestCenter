/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2025. All rights reserved.
 */

package com.huawei.cloududn.dialingtestapp.controller.executormanagement.websocket.dto;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * RegisterResponseDto UT test
 *
 * @author g00940940
 * @since 2025-11-18
 */
public class RegisterResponseDtoTest {
    @Test
    public void testRegisterResponseDto_AllMethods_Success() {
        RegisterResponseDto dto1 = new RegisterResponseDto();
        assertNotNull(dto1);
        
        RegisterResponseDto dto2 = new RegisterResponseDto(1001, "testuser", "hexResponse");
        dto2.setChallengeId(1001);
        dto2.setUsername("testuser");
        dto2.setResponse("hexResponse");
        
        assertEquals(1001, dto2.getChallengeId());
        assertEquals("testuser", dto2.getUsername());
        assertEquals("hexResponse", dto2.getResponse());
        assertNotNull(dto2.toString());
    }
}

