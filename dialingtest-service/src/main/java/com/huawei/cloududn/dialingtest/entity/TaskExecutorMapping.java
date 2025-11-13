/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2025. All rights reserved.
 */

package com.huawei.cloududn.dialingtest.entity;

/**
 * 任务与执行机映射关系实体类
 *
 * @author g00940940
 * @since 2025-11-09
 */
public class TaskExecutorMapping {
    private Long id;
    private String taskId;
    private String executorName;
    private String ueSerial;
    private String assignTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getExecutorName() {
        return executorName;
    }

    public void setExecutorName(String executorName) {
        this.executorName = executorName;
    }

    public String getUeSerial() {
        return ueSerial;
    }

    public void setUeSerial(String ueSerial) {
        this.ueSerial = ueSerial;
    }

    public String getAssignTime() {
        return assignTime;
    }

    public void setAssignTime(String assignTime) {
        this.assignTime = assignTime;
    }
}

