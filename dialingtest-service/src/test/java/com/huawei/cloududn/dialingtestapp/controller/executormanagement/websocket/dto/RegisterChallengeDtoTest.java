/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2025. All rights reserved.
 */

package com.huawei.cloududn.dialingtestapp.controller.executormanagement.websocket.dto;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * RegisterChallengeDto UT test
 *
 * @author g00940940
 * @since 2025-11-18
 */
public class RegisterChallengeDtoTest {
    @Test
    public void testRegisterChallengeDto_AllMethods_Success() {
        RegisterChallengeDto dto1 = new RegisterChallengeDto();
        assertNotNull(dto1);
        
        RegisterChallengeDto dto2 = new RegisterChallengeDto(1001, "base64Challenge");
        dto2.setChallengeId(1001);
        dto2.setChallenge("base64Challenge");
        
        assertEquals(1001, dto2.getChallengeId());
        assertEquals("base64Challenge", dto2.getChallenge());
        assertNotNull(dto2.toString());
    }
}

