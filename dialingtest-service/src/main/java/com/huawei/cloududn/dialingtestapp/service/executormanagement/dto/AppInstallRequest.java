/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2025. All rights reserved.
 */

package com.huawei.cloududn.dialingtestapp.service.executormanagement.dto;

/**
 * App安装请求DTO
 * 支持APK直装和脚本安装两种方式
 *
 * @author g00940940
 * @since 2025-11-11
 */
public class AppInstallRequest {

    private String serialNo;
    private Integer taskId;
    private String appName;
    private byte[] script;     // Airtest脚本内容（脚本安装方式）
    private byte[] packageFile; // APK文件内容（APK直装方式）
    private String crc;        // CRC校验码

    public AppInstallRequest() {
    }

    public AppInstallRequest(String serialNo, Integer taskId, String appName, byte[] script,
                            byte[] packageFile, String crc) {
        this.serialNo = serialNo;
        this.taskId = taskId;
        this.appName = appName;
        this.script = script;
        this.packageFile = packageFile;
        this.crc = crc;
    }

    public String getSerialNo() {
        return serialNo;
    }

    public void setSerialNo(String serialNo) {
        this.serialNo = serialNo;
    }

    public Integer getTaskId() {
        return taskId;
    }

    public void setTaskId(Integer taskId) {
        this.taskId = taskId;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public byte[] getScript() {
        return script;
    }

    public void setScript(byte[] script) {
        this.script = script;
    }

    public byte[] getPackageFile() {
        return packageFile;
    }

    public void setPackageFile(byte[] packageFile) {
        this.packageFile = packageFile;
    }

    public String getCrc() {
        return crc;
    }

    public void setCrc(String crc) {
        this.crc = crc;
    }

    /**
     * 检查是否为APK直装方式
     */
    public boolean isApkInstall() {
        return packageFile != null && packageFile.length > 0;
    }

    /**
     * 检查是否为脚本安装方式
     */
    public boolean isScriptInstall() {
        return script != null && script.length > 0;
    }

    @Override
    public String toString() {
        return "AppInstallRequest{" +
                "serialNo='" + serialNo + '\'' +
                ", taskId=" + taskId +
                ", appName='" + appName + '\'' +
                ", script=" + (script != null ? script.length : 0) + " bytes" +
                ", packageFile=" + (packageFile != null ? packageFile.length : 0) + " bytes" +
                ", crc='" + crc + '\'' +
                '}';
    }
}
