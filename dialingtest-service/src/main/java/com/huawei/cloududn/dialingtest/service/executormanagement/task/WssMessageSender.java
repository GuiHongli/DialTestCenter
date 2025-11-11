/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2025. All rights reserved.
 */

package com.huawei.cloududn.dialingtest.service.executormanagement.task;

import com.huawei.cloududn.dialingtest.controller.executormanagement.websocket.WebSocketSessionRegistry;

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
     * Send task assign message (V3 TLV format).
     * V3版本：使用TLV格式发送任务分配消息
     *
     * @param sessionId target session id
     * @param taskId    task ID
     * @param scriptName script name
     * @param version   script version
     * @param parameters parameters string
     */
    public void sendTaskAssign(String sessionId, String taskId, String scriptName, String version, String parameters) {
        // TODO: Implement TLV encoding for task assign
        logger.info("Task assign not yet implemented for V3 TLV format: sessionId={}, taskId={}", sessionId, taskId);
    }

    /**
     * Send task cancel message (V3 TLV format).
     * V3版本：使用TLV格式发送任务取消消息
     *
     * @param sessionId target session id
     * @param taskId    task ID
     */
    public void sendTaskCancel(String sessionId, String taskId) {
        // TODO: Implement TLV encoding for task cancel
        logger.info("Task cancel not yet implemented for V3 TLV format: sessionId={}, taskId={}", sessionId, taskId);
    }

    /**
     * Send script update notify message (V3 TLV format).
     * V3版本：使用TLV格式发送脚本更新通知
     *
     * @param sessionId   target session id
     * @param scriptName  script name
     * @param version     script version
     * @param fileLen     file length
     * @param scriptFile  script file content (binary)
     * @param crc         CRC checksum
     */
    public void sendScriptUpdateNotify(String sessionId, String scriptName, String version,
                                      int fileLen, byte[] scriptFile, String crc) {
        // TODO: Implement TLV encoding for script update notify
        logger.info("Script update notify not yet implemented for V3 TLV format: sessionId={}, scriptName={}", sessionId, scriptName);
    }

    /**
     * Send app install message (V3 TLV format).
     * V3版本：使用TLV格式发送App安装消息
     *
     * @param sessionId target session id
     * @param serialNo  UE serial number
     * @param taskId    task ID
     * @param appName   app name
     * @param script    install script (optional)
     * @param packageData app package data (optional)
     * @param crc       CRC checksum (optional)
     */
    public void sendAppInstall(String sessionId, String serialNo, int taskId, String appName,
                             byte[] script, byte[] packageData, String crc) {
        // TODO: Implement TLV encoding for app install
        logger.info("App install not yet implemented for V3 TLV format: sessionId={}, serialNo={}", sessionId, serialNo);
    }

    /**
     * Send UE screencap query message (V3 TLV format).
     * V3版本：使用TLV格式发送UE截屏查询消息
     *
     * @param sessionId target session id
     * @param serialNo  UE serial number
     */
    public void sendQueryUeScreencap(String sessionId, String serialNo) {
        // TODO: Implement TLV encoding for UE screencap query
        logger.info("UE screencap query not yet implemented for V3 TLV format: sessionId={}, serialNo={}", sessionId, serialNo);
    }
}


