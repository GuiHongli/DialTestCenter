package com.huawei.cloududn.dialingtest.controller.executormanagement.websocket.dto;

/**
 * TaskStop-Response (0x36) 消息DTO
 * 方向: ADCA → CloudUDN
 * 说明: 拨测任务停止应答
 * 
 * @author DialTestCenter
 * @version V3
 */
public class TaskStopResponseDto {
    
    /**
     * 会话Token
     * Tag: 0x0008
     */
    private long token;
    
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
    
    /**
     * 描述信息（可选）
     * Tag: 0x0007
     */
    private String description;
    
    public TaskStopResponseDto() {
    }
    
    public TaskStopResponseDto(long token, int taskId, int state) {
        this.token = token;
        this.taskId = taskId;
        this.state = state;
    }
    
    public TaskStopResponseDto(long token, int taskId, int state, String description) {
        this.token = token;
        this.taskId = taskId;
        this.state = state;
        this.description = description;
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
    
    public int getState() {
        return state;
    }
    
    public void setState(int state) {
        this.state = state;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public boolean isSuccess() {
        return state == 0;
    }
    
    @Override
    public String toString() {
        return "TaskStopResponseDto{" +
                "token=" + token +
                ", taskId=" + taskId +
                ", state=" + state +
                ", description='" + description + '\'' +
                '}';
    }
}

