/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2025. All rights reserved.
 */

package com.huawei.cloududn.dialingtestapp.controller.executormanagement.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huawei.cloududn.dialingtestapp.controller.executormanagement.websocket.dto.MessageType;
import com.huawei.cloududn.dialingtestapp.controller.executormanagement.websocket.dto.AppInstallResponseDto;
import com.huawei.cloududn.dialingtestapp.controller.executormanagement.websocket.dto.AppListResponseDto;
import com.huawei.cloududn.dialingtestapp.controller.executormanagement.websocket.dto.DeRegisterRequestDto;
import com.huawei.cloududn.dialingtestapp.controller.executormanagement.websocket.dto.JsonMessageEnvelope;
import com.huawei.cloududn.dialingtestapp.controller.executormanagement.websocket.dto.RegisterRequestDto;
import com.huawei.cloududn.dialingtestapp.controller.executormanagement.websocket.dto.RegisterResponseDto;
import com.huawei.cloududn.dialingtestapp.controller.executormanagement.websocket.dto.ReportMsgDto;
import com.huawei.cloududn.dialingtestapp.controller.executormanagement.websocket.dto.ScreencapResponseDto;
import com.huawei.cloududn.dialingtestapp.controller.executormanagement.websocket.dto.ScriptUpdateAckDto;
import com.huawei.cloududn.dialingtestapp.controller.executormanagement.websocket.dto.TaskStartResponseDto;
import com.huawei.cloududn.dialingtestapp.controller.executormanagement.websocket.dto.TaskStopResponseDto;
import com.huawei.cloududn.dialingtestapp.controller.executormanagement.websocket.flow.InboundFileHandler;
import com.huawei.cloududn.dialingtestapp.service.executormanagement.ExecutorMgmtService;
import com.huawei.cloududn.dialingtestapp.service.executormanagement.auth.AuthSessionService;
import com.huawei.cloududn.dialingtestapp.service.executormanagement.task.TaskInterfaceService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;

import javax.websocket.Session;

/**
 * V4 入站消息分发器
 * 职责：
 * 1. 解析 JSON 信令并分发给业务层
 * 2. 将 Binary 分片委托给 InboundFileHandler
 *
 * @author g00940940
 * @since 2025-11-14
 */
@Component
public class WssMessageDispatcher {
    private static final Logger logger = LoggerFactory.getLogger(WssMessageDispatcher.class);

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private InboundFileHandler inboundFileHandler;

    @Autowired
    private AuthSessionService authSessionService;

    @Autowired
    private ExecutorMgmtService executorMgmtService;

    @Autowired
    private TaskInterfaceService taskInterfaceService;

    /**
     * 分发 JSON 信令
     *
     * @param jsonMessage JSON消息字符串
     * @param session WebSocket会话
     */
    public void dispatch(String jsonMessage, Session session) {
        try {
            JsonMessageEnvelope envelope = objectMapper.readValue(jsonMessage, JsonMessageEnvelope.class);
            String messageType = envelope.getType();

            logger.debug("Dispatching JSON message, sessionId={}, type={}", session.getId(), messageType);

            MessageType type = MessageType.fromJsonType(messageType);

            switch (type) {
                case REGISTER_REQUEST: {
                    RegisterRequestDto dto = objectMapper.convertValue(envelope.getPayload(), RegisterRequestDto.class);
                    authSessionService.handleRegisterRequest(dto, session);
                    break;
                }
                case REGISTER_RESPONSE: {
                    RegisterResponseDto dto = objectMapper.convertValue(envelope.getPayload(), RegisterResponseDto.class);
                    authSessionService.handleRegisterResponse(dto, session);
                    break;
                }
                case DEREGISTER_REQUEST: {
                    DeRegisterRequestDto dto = objectMapper.convertValue(envelope.getPayload(), DeRegisterRequestDto.class);
                    executorMgmtService.handleDeRegisterRequest(dto, session);
                    break;
                }
                case REPORT_MSG: {
                    ReportMsgDto dto = objectMapper.convertValue(envelope.getPayload(), ReportMsgDto.class);
                    executorMgmtService.handleReportMsg(dto, session);
                    break;
                }
                case APP_LIST_RESPONSE: {
                    AppListResponseDto dto = objectMapper.convertValue(envelope.getPayload(), AppListResponseDto.class);
                    taskInterfaceService.handleAppListResponse(dto, session);
                    break;
                }
                case APP_INSTALL_RESPONSE: {
                    AppInstallResponseDto dto = objectMapper.convertValue(envelope.getPayload(), AppInstallResponseDto.class);
                    taskInterfaceService.handleAppInstallResponse(dto, session);
                    break;
                }
                case SCREENCAP_RESPONSE: {
                    ScreencapResponseDto dto = objectMapper.convertValue(envelope.getPayload(), ScreencapResponseDto.class);
                    taskInterfaceService.handleScreencapResponse(dto, session);
                    break;
                }
                case SCRIPT_UPDATE_ACK: {
                    ScriptUpdateAckDto dto = objectMapper.convertValue(envelope.getPayload(), ScriptUpdateAckDto.class);
                    taskInterfaceService.handleScriptUpdateAck(dto, session);
                    break;
                }
                case TASK_START_RESPONSE: {
                    TaskStartResponseDto dto = objectMapper.convertValue(envelope.getPayload(), TaskStartResponseDto.class);
                    taskInterfaceService.handleTaskStartResponse(dto, session);
                    break;
                }
                case TASK_STOP_RESPONSE: {
                    TaskStopResponseDto dto = objectMapper.convertValue(envelope.getPayload(), TaskStopResponseDto.class);
                    taskInterfaceService.handleTaskStopResponse(dto, session);
                    break;
                }
                default: {
                    logger.warn("Unknown or unsupported message type: {}", messageType);
                    break;
                }
            }

        } catch (IllegalArgumentException e) {
            logger.error("Invalid message type, sessionId={}", session.getId(), e);
        } catch (Exception e) {
            logger.error("Failed to dispatch JSON message, sessionId={}", session.getId(), e);
        }
    }

    /**
     * 分发二进制分片
     *
     * @param buffer 二进制数据
     * @param session WebSocket会话
     */
    public void dispatch(ByteBuffer buffer, Session session) {
        try {
            String sessionId = session.getId();

            if (inboundFileHandler.isReceivingFile(sessionId)) {
                logger.debug("Handling binary chunk for sessionId={}, size={} bytes",
                        sessionId, buffer.remaining());
                inboundFileHandler.handleChunk(sessionId, buffer);
            } else {
                logger.warn("Received unexpected binary chunk, sessionId={}", sessionId);
            }

        } catch (Exception e) {
            logger.error("Failed to dispatch binary chunk, sessionId={}", session.getId(), e);
        }
    }
}
