/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.dialtest.center.controller;

import com.huawei.dialtest.center.dto.BaseApiResponse;
import com.huawei.dialtest.center.dto.PagedResponse;
import com.huawei.dialtest.center.entity.OperationLog;
import com.huawei.dialtest.center.service.OperationLogService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 操作记录控制器
 * 提供操作记录的REST API接口
 *
 * @author g00940940
 * @since 2025-09-09
 */
@RestController
@RequestMapping("/api/operation-logs")
@CrossOrigin(origins = "*")
public class OperationLogController {

    private static final Logger logger = LoggerFactory.getLogger(OperationLogController.class);

    @Autowired
    private OperationLogService operationLogService;

    /**
     * 根据条件查询操作记录（分页）
     *
     * @param username 用户名（可选）
     * @param operationType 操作类型（可选）
     * @param target 操作对象（可选）
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @param page 页码（从0开始，默认0）
     * @param size 每页大小（默认20）
     * @return 操作记录分页结果
     */
    @GetMapping
    public ResponseEntity<BaseApiResponse<PagedResponse<OperationLog>>> getOperationLogsByConditions(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String operationType,
            @RequestParam(required = false) String target,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) java.time.LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) java.time.LocalDateTime endTime,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            logger.info("Received request to get operation logs by conditions: username={}, operationType={}, target={}, startTime={}, endTime={}, page={}, size={}", 
                       username, operationType, target, startTime, endTime, page, size);
            
            Page<OperationLog> operationLogs = operationLogService.getOperationLogsByConditions(username, operationType, target, startTime, endTime, page, size);
            PagedResponse<OperationLog> pagedResponse = new PagedResponse<>(
                operationLogs.getContent(), 
                operationLogs.getTotalElements(), 
                page, 
                size
            );
            logger.info("Successfully retrieved {} operation logs", operationLogs.getTotalElements());
            
            return ResponseEntity.ok(BaseApiResponse.success(pagedResponse));
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid request parameters: {}", e.getMessage());
            return ResponseEntity.badRequest().body(BaseApiResponse.error("VALIDATION_ERROR", e.getMessage()));
        } catch (Exception e) {
            logger.error("Failed to get operation logs by conditions", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(BaseApiResponse.error("INTERNAL_ERROR", "Failed to get operation logs by conditions"));
        }
    }

    /**
     * 根据ID获取操作记录
     *
     * @param id 操作记录ID
     * @return 操作记录详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<BaseApiResponse<OperationLog>> getOperationLogById(@PathVariable Long id) {
        try {
            logger.info("Received request to get operation log by ID: {}", id);
            
            Optional<OperationLog> operationLog = operationLogService.getOperationLogById(id);
            if (operationLog.isPresent()) {
                logger.info("Successfully retrieved operation log with ID: {}", id);
                return ResponseEntity.ok(BaseApiResponse.success(operationLog.get()));
            } else {
                logger.warn("Operation log not found with ID: {}", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(BaseApiResponse.error("NOT_FOUND", "Operation log not found"));
            }
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid request parameters: {}", e.getMessage());
            return ResponseEntity.badRequest().body(BaseApiResponse.error("VALIDATION_ERROR", e.getMessage()));
        } catch (Exception e) {
            logger.error("Failed to get operation log by ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(BaseApiResponse.error("INTERNAL_ERROR", "Failed to get operation log"));
        }
    }





    /**
     * 搜索操作记录
     *
     * @param username 用户名（可选）
     * @param operationType 操作类型（可选）
     * @param target 操作对象（可选）
     * @param description 操作描述（可选）
     * @param page 页码
     * @param size 每页大小
     * @return 操作记录分页结果
     */
    @GetMapping("/search")
    public ResponseEntity<BaseApiResponse<PagedResponse<OperationLog>>> searchOperationLogs(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String operationType,
            @RequestParam(required = false) String target,
            @RequestParam(required = false) String description,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            logger.info("Received request to search operation logs: username={}, type={}, target={}, description={}", 
                    username, operationType, target, description);
            
            Page<OperationLog> operationLogs = operationLogService.searchOperationLogs(username, operationType, target, description, page, size);
            PagedResponse<OperationLog> pagedResponse = new PagedResponse<>(
                operationLogs.getContent(), 
                operationLogs.getTotalElements(), 
                page, 
                size
            );
            logger.info("Successfully retrieved {} operation logs from search", operationLogs.getTotalElements());
            
            return ResponseEntity.ok(BaseApiResponse.success(pagedResponse));
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid request parameters: {}", e.getMessage());
            return ResponseEntity.badRequest().body(BaseApiResponse.error("VALIDATION_ERROR", e.getMessage()));
        } catch (Exception e) {
            logger.error("Failed to search operation logs", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(BaseApiResponse.error("INTERNAL_ERROR", "Failed to search operation logs"));
        }
    }

    /**
     * 获取最近的操作记录
     *
     * @param limit 限制数量（默认10）
     * @return 最近的操作记录列表
     */
    @GetMapping("/recent")
    public ResponseEntity<BaseApiResponse<List<OperationLog>>> getRecentOperationLogs(@RequestParam(defaultValue = "10") int limit) {
        try {
            logger.info("Received request to get recent {} operation logs", limit);
            
            List<OperationLog> operationLogs = operationLogService.getRecentOperationLogs(limit);
            logger.info("Successfully retrieved {} recent operation logs", operationLogs.size());
            
            return ResponseEntity.ok(BaseApiResponse.success(operationLogs));
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid request parameters: {}", e.getMessage());
            return ResponseEntity.badRequest().body(BaseApiResponse.error("VALIDATION_ERROR", e.getMessage()));
        } catch (Exception e) {
            logger.error("Failed to get recent operation logs", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(BaseApiResponse.error("INTERNAL_ERROR", "Failed to get recent operation logs"));
        }
    }

    /**
     * 记录操作日志
     *
     * @param request 操作记录请求
     * @return 创建的操作记录
     */
    @PostMapping
    public ResponseEntity<BaseApiResponse<OperationLog>> logOperation(@RequestBody Map<String, String> request) {
        try {
            String username = request.get("username");
            String operationType = request.get("operationType");
            String target = request.get("target");
            String description = request.get("description");

            if (username == null || username.trim().isEmpty()) {
                logger.warn("Username is required");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(BaseApiResponse.error("VALIDATION_ERROR", "Username is required"));
            }

            if (operationType == null || operationType.trim().isEmpty()) {
                logger.warn("Operation type is required");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(BaseApiResponse.error("VALIDATION_ERROR", "Operation type is required"));
            }

            if (target == null || target.trim().isEmpty()) {
                logger.warn("Target is required");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(BaseApiResponse.error("VALIDATION_ERROR", "Target is required"));
            }

            logger.info("Received request to log operation: user={}, type={}, target={}", username, operationType, target);
            
            OperationLog operationLog = operationLogService.logOperation(username, operationType, target, description);
            logger.info("Successfully logged operation with ID: {}", operationLog.getId());
            
            return ResponseEntity.ok(BaseApiResponse.success(operationLog));
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid request parameters: {}", e.getMessage());
            return ResponseEntity.badRequest().body(BaseApiResponse.error("VALIDATION_ERROR", e.getMessage()));
        } catch (Exception e) {
            logger.error("Failed to log operation", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(BaseApiResponse.error("INTERNAL_ERROR", "Failed to log operation"));
        }
    }



}
