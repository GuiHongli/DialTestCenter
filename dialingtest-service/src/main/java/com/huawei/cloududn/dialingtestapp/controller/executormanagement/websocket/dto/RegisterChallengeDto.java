package com.huawei.cloududn.dialingtestapp.controller.executormanagement.websocket.dto;

/**
 * Register-Challenge (0x02) 消息DTO
 * 方向: ADCA ← CloudUDN
 * 说明: 注册挑战消息，内容包括ChallengeID、Challenge随机内容
 * 
 * @author DialTestCenter
 * @version V3
 */
public class RegisterChallengeDto {
    
    /**
     * 挑战序号，用于定位认证上下文
     * Tag: 0x0002
     */
    private int challengeId;
    
    /**
     * 16字节随机数
     * Tag: 0x0003
     */
    private byte[] challenge;
    
    public RegisterChallengeDto() {
    }
    
    public RegisterChallengeDto(int challengeId, byte[] challenge) {
        this.challengeId = challengeId;
        this.challenge = challenge;
    }
    
    public int getChallengeId() {
        return challengeId;
    }
    
    public void setChallengeId(int challengeId) {
        this.challengeId = challengeId;
    }
    
    public byte[] getChallenge() {
        return challenge;
    }
    
    public void setChallenge(byte[] challenge) {
        this.challenge = challenge;
    }
    
    @Override
    public String toString() {
        return "RegisterChallengeDto{" +
                "challengeId=" + challengeId +
                ", challengeLength=" + (challenge != null ? challenge.length : 0) +
                '}';
    }
}

