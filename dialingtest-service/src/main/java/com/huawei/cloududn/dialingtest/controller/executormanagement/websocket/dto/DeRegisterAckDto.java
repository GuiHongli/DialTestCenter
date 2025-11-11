package com.huawei.cloududn.dialingtest.controller.executormanagement.websocket.dto;

/**
 * DeRegister-Ack (0x06) 消息DTO
 * 方向: ADCA ← CloudUDN
 * 说明: 去注册应答
 * 
 * @author DialTestCenter
 * @version V3
 */
public class DeRegisterAckDto {
    
    /**
     * 会话Token
     * Tag: 0x0008
     */
    private long token;
    
    /**
     * 错误码：0-成功，非0-失败
     * Tag: 0x0006
     */
    private int result;
    
    public DeRegisterAckDto() {
    }
    
    public DeRegisterAckDto(long token, int result) {
        this.token = token;
        this.result = result;
    }
    
    public long getToken() {
        return token;
    }
    
    public void setToken(long token) {
        this.token = token;
    }
    
    public int getResult() {
        return result;
    }
    
    public void setResult(int result) {
        this.result = result;
    }
    
    public boolean isSuccess() {
        return result == 0;
    }
    
    @Override
    public String toString() {
        return "DeRegisterAckDto{" +
                "token=" + token +
                ", result=" + result +
                '}';
    }
}

