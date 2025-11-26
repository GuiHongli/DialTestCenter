package com.huawei.cloududn.dialingtestapp.controller.executormanagement.websocket.dto;

/**
 * AppInstall-Response (0x24) 消息DTO
 * 方向: ADCA → CloudUDN
 * 说明: App安装应答
 * 
 * @author DialTestCenter
 * @version V3
 */
public class AppInstallResponseDto {
    
    /**
     * 会话Token
     * Tag: 0x0008
     */
    private long token;
    
    /**
     * 手机序列号
     * Tag: 0x000A
     */
    private String serialNo;
    
    /**
     * 任务ID
     * Tag: 0x0020
     */
    private int taskId;
    
    /**
     * 错误码：0-OK，非0-异常
     * Tag: 0x0009
     */
    private int state;
    
    public AppInstallResponseDto() {
    }
    
    public AppInstallResponseDto(long token, String serialNo, int taskId, int state) {
        this.token = token;
        this.serialNo = serialNo;
        this.taskId = taskId;
        this.state = state;
    }
    
    public long getToken() {
        return token;
    }
    
    public void setToken(long token) {
        this.token = token;
    }
    
    public String getSerialNo() {
        return serialNo;
    }
    
    public void setSerialNo(String serialNo) {
        this.serialNo = serialNo;
    }
    
    public int getTaskId() {
        return taskId;
    }
    
    public void setTaskId(int taskId) {
        this.taskId = taskId;
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
        return "AppInstallResponseDto{" +
                "token=" + token +
                ", serialNo='" + serialNo + '\'' +
                ", taskId=" + taskId +
                ", state=" + state +
                '}';
    }
}

