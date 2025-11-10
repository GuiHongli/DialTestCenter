/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2025. All rights reserved.
 */

package com.huawei.cloududn.dialingtest.controller.executormanagement.websocket.dto;

/**
 * Wrapper for unified WSS messages.
 *
 * <p>Format: {"message_type": "...", "data": {...}}</p>
 *
 * @author g00940940
 * @since 2025-11-04
 */
public class WssMessage {

    private String message_type;
    private Object data;

    public WssMessage() {
    }

    public WssMessage(String messageType, Object data) {
        this.message_type = messageType;
        this.data = data;
    }

    public String getMessage_type() {
        return message_type;
    }

    public void setMessage_type(String message_type) {
        this.message_type = message_type;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}


