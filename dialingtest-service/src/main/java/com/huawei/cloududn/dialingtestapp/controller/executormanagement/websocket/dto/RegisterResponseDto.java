package com.huawei.cloududn.dialingtestapp.controller.executormanagement.websocket.dto;

/**
 * Register-Response (0x03) 消息DTO
 * 方向: ADCA → CloudUDN
 * 说明: 注册应答消息，内容包括用户名、ChallengeID，经Challenge进行MD5加密后的密码
 * 
 * @author DialTestCenter
 * @version V3
 */
public class RegisterResponseDto {
    
    /**
     * 复制Register-Challenge中的值
     * Tag: 0x0002
     */
    private int challengeId;
    
    /**
     * 认证用户名
     * Tag: 0x0004
     */
    private String username;
    
    /**
     * 16字节MD5(NTLM-Hash + Challenge)
     * Tag: 0x0005
     */
    private byte[] response;
    
    public RegisterResponseDto() {
    }
    
    public RegisterResponseDto(int challengeId, String username, byte[] response) {
        this.challengeId = challengeId;
        this.username = username;
        this.response = response;
    }
    
    public int getChallengeId() {
        return challengeId;
    }
    
    public void setChallengeId(int challengeId) {
        this.challengeId = challengeId;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public byte[] getResponse() {
        return response;
    }
    
    public void setResponse(byte[] response) {
        this.response = response;
    }
    
    @Override
    public String toString() {
        return "RegisterResponseDto{" +
                "challengeId=" + challengeId +
                ", username='" + username + '\'' +
                ", responseLength=" + (response != null ? response.length : 0) +
                '}';
    }
}

