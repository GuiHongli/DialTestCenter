package com.huawei.cloududn.dialingtest.controller.executormanagement.websocket.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * TaskStart-Response (0x34) 消息DTO
 * 方向: ADCA → CloudUDN
 * 说明: 拨测任务启动应答，上报拨测结果和日志信息
 * 
 * @author DialTestCenter
 * @version V3
 */
public class TaskStartResponseDto {
    
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
     * 脚本执行结果：Success-成功，Fail-执行异常
     * Tag: 0x0006
     */
    private String result;
    
    /**
     * VPN阻塞结果（可选）
     * Tag: 0x002C
     */
    private String block;
    
    /**
     * 描述信息，异常时可用于定位
     * Tag: 0x0007
     */
    private String description;
    
    /**
     * 各UE执行结果容器，包含0~N个sub-result-item
     * Tag: 0x0104
     */
    private List<SubResultItemDto> subResult;
    
    /**
     * 日志文件总长度
     * Tag: 0x0028
     */
    private int fileLen;
    
    /**
     * 执行结果日志等标注信息（多UE统一打包）
     * Tag: 0x0027
     */
    private byte[] files;
    
    /**
     * CRC校验值
     * Tag: 0x0025
     */
    private byte[] crc;
    
    public TaskStartResponseDto() {
        this.subResult = new ArrayList<>();
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
    
    public String getResult() {
        return result;
    }
    
    public void setResult(String result) {
        this.result = result;
    }
    
    public String getBlock() {
        return block;
    }
    
    public void setBlock(String block) {
        this.block = block;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public List<SubResultItemDto> getSubResult() {
        return subResult;
    }
    
    public void setSubResult(List<SubResultItemDto> subResult) {
        this.subResult = subResult;
    }
    
    public int getFileLen() {
        return fileLen;
    }
    
    public void setFileLen(int fileLen) {
        this.fileLen = fileLen;
    }
    
    public byte[] getFiles() {
        return files;
    }
    
    public void setFiles(byte[] files) {
        this.files = files;
    }
    
    public byte[] getCrc() {
        return crc;
    }
    
    public void setCrc(byte[] crc) {
        this.crc = crc;
    }
    
    @Override
    public String toString() {
        return "TaskStartResponseDto{" +
                "token=" + token +
                ", taskId=" + taskId +
                ", result='" + result + '\'' +
                ", subResultCount=" + (subResult != null ? subResult.size() : 0) +
                ", fileLen=" + fileLen +
                '}';
    }
}

