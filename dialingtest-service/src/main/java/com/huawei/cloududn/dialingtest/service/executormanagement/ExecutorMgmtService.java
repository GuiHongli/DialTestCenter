/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2025. All rights reserved.
 */

package com.huawei.cloududn.dialingtest.service.executormanagement;

import com.fasterxml.jackson.databind.JsonNode;
import com.huawei.cloududn.dialingtest.controller.executormanagement.websocket.codec.DtoTlvConverter;
import com.huawei.cloududn.dialingtest.controller.executormanagement.websocket.codec.FieldTag;
import com.huawei.cloududn.dialingtest.controller.executormanagement.websocket.codec.TlvDecoder;
import com.huawei.cloududn.dialingtest.controller.executormanagement.websocket.dto.DeRegisterAckDto;
import com.huawei.cloududn.dialingtest.controller.executormanagement.websocket.dto.DeRegisterRequestDto;
import com.huawei.cloududn.dialingtest.controller.executormanagement.websocket.dto.ReportAckDto;
import com.huawei.cloududn.dialingtest.controller.executormanagement.websocket.dto.ReportMsgDto;
import com.huawei.cloududn.dialingtest.controller.executormanagement.websocket.dto.UeItemDto;
import com.huawei.cloududn.dialingtest.dao.executormanagement.ExecutorDao;
import com.huawei.cloududn.dialingtest.dao.executormanagement.UeDao;
import com.huawei.cloududn.dialingtest.model.Ue;
import com.huawei.cloududn.dialingtest.service.executormanagement.task.WssMessageSender;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;
import java.time.Instant;

import javax.websocket.Session;
import java.util.Iterator;

/**
 * Core executor management service: heartbeat and disconnect handling.
 *
 * <p>Implementation to be extended to update DB and UE list.</p>
 *
 * @author g00940940
 * @since 2025-11-04
 */
@Service
public class ExecutorMgmtService {

    private static final Logger logger = LoggerFactory.getLogger(ExecutorMgmtService.class);

    @Autowired
    private ExecutorDao executorDao;

    @Autowired
    private UeDao ueDao;

    @Autowired
    private SessionBindingRegistry registry;
    
    @Autowired
    private WssMessageSender wssMessageSender;

    /**
     * Handle heartbeat payload from agent (Legacy JSON version).
     *
     * @param data    heartbeat payload
     * @param session ws session
     * @deprecated Use {@link #handleReportMsg(ReportMsgDto, Session)} for V3 TLV protocol
     */
    @Deprecated
    public void handleHeartbeatStatus(JsonNode data, Session session) {
        logger.info("Received heartbeat, sessionId={}", session.getId());
        String executorName = registry.getExecutorName(session.getId());
        if (executorName == null) {
            logger.warn("Skip heartbeat: no binding for sessionId={}", session.getId());
            return;
        }
        logger.debug("Updating executor status to online, name={}", executorName);
        executorDao.updateStatus(executorName, 1, Instant.now());
        JsonNode ueList = data != null ? data.get("ue_list") : null;
        if (ueList != null && ueList.isArray()) {
            int ueCount = ueList.size();
            logger.debug("Processing UE list from heartbeat, executor={}, ueCount={}", executorName, ueCount);
            for (Iterator<JsonNode> it = ueList.elements(); it.hasNext(); ) {
                JsonNode ue = it.next();
                Ue ueModel = new Ue();
                ueModel.setMsisdn(text(ue, "msisdn"));
                ueModel.setExecutorName(executorName);
                ueModel.setVendor(text(ue, "vendor"));
                ueModel.setOs(text(ue, "os_version"));
                ueModel.setInfo(ue.toString());
                ueModel.setTaskInfo(null);
                ueDao.upsert(ueModel);
            }
            logger.debug("UE list processed successfully, executor={}, upserted {} records", executorName, ueCount);
        } else {
            logger.debug("No UE list in heartbeat, executor={}", executorName);
        }
    }
    
    /**
     * Handle Report-Msg (0x11): process heartbeat and UE list (V3 TLV version).
     * V3版本：接收ReportMsgDto，更新数据库，发送ReportAck
     *
     * @param dto     ReportMsg DTO (包含token、state、ue-list)
     * @param session WebSocket session
     */
    public void handleReportMsg(ReportMsgDto dto, Session session) {
        logger.info("Received Report-Msg, sessionId={}, token={}, state={}", 
            session.getId(), dto.getToken(), dto.getState());
        
        // Get executor name from session binding
        String executorName = registry.getExecutorName(session.getId());
        if (executorName == null) {
            logger.warn("Skip Report-Msg: no binding for sessionId={}", session.getId());
            sendReportAck(session.getId(), dto.getToken(), 1); // Error: no binding
            return;
        }
        
        // Update executor status and last_online_time
        logger.debug("Updating executor status to ONLINE, name={}", executorName);
        executorDao.updateStatus(executorName, 1, Instant.now()); // 1=ONLINE
        
        // Process UE list
        if (dto.getUeList() != null && !dto.getUeList().isEmpty()) {
            int ueCount = dto.getUeList().size();
            logger.debug("Processing UE list from Report-Msg, executor={}, ueCount={}", 
                executorName, ueCount);
            
            for (UeItemDto ueItem : dto.getUeList()) {
                processUeItem(executorName, ueItem);
            }
            
            logger.debug("UE list processed successfully, executor={}, upserted {} records", 
                executorName, ueCount);
        } else {
            logger.debug("No UE list in Report-Msg, executor={}", executorName);
        }
        
        // Send Report-Ack (0x12)
        sendReportAck(session.getId(), dto.getToken(), 0); // 0=OK
        
        logger.info("Report-Msg processed successfully, executor={}", executorName);
    }
    
    /**
     * Process single UE item and upsert to database.
     * V3版本：处理UeItemDto，包含完整UE信息
     *
     * @param executorName executor name
     * @param ueItem       UE item DTO
     */
    private void processUeItem(String executorName, UeItemDto ueItem) {
        try {
            Ue ueModel = new Ue();
            
            // Map UeItemDto to Ue entity
            ueModel.setMsisdn(ueItem.getSerialNo()); // Use serial_no as msisdn
            ueModel.setExecutorName(executorName);
            ueModel.setVendor(ueItem.getBrand());
            ueModel.setOs(ueItem.getOs() + " " + ueItem.getVersion());
            
            // Store detailed info as JSON in info field
            String infoJson = buildUeInfoJson(ueItem);
            ueModel.setInfo(infoJson);
            
            // Upsert to database
            ueDao.upsert(ueModel);
            
            logger.debug("UE info updated: serialNo={}, brand={}, model={}", 
                ueItem.getSerialNo(), ueItem.getBrand(), ueItem.getModel());
        } catch (IllegalArgumentException e) {
            logger.error("Failed to process UE item: serialNo={}", ueItem.getSerialNo(), e);
        }
    }
    
    /**
     * Build UE info JSON string.
     *
     * @param ueItem UE item DTO
     * @return JSON string
     */
    private String buildUeInfoJson(UeItemDto ueItem) {
        // Simple JSON construction (should use ObjectMapper in production)
        return String.format(
            "{\"serial\":\"%s\",\"brand\":\"%s\",\"model\":\"%s\",\"os\":\"%s\"," +
            "\"version\":\"%s\",\"resolution\":\"%s\",\"ipv4\":\"%s\",\"ipv6\":\"%s\"," +
            "\"battery\":%d}",
            ueItem.getSerialNo(),
            ueItem.getBrand(),
            ueItem.getModel(),
            ueItem.getOs(),
            ueItem.getVersion(),
            ueItem.getWmsize(),
            ueItem.getIpv4() != null ? ueItem.getIpv4() : "",
            ueItem.getIpv6() != null ? ueItem.getIpv6() : "",
            ueItem.getBattery() != null ? ueItem.getBattery() : 0
        );
    }
    
    /**
     * Send Report-Ack (0x12).
     * V3新增：发送心跳应答
     *
     * @param sessionId session ID
     * @param token     token from Report-Msg
     * @param state     state (0=OK, non-zero=error)
     */
    private void sendReportAck(String sessionId, long token, int state) {
        ReportAckDto ackDto = new ReportAckDto(token, state);
        ByteBuffer buffer = DtoTlvConverter.encodeReportAck(ackDto);
        wssMessageSender.sendBinary(sessionId, buffer);
        
        logger.debug("Sent Report-Ack to sessionId={}, token={}, state={}", 
            sessionId, token, state);
    }
    
    /**
     * Handle DeRegister-Request (0x05): executor logout (V3 TLV version).
     * V3新增：处理Agent注销请求
     *
     * @param dto     DeRegister request DTO
     * @param session WebSocket session
     */
    public void handleDeRegisterRequest(DeRegisterRequestDto dto, Session session) {
        logger.info("Received DeRegister-Request, sessionId={}, token={}", 
            session.getId(), dto.getToken());
        
        // Get executor name from session binding
        String executorName = registry.getExecutorName(session.getId());
        if (executorName == null) {
            logger.warn("Skip DeRegister: no binding for sessionId={}", session.getId());
            sendDeRegisterAck(session.getId(), dto.getToken(), 1, "No binding found");
            return;
        }
        
        try {
            // Update executor status to OFFLINE
            executorDao.updateStatus(executorName, 0, Instant.now()); // 0=OFFLINE
            
            // Unbind session
            registry.unbind(session.getId());
            
            // Send DeRegister-Ack (0x06)
            sendDeRegisterAck(session.getId(), dto.getToken(), 0, "Logout successful");
            
            logger.info("DeRegister completed successfully, executor={}", executorName);
        } catch (IllegalArgumentException e) {
            logger.error("Failed to deregister executor={}", executorName, e);
            sendDeRegisterAck(session.getId(), dto.getToken(), 2, "Database error");
        }
    }
    
    /**
     * Send DeRegister-Ack (0x06).
     * V3新增：发送注销应答
     *
     * @param sessionId   session ID
     * @param token       token from DeRegister-Request
     * @param resultCode  result code (0=OK, non-zero=error)
     * @param description description message
     */
    private void sendDeRegisterAck(String sessionId, long token, int resultCode, String description) {
        DeRegisterAckDto ackDto = new DeRegisterAckDto(token, resultCode, description);
        ByteBuffer buffer = DtoTlvConverter.encodeDeRegisterAck(ackDto);
        wssMessageSender.sendBinary(sessionId, buffer);
        
        logger.debug("Sent DeRegister-Ack to sessionId={}, token={}, resultCode={}", 
            sessionId, token, resultCode);
    }

    /**
     * Handle disconnect event by session id.
     *
     * @param sessionId session id
     */
    public void handleExecutorDisconnect(String sessionId) {
        logger.info("Handle disconnect for sessionId={}", sessionId);
        String executorName = registry.getExecutorName(sessionId);
        if (executorName != null) {
            logger.debug("Updating executor status to offline, name={}", executorName);
            executorDao.updateStatus(executorName, 0, Instant.now());
            registry.unbind(sessionId);
            logger.info("Executor disconnected and status updated, name={}", executorName);
        } else {
            logger.debug("No executor binding found for sessionId={}", sessionId);
        }
    }

    /**
     * Handle deregister request from agent.
     *
     * @param data    deregister payload
     * @param session ws session
     */
    public void handleDeregister(JsonNode data, Session session) {
        String executorName = text(data, "name");
        logger.info("Received deregister request, executorName={}, sessionId={}", executorName, session.getId());
        if (executorName == null || executorName.isEmpty()) {
            logger.warn("Deregister request missing executor name, sessionId={}", session.getId());
            return;
        } else {
            String boundExecutor = registry.getExecutorName(session.getId());
            if (executorName.equals(boundExecutor)) {
                logger.debug("Updating executor status to offline, name={}", executorName);
                executorDao.updateStatus(executorName, 0, Instant.now());
                registry.unbind(session.getId());
                logger.info("Executor {} deregistered successfully", executorName);
            } else {
                logger.warn("Deregister name mismatch: requested={}, bound={}", executorName, boundExecutor);
            }
        }
    }

    /**
     * Handle executor info response from agent.
     *
     * @param data    executor info payload
     * @param session ws session
     */
    public void handleExecutorInfoResponse(JsonNode data, Session session) {
        String executorName = text(data, "executor_name");
        logger.info("Received executor_info_response, executorName={}, sessionId={}", executorName, session.getId());
        if (executorName == null || executorName.isEmpty()) {
            logger.warn("Executor info response missing executor name, sessionId={}", session.getId());
            return;
        } else {
            JsonNode ueDetails = data != null ? data.get("ue_details") : null;
            if (ueDetails != null && ueDetails.isArray()) {
                int ueCount = ueDetails.size();
                logger.debug("Processing UE details, executor={}, count={}", executorName, ueCount);
                int processedCount = 0;
                for (Iterator<JsonNode> it = ueDetails.elements(); it.hasNext(); ) {
                    JsonNode ueDetail = it.next();
                    String msisdn = text(ueDetail, "msisdn");
                    if (msisdn != null && !msisdn.isEmpty()) {
                        Ue ueModel = new Ue();
                        ueModel.setMsisdn(msisdn);
                        ueModel.setExecutorName(executorName);
                        ueModel.setVendor(text(ueDetail, "vendor"));
                        ueModel.setOs(text(ueDetail, "os_version"));
                        ueModel.setInfo(ueDetail.toString());
                        ueModel.setTaskInfo(null);
                        ueDao.upsert(ueModel);
                        processedCount++;
                        logger.debug("Updated UE info for msisdn={}", msisdn);
                    } else {
                        logger.debug("Skipping UE with empty msisdn");
                    }
                }
                logger.info("Executor info response processed successfully for {}, {} UE records updated", executorName, processedCount);
            } else {
                logger.debug("No UE details in executor info response");
            }
        }
    }

    private static String text(JsonNode node, String field) {
        return node != null && node.has(field) ? node.get(field).asText("") : "";
    }
}


