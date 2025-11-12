package com.huawei.cloududn.dialingtestapp.controller.executormanagement.websocket.dto;

/**
 * ScreanCap-Response (0x26) 消息DTO
 * 方向: ADCA → CloudUDN
 * 说明: 查询UE界面应答
 * 
 * @author DialTestCenter
 * @version V3
 */
public class ScreencapResponseDto {
    
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
     * 错误码：0-OK，非0-异常
     * Tag: 0x0009
     */
    private int state;
    
    /**
     * 文件名（screencap_202509111200531.png）
     * Tag: 0x0026
     */
    private String filename;
    
    /**
     * 文件内容（PNG格式二进制数据）
     * Tag: 0x0027
     */
    private byte[] content;
    
    public ScreencapResponseDto() {
    }
    
    public ScreencapResponseDto(long token, String serialNo, int state, String filename, byte[] content) {
        this.token = token;
        this.serialNo = serialNo;
        this.state = state;
        this.filename = filename;
        this.content = content;
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
    
    public int getState() {
        return state;
    }
    
    public void setState(int state) {
        this.state = state;
    }
    
    public String getFilename() {
        return filename;
    }
    
    public void setFilename(String filename) {
        this.filename = filename;
    }
    
    public byte[] getContent() {
        return content;
    }
    
    public void setContent(byte[] content) {
        this.content = content;
    }
    
    public boolean isSuccess() {
        return state == 0;
    }
    
    @Override
    public String toString() {
        return "ScreencapResponseDto{" +
                "token=" + token +
                ", serialNo='" + serialNo + '\'' +
                ", state=" + state +
                ", filename='" + filename + '\'' +
                ", contentLength=" + (content != null ? content.length : 0) +
                '}';
    }
}

