package com.huawei.cloududn.dialingtestapp.controller.executormanagement.websocket.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * Report-Msg (0x11) 消息DTO
 * 方向: ADCA → CloudUDN
 * 说明: 周期性（30秒）心跳/状态上报消息，上报执行机状态和连接的所有UE详细信息
 *
 * @author DialTestCenter
 * @version V4
 */
public class ReportMsgDto {

    @JsonProperty("token")
    private long token;

    @JsonProperty("state")
    private String state;

    @JsonProperty("ue-list")
    private List<UeItemDto> ueList;

    public ReportMsgDto() {
        this.ueList = new ArrayList<>();
    }

    public ReportMsgDto(long token, String state, List<UeItemDto> ueList) {
        this.token = token;
        this.state = state;
        this.ueList = ueList != null ? ueList : new ArrayList<>();
    }

    public long getToken() {
        return token;
    }

    public void setToken(long token) {
        this.token = token;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public List<UeItemDto> getUeList() {
        return ueList;
    }

    public void setUeList(List<UeItemDto> ueList) {
        this.ueList = ueList;
    }

    @Override
    public String toString() {
        return "ReportMsgDto{" +
                "token=" + token +
                ", state='" + state + '\'' +
                ", ueCount=" + (ueList != null ? ueList.size() : 0) +
                '}';
    }
}
