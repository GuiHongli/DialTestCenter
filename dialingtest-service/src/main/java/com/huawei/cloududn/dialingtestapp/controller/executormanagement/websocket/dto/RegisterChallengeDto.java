package com.huawei.cloududn.dialingtestapp.controller.executormanagement.websocket.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Register-Challenge (0x02) 消息DTO
 * 方向: ADCA ← CloudUDN
 * 说明: 注册挑战消息，内容包括ChallengeID、Challenge随机内容
 * V4版本：challenge字段使用Base64编码的字符串
 *
 * @author DialTestCenter
 * @version V4
 */
public class RegisterChallengeDto {

    @JsonProperty("challenge-id")
    private int challengeId;

    @JsonProperty("challenge")
    private String challenge;
    
    public RegisterChallengeDto() {
    }
    
    public RegisterChallengeDto(int challengeId, String challenge) {
        this.challengeId = challengeId;
        this.challenge = challenge;
    }

    public int getChallengeId() {
        return challengeId;
    }

    public void setChallengeId(int challengeId) {
        this.challengeId = challengeId;
    }

    public String getChallenge() {
        return challenge;
    }

    public void setChallenge(String challenge) {
        this.challenge = challenge;
    }

    @Override
    public String toString() {
        return "RegisterChallengeDto{" +
                "challengeId=" + challengeId +
                ", challenge='" + challenge + '\'' +
                '}';
    }
}

