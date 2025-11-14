/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2025. All rights reserved.
 */

package com.huawei.cloududn.dialingtestapp.service.executormanagement.task;

import com.huawei.cloududn.dialingtestapp.controller.executormanagement.websocket.WebSocketSessionRegistry;
import com.huawei.cloududn.dialingtestapp.controller.executormanagement.websocket.dto.AppInstallRequestDto;
import com.huawei.cloududn.dialingtestapp.controller.executormanagement.websocket.dto.AppListQueryDto;
import com.huawei.cloududn.dialingtestapp.controller.executormanagement.websocket.dto.ScreencapQueryDto;
import com.huawei.cloududn.dialingtestapp.controller.executormanagement.websocket.dto.TaskStartRequestDto;
import com.huawei.cloududn.dialingtestapp.controller.executormanagement.websocket.dto.TaskStopRequestDto;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;

/**
 * V3版本的消息发送器（已废弃）
 * 使用TLV二进制格式发送消息
 *
 * @deprecated V4版本请使用 {@link com.huawei.cloududn.dialingtestapp.controller.executormanagement.websocket.flow.WssMessageSender}
 * @author g00940940
 * @since 2025-11-11
 */
@Deprecated
@Component
public class WssMessageSenderV3 {

    private static final Logger logger = LoggerFactory.getLogger(WssMessageSenderV3.class);

    @Autowired
    private WebSocketSessionRegistry sessionRegistry;

    /**
     * Send binary message safely.
     * V3版本：使用二进制格式发送（已废弃）
     *
     * @param sessionId session id
     * @param buffer    binary buffer
     * @deprecated V4版本请使用JSON格式
     */
    @Deprecated
    public void sendBinary(String sessionId, ByteBuffer buffer) {
        logger.warn("Using deprecated V3 sendBinary method. Please migrate to V4 JSON format.");
        sessionRegistry.sendBinary(sessionId, buffer);
    }

    /**
     * Send task start message (V3 TLV format - 已废弃).
     *
     * @param sessionId target session id
     * @param taskDto   TaskStartRequestDto
     * @deprecated V4版本请使用 flow.WssMessageSender
     */
    @Deprecated
    public void sendTaskStart(String sessionId, TaskStartRequestDto taskDto) {
        logger.error("V3 TLV methods are no longer supported. Please use V4 JSON format via flow.WssMessageSender");
        throw new UnsupportedOperationException("V3 TLV format is no longer supported. Please use V4 JSON format.");
    }

    /**
     * Send task stop message (V3 TLV format - 已废弃).
     *
     * @param sessionId target session id
     * @param taskDto   TaskStopRequestDto
     * @deprecated V4版本请使用 flow.WssMessageSender
     */
    @Deprecated
    public void sendTaskStop(String sessionId, TaskStopRequestDto taskDto) {
        logger.error("V3 TLV methods are no longer supported. Please use V4 JSON format via flow.WssMessageSender");
        throw new UnsupportedOperationException("V3 TLV format is no longer supported. Please use V4 JSON format.");
    }

    /**
     * Send app install message (V3 TLV format - 已废弃).
     *
     * @param sessionId  target session id
     * @param installDto AppInstallRequestDto
     * @deprecated V4版本请使用 flow.WssMessageSender
     */
    @Deprecated
    public void sendAppInstallRequest(String sessionId, AppInstallRequestDto installDto) {
        logger.error("V3 TLV methods are no longer supported. Please use V4 JSON format via flow.WssMessageSender");
        throw new UnsupportedOperationException("V3 TLV format is no longer supported. Please use V4 JSON format.");
    }

    /**
     * Send app list query message (V3 TLV format - 已废弃).
     *
     * @param sessionId target session id
     * @param queryDto  AppListQueryDto
     * @deprecated V4版本请使用 flow.WssMessageSender
     */
    @Deprecated
    public void sendAppListQuery(String sessionId, AppListQueryDto queryDto) {
        logger.error("V3 TLV methods are no longer supported. Please use V4 JSON format via flow.WssMessageSender");
        throw new UnsupportedOperationException("V3 TLV format is no longer supported. Please use V4 JSON format.");
    }

    /**
     * Send screencap query message (V3 TLV format - 已废弃).
     *
     * @param sessionId target session id
     * @param queryDto  ScreencapQueryDto
     * @deprecated V4版本请使用 flow.WssMessageSender
     */
    @Deprecated
    public void sendScreanCapQuery(String sessionId, ScreencapQueryDto queryDto) {
        logger.error("V3 TLV methods are no longer supported. Please use V4 JSON format via flow.WssMessageSender");
        throw new UnsupportedOperationException("V3 TLV format is no longer supported. Please use V4 JSON format.");
    }
}

