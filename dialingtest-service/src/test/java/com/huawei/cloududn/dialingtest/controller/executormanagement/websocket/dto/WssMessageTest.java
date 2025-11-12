/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2025. All rights reserved.
 */

package com.huawei.cloududn.dialingtest.controller.executormanagement.websocket.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * WssMessage DTO 单元测试
 *
 * @author g00940940
 * @since 2025-11-09
 */
public class WssMessageTest {
    @Test
    public void testDefaultConstructor() {
        WssMessage message = new WssMessage();
        assertNull(message.getMessage_type());
        assertNull(message.getData());
    }

    @Test
    public void testParameterizedConstructor() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> data = new HashMap<>();
        data.put("key", "value");
        JsonNode jsonData = mapper.valueToTree(data);
        WssMessage message = new WssMessage("heartbeat_status", jsonData);
        assertEquals("heartbeat_status", message.getMessage_type());
        assertEquals(jsonData, message.getData());
    }

    @Test
    public void testSetAndGetMessageType() {
        WssMessage message = new WssMessage();
        message.setMessage_type("update_executor_info");
        assertEquals("update_executor_info", message.getMessage_type());
    }

    @Test
    public void testSetAndGetData() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        WssMessage message = new WssMessage();
        Map<String, Object> data = new HashMap<>();
        data.put("ue_list", new String[]{"ue1", "ue2"});
        JsonNode jsonData = mapper.valueToTree(data);
        message.setData(jsonData);
        assertNotNull(message.getData());
        assertEquals(jsonData, message.getData());
    }

    @Test
    public void testSetAndGetData_JsonNode() throws Exception {
        WssMessage message = new WssMessage();
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonData = mapper.readTree("{\"status\":\"online\"}");
        message.setData(jsonData);
        assertNotNull(message.getData());
        assertEquals(jsonData, message.getData());
    }

    @Test
    public void testSetMessageType_Null() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> data = new HashMap<>();
        JsonNode jsonData = mapper.valueToTree(data);
        WssMessage message = new WssMessage("initial", jsonData);
        message.setMessage_type(null);
        assertNull(message.getMessage_type());
    }

    @Test
    public void testSetData_Null() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> data = new HashMap<>();
        JsonNode jsonData = mapper.valueToTree(data);
        WssMessage message = new WssMessage("type", jsonData);
        message.setData(null);
        assertNull(message.getData());
    }
}

