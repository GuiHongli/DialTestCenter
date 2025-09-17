/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.dialtest.center.dto;

/**
 * 统一API响应格式
 *
 * @author g00940940
 * @since 2025-01-27
 */
public class BaseApiResponse<T> {
    private boolean success;
    private T data;
    private String message;
    private String errorCode;

    public BaseApiResponse() {
    }

    public BaseApiResponse(boolean success, T data, String message) {
        this.success = success;
        this.data = data;
        this.message = message;
    }

    public static <T> BaseApiResponse<T> success(T data) {
        return new BaseApiResponse<>(true, data, "Operation successful");
    }

    public static <T> BaseApiResponse<T> success(T data, String message) {
        return new BaseApiResponse<>(true, data, message);
    }

    public static <T> BaseApiResponse<T> error(String message) {
        return new BaseApiResponse<>(false, null, message);
    }

    public static <T> BaseApiResponse<T> error(String errorCode, String message) {
        BaseApiResponse<T> response = new BaseApiResponse<>(false, null, message);
        response.setErrorCode(errorCode);
        return response;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }
}