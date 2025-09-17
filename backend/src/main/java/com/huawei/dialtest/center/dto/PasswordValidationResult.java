/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.dialtest.center.dto;

/**
 * 密码验证结果DTO
 *
 * @author g00940940
 * @since 2025-01-27
 */
public class PasswordValidationResult {
    private boolean valid;
    private String message;

    /**
     * 默认构造函数
     */
    public PasswordValidationResult() {
    }

    /**
     * 带参数的构造函数
     *
     * @param valid 密码是否有效
     * @param message 验证结果消息
     */
    public PasswordValidationResult(boolean valid, String message) {
        this.valid = valid;
        this.message = message;
    }

    /**
     * 获取密码是否有效
     *
     * @return 密码是否有效
     */
    public boolean isValid() {
        return valid;
    }

    /**
     * 设置密码是否有效
     *
     * @param valid 密码是否有效
     */
    public void setValid(boolean valid) {
        this.valid = valid;
    }

    /**
     * 获取验证结果消息
     *
     * @return 验证结果消息
     */
    public String getMessage() {
        return message;
    }

    /**
     * 设置验证结果消息
     *
     * @param message 验证结果消息
     */
    public void setMessage(String message) {
        this.message = message;
    }
}
