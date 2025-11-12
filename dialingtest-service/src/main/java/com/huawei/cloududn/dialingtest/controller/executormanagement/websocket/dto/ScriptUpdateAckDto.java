package com.huawei.cloududn.dialingtest.controller.executormanagement.websocket.dto;

/**
 * ScriptUpdate-Ack (0x32) 消息DTO
 * 方向: ADCA → CloudUDN
 * 说明: 更新脚本应答
 * 
 * @author DialTestCenter
 * @version V3
 */
public class ScriptUpdateAckDto {
    
    /**
     * 会话Token
     * Tag: 0x0008
     */
    private long token;
    
    /**
     * 脚本名称
     * Tag: 0x0021
     */
    private String scriptName;
    
    /**
     * 版本号
     * Tag: 0x000E
     */
    private String version;
    
    /**
     * 错误码：0-OK，非0-异常
     * Tag: 0x0009
     */
    private int state;
    
    public ScriptUpdateAckDto() {
    }
    
    public ScriptUpdateAckDto(long token, String scriptName, String version, int state) {
        this.token = token;
        this.scriptName = scriptName;
        this.version = version;
        this.state = state;
    }
    
    public long getToken() {
        return token;
    }
    
    public void setToken(long token) {
        this.token = token;
    }
    
    public String getScriptName() {
        return scriptName;
    }
    
    public void setScriptName(String scriptName) {
        this.scriptName = scriptName;
    }
    
    public String getVersion() {
        return version;
    }
    
    public void setVersion(String version) {
        this.version = version;
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
        return "ScriptUpdateAckDto{" +
                "token=" + token +
                ", scriptName='" + scriptName + '\'' +
                ", version='" + version + '\'' +
                ", state=" + state +
                '}';
    }
}

