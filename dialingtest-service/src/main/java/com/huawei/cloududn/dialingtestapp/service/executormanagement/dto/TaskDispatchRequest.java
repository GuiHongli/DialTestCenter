/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2025. All rights reserved.
 */

package com.huawei.cloududn.dialingtestapp.service.executormanagement.dto;

import java.util.List;

/**
 * 任务分发请求DTO
 * 用于向指定执行机分发拨测任务
 *
 * @author g00940940
 * @since 2025-11-11
 */
public class TaskDispatchRequest {

    private String executorName;
    private Integer taskId;
    private String scriptName;
    private String version;
    private List<String> serialNoList;
    private String proctype;
    private String parameters;

    public TaskDispatchRequest() {
    }

    public TaskDispatchRequest(String executorName, Integer taskId, String scriptName, String version,
                              List<String> serialNoList, String proctype, String parameters) {
        this.executorName = executorName;
        this.taskId = taskId;
        this.scriptName = scriptName;
        this.version = version;
        this.serialNoList = serialNoList;
        this.proctype = proctype;
        this.parameters = parameters;
    }

    public String getExecutorName() {
        return executorName;
    }

    public void setExecutorName(String executorName) {
        this.executorName = executorName;
    }

    public Integer getTaskId() {
        return taskId;
    }

    public void setTaskId(Integer taskId) {
        this.taskId = taskId;
    }

    public String getScriptName() {
        return scriptName;
    }

    public void setScriptName(String scriptName) {
        this.scriptName = scriptName;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public List<String> getSerialNoList() {
        return serialNoList;
    }

    public void setSerialNoList(List<String> serialNoList) {
        this.serialNoList = serialNoList;
    }

    public String getProctype() {
        return proctype;
    }

    public void setProctype(String proctype) {
        this.proctype = proctype;
    }

    public String getParameters() {
        return parameters;
    }

    public void setParameters(String parameters) {
        this.parameters = parameters;
    }

    @Override
    public String toString() {
        return "TaskDispatchRequest{" +
                "executorName='" + executorName + '\'' +
                ", taskId=" + taskId +
                ", scriptName='" + scriptName + '\'' +
                ", version='" + version + '\'' +
                ", serialNoList=" + serialNoList +
                ", proctype='" + proctype + '\'' +
                ", parameters='" + parameters + '\'' +
                '}';
    }
}
