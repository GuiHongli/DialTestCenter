/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2025. All rights reserved.
 */

package com.huawei.cloududn.dialingtestapp.service.executormanagement.dto;

/**
 * 脚本更新请求DTO
 * 用于向执行机推送新的拨测脚本
 *
 * @author g00940940
 * @since 2025-11-11
 */
public class ScriptUpdateRequest {

    private String scriptName;
    private String version;
    private byte[] scriptFile;
    private String crc;

    public ScriptUpdateRequest() {
    }

    public ScriptUpdateRequest(String scriptName, String version, byte[] scriptFile, String crc) {
        this.scriptName = scriptName;
        this.version = version;
        this.scriptFile = scriptFile;
        this.crc = crc;
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

    public byte[] getScriptFile() {
        return scriptFile;
    }

    public void setScriptFile(byte[] scriptFile) {
        this.scriptFile = scriptFile;
    }

    public String getCrc() {
        return crc;
    }

    public void setCrc(String crc) {
        this.crc = crc;
    }

    @Override
    public String toString() {
        return "ScriptUpdateRequest{" +
                "scriptName='" + scriptName + '\'' +
                ", version='" + version + '\'' +
                ", scriptFile=" + (scriptFile != null ? scriptFile.length : 0) + " bytes" +
                ", crc='" + crc + '\'' +
                '}';
    }
}
