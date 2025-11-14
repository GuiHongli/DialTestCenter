package com.huawei.cloududn.dialingtestapp.controller.executormanagement.websocket.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * ScreanCap-Response (0x26) 消息DTO
 * 方向: ADCA → CloudUDN
 * 说明: 查询UE界面应答
 * V4版本：移除content字段，文件内容通过后续的Binary分片传输
 *
 * @author DialTestCenter
 * @version V4
 */
public class ScreencapResponseDto {

    @JsonProperty("token")
    private long token;

    @JsonProperty("serial-no")
    private String serialNo;

    @JsonProperty("state")
    private int state;

    @JsonProperty("filename")
    private String filename;

    @JsonProperty("filelen")
    private Integer filelen;

    @JsonProperty("crc")
    private String crc;

    public ScreencapResponseDto() {
    }

    public ScreencapResponseDto(long token, String serialNo, int state, String filename, Integer filelen, String crc) {
        this.token = token;
        this.serialNo = serialNo;
        this.state = state;
        this.filename = filename;
        this.filelen = filelen;
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

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public Integer getFilelen() {
        return filelen;
    }

    public void setFilelen(Integer filelen) {
        this.filelen = filelen;
    }

    public String getCrc() {
        return crc;
    }

    public void setCrc(String crc) {
        this.crc = crc;
    }

    public boolean isSuccess() {
        return state == 0;
    }

    @Override
    public String toString() {
        return "ScreencapResponseDto{" +
                "token=" + token +
                ", serialNo='" + serialNo + '\'' +
                ", state=" + state +
                ", filename='" + filename + '\'' +
                ", filelen=" + filelen +
                ", crc='" + crc + '\'' +
                '}';
    }
}
