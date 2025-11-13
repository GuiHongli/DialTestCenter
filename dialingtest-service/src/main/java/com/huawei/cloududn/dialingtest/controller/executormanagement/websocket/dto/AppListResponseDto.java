package com.huawei.cloududn.dialingtest.controller.executormanagement.websocket.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * AppList-Response (0x22) 消息DTO
 * 方向: ADCA → CloudUDN
 * 说明: App列表信息
 * 
 * @author DialTestCenter
 * @version V3
 */
public class AppListResponseDto {
    
    /**
     * 会话Token
     * Tag: 0x0008
     */
    private long token;
    
    /**
     * 手机序列号
     * Tag: 0x000A
     */
    private String serialNo;
    
    /**
     * 错误码：0-OK，非0-异常
     * Tag: 0x0009
     */
    private int state;
    
    /**
     * App列表容器，包含0~N个app-item
     * Tag: 0x0102
     */
    private List<AppItemDto> appList;
    
    public AppListResponseDto() {
        this.appList = new ArrayList<>();
    }
    
    public AppListResponseDto(long token, String serialNo, int state, List<AppItemDto> appList) {
        this.token = token;
        this.serialNo = serialNo;
        this.state = state;
        this.appList = appList != null ? appList : new ArrayList<>();
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
    
    public List<AppItemDto> getAppList() {
        return appList;
    }
    
    public void setAppList(List<AppItemDto> appList) {
        this.appList = appList;
    }
    
    public boolean isSuccess() {
        return state == 0;
    }
    
    @Override
    public String toString() {
        return "AppListResponseDto{" +
                "token=" + token +
                ", serialNo='" + serialNo + '\'' +
                ", state=" + state +
                ", appCount=" + (appList != null ? appList.size() : 0) +
                '}';
    }
}

