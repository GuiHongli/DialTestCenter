/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.dialtest.center.entity;

import java.util.Objects;

/**
 * 用户角色关系实体类，用于管理用户与角色的关联关系
 * 支持用户角色分配、权限管理等功能
 * 包含用户名、角色类型等字段
 *
 * @author g00940940
 * @since 2025-09-06
 */
public class DialUserRole {
    private Long id;
    private String username;
    private Role role;

    /**
     * 默认构造函数
     *
     * @author g00940940
     * @since 2025-09-06
     */
    public DialUserRole() {}

    /**
     * 带参数构造函数，用于创建新的用户角色关系实例
     *
     * @param username 用户名
     * @param role 角色类型
     * @author g00940940
     * @since 2025-09-06
     */
    public DialUserRole(String username, Role role) {
        this.username = username;
        this.role = role;
    }

    // Getter和Setter方法
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DialUserRole userRole = (DialUserRole) o;
        return Objects.equals(id, userRole.id) &&
               Objects.equals(username, userRole.username) &&
               role == userRole.role;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, username, role);
    }

    @Override
    public String toString() {
        return "DialUserRole{" +
               "id=" + id +
               ", username='" + username + '\'' +
               ", role=" + role +
               '}';
    }
}
