package com.huawei.cloududn.dialingtestapp.controller.executormanagement.websocket.dto;

/**
 * ScriptUpdate-Notify (0x31) 消息DTO
 * 方向: ADCA ← CloudUDN
 * 说明: 更新脚本通知，服务端下发拨测脚本更新，直接传输脚本文件内容（zip压缩格式）
 * 
 * @author DialTestCenter
 * @version V3
 */
public class ScriptUpdateNotifyDto {
    
    /**
     * 会话Token
     * Tag: 0x0008
     */
    private long token;
    
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
     * 文件长度（字节数）
     * Tag: 0x0028
     */
    private int fileLen;
    
    /**
     * 脚本文件内容（zip压缩）
     * Tag: 0x0029
     */
    private byte[] scriptFile;
    
    /**
     * CRC校验值
     * Tag: 0x0025
     */
    private byte[] crc;
    
    public ScriptUpdateNotifyDto() {
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
    
    public int getFileLen() {
        return fileLen;
    }
    
    public void setFileLen(int fileLen) {
        this.fileLen = fileLen;
    }
    
    public byte[] getScriptFile() {
        return scriptFile;
    }
    
    public void setScriptFile(byte[] scriptFile) {
        this.scriptFile = scriptFile;
    }
    
    public byte[] getCrc() {
        return crc;
    }
    
    public void setCrc(byte[] crc) {
        this.crc = crc;
    }
    
    @Override
    public String toString() {
        return "ScriptUpdateNotifyDto{" +
                "token=" + token +
                ", scriptName='" + scriptName + '\'' +
                ", version='" + version + '\'' +
                ", fileLen=" + fileLen +
                '}';
    }
}

