package com.huawei.cloududn.dialingtestapp.controller.executormanagement.websocket.dto;

/**
 * Report-Ack (0x12) 消息DTO
 * 方向: ADCA ← CloudUDN
 * 说明: 状态报告应答
 * 
 * @author DialTestCenter
 * @version V3
 */
public class ReportAckDto {
    
    /**
     * 会话Token
     * Tag: 0x0008
     */
    private long token;
    
    /**
     * 错误码：0-OK，非0-异常
     * Tag: 0x0009
     */
    private int state;
    
    public ReportAckDto() {
    }
    
    public ReportAckDto(long token, int state) {
        this.token = token;
        this.state = state;
    }
    
    public long getToken() {
        return token;
    }
    
    public void setToken(long token) {
        this.token = token;
    }
    
    public int getState() {
        return state;
    }
    
    public void setState(int state) {
        this.state = state;
    }
    
    public boolean isSuccess() {
        return state == 0;
    }
    
    @Override
    public String toString() {
        return "ReportAckDto{" +
                "token=" + token +
                ", state=" + state +
                '}';
    }
}

