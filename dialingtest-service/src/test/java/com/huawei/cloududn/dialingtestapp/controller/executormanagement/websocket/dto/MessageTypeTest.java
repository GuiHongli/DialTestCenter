/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2025. All rights reserved.
 */

package com.huawei.cloududn.dialingtestapp.controller.executormanagement.websocket.dto;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * MessageType UT test
 *
 * @author g00940940
 * @since 2025-11-18
 */
public class MessageTypeTest {
    @Test
    public void testMessageType_AllMethods_Success() {
        MessageType type = MessageType.REGISTER_REQUEST;
        assertEquals(0x01, type.getId());
        assertEquals("RegisterRequest", type.getName());
        assertNotNull(type.getDescription());
        assertNotNull(type.getDirection());
        assertEquals("RegisterRequest", type.getJsonTypeName());
        
        MessageType fromId = MessageType.fromId(0x01);
        assertEquals(MessageType.REGISTER_REQUEST, fromId);
        
        MessageType fromJson = MessageType.fromJsonType("RegisterRequest");
        assertEquals(MessageType.REGISTER_REQUEST, fromJson);
        
        MessageType.Direction direction = MessageType.Direction.AGENT_TO_SERVER;
        assertNotNull(direction.getDescription());
        
        for (MessageType mt : MessageType.values()) {
            assertNotNull(mt.getId());
            assertNotNull(mt.getName());
            assertNotNull(mt.getDescription());
        }
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testMessageType_FromIdInvalid_ThrowException() {
        MessageType.fromId(0xFFFF);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testMessageType_FromJsonTypeNull_ThrowException() {
        MessageType.fromJsonType(null);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testMessageType_FromJsonTypeInvalid_ThrowException() {
        MessageType.fromJsonType("InvalidType");
    }
}

