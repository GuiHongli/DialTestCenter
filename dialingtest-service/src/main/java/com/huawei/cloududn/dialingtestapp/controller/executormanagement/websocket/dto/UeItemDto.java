package com.huawei.cloududn.dialingtestapp.controller.executormanagement.websocket.dto;

/**
 * UE信息子对象
 * 用于Report-Msg消息的ue-list容器中
 * 
 * @author DialTestCenter
 * @version V3
 */
public class UeItemDto {
    
    /**
     * 手机序列号
     * Tag: 0x000A
     */
    private String serialNo;
    
    /**
     * 手机品牌（huawei/xiaomi/apple...）
     * Tag: 0x000B
     */
    private String brand;
    
    /**
     * 手机型号（NOH-AN01/Xiaomi-12...）
     * Tag: 0x000C
     */
    private String model;
    
    /**
     * 操作系统（Android/iOS/HarmonyOS）
     * Tag: 0x000D
     */
    private String os;
    
    /**
     * 操作系统版本（12.1.3）
     * Tag: 0x000E
     */
    private String version;
    
    /**
     * 屏幕分辨率（2772 x 1344）
     * Tag: 0x000F
     */
    private String wmsize;
    
    /**
     * IPv4地址
     * Tag: 0x0010
     */
    private String ipv4;
    
    /**
     * IPv6地址
     * Tag: 0x0011
     */
    private String ipv6;
    
    /**
     * 电量百分比（0-100）
     * Tag: 0x0012
     */
    private Integer battery;
    
    public UeItemDto() {
    }
    
    public String getSerialNo() {
        return serialNo;
    }
    
    public void setSerialNo(String serialNo) {
        this.serialNo = serialNo;
    }
    
    public String getBrand() {
        return brand;
    }
    
    public void setBrand(String brand) {
        this.brand = brand;
    }
    
    public String getModel() {
        return model;
    }
    
    public void setModel(String model) {
        this.model = model;
    }
    
    public String getOs() {
        return os;
    }
    
    public void setOs(String os) {
        this.os = os;
    }
    
    public String getVersion() {
        return version;
    }
    
    public void setVersion(String version) {
        this.version = version;
    }
    
    public String getWmsize() {
        return wmsize;
    }
    
    public void setWmsize(String wmsize) {
        this.wmsize = wmsize;
    }
    
    public String getIpv4() {
        return ipv4;
    }
    
    public void setIpv4(String ipv4) {
        this.ipv4 = ipv4;
    }
    
    public String getIpv6() {
        return ipv6;
    }
    
    public void setIpv6(String ipv6) {
        this.ipv6 = ipv6;
    }
    
    public Integer getBattery() {
        return battery;
    }
    
    public void setBattery(Integer battery) {
        this.battery = battery;
    }
    
    @Override
    public String toString() {
        return "UeItemDto{" +
                "serialNo='" + serialNo + '\'' +
                ", brand='" + brand + '\'' +
                ", model='" + model + '\'' +
                ", os='" + os + '\'' +
                ", version='" + version + '\'' +
                ", battery=" + battery +
                '}';
    }
}

