package com.huawei.cloududn.dialingtestapp.controller.executormanagement.websocket.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * AppInstall-Request (0x23) 消息DTO
 * 方向: ADCA ← CloudUDN
 * 说明: App安装请求
 * V4版本：移除package/script字段，文件内容通过后续的Binary分片传输
 *
 * @author DialTestCenter
 * @version V4
 */
public class AppInstallRequestDto {

    @JsonProperty("token")
    private long token;

    @JsonProperty("serial-no")
    private String serialNo;

    @JsonProperty("taskid")
    private int taskId;

    @JsonProperty("appname")
    private String appName;

    @JsonProperty("filelen")
    private Integer filelen;

    @JsonProperty("filetype")
    private String filetype;

    @JsonProperty("crc")
    private String crc;


    public AppInstallRequestDto() {
    }

    public AppInstallRequestDto(long token, String serialNo, int taskId, String appName, 
                                Integer filelen, String filetype, String crc) {
        this.token = token;
        this.serialNo = serialNo;
        this.taskId = taskId;
        this.appName = appName;
        this.filelen = filelen;
        this.filetype = filetype;
        this.crc = crc;
    }

    public long getToken() {
        return token;
    }

    public void setToken(long token) {
        this.token = token;
    }

    public String getSerialNo() {
        return serialNo;
    }

    public void setSerialNo(String serialNo) {
        this.serialNo = serialNo;
    }

    public int getTaskId() {
        return taskId;
    }

    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public Integer getFilelen() {
        return filelen;
    }

    public void setFilelen(Integer filelen) {
        this.filelen = filelen;
    }

    public String getFiletype() {
        return filetype;
    }

    public void setFiletype(String filetype) {
        this.filetype = filetype;
    }

    public String getCrc() {
        return crc;
    }

    public void setCrc(String crc) {
        this.crc = crc;
    }

    @Override
    public String toString() {
        return "AppInstallRequestDto{" +
                "token=" + token +
                ", serialNo='" + serialNo + '\'' +
                ", taskId=" + taskId +
                ", appName='" + appName + '\'' +
                ", filelen=" + filelen +
                ", filetype='" + filetype + '\'' +
                ", crc='" + crc + '\'' +
                '}';
    }
}

