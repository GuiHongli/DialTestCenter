package com.dialtest.center.entity;

/**
 * 用户角色枚举
 * 角色与权限强绑定，不进行拆分处理
 */
public enum Role {
    ADMIN("管理员", "拥有所有权限"),
    OPERATOR("操作员", "可以执行拨测任务相关的所有操作"),
    BROWSER("浏览者", "仅查看"),
    EXECUTOR("执行机", "执行机注册使用");
    
    private final String description;
    private final String permission;
    
    Role(String description, String permission) {
        this.description = description;
        this.permission = permission;
    }
    
    public String getDescription() {
        return description;
    }
    
    public String getPermission() {
        return permission;
    }
    
    /**
     * 检查角色是否有管理员权限
     */
    public boolean isAdmin() {
        return this == ADMIN;
    }
    
    /**
     * 检查角色是否可以管理用户
     */
    public boolean canManageUsers() {
        return this == ADMIN;
    }
    
    /**
     * 检查角色是否可以执行拨测任务
     */
    public boolean canExecuteTasks() {
        return this == ADMIN || this == OPERATOR;
    }
    
    /**
     * 检查角色是否可以注册执行机
     */
    public boolean canRegisterExecutor() {
        return this == ADMIN || this == EXECUTOR;
    }
}
