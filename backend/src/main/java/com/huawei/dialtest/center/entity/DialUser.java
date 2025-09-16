/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.dialtest.center.entity;

/**
 * 用户实体类，用于存储用户基本信息
 * 包含用户名、密码和最后登录时间等字段
 *
 * @author g00940940
 * @since 2025-09-09
 */
public class DialUser {
    private Long id;
    private String username;
    private String password;
    private String lastLoginTime;

    /**
     * 默认构造函数
     */
    public DialUser() {
    }

    /**
     * 带参数的构造函数
     *
     * @param username 用户名
     * @param password 密码
     */
    public DialUser(String username, String password) {
        this.username = username;
        this.password = password;
    }

    /**
     * 获取用户ID
     *
     * @return 用户ID
     */
    public Long getId() {
        return id;
    }

    /**
     * 设置用户ID
     *
     * @param id 用户ID
     */
    public void setId(Long id) {
        this.id = id;
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

    /**
     * 获取最后登录时间
     *
     * @return 最后登录时间
     */
    public String getLastLoginTime() {
        return lastLoginTime;
    }

    /**
     * 设置最后登录时间
     *
     * @param lastLoginTime 最后登录时间
     */
    public void setLastLoginTime(String lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DialUser user = (DialUser) o;
        return id != null && id.equals(user.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "DialUser{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", lastLoginTime='" + lastLoginTime + '\'' +
                '}';
    }
}
