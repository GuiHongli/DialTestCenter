/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.dialtest.center.dto;

/**
 * 密码验证请求DTO
 *
 * @author g00940940
 * @since 2025-01-27
 */
public class PasswordValidationRequest {
    private String username;
    private String password;

    /**
     * 默认构造函数
     */
    public PasswordValidationRequest() {
    }

    /**
     * 带参数的构造函数
     *
     * @param username 用户名
     * @param password 密码
     */
    public PasswordValidationRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

    /**
     * 获取用户名
     *
     * @return 用户名
     */
    public String getUsername() {
        return username;
    }

    /**
     * 设置用户名
     *
     * @param username 用户名
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * 获取密码
     *
     * @return 密码
     */
    public String getPassword() {
        return password;
    }

    /**
     * 设置密码
     *
     * @param password 密码
     */
    public void setPassword(String password) {
        this.password = password;
    }
}
