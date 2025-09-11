/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.dialtest.center.entity;

import java.time.LocalDateTime;

/**
 * 操作记录实体类，用于存储用户所有操作记录
 * 包含用户名、操作时间、操作类型、操作对象和操作描述等字段
 *
 * @author g00940940
 * @since 2025-09-09
 */
public class OperationLog {
    private Long id;
    private String username;
    private LocalDateTime operationTime;
    private String operationType;
    private String target;
    private String description;

    /**
     * 默认构造函数
     */
    public OperationLog() {
    }

    /**
     * 带参数的构造函数
     *
     * @param username 用户名
     * @param operationType 操作类型
     * @param target 操作对象
     * @param description 操作描述
     */
    public OperationLog(String username, String operationType, String target, String description) {
        this.username = username;
        this.operationType = operationType;
        this.target = target;
        this.description = description;
        this.operationTime = LocalDateTime.now();
    }

    /**
     * 获取操作记录ID
     *
     * @return 操作记录ID
     */
    public Long getId() {
        return id;
    }

    /**
     * 设置操作记录ID
     *
     * @param id 操作记录ID
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
     * 获取操作时间
     *
     * @return 操作时间
     */
    public LocalDateTime getOperationTime() {
        return operationTime;
    }

    /**
     * 设置操作时间
     *
     * @param operationTime 操作时间
     */
    public void setOperationTime(LocalDateTime operationTime) {
        this.operationTime = operationTime;
    }

    /**
     * 获取操作类型
     *
     * @return 操作类型
     */
    public String getOperationType() {
        return operationType;
    }

    /**
     * 设置操作类型
     *
     * @param operationType 操作类型
     */
    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }

    /**
     * 获取操作对象
     *
     * @return 操作对象
     */
    public String getTarget() {
        return target;
    }

    /**
     * 设置操作对象
     *
     * @param target 操作对象
     */
    public void setTarget(String target) {
        this.target = target;
    }

    /**
     * 获取操作描述
     *
     * @return 操作描述
     */
    public String getDescription() {
        return description;
    }

    /**
     * 设置操作描述
     *
     * @param description 操作描述
     */
    public void setDescription(String description) {
        this.description = description;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        OperationLog that = (OperationLog) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "OperationLog{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", operationTime=" + operationTime +
                ", operationType='" + operationType + '\'' +
                ", target='" + target + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
