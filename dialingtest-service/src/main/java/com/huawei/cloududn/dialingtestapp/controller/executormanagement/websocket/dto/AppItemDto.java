package com.huawei.cloududn.dialingtestapp.controller.executormanagement.websocket.dto;

/**
 * App信息子对象
 * 用于AppList-Response消息的app-list容器中
 * 
 * @author DialTestCenter
 * @version V3
 */
public class AppItemDto {
    
    /**
     * 软件包名（com.tencent.mm）
     * Tag: 0x0022
     */
    private String packageName;
    
    /**
     * 软件名称（WeChat），可选
     * Tag: 0x0023
     */
    private String name;
    
    /**
     * 软件版本号（8.0.62）
     * Tag: 0x000E
     */
    private String version;
    
    public AppItemDto() {
    }
    
    public AppItemDto(String packageName, String name, String version) {
        this.packageName = packageName;
        this.name = name;
        this.version = version;
    }
    
    public String getPackageName() {
        return packageName;
    }
    
    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getVersion() {
        return version;
    }
    
    public void setVersion(String version) {
        this.version = version;
    }
    
    @Override
    public String toString() {
        return "AppItemDto{" +
                "packageName='" + packageName + '\'' +
                ", name='" + name + '\'' +
                ", version='" + version + '\'' +
                '}';
    }
}

