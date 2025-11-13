/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.cloududn.dialingtest.controller;

import com.huawei.cloududn.dialingtest.api.AlarmsApi;
import com.huawei.cloududn.dialingtest.model.AlarmPageResponse;
import com.huawei.cloududn.dialingtest.model.AlarmResponse;
import com.huawei.cloududn.dialingtest.model.CreateAlarmRequest;
import com.huawei.cloududn.dialingtest.service.AlarmService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * 告警控制器，提供告警管理的REST API接口
 * 支持告警的创建、查询、结束等操作
 *
 * @author g00940940
 * @since 2025-01-15
 */
@RestController
@RequestMapping("/api")
public class AlarmController implements AlarmsApi {
    private static final Logger logger = LoggerFactory.getLogger(AlarmController.class);

    @Autowired
    private AlarmService alarmService;

    /**
     * 查询告警列表
     *
     * @param page 页码，从0开始
     * @param size 每页大小
     * @param currentOnly 是否只查询当前告警（未结束的告警）
     * @return 告警分页响应
     */
    @Override
    public ResponseEntity<AlarmPageResponse> getAlarms(
            Integer page, Integer size, Boolean currentOnly) {
        try {
            logger.info("Querying alarms with page: {}, size: {}, currentOnly: {}", 
                       page, size, currentOnly);

            AlarmPageResponse response = alarmService.getAlarms(page, size, currentOnly);

            logger.info("Successfully queried alarms, total elements: {}", 
                       response.getData().getTotalElements());

            return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(response);

        } catch (IllegalArgumentException e) {
            logger.warn("Invalid request parameters for alarms query: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .contentType(MediaType.APPLICATION_JSON)
                .body(createErrorPageResponse("Invalid request parameters: " + e.getMessage()));
        } catch (Exception e) {
            logger.error("Error querying alarms", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(MediaType.APPLICATION_JSON)
                .body(createErrorPageResponse("Internal server error"));
        }
    }

    /**
     * 创建告警
     *
     * @param xCsrfToken CSRF防护令牌
     * @param xUsername 操作用户名
     * @param body 告警创建请求
     * @return 告警响应
     */
    @Override
    public ResponseEntity<AlarmResponse> createAlarm(
            @RequestHeader("X-Csrf-Token") String xCsrfToken,
            @RequestHeader("X-Username") String xUsername,
            @Valid CreateAlarmRequest body) {
        try {
            logger.info("Creating alarm: summary={}, level={}", 
                       body.getAlarmSummary(), body.getAlarmLevel());

            AlarmResponse response = alarmService.createAlarm(body);

            logger.info("Successfully created alarm with ID: {}", 
                       response.getData().getId());

            return ResponseEntity.status(HttpStatus.CREATED)
                .contentType(MediaType.APPLICATION_JSON)
                .body(response);

        } catch (IllegalArgumentException e) {
            logger.warn("Invalid request parameters for alarm creation: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .contentType(MediaType.APPLICATION_JSON)
                .body(createErrorResponse("Invalid request parameters: " + e.getMessage()));
        } catch (IllegalStateException e) {
            logger.error("Failed to create alarm: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(MediaType.APPLICATION_JSON)
                .body(createErrorResponse("Failed to create alarm"));
        } catch (Exception e) {
            logger.error("Error creating alarm", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(MediaType.APPLICATION_JSON)
                .body(createErrorResponse("Internal server error"));
        }
    }

    /**
     * 结束告警（删除告警）
     *
     * @param id 告警ID
     * @param xCsrfToken CSRF防护令牌
     * @param xUsername 操作用户名
     * @return 告警响应
     */
    @Override
    public ResponseEntity<AlarmResponse> endAlarm(
            Integer id,
            @RequestHeader("X-Csrf-Token") String xCsrfToken,
            @RequestHeader("X-Username") String xUsername) {
        try {
            logger.info("Ending alarm with ID: {}", id);

            AlarmResponse response = alarmService.endAlarm(id);

            logger.info("Successfully ended alarm with ID: {}", id);

            return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(response);

        } catch (IllegalArgumentException e) {
            logger.warn("Invalid alarm ID: {}", id);
            return ResponseEntity.badRequest()
                .contentType(MediaType.APPLICATION_JSON)
                .body(createErrorResponse("Invalid alarm ID: " + e.getMessage()));
        } catch (IllegalStateException e) {
            logger.warn("Failed to end alarm: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .contentType(MediaType.APPLICATION_JSON)
                .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            logger.error("Error ending alarm with ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .contentType(MediaType.APPLICATION_JSON)
                .body(createErrorResponse("Alarm not found or operation failed"));
        }
    }

    /**
     * 创建错误响应
     *
     * @param message 错误消息
     * @return 错误响应
     */
    private AlarmResponse createErrorResponse(String message) {
        AlarmResponse response = new AlarmResponse();
        response.setSuccess(false);
        response.setMessage(message);
        response.setData(null);
        return response;
    }

    /**
     * 创建分页错误响应
     *
     * @param message 错误消息
     * @return 分页错误响应
     */
    private AlarmPageResponse createErrorPageResponse(String message) {
        AlarmPageResponse response = new AlarmPageResponse();
        response.setSuccess(false);
        response.setMessage(message);
        response.setData(null);
        return response;
    }
}

