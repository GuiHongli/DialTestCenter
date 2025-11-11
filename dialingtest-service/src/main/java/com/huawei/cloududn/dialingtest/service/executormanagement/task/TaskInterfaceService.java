/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2025. All rights reserved.
 */

package com.huawei.cloududn.dialingtest.service.executormanagement.task;

import com.huawei.cloududn.dialingtest.controller.executormanagement.websocket.codec.TlvDecoder;
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
     * Handle inbound task status update and trigger state machine event (V3 TLV format).
     * V3版本：处理TLV格式的任务状态更新
     *
     * @param decoded decoded TLV message
     * @param session session
     */
    public void handleTaskStatusUpdate(TlvDecoder.DecodedMessage decoded, Session session) {
        // TODO: V3版本需要重新实现任务状态更新处理
        logger.info("Task status update received (V3 TLV implementation pending), sessionId={}", session.getId());
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
     * Extract result data from task status update message (V3 TLV format).
     * V3版本：从TLV消息中提取结果数据
     *
     * @param decoded decoded TLV message
     * @return result data map
     */
    private Map<String, Object> extractResultData(TlvDecoder.DecodedMessage decoded) {
        // TODO: V3版本需要重新实现结果数据提取
        Map<String, Object> resultData = new HashMap<>();
        logger.debug("Extract result data (V3 TLV implementation pending)");
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
     * Dispatch task_assign to specified agent session (V3 TLV format).
     * V3版本：使用TLV格式发送任务分配
     *
     * @param sessionId target session id
     * @param taskId task ID
     * @param scriptName script name
     * @param version script version
     * @param parameters parameters string
     */
    public void dispatchTaskToAgent(String sessionId, String taskId, String scriptName, String version, String parameters) {
        logger.info("Dispatch task_assign to sessionId={}, taskId={}", sessionId, taskId);
        wssMessageSender.sendTaskAssign(sessionId, taskId, scriptName, version, parameters);
    }

    /**
     * Dispatch task_cancel to specified agent session (V3 TLV format).
     * V3版本：使用TLV格式发送任务取消
     *
     * @param sessionId target session id
     * @param taskId task id to cancel
     */
    public void dispatchTaskCancel(String sessionId, String taskId) {
        logger.info("Dispatch task_cancel to sessionId={}, taskId={}", sessionId, taskId);
        wssMessageSender.sendTaskCancel(sessionId, taskId);
    }

    /**
     * Dispatch script_update_notify to specified agent session (V3 TLV format).
     * V3版本：使用TLV格式发送脚本更新通知
     *
     * @param sessionId target session id
     * @param scriptName script name
     * @param version script version
     * @param fileLen file length
     * @param scriptFile script file content
     * @param crc CRC checksum
     */
    public void dispatchScriptUpdate(String sessionId, String scriptName, String version, int fileLen, byte[] scriptFile, String crc) {
        logger.info("Dispatch script_update_notify to sessionId={}, scriptName={}, version={}", sessionId, scriptName, version);
        wssMessageSender.sendScriptUpdateNotify(sessionId, scriptName, version, fileLen, scriptFile, crc);
    }

    /**
     * Dispatch app_install to specified agent session (V3 TLV format).
     * V3版本：使用TLV格式发送App安装
     *
     * @param sessionId target session id
     * @param serialNo UE serial number
     * @param taskId task ID
     * @param appName app name
     * @param script install script (optional)
     * @param packageData app package data (optional)
     * @param crc CRC checksum (optional)
     */
    public void dispatchAppInstall(String sessionId, String serialNo, int taskId, String appName, byte[] script, byte[] packageData, String crc) {
        logger.info("Dispatch app_install to sessionId={}, serialNo={}", sessionId, serialNo);
        wssMessageSender.sendAppInstall(sessionId, serialNo, taskId, appName, script, packageData, crc);
    }

    /**
     * Query UE screencap from specified agent session (V3 TLV format).
     * V3版本：使用TLV格式查询UE截屏
     *
     * @param sessionId target session id
     * @param serialNo UE serial number
     */
    public void queryUeScreencap(String sessionId, String serialNo) {
        logger.info("Query UE screencap from sessionId={}, serialNo={}", sessionId, serialNo);
        wssMessageSender.sendQueryUeScreencap(sessionId, serialNo);
    }

    /**
     * Handle inbound UE screencap response (V3 TLV format).
     * V3版本：处理TLV格式的UE截屏响应
     *
     * @param decoded decoded TLV message
     * @param session ws session
     */
    public void handleScreencapResponse(TlvDecoder.DecodedMessage decoded, Session session) {
        String serialNo = decoded.getField(com.huawei.cloududn.dialingtest.controller.executormanagement.websocket.codec.FieldTag.SERIAL_NO).getAsString();
        int result = decoded.getField(com.huawei.cloududn.dialingtest.controller.executormanagement.websocket.codec.FieldTag.RESULT).getAsInt();
        logger.info("Handle screencap_response, serialNo={}, result={}, sessionId={}", serialNo, result, session.getId());
        // TODO: notify upper module with image payload if needed
    }

    /**
     * Handle inbound app install response (V3 TLV format).
     * V3版本：处理TLV格式的App安装响应
     *
     * @param decoded decoded TLV message
     * @param session ws session
     */
    public void handleAppInstallResponse(TlvDecoder.DecodedMessage decoded, Session session) {
        String serialNo = decoded.getField(com.huawei.cloududn.dialingtest.controller.executormanagement.websocket.codec.FieldTag.SERIAL_NO).getAsString();
        int taskId = decoded.getField(com.huawei.cloududn.dialingtest.controller.executormanagement.websocket.codec.FieldTag.TASKID).getAsInt();
        int result = decoded.getField(com.huawei.cloududn.dialingtest.controller.executormanagement.websocket.codec.FieldTag.RESULT).getAsInt();
        logger.info("Handle app_install_response, serialNo={}, taskId={}, result={}, sessionId={}", serialNo, taskId, result, session.getId());
        // TODO: notify upper module with install result if needed
    }

    /**
     * Handle inbound script update ack (V3 TLV format).
     * V3版本：处理TLV格式的脚本更新确认
     *
     * @param decoded decoded TLV message
     * @param session ws session
     */
    public void handleScriptUpdateAck(TlvDecoder.DecodedMessage decoded, Session session) {
        String scriptName = decoded.getField(com.huawei.cloududn.dialingtest.controller.executormanagement.websocket.codec.FieldTag.SCRIPT_NAME).getAsString();
        String version = decoded.getField(com.huawei.cloududn.dialingtest.controller.executormanagement.websocket.codec.FieldTag.VERSION).getAsString();
        int result = decoded.getField(com.huawei.cloududn.dialingtest.controller.executormanagement.websocket.codec.FieldTag.RESULT).getAsInt();
        logger.info("Handle script_update_ack, scriptName={}, version={}, result={}, sessionId={}", scriptName, version, result, session.getId());
        // TODO: notify upper module with script update result if needed
    }

    /**
     * Handle inbound task start response (V3 TLV format).
     * V3版本：处理TLV格式的任务启动响应
     *
     * @param decoded decoded TLV message
     * @param session ws session
     */
    public void handleTaskStartResponse(TlvDecoder.DecodedMessage decoded, Session session) {
        int taskId = decoded.getField(com.huawei.cloududn.dialingtest.controller.executormanagement.websocket.codec.FieldTag.TASKID).getAsInt();
        String result = decoded.getField(com.huawei.cloududn.dialingtest.controller.executormanagement.websocket.codec.FieldTag.RESULT).getAsString();
        logger.info("Handle task_start_response, taskId={}, result={}, sessionId={}", taskId, result, session.getId());
        // TODO: process task result and sub-results
    }

    /**
     * Handle inbound task stop response (V3 TLV format).
     * V3版本：处理TLV格式的任务停止响应
     *
     * @param decoded decoded TLV message
     * @param session ws session
     */
    public void handleTaskStopResponse(TlvDecoder.DecodedMessage decoded, Session session) {
        int taskId = decoded.getField(com.huawei.cloududn.dialingtest.controller.executormanagement.websocket.codec.FieldTag.TASKID).getAsInt();
        int result = decoded.getField(com.huawei.cloududn.dialingtest.controller.executormanagement.websocket.codec.FieldTag.RESULT).getAsInt();
        logger.info("Handle task_stop_response, taskId={}, result={}, sessionId={}", taskId, result, session.getId());
        // TODO: process task stop confirmation
    }

    /**
     * Handle inbound app list response (V3 TLV format).
     * V3版本：处理TLV格式的App列表响应
     *
     * @param decoded decoded TLV message
     * @param session ws session
     */
    public void handleAppListResponse(TlvDecoder.DecodedMessage decoded, Session session) {
        String serialNo = decoded.getField(com.huawei.cloududn.dialingtest.controller.executormanagement.websocket.codec.FieldTag.SERIAL_NO).getAsString();
        int result = decoded.getField(com.huawei.cloududn.dialingtest.controller.executormanagement.websocket.codec.FieldTag.RESULT).getAsInt();
        logger.info("Handle app_list_response, serialNo={}, result={}, sessionId={}", serialNo, result, session.getId());
        // TODO: process app list data
    }

}


