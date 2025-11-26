package com.huawei.cloududn.dialingtestapp.controller.executormanagement.websocket.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * ScriptUpdate-Notify (0x31) 消息DTO
 * 方向: ADCA ← CloudUDN
 * 说明: 更新脚本通知，服务端下发拨测脚本更新
 * V4版本：移除scriptFile字段，文件内容通过后续的Binary分片传输
 *
 * @author DialTestCenter
 * @version V4
 */
public class ScriptUpdateNotifyDto {

    @JsonProperty("token")
    private long token;

    @JsonProperty("script-name")
    private String scriptName;

    @JsonProperty("version")
    private String version;

    @JsonProperty("filelen")
    private int filelen;

    @JsonProperty("crc")
    private String crc;

    public ScriptUpdateNotifyDto() {
    }

    public ScriptUpdateNotifyDto(long token, String scriptName, String version, int filelen, String crc) {
        this.token = token;
        this.scriptName = scriptName;
        this.version = version;
        this.filelen = filelen;
        this.crc = crc;
    }

    public long getToken() {
        return token;
    }

    public void setToken(long token) {
        this.token = token;
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

    public int getFilelen() {
        return filelen;
    }

    public void setFilelen(int filelen) {
        this.filelen = filelen;
    }

    public String getCrc() {
        return crc;
    }

    public void setCrc(String crc) {
        this.crc = crc;
    }

    @Override
    public String toString() {
        return "ScriptUpdateNotifyDto{" +
                "token=" + token +
                ", scriptName='" + scriptName + '\'' +
                ", version='" + version + '\'' +
                ", filelen=" + filelen +
                ", crc='" + crc + '\'' +
                '}';
    }
}
