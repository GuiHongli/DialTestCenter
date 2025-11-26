/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2025. All rights reserved.
 */

package com.huawei.cloududn.dialingtestapp.controller.executormanagement.websocket.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * V4 JSON消息信封
 * 所有WebSocket JSON消息的统一外层结构
 *
 * @author g00940940
 * @since 2025-11-14
 */
public class JsonMessageEnvelope {
    @JsonProperty("type")
    private String type;

    @JsonProperty("token")
    private Long token;

    @JsonProperty("requestId")
    private String requestId;

    @JsonProperty("payload")
    private Object payload;

    public JsonMessageEnvelope() {
    }

    public JsonMessageEnvelope(String type, Long token, Object payload) {
        this.type = type;
        this.token = token;
        this.payload = payload;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getToken() {
        return token;
    }

    public void setToken(Long token) {
        this.token = token;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public Object getPayload() {
        return payload;
    }

    public void setPayload(Object payload) {
        this.payload = payload;
    }

    @Override
    public String toString() {
        return "JsonMessageEnvelope{" +
                "type='" + type + '\'' +
                ", token=" + token +
                ", requestId='" + requestId + '\'' +
                '}';
    }
}

