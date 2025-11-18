/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2025. All rights reserved.
 */

package com.huawei.cloududn.dialingtestapp.controller.executormanagement.websocket.dto;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * JsonMessageEnvelope UT test
 *
 * @author g00940940
 * @since 2025-11-18
 */
public class JsonMessageEnvelopeTest {
    @Test
    public void testJsonMessageEnvelope_AllMethods_Success() {
        JsonMessageEnvelope envelope1 = new JsonMessageEnvelope();
        assertNotNull(envelope1);
        
        Object payload = new Object();
        JsonMessageEnvelope envelope2 = new JsonMessageEnvelope("RegisterRequest", 123456L, payload);
        envelope2.setType("RegisterRequest");
        envelope2.setToken(123456L);
        envelope2.setRequestId("req-001");
        envelope2.setPayload(payload);
        
        assertEquals("RegisterRequest", envelope2.getType());
        assertEquals(Long.valueOf(123456L), envelope2.getToken());
        assertEquals("req-001", envelope2.getRequestId());
        assertEquals(payload, envelope2.getPayload());
        assertNotNull(envelope2.toString());
    }
}

