/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.cloududn.dialingtestapp.service.taskmanagement.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * 人工启动拨测任务请求体。
 *
 * @author g00940940
 * @since 2025-10-24
 */
public class StartTaskRequest {
    @JsonProperty("business_type")
    private String businessType;
    @JsonProperty("scenario")
    private String scenario;
    @JsonProperty("script_names")
    private List<String> scriptNames;
    @JsonProperty("target_ues")
    private List<String> targetUes;
    @JsonProperty("failed_apps")
    private List<String> failedApps;

    public String getBusinessType() {
        return businessType;
    }

    public void setBusinessType(String businessType) {
        this.businessType = businessType;
    }

    public String getScenario() {
        return scenario;
    }

    public void setScenario(String scenario) {
        this.scenario = scenario;
    }

    public List<String> getScriptNames() {
        return scriptNames;
    }

    public void setScriptNames(List<String> scriptNames) {
        this.scriptNames = scriptNames;
    }

    public List<String> getTargetUes() {
        return targetUes;
    }

    public void setTargetUes(List<String> targetUes) {
        this.targetUes = targetUes;
    }

    public List<String> getFailedApps() {
        return failedApps;
    }

    public void setFailedApps(List<String> failedApps) {
        this.failedApps = failedApps;
    }
}


