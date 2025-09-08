/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.dialtest.center.entity;

/**
 * 用户角色枚举类，定义系统中的用户角色类型和权限
 * 角色与权限强绑定，不进行拆分处理
 * 包含管理员、操作员、浏览者、执行机四种角色类型
 *
 * @author g00940940
 * @since 2025-09-06
 */
public enum Role {
    ADMIN("管理员", "拥有所有权限"),
    OPERATOR("操作员", "可以执行拨测任务相关的所有操作"),
    BROWSER("浏览者", "仅查看"),
    EXECUTOR("执行机", "执行机注册使用");

    private final String description;
    private final String permission;

    /**
     * 角色构造函数
     *
     * @param description 角色描述
     * @param permission 权限说明
     */
    Role(String description, String permission) {
        this.description = description;
        this.permission = permission;
    }

    /**
     * 获取角色描述
     *
     * @return 角色描述
     */
    public String getDescription() {
        return description;
    }

    /**
     * 获取权限说明
     *
     * @return 权限说明
     */
    public String getPermission() {
        return permission;
    }

    /**
     * 检查角色是否有管理员权限
     *
     * @return 如果是管理员角色返回true，否则返回false
     */
    public boolean isAdmin() {
        return this == ADMIN;
    }

    /**
     * 检查角色是否可以管理用户
     *
     * @return 如果可以管理用户返回true，否则返回false
     */
    public boolean canManageUsers() {
        return this == ADMIN;
    }

    /**
     * 检查角色是否可以执行拨测任务
     *
     * @return 如果可以执行拨测任务返回true，否则返回false
     */
    public boolean canExecuteTasks() {
        return this == ADMIN || this == OPERATOR;
    }

    /**
     * 检查角色是否可以注册执行机
     *
     * @return 如果可以注册执行机返回true，否则返回false
     */
    public boolean canRegisterExecutor() {
        return this == ADMIN || this == EXECUTOR;
    }
}
