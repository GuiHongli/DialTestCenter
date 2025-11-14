package com.huawei.cloududn.dialingtestapp.controller.executormanagement.websocket.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Register-Response (0x03) 消息DTO
 * 方向: ADCA → CloudUDN
 * 说明: 注册应答消息，内容包括用户名、ChallengeID，经Challenge进行MD5加密后的密码
 * V4版本：response字段使用Hex编码的字符串
 *
 * @author DialTestCenter
 * @version V4
 */
public class RegisterResponseDto {

    @JsonProperty("challenge-id")
    private int challengeId;

    @JsonProperty("username")
    private String username;

    @JsonProperty("response")
    private String response;

    public RegisterResponseDto() {
    }

    public RegisterResponseDto(int challengeId, String username, String response) {
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

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    @Override
    public String toString() {
        return "RegisterResponseDto{" +
                "challengeId=" + challengeId +
                ", username='" + username + '\'' +
                ", response='" + response + '\'' +
                '}';
    }
}
