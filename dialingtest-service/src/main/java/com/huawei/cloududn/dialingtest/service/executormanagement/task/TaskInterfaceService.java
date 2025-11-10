/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2025. All rights reserved.
 */

package com.huawei.cloududn.dialingtest.service.executormanagement.task;

import com.fasterxml.jackson.databind.JsonNode;
import com.huawei.cloududn.dialingtest.dao.taskmanagement.TaskExecutorMappingDao;
import com.huawei.cloududn.dialingtest.entity.TaskExecutorMapping;
import com.huawei.cloududn.dialingtest.service.taskmanagement.orchestration.TaskOrchestratorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;

import javax.websocket.Session;
import java.util.Map;

/**
 * Task adapter between task management and WSS gateway.
 *
 * <p>Handles task_status_update inbound and task_assign outbound.</p>
 *
 * @author g00940940
 * @since 2025-11-04
 */
@Service
public class TaskInterfaceService {

    private static final Logger logger = LoggerFactory.getLogger(TaskInterfaceService.class);

    @Autowired
    private WssMessageSender wssMessageSender;

    @Autowired
    private TaskOrchestratorService taskOrchestratorService;

    @Autowired
    private TaskExecutorMappingDao taskExecutorMappingDao;

    /**
     * Handle inbound task status update and trigger state machine event.
     *
     * @param data    status data
     * @param session session
     */
    public void handleTaskStatusUpdate(JsonNode data, Session session) {
        String taskId = text(data, "task_id");
        String status = text(data, "status");
        logger.info("Handle task_status_update, taskId={}, status={}, sessionId={}", taskId, status, session.getId());
        boolean isSuccess = mapStatusToSuccess(status);
        Map<String, Object> resultData = extractResultData(data);
        try {
            TaskExecutorMapping mapping = taskExecutorMappingDao.findByTaskId(taskId);
            if (mapping == null) {
                logger.warn("No task executor mapping found for taskId={}, treating as direct task ID", taskId);
            } else {
                logger.debug("Found mapping for taskId={}, executorName={}", taskId, mapping.getExecutorName());
            }
            taskOrchestratorService.sendResultEvent(parseMainTaskId(taskId), isSuccess, resultData);
            logger.info("Successfully triggered state machine event for taskId={}, success={}", taskId, isSuccess);
        } catch (NumberFormatException e) {
            logger.error("Failed to parse task ID: taskId={}", taskId, e);
        } catch (Exception e) {
            logger.error("Failed to process task status update: taskId={}", taskId, e);
        }
    }

    /**
     * Map agent status to boolean success flag.
     *
     * @param status agent status
     * @return true if success
     */
    private boolean mapStatusToSuccess(String status) {
        if (status == null || status.isEmpty()) {
            return false;
        } else {
            String statusLower = status.toLowerCase();
            if ("success".equals(statusLower)) {
                return true;
            } else if ("failed".equals(statusLower)) {
                return false;
            } else if ("timeout".equals(statusLower)) {
                return false;
            } else {
                logger.warn("Unknown task status: {}, defaulting to false", status);
                return false;
            }
        }
    }

    /**
     * Extract result data from task status update message.
     *
     * @param data JSON data
     * @return result data map
     */
    private Map<String, Object> extractResultData(JsonNode data) {
        Map<String, Object> resultData = new HashMap<>();
        if (data != null) {
            if (data.has("result_code")) {
                resultData.put("result_code", data.get("result_code").asInt(0));
            }
            if (data.has("message")) {
                resultData.put("message", data.get("message").asText(""));
            }
            if (data.has("log_path")) {
                resultData.put("log_path", data.get("log_path").asText(""));
            }
        }
        return resultData;
    }

    /**
     * Parse main task ID from task ID string.
     * Format: T_timestamp_uuid or direct long ID
     *
     * @param taskId task ID string
     * @return main task ID as Long
     */
    private Long parseMainTaskId(String taskId) {
        if (taskId == null || taskId.isEmpty()) {
            throw new IllegalArgumentException("Task ID cannot be null or empty");
        } else {
            try {
                return Long.parseLong(taskId);
            } catch (NumberFormatException e) {
                logger.warn("Task ID is not a number, using hash as fallback: taskId={}", taskId);
                return (long) Math.abs(taskId.hashCode());
            }
        }
    }

    /**
     * Dispatch task_assign to specified agent session.
     *
     * @param sessionId target session id
     * @param taskPayload task payload with fields defined by design
     */
    public void dispatchTaskToAgent(String sessionId, Object taskPayload) {
        logger.info("Dispatch task_assign to sessionId={}", sessionId);
        wssMessageSender.sendTaskAssign(sessionId, taskPayload);
    }

    /**
     * Dispatch task_cancel to specified agent session.
     *
     * @param sessionId target session id
     * @param taskId task id to cancel
     */
    public void dispatchTaskCancel(String sessionId, String taskId) {
        logger.info("Dispatch task_cancel to sessionId={}, taskId={}", sessionId, taskId);
        wssMessageSender.sendTaskCancel(sessionId, taskId);
    }

    /**
     * Dispatch script_update_notify to specified agent session.
     *
     * @param sessionId target session id
     * @param packageName script package name
     * @param version script version
     * @param packageUrl script package URL
     * @param checksum package checksum
     */
    public void dispatchScriptUpdate(String sessionId, String packageName, String version, String packageUrl, String checksum) {
        logger.info("Dispatch script_update_notify to sessionId={}, package={}, version={}", sessionId, packageName, version);
        wssMessageSender.sendScriptUpdateNotify(sessionId, packageName, version, packageUrl, checksum);
    }

    /**
     * Dispatch app_install to specified agent session.
     *
     * @param sessionId target session id
     * @param installPayload app install payload
     */
    public void dispatchAppInstall(String sessionId, Object installPayload) {
        logger.info("Dispatch app_install to sessionId={}", sessionId);
        wssMessageSender.sendAppInstall(sessionId, installPayload);
    }

    /**
     * Query UE screencap from specified agent session.
     *
     * @param sessionId target session id
     * @param ueSerial UE serial number
     */
    public void queryUeScreencap(String sessionId, String ueSerial) {
        logger.info("Query UE screencap from sessionId={}, ueSerial={}", sessionId, ueSerial);
        wssMessageSender.sendQueryUeScreencap(sessionId, ueSerial);
    }

    /**
     * Handle inbound UE screencap response.
     *
     * @param data    response data
     * @param session ws session
     */
    public void handleUeScreencapResponse(JsonNode data, Session session) {
        String ueSerial = text(data, "ue_serial");
        String status = text(data, "status");
        logger.info("Handle ue_screencap_response, ueSerial={}, status={}, sessionId={}", ueSerial, status, session.getId());
        // TODO: notify upper module with image payload if needed
    }

    /**
     * Handle inbound app install result.
     *
     * @param data    result data
     * @param session ws session
     */
    public void handleAppInstallResult(JsonNode data, Session session) {
        String ueSerial = text(data, "ue_serial");
        String status = text(data, "status");
        logger.info("Handle app_install_result, ueSerial={}, status={}, sessionId={}", ueSerial, status, session.getId());
        // TODO: notify upper module with install result if needed
    }

    /**
     * Handle inbound script update ack.
     *
     * @param data    ack data
     * @param session ws session
     */
    public void handleScriptUpdateAck(JsonNode data, Session session) {
        String packageName = text(data, "package_name");
        String version = text(data, "version");
        String status = text(data, "status");
        logger.info("Handle script_update_ack, package={}, version={}, status={}, sessionId={}", packageName, version, status, session.getId());
        // TODO: notify upper module with script update result if needed
    }

    private static String text(JsonNode node, String field) {
        return node != null && node.has(field) ? node.get(field).asText("") : "";
    }
}


