package com.huawei.cloududn.dialingtestapp.controller.executormanagement.websocket.codec;

/**
 * TLV字段Tag枚举定义
 * 定义所有消息体内部字段的Tag值 (2字节)
 * 
 * @author DialTestCenter
 * @version V3
 */
public enum FieldTag {
    // 基础字段 (0x0001-0x0012)
    HOSTNAME(0x0001, "hostname", "执行机名称"),
    CHALLENGE_ID(0x0002, "challenge-id", "挑战序号"),
    CHALLENGE(0x0003, "challenge", "挑战随机数（16字节）"),
    USERNAME(0x0004, "username", "认证用户名"),
    RESPONSE(0x0005, "response", "CHAP响应（16字节MD5）"),
    RESULT(0x0006, "result", "结果码（0-成功，非0-失败）"),
    DESCRIPTION(0x0007, "description", "描述信息"),
    TOKEN(0x0008, "token", "会话Token（8字节）"),
    STATE(0x0009, "state", "状态信息"),
    SERIAL_NO(0x000A, "serial-no", "手机序列号"),
    BRAND(0x000B, "brand", "手机品牌"),
    MODEL(0x000C, "model", "手机型号"),
    OS(0x000D, "os", "操作系统类型"),
    VERSION(0x000E, "version", "版本号"),
    WMSIZE(0x000F, "wmsize", "屏幕分辨率"),
    IPV4(0x0010, "ipv4", "IPv4地址"),
    IPV6(0x0011, "ipv6", "IPv6地址"),
    BATTERY(0x0012, "battery", "电量百分比"),
    
    // 任务字段 (0x0020-0x002C)
    TASKID(0x0020, "taskid", "任务ID"),
    SCRIPT_NAME(0x0021, "script-name", "脚本名称"),
    PACKAGE(0x0022, "package", "软件包名/安装包内容"),
    APPNAME(0x0023, "appname", "App名称"),
    SCRIPT(0x0024, "script", "安装脚本内容"),
    CRC(0x0025, "crc", "CRC校验值"),
    FILENAME(0x0026, "filename", "文件名"),
    CONTENT(0x0027, "content", "文件内容"),
    FILELEN(0x0028, "filelen", "文件长度"),
    SCRIPTFILE(0x0029, "scriptfile", "脚本文件内容"),
    PARAMETERS(0x002A, "parameters", "参数列表"),
    PROCTYPE(0x002B, "proctype", "多UE执行关系"),
    BLOCK(0x002C, "block", "VPN阻塞结果"),
    
    // 容器字段 (0x0100-0x0105)
    UE_LIST(0x0100, "ue-list", "UE列表容器"),
    UE_ITEM(0x0101, "ue-item", "单个UE信息容器"),
    APP_LIST(0x0102, "app-list", "App列表容器"),
    APP_ITEM(0x0103, "app-item", "单个App信息容器"),
    SUB_RESULT(0x0104, "sub-result", "子结果容器"),
    SERIAL_NO_LIST(0x0105, "serial-no-list", "序列号列表容器"),
    
    // 特殊字段 - 用于兼容不同类型
    NAME(0x0023, "name", "软件名称（与appname复用Tag）"),
    FILES(0x0027, "files", "执行结果日志（与content复用Tag）");
    
    private final int value;
    private final String name;
    private final String description;
    
    FieldTag(int value, String name, String description) {
        this.value = value;
        this.name = name;
        this.description = description;
    }
    
    public int getValue() {
        return value;
    }
    
    public String getName() {
        return name;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * 根据Tag值获取枚举
     */
    public static FieldTag fromValue(int value) {
        for (FieldTag tag : values()) {
            if (tag.value == value) {
                return tag;
            }
        }
        throw new IllegalArgumentException("Unknown FieldTag value: 0x" + Integer.toHexString(value));
    }
}

