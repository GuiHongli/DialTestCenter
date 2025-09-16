/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.dialtest.center.dto;

import java.time.LocalDateTime;

/**
 * 错误响应格式
 *
 * @author g00940940
 * @since 2025-01-27
 */
public class ErrorResponse {
    private boolean success;
    private String error;
    private String message;
    private LocalDateTime timestamp;

    public ErrorResponse() {
        this.success = false;
        this.timestamp = LocalDateTime.now();
    }

    public ErrorResponse(String error, String message) {
        this.success = false;
        this.error = error;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }

    public static ErrorResponse of(String error, String message) {
        return new ErrorResponse(error, message);
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
