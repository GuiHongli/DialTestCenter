package com.huawei.cloududn.dialingtestapp.controller.executormanagement.websocket.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Register-Request (0x01) 消息DTO
 * 方向: ADCA → CloudUDN
 * 说明: 注册请求消息，内容包括执行机HostName
 *
 * @author DialTestCenter
 * @version V4
 */
public class RegisterRequestDto {

    @JsonProperty("hostname")
    private String hostname;
    
    public RegisterRequestDto() {
    }
    
    public RegisterRequestDto(String hostname) {
        this.hostname = hostname;
    }
    
    public String getHostname() {
        return hostname;
    }
    
    public void setHostname(String hostname) {
        this.hostname = hostname;
    }
    
    @Override
    public String toString() {
        return "RegisterRequestDto{" +
                "hostname='" + hostname + '\'' +
                '}';
    }
}

