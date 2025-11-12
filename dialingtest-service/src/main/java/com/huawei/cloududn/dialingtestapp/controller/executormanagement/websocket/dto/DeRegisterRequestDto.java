package com.huawei.cloududn.dialingtestapp.controller.executormanagement.websocket.dto;

/**
 * DeRegister-Request (0x05) 消息DTO
 * 方向: ADCA → CloudUDN
 * 说明: 去注册请求，内容包括执行机HostName
 * 
 * @author DialTestCenter
 * @version V3
 */
public class DeRegisterRequestDto {
    
    /**
     * 会话Token
     * Tag: 0x0008
     */
    private long token;
    
    /**
     * 执行机名称
     * Tag: 0x0001
     */
    private String hostname;
    
    public DeRegisterRequestDto() {
    }
    
    public DeRegisterRequestDto(long token, String hostname) {
        this.token = token;
        this.hostname = hostname;
    }
    
    public long getToken() {
        return token;
    }
    
    public void setToken(long token) {
        this.token = token;
    }
    
    public String getHostname() {
        return hostname;
    }
    
    public void setHostname(String hostname) {
        this.hostname = hostname;
    }
    
    @Override
    public String toString() {
        return "DeRegisterRequestDto{" +
                "token=" + token +
                ", hostname='" + hostname + '\'' +
                '}';
    }
}

