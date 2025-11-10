/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2025. All rights reserved.
 */

package com.huawei.cloududn.dialingtest.controller.executormanagement.websocket;

import com.fasterxml.jackson.databind.JsonNode;
import com.huawei.cloududn.dialingtest.service.executormanagement.auth.AuthSessionService;
import com.huawei.cloududn.dialingtest.service.executormanagement.ExecutorMgmtService;
import com.huawei.cloududn.dialingtest.service.executormanagement.task.TaskInterfaceService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.websocket.Session;

/**
 * Dispatch inbound WSS messages by message_type.
 *
 * <p>Routes to Auth, Executor and Task services.</p>
 *
 * @author g00940940
 * @since 2025-11-04
 */
@Component
public class WssMessageDispatcher {

    private static final Logger logger = LoggerFactory.getLogger(WssMessageDispatcher.class);

    @Autowired
    private AuthSessionService authSessionService;

    @Autowired
    private ExecutorMgmtService executorMgmtService;

    @Autowired
    private TaskInterfaceService taskInterfaceService;

    /**
     * Dispatch message by type.
     *
     * @param messageType type field
     * @param data        JSON data
     * @param session     ws session
     */
    public void dispatch(String messageType, JsonNode data, Session session) {
        if (messageType == null) {
            logger.warn("Missing message_type, sessionId={}", session.getId());
            return;
        }
        if ("register_request".equals(messageType)) {
            authSessionService.handleRegisterRequest(data, session);
        } else if ("register_auth".equals(messageType)) {
            authSessionService.handleRegisterAuth(data, session);
        } else if ("deregister".equals(messageType)) {
            executorMgmtService.handleDeregister(data, session);
        } else if ("heartbeat_status".equals(messageType)) {
            executorMgmtService.handleHeartbeatStatus(data, session);
        } else if ("executor_info_response".equals(messageType)) {
            executorMgmtService.handleExecutorInfoResponse(data, session);
        } else if ("task_status_update".equals(messageType)) {
            taskInterfaceService.handleTaskStatusUpdate(data, session);
        } else if ("ue_screencap_response".equals(messageType)) {
            taskInterfaceService.handleUeScreencapResponse(data, session);
        } else if ("app_install_result".equals(messageType)) {
            taskInterfaceService.handleAppInstallResult(data, session);
        } else if ("script_update_ack".equals(messageType)) {
            taskInterfaceService.handleScriptUpdateAck(data, session);
        } else {
            logger.warn("Unknown message_type={}, sessionId={}", messageType, session.getId());
        }
    }
}
