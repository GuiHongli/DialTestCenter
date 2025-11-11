package com.huawei.cloududn.dialingtest.controller.executormanagement.websocket.dto;

/**
 * TaskStop-Request (0x35) 消息DTO
 * 方向: ADCA ← CloudUDN
 * 说明: 拨测任务停止请求
 * 
 * @author DialTestCenter
 * @version V3
 */
public class TaskStopRequestDto {
    
    /**
     * 会话Token
     * Tag: 0x0008
     */
    private long token;
    
    /**
     * 任务ID，和启动任务的TaskId需相同
     * Tag: 0x0020
     */
    private int taskId;
    
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
    
    public TaskStopRequestDto() {
    }
    
    public TaskStopRequestDto(long token, int taskId, String scriptName, String version) {
        this.token = token;
        this.taskId = taskId;
        this.scriptName = scriptName;
        this.version = version;
    }
    
    public long getToken() {
        return token;
    }
    
    public void setToken(long token) {
        this.token = token;
    }
    
    public int getTaskId() {
        return taskId;
    }
    
    public void setTaskId(int taskId) {
        this.taskId = taskId;
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
    
    @Override
    public String toString() {
        return "TaskStopRequestDto{" +
                "token=" + token +
                ", taskId=" + taskId +
                ", scriptName='" + scriptName + '\'' +
                ", version='" + version + '\'' +
                '}';
    }
}

