package com.huawei.cloududn.dialingtest.controller.executormanagement.websocket.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * TaskStart-Request (0x33) 消息DTO
 * 方向: ADCA ← CloudUDN
 * 说明: 拨测任务启动请求，支持多UE（0~N个）及多种执行关系
 * 
 * @author DialTestCenter
 * @version V3
 */
public class TaskStartRequestDto {
    
    /**
     * 会话Token
     * Tag: 0x0008
     */
    private long token;
    
    /**
     * 任务ID，用于区分不同任务
     * Tag: 0x0020
     */
    private int taskId;
    
    /**
     * 脚本名称（douyin_openlive）
     * Tag: 0x0021
     */
    private String scriptName;
    
    /**
     * 版本号（1.0）
     * Tag: 0x000E
     */
    private String version;
    
    /**
     * UE序列号列表，不带表示该执行机下所有UE
     * Tag: 0x0105 (容器)
     */
    private List<String> serialNoList;
    
    /**
     * 多UE执行关系：1-顺序(Series)，2-并行(Parallel)，3-组合(Coalesce)，默认1
     * Tag: 0x002B
     */
    private Integer procType;
    
    /**
     * 脚本其它参数列表（"--pixel 1080P --camera back"）
     * Tag: 0x002A
     */
    private String parameters;
    
    public TaskStartRequestDto() {
        this.serialNoList = new ArrayList<>();
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
    
    public List<String> getSerialNoList() {
        return serialNoList;
    }
    
    public void setSerialNoList(List<String> serialNoList) {
        this.serialNoList = serialNoList;
    }
    
    public Integer getProcType() {
        return procType;
    }
    
    public void setProcType(Integer procType) {
        this.procType = procType;
    }
    
    public String getParameters() {
        return parameters;
    }
    
    public void setParameters(String parameters) {
        this.parameters = parameters;
    }
    
    @Override
    public String toString() {
        return "TaskStartRequestDto{" +
                "token=" + token +
                ", taskId=" + taskId +
                ", scriptName='" + scriptName + '\'' +
                ", version='" + version + '\'' +
                ", serialNoCount=" + (serialNoList != null ? serialNoList.size() : 0) +
                ", procType=" + procType +
                '}';
    }
}

