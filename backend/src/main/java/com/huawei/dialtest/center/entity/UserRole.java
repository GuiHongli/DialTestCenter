/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.dialtest.center.entity;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

/**
 * 用户角色关系实体类，用于管理用户与角色的关联关系
 * 支持用户角色分配、权限管理等功能
 * 包含用户名、角色类型、创建时间、更新时间等字段
 *
 * @author g00940940
 * @since 2025-09-06
 */
@Entity
@Table(name = "user_role", 
       uniqueConstraints = @UniqueConstraint(name = "uk_username_role", columnNames = {"username", "role"}),
       indexes = {
           @Index(name = "idx_username", columnList = "username"),
           @Index(name = "idx_role", columnList = "role")
       })
public class UserRole {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", nullable = false, length = 100)
    private String username;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 50)
    private Role role;

    @CreationTimestamp
    @Column(name = "created_time")
    private LocalDateTime createdTime;

    @UpdateTimestamp
    @Column(name = "updated_time")
    private LocalDateTime updatedTime;

    /**
     * 默认构造函数
     *
     * @author g00940940
     * @since 2025-09-06
     */
    public UserRole() {}

    /**
     * 带参数构造函数，用于创建新的用户角色关系实例
     *
     * @param username 用户名
     * @param role 角色类型
     * @author g00940940
     * @since 2025-09-06
     */
    public UserRole(String username, Role role) {
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

    public LocalDateTime getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(LocalDateTime createdTime) {
        this.createdTime = createdTime;
    }

    public LocalDateTime getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(LocalDateTime updatedTime) {
        this.updatedTime = updatedTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UserRole userRole = (UserRole) o;
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
        return "UserRole{" +
               "id=" + id +
               ", username='" + username + '\'' +
               ", role=" + role +
               ", createdTime=" + createdTime +
               ", updatedTime=" + updatedTime +
               '}';
    }
}
