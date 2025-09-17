/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.dialtest.center.dto;

/**
 * 更新登录时间请求DTO
 *
 * @author g00940940
 * @since 2025-01-27
 */
public class UpdateLoginTimeRequest {
    private String username;

    /**
     * 默认构造函数
     */
    public UpdateLoginTimeRequest() {
    }

    /**
     * 带参数的构造函数
     *
     * @param username 用户名
     */
    public UpdateLoginTimeRequest(String username) {
        this.username = username;
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
}
