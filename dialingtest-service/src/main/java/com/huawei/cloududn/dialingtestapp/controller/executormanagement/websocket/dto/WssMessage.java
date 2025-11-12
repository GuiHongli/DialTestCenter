/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2025. All rights reserved.
 */

package com.huawei.cloududn.dialingtestapp.controller.executormanagement.websocket.dto;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * WSS message wrapper (Legacy JSON protocol).
 *
 * @author g00940940
 * @since 2025-11-04
 * @deprecated Use TLV protocol in V3
 */
@Deprecated
public class WssMessage {
    private String message_type;
    private JsonNode data;

    public WssMessage() {
    }

    public WssMessage(String message_type, JsonNode data) {
        this.message_type = message_type;
        this.data = data;
    }

    public String getMessage_type() {
        return message_type;
    }

    public void setMessage_type(String message_type) {
        this.message_type = message_type;
    }

    public JsonNode getData() {
        return data;
    }

    public void setData(JsonNode data) {
        this.data = data;
    }
}

