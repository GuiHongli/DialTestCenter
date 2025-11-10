/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2025. All rights reserved.
 */

package com.huawei.cloududn.dialingtest.service.executormanagement.task;

import com.huawei.cloududn.dialingtest.controller.executormanagement.websocket.WebSocketSessionRegistry;
import com.huawei.cloududn.dialingtest.controller.executormanagement.websocket.dto.WssMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Helper sender to push messages to specific agent sessions.
 *
 * @author g00940940
 * @since 2025-11-04
 */
@Component
public class WssMessageSender {

    private static final Logger logger = LoggerFactory.getLogger(WssMessageSender.class);

    @Autowired
    private WebSocketSessionRegistry sessionRegistry;

    /**
     * Send task_assign to agent by session id.
     *
     * @param sessionId session id
     * @param payload   task payload
     */
    public void sendTaskAssign(String sessionId, Object payload) {
        try {
            sessionRegistry.sendMessage(sessionId, new WssMessage("task_assign", payload));
        } catch (IOException e) {
            logger.error("Failed to send task_assign to sessionId={}", sessionId, e);
        }
    }

    /**
     * Send any WSS message safely.
     *
     * @param sessionId session id
     * @param message   message wrapper
     */
    public void send(String sessionId, WssMessage message) {
        try {
            sessionRegistry.sendMessage(sessionId, message);
        } catch (IOException e) {
            logger.error("Failed to send message to sessionId={}", sessionId, e);
        }
    }

    /**
     * Send query_ue_screencap command.
     *
     * @param sessionId target session id
     * @param ueSerial  UE serial number
     */
    public void sendQueryUeScreencap(String sessionId, String ueSerial) {
        Map<String, Object> data = new HashMap<>();
        data.put("ue_serial", ueSerial);
        send(sessionId, new WssMessage("query_ue_screencap", data));
    }

    /**
     * Send script_update_notify command.
     *
     * @param sessionId   target session id
     * @param packageName package name
     * @param version     version
     * @param packageUrl  package url
     * @param checksum    checksum
     */
    public void sendScriptUpdateNotify(String sessionId, String packageName, String version, String packageUrl, String checksum) {
        Map<String, Object> data = new HashMap<>();
        data.put("package_name", packageName);
        data.put("version", version);
        data.put("package_url", packageUrl);
        data.put("checksum", checksum);
        send(sessionId, new WssMessage("script_update_notify", data));
    }

    /**
     * Send app_install command.
     *
     * @param sessionId target session id
     * @param payload   full payload object (map-like) per design
     */
    public void sendAppInstall(String sessionId, Object payload) {
        send(sessionId, new WssMessage("app_install", payload));
    }

    /**
     * Send task_cancel command.
     *
     * @param sessionId target session id
     * @param taskId    task id
     */
    public void sendTaskCancel(String sessionId, String taskId) {
        Map<String, Object> data = new HashMap<>();
        data.put("task_id", taskId);
        send(sessionId, new WssMessage("task_cancel", data));
    }
}


