/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2025. All rights reserved.
 */

package com.huawei.cloududn.dialingtestapp.controller.executormanagement.websocket;

import com.huawei.cloududn.dialingtestapp.controller.executormanagement.websocket.codec.DtoTlvConverter;
import com.huawei.cloududn.dialingtestapp.controller.executormanagement.websocket.codec.MessageType;
import com.huawei.cloududn.dialingtestapp.controller.executormanagement.websocket.codec.TlvDecoder;
import com.huawei.cloududn.dialingtestapp.controller.executormanagement.websocket.dto.RegisterRequestDto;
import com.huawei.cloududn.dialingtestapp.controller.executormanagement.websocket.dto.ReportMsgDto;
import com.huawei.cloududn.dialingtestapp.service.executormanagement.ExecutorMgmtService;
import com.huawei.cloududn.dialingtestapp.service.executormanagement.auth.AuthSessionService;
import com.huawei.cloududn.dialingtestapp.service.executormanagement.task.TaskInterfaceService;

import com.huawei.cloududn.dialingtestapp.controller.executormanagement.websocket.dto.DeRegisterRequestDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;

import javax.websocket.Session;

/**
 * Dispatch inbound WSS messages by TLV message ID.
 * V3版本：从JSON分发改为TLV消息ID分发
 *
 * <p>Routes to Auth, Executor and Task services based on MessageType (0x01-0x36).</p>
 *
 * @author g00940940
 * @since 2025-11-11
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
     * Dispatch TLV binary message by message ID.
     * V3变更：基于TLV消息ID（0x01-0x36）进行路由分发
     *
     * @param buffer  TLV binary buffer
     * @param session ws session
     */
    public void dispatch(ByteBuffer buffer, Session session) {
        try {
            // Decode TLV message header
            TlvDecoder.DecodedMessage decoded = TlvDecoder.decodeMessage(buffer);
            MessageType messageType = decoded.getMessageType();
            
            logger.debug("Dispatching TLV message, sessionId={}, messageType={}(0x{:02X})", 
                session.getId(), messageType.getName(), messageType.getId());
            
            // Route based on message type
            if (messageType == MessageType.REGISTER_REQUEST) {
                RegisterRequestDto dto = DtoTlvConverter.decodeRegisterRequest(decoded);
                authSessionService.handleRegisterRequest(dto, session);
            } else if (messageType == MessageType.REGISTER_RESPONSE) {
                // Handle register response (0x03)
                authSessionService.handleRegisterResponse(decoded, session);
            } else if (messageType == MessageType.DEREGISTER_REQUEST) {
                // Handle deregister request (0x05)
                DeRegisterRequestDto dto =
                    DtoTlvConverter.decodeDeRegisterRequest(decoded);
                executorMgmtService.handleDeRegisterRequest(dto, session);
            } else if (messageType == MessageType.REPORT_MSG) {
                ReportMsgDto dto = DtoTlvConverter.decodeReportMsg(decoded);
                executorMgmtService.handleReportMsg(dto, session);
            } else if (messageType == MessageType.APP_LIST_RESPONSE) {
                // Handle app list response (0x22)
                taskInterfaceService.handleAppListResponse(decoded, session);
            } else if (messageType == MessageType.APP_INSTALL_RESPONSE) {
                // Handle app install response (0x24)
                taskInterfaceService.handleAppInstallResponse(decoded, session);
            } else if (messageType == MessageType.SCREENCAP_RESPONSE) {
                // Handle screencap response (0x26)
                taskInterfaceService.handleScreencapResponse(decoded, session);
            } else if (messageType == MessageType.SCRIPT_UPDATE_ACK) {
                // Handle script update ack (0x32)
                taskInterfaceService.handleScriptUpdateAck(decoded, session);
            } else if (messageType == MessageType.TASK_START_RESPONSE) {
                // Handle task start response (0x34)
                taskInterfaceService.handleTaskStartResponse(decoded, session);
            } else if (messageType == MessageType.TASK_STOP_RESPONSE) {
                // Handle task stop response (0x36)
                taskInterfaceService.handleTaskStopResponse(decoded, session);
            } else {
                logger.warn("Unknown or unhandled message type: {}(0x{:02X}), sessionId={}", 
                    messageType.getName(), messageType.getId(), session.getId());
            }
        } catch (IllegalArgumentException e) {
            logger.error("Failed to decode TLV message, sessionId={}", session.getId(), e);
        }
    }
}
