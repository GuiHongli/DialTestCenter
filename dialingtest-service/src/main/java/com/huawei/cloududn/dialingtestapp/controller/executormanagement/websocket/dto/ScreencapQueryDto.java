package com.huawei.cloududn.dialingtestapp.controller.executormanagement.websocket.dto;

/**
 * ScreanCap-Query (0x25) 消息DTO
 * 方向: ADCA ← CloudUDN
 * 说明: 查询UE界面请求
 * 
 * @author DialTestCenter
 * @version V3
 */
public class ScreencapQueryDto {
    
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
    
    public ScreencapQueryDto() {
    }
    
    public ScreencapQueryDto(long token, String serialNo) {
        this.token = token;
        this.serialNo = serialNo;
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
    
    @Override
    public String toString() {
        return "ScreencapQueryDto{" +
                "token=" + token +
                ", serialNo='" + serialNo + '\'' +
                '}';
    }
}

