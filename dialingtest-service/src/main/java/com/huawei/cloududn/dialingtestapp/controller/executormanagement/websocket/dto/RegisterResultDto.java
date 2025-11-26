package com.huawei.cloududn.dialingtestapp.controller.executormanagement.websocket.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Register-Result (0x04) 消息DTO
 * 方向: ADCA ← CloudUDN
 * 说明: 注册结果消息，内容包括注册结果(成功/失败)，注册描述，会话Token等
 *
 * @author DialTestCenter
 * @version V4
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RegisterResultDto {

    @JsonProperty("result")
    private int result;

    @JsonProperty("description")
    private String description;

    @JsonProperty("token")
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
    
    /**
     * Add 'success' field for test compatibility
     *
     * @return true if result is 0, false otherwise
     */
    @JsonProperty("success")
    public boolean getSuccess() {
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
