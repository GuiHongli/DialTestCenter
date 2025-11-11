/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2025. All rights reserved.
 */

package com.huawei.cloududn.dialingtest.service.executormanagement.task;

import com.huawei.cloududn.dialingtest.controller.executormanagement.websocket.codec.DtoTlvConverter;
import com.huawei.cloududn.dialingtest.controller.executormanagement.websocket.codec.TlvDecoder;
import com.huawei.cloududn.dialingtest.controller.executormanagement.websocket.dto.*;
import com.huawei.cloududn.dialingtest.dao.executormanagement.ExecutorDao;
import com.huawei.cloududn.dialingtest.service.executormanagement.ExecutorSelectionService;
import com.huawei.cloududn.dialingtest.service.executormanagement.SessionBindingRegistry;
import com.huawei.cloududn.dialingtest.service.executormanagement.dto.AppInstallRequest;
import com.huawei.cloududn.dialingtest.service.executormanagement.dto.ScriptUpdateRequest;
import com.huawei.cloududn.dialingtest.service.executormanagement.dto.TaskDispatchRequest;
import com.huawei.cloududn.dialingtest.service.taskmanagement.orchestration.TaskOrchestratorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.websocket.Session;

/**
 * 任务接口服务：作为任务管理模块与执行机管理模块之间的适配器
 * V3版本：支持完整的任务分发、状态上报、环境管理等功能
 *
 * @author g00940940
 * @since 2025-11-04
 */
@Service
public class TaskInterfaceService {

    private static final Logger logger = LoggerFactory.getLogger(TaskInterfaceService.class);

    // 任务ID到执行机名称的映射，用于任务停止时查找
    private final Map<Integer, String> taskToExecutorMap = new ConcurrentHashMap<>();

    @Autowired
    private WssMessageSender wssMessageSender;

    @Autowired
    private ExecutorDao executorDao;

    @Autowired
    private SessionBindingRegistry sessionBindingRegistry;

    @Autowired
    private ExecutorSelectionService executorSelectionService;

    @Autowired
    private TaskOrchestratorService taskOrchestratorService;

    /**
     * 向指定执行机分发拨测任务
     * V3版本：支持serial-no-list/proctype/sub-result等新字段
     *
     * @param request 任务分发请求
     */
    public void dispatchTaskToAgent(TaskDispatchRequest request) {
        logger.info("Dispatching task to executor: {}", request.getExecutorName());

        try {
            // 获取执行机会话
            String sessionId = sessionBindingRegistry.getSessionId(request.getExecutorName());
            if (sessionId == null) {
                logger.error("No session found for executor: {}", request.getExecutorName());
                throw new IllegalStateException("Executor not connected: " + request.getExecutorName());
            }

            // 构造TaskStartRequestDto
            TaskStartRequestDto taskDto = new TaskStartRequestDto();
            taskDto.setTaskId(request.getTaskId());
            taskDto.setScriptName(request.getScriptName());
            taskDto.setVersion(request.getVersion());
            taskDto.setSerialNoList(request.getSerialNoList());
            taskDto.setProcType(request.getProctype() != null ? Integer.valueOf(request.getProctype()) : 1);
            taskDto.setParameters(request.getParameters());

            // 发送任务开始消息
            ByteBuffer buffer = DtoTlvConverter.encodeTaskStart(taskDto);
            wssMessageSender.sendBinary(sessionId, buffer);

            // 记录任务到执行机的映射，用于后续停止操作
            taskToExecutorMap.put(request.getTaskId(), request.getExecutorName());

            logger.info("Task dispatched successfully: taskId={}, executor={}, sessionId={}",
                request.getTaskId(), request.getExecutorName(), sessionId);

        } catch (Exception e) {
            logger.error("Failed to dispatch task: taskId={}, executor={}",
                request.getTaskId(), request.getExecutorName(), e);
            throw new RuntimeException("Task dispatch failed", e);
        }
    }

    /**
     * 处理任务启动响应
     * V3版本：处理TaskStart-Response消息，支持sub-result等新字段
     *
     * @param dto     任务启动响应DTO
     * @param session WebSocket会话
     */
    public void handleTaskStartResponse(TaskStartResponseDto dto, Session session) {
        logger.info("Handling task start response: taskId={}, result={}", dto.getTaskId(), dto.getResult());

        try {
            // 通知上层任务管理模块更新任务状态
            boolean isSuccess = "SUCCESS".equalsIgnoreCase(dto.getResult()) ||
                               "success".equalsIgnoreCase(dto.getResult());

            // 构造结果数据
            java.util.Map<String, Object> resultData = new java.util.HashMap<>();
            resultData.put("task_id", dto.getTaskId());
            resultData.put("result", dto.getResult());
            resultData.put("block", dto.getBlock());
            resultData.put("sub_results", dto.getSubResult());
            resultData.put("files", dto.getFiles());
            resultData.put("crc", dto.getCrc());

            taskOrchestratorService.sendResultEvent((long) dto.getTaskId(), isSuccess, resultData);

            logger.info("Task start response processed successfully: taskId={}, result={}", dto.getTaskId(), dto.getResult());

        } catch (Exception e) {
            logger.error("Failed to handle task start response: taskId={}", dto.getTaskId(), e);
        }
    }

    /**
     * 处理任务停止请求
     * V3版本：接收上层模块的中断指令，发送TaskStop-Request
     *
     * @param taskId 任务ID
     */
    public void handleTaskStopRequest(Integer taskId) {
        logger.info("Handling task stop request: taskId={}", taskId);

        try {
            // 根据taskId找到对应的executor和session
            String executorName = taskToExecutorMap.get(taskId);
            if (executorName == null) {
                logger.warn("No executor mapping found for task: taskId={}", taskId);
                return;
            }

            String sessionId = sessionBindingRegistry.getSessionId(executorName);

            if (sessionId != null) {
                TaskStopRequestDto stopDto = new TaskStopRequestDto();
                stopDto.setTaskId(taskId);
                ByteBuffer buffer = DtoTlvConverter.encodeTaskStop(stopDto);
                wssMessageSender.sendBinary(sessionId, buffer);
                logger.info("Task stop request sent: taskId={}, executor={}", taskId, executorName);
            } else {
                logger.warn("No session found for task stop request: taskId={}, executor={}", taskId, executorName);
            }

        } catch (Exception e) {
            logger.error("Failed to handle task stop request: taskId={}", taskId, e);
        }
    }

    /**
     * 处理任务停止响应
     * V3版本：处理TaskStop-Response消息
     *
     * @param dto     任务停止响应DTO
     * @param session WebSocket会话
     */
    public void handleTaskStopResponse(TaskStopResponseDto dto, Session session) {
        logger.info("Handling task stop response: taskId={}, state={}", dto.getTaskId(), dto.getState());

        try {
            // 通知上层任务管理模块任务已停止
            taskOrchestratorService.stopTask((long) dto.getTaskId());

            // 清理任务映射
            taskToExecutorMap.remove(dto.getTaskId());

            logger.info("Task stop response processed successfully: taskId={}, state={}", dto.getTaskId(), dto.getState());

        } catch (Exception e) {
            logger.error("Failed to handle task stop response: taskId={}", dto.getTaskId(), e);
        }
    }

    /**
     * 发送App列表查询请求
     * V3版本：查询指定执行机和UE的已安装App列表
     *
     * @param executorName 执行机名称
     * @param serialNo UE序列号
     */
    public void sendAppListQuery(String executorName, String serialNo) {
        logger.info("Sending app list query: executor={}, serialNo={}", executorName, serialNo);

        try {
            String sessionId = sessionBindingRegistry.getSessionId(executorName);
            if (sessionId != null) {
                AppListQueryDto queryDto = new AppListQueryDto();
                queryDto.setSerialNo(serialNo);
                ByteBuffer buffer = DtoTlvConverter.encodeAppListQuery(queryDto);
                wssMessageSender.sendBinary(sessionId, buffer);
                logger.debug("App list query sent: executor={}, serialNo={}", executorName, serialNo);
            } else {
                logger.warn("No session found for app list query: executor={}", executorName);
            }

        } catch (Exception e) {
            logger.error("Failed to send app list query: executor={}, serialNo={}", executorName, serialNo, e);
        }
    }

    /**
     * 发送脚本更新请求
     * V3版本：向指定执行机推送新的拨测脚本
     *
     * @param executorName 执行机名称
     * @param script       脚本更新请求
     */
    public void sendScriptUpdate(String executorName, ScriptUpdateRequest script) {
        logger.info("Sending script update: executor={}, scriptName={}", executorName, script.getScriptName());

        try {
            String sessionId = sessionBindingRegistry.getSessionId(executorName);
            if (sessionId != null) {
                ScriptUpdateNotifyDto updateDto = new ScriptUpdateNotifyDto();
                updateDto.setScriptName(script.getScriptName());
                updateDto.setVersion(script.getVersion());
                updateDto.setFileLen(script.getScriptFile().length);
                updateDto.setScriptFile(script.getScriptFile());
                updateDto.setCrc(script.getCrc() != null ? script.getCrc().getBytes(java.nio.charset.StandardCharsets.UTF_8) : new byte[0]);
                ByteBuffer buffer = DtoTlvConverter.encodeScriptUpdateNotify(updateDto);
                wssMessageSender.sendBinary(sessionId, buffer);
                logger.debug("Script update sent: executor={}, scriptName={}", executorName, script.getScriptName());
            } else {
                logger.warn("No session found for script update: executor={}", executorName);
            }

        } catch (Exception e) {
            logger.error("Failed to send script update: executor={}, scriptName={}",
                executorName, script.getScriptName(), e);
        }
    }

    /**
     * 发送UE截屏查询请求
     * V3版本：查询指定执行机和UE的屏幕截图
     *
     * @param executorName 执行机名称
     * @param serialNo UE序列号
     */
    public void sendScreanCapQuery(String executorName, String serialNo) {
        logger.info("Sending screencap query: executor={}, serialNo={}", executorName, serialNo);

        try {
            String sessionId = sessionBindingRegistry.getSessionId(executorName);
            if (sessionId != null) {
                ScreencapQueryDto queryDto = new ScreencapQueryDto();
                queryDto.setSerialNo(serialNo);
                ByteBuffer buffer = DtoTlvConverter.encodeScreencapQuery(queryDto);
                wssMessageSender.sendBinary(sessionId, buffer);
                logger.debug("Screencap query sent: executor={}, serialNo={}", executorName, serialNo);
            } else {
                logger.warn("No session found for screencap query: executor={}", executorName);
            }

        } catch (Exception e) {
            logger.error("Failed to send screencap query: executor={}, serialNo={}", executorName, serialNo, e);
        }
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

        // 通知上层模块截屏结果
        if (result == 0) {
            logger.info("Screencap completed successfully for serialNo: {}", serialNo);
        } else {
            logger.warn("Screencap failed for serialNo: {}, result={}", serialNo, result);
        }
        // TODO: 如果需要，可以在这里处理图片数据
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

        // 通知上层模块App安装结果
        if (result == 0) {
            logger.info("App installation completed successfully: serialNo={}, taskId={}", serialNo, taskId);
        } else {
            logger.warn("App installation failed: serialNo={}, taskId={}, result={}", serialNo, taskId, result);
        }
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

        // 通知上层模块脚本更新结果
        if (result == 0) {
            logger.info("Script update completed successfully: scriptName={}, version={}", scriptName, version);
        } else {
            logger.warn("Script update failed: scriptName={}, version={}, result={}", scriptName, version, result);
        }
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

        // 处理任务结果和子结果
        boolean isSuccess = "SUCCESS".equalsIgnoreCase(result) || "success".equalsIgnoreCase(result);

        java.util.Map<String, Object> resultData = new java.util.HashMap<>();
        resultData.put("task_id", taskId);
        resultData.put("result", result);

        taskOrchestratorService.sendResultEvent((long) taskId, isSuccess, resultData);

        logger.info("Task start response processed: taskId={}, result={}", taskId, result);
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

        // 处理任务停止确认
        taskOrchestratorService.stopTask((long) taskId);

        // 清理任务映射
        taskToExecutorMap.remove(taskId);

        logger.info("Task stop confirmation processed: taskId={}, result={}", taskId, result);
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

        // 处理App列表数据
        if (result == 0) {
            logger.info("App list retrieved successfully for serialNo: {}", serialNo);
            // TODO: 如果需要解析具体的App列表数据，可以在这里处理
        } else {
            logger.warn("Failed to retrieve app list for serialNo: {}, result={}", serialNo, result);
        }
    }

}


