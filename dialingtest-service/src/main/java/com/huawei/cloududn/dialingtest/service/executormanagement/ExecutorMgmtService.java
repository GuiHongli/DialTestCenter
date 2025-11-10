/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2025. All rights reserved.
 */

package com.huawei.cloududn.dialingtest.service.executormanagement;

import com.fasterxml.jackson.databind.JsonNode;
import com.huawei.cloududn.dialingtest.dao.executormanagement.ExecutorDao;
import com.huawei.cloududn.dialingtest.dao.executormanagement.UeDao;
import com.huawei.cloududn.dialingtest.model.Ue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import java.time.Instant;
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

    /**
     * Handle heartbeat payload from agent.
     *
     * @param data    heartbeat payload
     * @param session ws session
     */
    public void handleHeartbeatStatus(JsonNode data, WebSocketSession session) {
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
    public void handleDeregister(JsonNode data, WebSocketSession session) {
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
    public void handleExecutorInfoResponse(JsonNode data, WebSocketSession session) {
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


