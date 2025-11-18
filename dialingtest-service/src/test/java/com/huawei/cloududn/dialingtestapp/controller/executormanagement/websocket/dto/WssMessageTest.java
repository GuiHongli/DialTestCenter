/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2025. All rights reserved.
 */

package com.huawei.cloududn.dialingtestapp.controller.executormanagement.websocket.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * WssMessage UT test
 *
 * @author g00940940
 * @since 2025-11-18
 */
public class WssMessageTest {
    @Test
    @SuppressWarnings("deprecation")
    public void testWssMessage_AllMethods_Success() {
        WssMessage msg1 = new WssMessage();
        assertNotNull(msg1);
        
        JsonNode data = new TextNode("test");
        WssMessage msg2 = new WssMessage("test_type", data);
        msg2.setMessage_type("test_type");
        msg2.setData(data);
        
        assertEquals("test_type", msg2.getMessage_type());
        assertEquals(data, msg2.getData());
    }
}

