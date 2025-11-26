/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2025. All rights reserved.
 */

package com.huawei.cloududn.dialingtestapp.controller.executormanagement.websocket.dto;

/**
 * 消息类型枚举定义
 * V4版本：支持JSON类型名称映射
 * 保留V3的消息ID用于向后兼容
 *
 * @author DialTestCenter
 * @version V4
 */
public enum MessageType {
    // 注册消息 (0x01-0x06)
    REGISTER_REQUEST(0x01, "RegisterRequest", "注册请求", Direction.AGENT_TO_SERVER),
    REGISTER_CHALLENGE(0x02, "RegisterChallenge", "注册挑战", Direction.SERVER_TO_AGENT),
    REGISTER_RESPONSE(0x03, "RegisterResponse", "注册应答", Direction.AGENT_TO_SERVER),
    REGISTER_RESULT(0x04, "RegisterResult", "注册结果", Direction.SERVER_TO_AGENT),
    DEREGISTER_REQUEST(0x05, "DeRegisterRequest", "去注册请求", Direction.AGENT_TO_SERVER),
    DEREGISTER_ACK(0x06, "DeRegisterAck", "去注册应答", Direction.SERVER_TO_AGENT),

    // 状态报告消息 (0x11-0x12)
    REPORT_MSG(0x11, "ReportMsg", "状态报告", Direction.AGENT_TO_SERVER),
    REPORT_ACK(0x12, "ReportAck", "状态报告应答", Direction.SERVER_TO_AGENT),

    // UE&App管理消息 (0x21-0x26)
    APP_LIST_QUERY(0x21, "AppListQuery", "查询App清单", Direction.SERVER_TO_AGENT),
    APP_LIST_RESPONSE(0x22, "AppListResponse", "App列表信息", Direction.AGENT_TO_SERVER),
    APP_INSTALL_REQUEST(0x23, "AppInstallRequest", "App安装请求", Direction.SERVER_TO_AGENT),
    APP_INSTALL_RESPONSE(0x24, "AppInstallResponse", "App安装应答", Direction.AGENT_TO_SERVER),
    SCREENCAP_QUERY(0x25, "ScreencapQuery", "查询UE界面", Direction.SERVER_TO_AGENT),
    SCREENCAP_RESPONSE(0x26, "ScreencapResponse", "查询UE界面应答", Direction.AGENT_TO_SERVER),

    // 脚本&任务管理消息 (0x31-0x36)
    SCRIPT_UPDATE_NOTIFY(0x31, "ScriptUpdateNotify", "更新脚本通知", Direction.SERVER_TO_AGENT),
    SCRIPT_UPDATE_ACK(0x32, "ScriptUpdateAck", "更新脚本应答", Direction.AGENT_TO_SERVER),
    TASK_START_REQUEST(0x33, "TaskStartRequest", "拨测任务启动请求", Direction.SERVER_TO_AGENT),
    TASK_START_RESPONSE(0x34, "TaskStartResponse", "拨测任务启动应答", Direction.AGENT_TO_SERVER),
    TASK_STOP_REQUEST(0x35, "TaskStopRequest", "拨测任务停止请求", Direction.SERVER_TO_AGENT),
    TASK_STOP_RESPONSE(0x36, "TaskStopResponse", "拨测任务停止应答", Direction.AGENT_TO_SERVER);

    private final int id;
    private final String name;
    private final String description;
    private final Direction direction;

    MessageType(int id, String name, String description, Direction direction) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.direction = direction;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Direction getDirection() {
        return direction;
    }

    /**
     * 根据消息ID获取枚举
     *
     * @param id 消息ID
     * @return MessageType枚举
     */
    public static MessageType fromId(int id) {
        for (MessageType type : values()) {
            if (type.id == id) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown MessageType id: 0x" + Integer.toHexString(id));
    }

    /**
     * 根据JSON类型名称获取枚举（V4新增）
     *
     * @param jsonType JSON类型名称
     * @return MessageType枚举
     */
    public static MessageType fromJsonType(String jsonType) {
        if (jsonType == null) {
            throw new IllegalArgumentException("JSON type cannot be null");
        }
        for (MessageType type : values()) {
            if (type.name.equals(jsonType)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown JSON type: " + jsonType);
    }

    /**
     * 获取JSON类型名称（V4新增）
     *
     * @return JSON类型名称
     */
    public String getJsonTypeName() {
        return this.name;
    }

    /**
     * 消息方向枚举
     */
    public enum Direction {
        AGENT_TO_SERVER("ADCA → CloudUDN"),
        SERVER_TO_AGENT("ADCA ← CloudUDN");

        private final String description;

        Direction(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}

