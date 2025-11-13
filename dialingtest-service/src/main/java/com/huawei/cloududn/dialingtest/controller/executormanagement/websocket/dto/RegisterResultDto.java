package com.huawei.cloududn.dialingtest.controller.executormanagement.websocket.dto;

/**
 * Register-Result (0x04) 消息DTO
 * 方向: ADCA ← CloudUDN
 * 说明: 注册结果消息，内容包括注册结果(成功/失败)，注册描述，会话Token等
 * 
 * @author DialTestCenter
 * @version V3
 */
public class RegisterResultDto {
    
    /**
     * 错误码：0-成功，非0-失败
     * Tag: 0x0006
     */
    private int result;
    
    /**
     * 描述内容，失败原因等
     * Tag: 0x0007
     */
    private String description;
    
    /**
     * 8字节会话ID，成功时必填
     * Tag: 0x0008
     */
    private Long token;
    
    public RegisterResultDto() {
    }
    
    public RegisterResultDto(int result, String description) {
        this.result = result;
        this.description = description;
    }
    
    public RegisterResultDto(int result, String description, Long token) {
        this.result = result;
        this.description = description;
        this.token = token;
    }
    
    public int getResult() {
        return result;
    }
    
    public void setResult(int result) {
        this.result = result;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Long getToken() {
        return token;
    }
    
    public void setToken(Long token) {
        this.token = token;
    }
    
    public boolean isSuccess() {
        return result == 0;
    }
    
    @Override
    public String toString() {
        return "RegisterResultDto{" +
                "result=" + result +
                ", description='" + description + '\'' +
                ", token=" + token +
                '}';
    }
}

