/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2025. All rights reserved.
 */

package com.huawei.cloududn.dialingtestapp.service.executormanagement;

import com.huawei.cloududn.dialingtestapp.controller.executormanagement.websocket.codec.DtoTlvConverter;
import com.huawei.cloududn.dialingtestapp.controller.executormanagement.websocket.codec.FieldTag;
import com.huawei.cloududn.dialingtestapp.controller.executormanagement.websocket.codec.TlvDecoder;
import com.huawei.cloududn.dialingtestapp.controller.executormanagement.websocket.dto.ReportAckDto;
import com.huawei.cloududn.dialingtestapp.controller.executormanagement.websocket.dto.ReportMsgDto;
import com.huawei.cloududn.dialingtestapp.controller.executormanagement.websocket.dto.UeItemDto;
import com.huawei.cloududn.dialingtestapp.dao.executormanagement.ExecutorDao;
import com.huawei.cloududn.dialingtestapp.dao.executormanagement.UeDao;
import com.huawei.cloududn.dialingtest.model.Ue;
import com.huawei.cloududn.dialingtestapp.service.executormanagement.task.WssMessageSender;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;
import java.time.Instant;

import javax.websocket.Session;

/**
 * Core executor management service: heartbeat and disconnect handling.
 * V3版本示例：handleReportMsg接收ReportMsgDto, 发送ReportAck
 *
 * <p>V3 Changes:</p>
 * <ul>
 *   <li>handleReportMsg: 接收ReportMsgDto（包含UE详细信息），发送ReportAck</li>
 *   <li>handleDeregister: 接收DecodedMessage，处理去注册请求</li>
 * </ul>
 *
 * @author g00940940
 * @since 2025-11-11
 */
@Service
public class ExecutorMgmtServiceV3Example {

    private static final Logger logger = LoggerFactory.getLogger(ExecutorMgmtServiceV3Example.class);

    @Autowired
    private ExecutorDao executorDao;

    @Autowired
    private UeDao ueDao;

    @Autowired
    private SessionBindingRegistry sessionBindingRegistry;

    @Autowired
    private WssMessageSender wssMessageSender;

    /**
     * Handle Report-Msg (0x11): process heartbeat and UE list
     * V3版本：接收ReportMsgDto，更新数据库，发送ReportAck
     *
     * @param dto     ReportMsg DTO (包含token、state、ue-list)
     * @param session WebSocket session
     */
    public void handleReportMsg(ReportMsgDto dto, Session session) {
        logger.info("Received Report-Msg, sessionId={}, token={}, state={}", 
            session.getId(), dto.getToken(), dto.getState());
        
        // Get executor name from session binding
        String executorName = sessionBindingRegistry.getExecutorName(session.getId());
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
     * Process single UE item and upsert to database
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
     * Build UE info JSON string
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
     * Send Report-Ack (0x12)
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
     * Handle DeRegister-Request (0x05)
     * V3版本：处理去注册请求
     *
     * @param decoded Decoded TLV message
     * @param session WebSocket session
     */
    public void handleDeregister(TlvDecoder.DecodedMessage decoded, Session session) {
        long token = decoded.getField(FieldTag.TOKEN).getAsLong();
        String hostname = decoded.getField(FieldTag.HOSTNAME).getAsString();
        
        logger.info("Received DeRegister-Request, sessionId={}, hostname={}", 
            session.getId(), hostname);
        
        // Update executor status to OFFLINE
        executorDao.updateStatus(hostname, 0, Instant.now()); // 0=OFFLINE
        
        // Unbind session
        sessionBindingRegistry.unbind(session.getId());
        
        // Send DeRegister-Ack (0x06) - would need to create DeRegisterAckDto encoder
        logger.info("Executor deregistered successfully, hostname={}", hostname);
    }

    /**
     * Handle executor disconnect (connection closed)
     *
     * @param sessionId session ID
     */
    public void handleExecutorDisconnect(String sessionId) {
        String executorName = sessionBindingRegistry.getExecutorName(sessionId);
        if (executorName != null) {
            logger.info("Executor disconnected, sessionId={}, name={}", sessionId, executorName);
            executorDao.updateStatus(executorName, 0, Instant.now()); // 0=OFFLINE
            sessionBindingRegistry.unbind(sessionId);
        } else {
            logger.debug("Disconnected session had no executor binding, sessionId={}", sessionId);
        }
    }
}

