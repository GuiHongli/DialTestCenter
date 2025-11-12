/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2025. All rights reserved.
 */

package com.huawei.cloududn.dialingtestapp.service.executormanagement.task;

import com.huawei.cloududn.dialingtestapp.controller.executormanagement.websocket.WebSocketSessionRegistry;
import com.huawei.cloududn.dialingtestapp.controller.executormanagement.websocket.codec.DtoTlvConverter;
import com.huawei.cloududn.dialingtestapp.controller.executormanagement.websocket.dto.*;

import com.huawei.cloududn.dialingtestapp.controller.executormanagement.websocket.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Helper sender to push TLV binary messages to specific agent sessions.
 * V3版本：从sendText改为sendBinary，使用TlvEncoder编码
 *
 * @author g00940940
 * @since 2025-11-11
 */
@Component
public class WssMessageSender {

    private static final Logger logger = LoggerFactory.getLogger(WssMessageSender.class);

    @Autowired
    private WebSocketSessionRegistry sessionRegistry;

    /**
     * Send binary TLV message safely.
     * V3版本：使用二进制格式发送
     *
     * @param sessionId session id
     * @param buffer    TLV binary buffer
     */
    public void sendBinary(String sessionId, ByteBuffer buffer) {
        try {
            sessionRegistry.sendBinary(sessionId, buffer);
        } catch (IOException e) {
            logger.error("Failed to send binary message to sessionId={}", sessionId, e);
        }
    }

    /**
     * Send task start message (V3 TLV format).
     * V3版本：使用TLV格式发送任务启动消息
     *
     * @param sessionId target session id
     * @param taskDto   TaskStartRequestDto
     */
    public void sendTaskStart(String sessionId, TaskStartRequestDto taskDto) {
        try {
            ByteBuffer buffer = DtoTlvConverter.encodeTaskStart(taskDto);
            sendBinary(sessionId, buffer);
            logger.info("Sent Task-Start to sessionId={}, taskId={}", sessionId, taskDto.getTaskId());
        } catch (Exception e) {
            logger.error("Failed to send Task-Start to sessionId={}, taskId={}", sessionId, taskDto.getTaskId(), e);
        }
    }

    /**
     * Send task stop message (V3 TLV format).
     * V3版本：使用TLV格式发送任务停止消息
     *
     * @param sessionId target session id
     * @param taskDto   TaskStopRequestDto
     */
    public void sendTaskStop(String sessionId, TaskStopRequestDto taskDto) {
        try {
            ByteBuffer buffer = DtoTlvConverter.encodeTaskStop(taskDto);
            sendBinary(sessionId, buffer);
            logger.info("Sent Task-Stop to sessionId={}, taskId={}", sessionId, taskDto.getTaskId());
        } catch (Exception e) {
            logger.error("Failed to send Task-Stop to sessionId={}, taskId={}", sessionId, taskDto.getTaskId(), e);
        }
    }

    /**
     * Send script update message (V3 TLV format).
     * V3版本：使用TLV格式发送脚本更新消息
     *
     * @param sessionId target session id
     * @param updateDto ScriptUpdateNotifyDto
     */
    public void sendScriptUpdate(String sessionId, ScriptUpdateNotifyDto updateDto) {
        try {
            ByteBuffer buffer = DtoTlvConverter.encodeScriptUpdateNotify(updateDto);
            sendBinary(sessionId, buffer);
            logger.info("Sent Script-Update to sessionId={}, scriptName={}", sessionId, updateDto.getScriptName());
        } catch (Exception e) {
            logger.error("Failed to send Script-Update to sessionId={}, scriptName={}",
                sessionId, updateDto.getScriptName(), e);
        }
    }

    /**
     * Send app install message (V3 TLV format).
     * V3版本：使用TLV格式发送App安装消息
     *
     * @param sessionId target session id
     * @param installDto AppInstallRequestDto
     */
    public void sendAppInstallRequest(String sessionId, AppInstallRequestDto installDto) {
        try {
            ByteBuffer buffer = DtoTlvConverter.encodeAppInstallRequest(installDto);
            sendBinary(sessionId, buffer);
            logger.info("Sent App-Install to sessionId={}, serialNo={}", sessionId, installDto.getSerialNo());
        } catch (Exception e) {
            logger.error("Failed to send App-Install to sessionId={}, serialNo={}",
                sessionId, installDto.getSerialNo(), e);
        }
    }

    /**
     * Send app list query message (V3 TLV format).
     * V3版本：使用TLV格式发送App列表查询消息
     *
     * @param sessionId target session id
     * @param queryDto  AppListQueryDto
     */
    public void sendAppListQuery(String sessionId, AppListQueryDto queryDto) {
        try {
            ByteBuffer buffer = DtoTlvConverter.encodeAppListQuery(queryDto);
            sendBinary(sessionId, buffer);
            logger.info("Sent App-List-Query to sessionId={}, serialNo={}", sessionId, queryDto.getSerialNo());
        } catch (Exception e) {
            logger.error("Failed to send App-List-Query to sessionId={}, serialNo={}",
                sessionId, queryDto.getSerialNo(), e);
        }
    }

    /**
     * Send screencap query message (V3 TLV format).
     * V3版本：使用TLV格式发送截屏查询消息
     *
     * @param sessionId target session id
     * @param queryDto  ScreencapQueryDto
     */
    public void sendScreanCapQuery(String sessionId, ScreencapQueryDto queryDto) {
        try {
            ByteBuffer buffer = DtoTlvConverter.encodeScreencapQuery(queryDto);
            sendBinary(sessionId, buffer);
            logger.info("Sent Screencap-Query to sessionId={}, serialNo={}", sessionId, queryDto.getSerialNo());
        } catch (Exception e) {
            logger.error("Failed to send Screencap-Query to sessionId={}, serialNo={}",
                sessionId, queryDto.getSerialNo(), e);
        }
    }
}


