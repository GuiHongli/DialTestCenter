package com.huawei.cloududn.dialingtest.controller.executormanagement.websocket.dto;

/**
 * AppInstall-Request (0x23) 消息DTO
 * 方向: ADCA ← CloudUDN
 * 说明: App安装请求，支持两种场景：
 * 1. 场景1-直接安装：下发package字段（APK文件内容），Agent通过adb命令安装
 * 2. 场景2-脚本安装：下发script字段（airtest脚本），Agent执行脚本从应用市场安装
 * 
 * @author DialTestCenter
 * @version V3
 */
public class AppInstallRequestDto {
    
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
     * App名称
     * Tag: 0x0023
     */
    private String appName;
    
    /**
     * Airtest安装脚本内容（场景2使用）
     * Tag: 0x0024
     */
    private byte[] script;
    
    /**
     * APK安装包文件内容（场景1使用）
     * Tag: 0x0022
     */
    private byte[] packageFile;
    
    /**
     * CRC校验值
     * Tag: 0x0025
     */
    private byte[] crc;
    
    public AppInstallRequestDto() {
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
    
    public String getAppName() {
        return appName;
    }
    
    public void setAppName(String appName) {
        this.appName = appName;
    }
    
    public byte[] getScript() {
        return script;
    }
    
    public void setScript(byte[] script) {
        this.script = script;
    }
    
    public byte[] getPackageFile() {
        return packageFile;
    }
    
    public void setPackageFile(byte[] packageFile) {
        this.packageFile = packageFile;
    }
    
    public byte[] getCrc() {
        return crc;
    }
    
    public void setCrc(byte[] crc) {
        this.crc = crc;
    }
    
    @Override
    public String toString() {
        return "AppInstallRequestDto{" +
                "token=" + token +
                ", serialNo='" + serialNo + '\'' +
                ", taskId=" + taskId +
                ", appName='" + appName + '\'' +
                ", hasScript=" + (script != null && script.length > 0) +
                ", hasPackage=" + (packageFile != null && packageFile.length > 0) +
                '}';
    }
}

