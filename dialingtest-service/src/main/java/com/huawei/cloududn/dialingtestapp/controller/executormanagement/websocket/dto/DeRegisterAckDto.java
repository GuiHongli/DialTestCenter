/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2025. All rights reserved.
 */

package com.huawei.cloududn.dialingtestapp.controller.executormanagement.websocket.dto;

/**
 * DeRegister-Ack DTO (0x06).
 * V3新增：注销确认消息
 *
 * @author g00940940
 * @since 2025-11-11
 */
public class DeRegisterAckDto {
    private long token;
    private int resultCode;
    private String description;

    public DeRegisterAckDto(long token, int resultCode, String description) {
        this.token = token;
        this.resultCode = resultCode;
        this.description = description;
    }

    public long getToken() {
        return token;
    }

    public void setToken(long token) {
        this.token = token;
    }

    public int getResultCode() {
        return resultCode;
    }

    public void setResultCode(int resultCode) {
        this.resultCode = resultCode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
