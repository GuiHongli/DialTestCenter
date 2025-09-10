/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.dialtest.center.service;

import com.huawei.dialtest.center.entity.OperationLog;
import com.huawei.dialtest.center.repository.OperationLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 操作记录服务类
 * 提供操作记录的增删改查和业务逻辑处理
 *
 * @author g00940940
 * @since 2025-09-09
 */
@Service
@Transactional
public class OperationLogService {

    private static final Logger logger = LoggerFactory.getLogger(OperationLogService.class);

    @Autowired
    private OperationLogRepository operationLogRepository;

    /**
     * 记录操作日志
     *
     * @param username 用户名
     * @param operationType 操作类型
     * @param target 操作对象
     * @param description 操作描述
     * @return 保存的操作记录
     */
    public OperationLog logOperation(String username, String operationType, String target, String description) {
        try {
            logger.info("Recording operation: user={}, type={}, target={}", username, operationType, target);
            
            OperationLog operationLog = new OperationLog(username, operationType, target, description);
            OperationLog savedLog = operationLogRepository.save(operationLog);
            
            logger.info("Successfully recorded operation with ID: {}", savedLog.getId());
            return savedLog;
        } catch (Exception e) {
            logger.error("Failed to record operation: user={}, type={}, target={}", username, operationType, target, e);
            throw new RuntimeException("Failed to record operation", e);
        }
    }

    /**
     * 记录操作日志（简化版本）
     *
     * @param username 用户名
     * @param operationType 操作类型
     * @param target 操作对象
     * @return 保存的操作记录
     */
    public OperationLog logOperation(String username, String operationType, String target) {
        return logOperation(username, operationType, target, null);
    }

    /**
     * 根据条件查询操作记录（分页）
     *
     * @param username 用户名（可选）
     * @param operationType 操作类型（可选）
     * @param target 操作对象（可选）
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @return 操作记录分页结果
     */
    @Transactional(readOnly = true)
    public Page<OperationLog> getOperationLogsByConditions(String username, String operationType, String target,
                                                           LocalDateTime startTime, LocalDateTime endTime,
                                                           int page, int size) {
        try {
            logger.info("Getting operation logs by conditions: username={}, operationType={}, target={}, startTime={}, endTime={}, page={}, size={}",
                       username, operationType, target, startTime, endTime, page, size);

            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "operationTime"));
            
            // 如果只有用户名参数，使用简单查询
            if (username != null && operationType == null && target == null && startTime == null && endTime == null) {
                Page<OperationLog> result = operationLogRepository.findByUsernameOrderByOperationTimeDesc(username, pageable);
                logger.info("Successfully retrieved {} operation logs by username", result.getTotalElements());
                return result;
            }
            
            // 如果没有任何条件，使用简单查询
            if (username == null && operationType == null && target == null && startTime == null && endTime == null) {
                Page<OperationLog> result = operationLogRepository.findAllOrderByOperationTimeDesc(pageable);
                logger.info("Successfully retrieved {} operation logs", result.getTotalElements());
                return result;
            }
            
            // 使用复杂条件查询
            Page<OperationLog> result = operationLogRepository.findByConditions(username, operationType, target, startTime, endTime, pageable);
            logger.info("Successfully retrieved {} operation logs with complex conditions", result.getTotalElements());
            return result;
        } catch (Exception e) {
            logger.error("Failed to get operation logs by conditions", e);
            throw new RuntimeException("Failed to get operation logs by conditions", e);
        }
    }

    /**
     * 根据ID获取操作记录
     *
     * @param id 操作记录ID
     * @return 操作记录
     */
    @Transactional(readOnly = true)
    public Optional<OperationLog> getOperationLogById(Long id) {
        try {
            logger.info("Getting operation log by ID: {}", id);
            return operationLogRepository.findById(id);
        } catch (Exception e) {
            logger.error("Failed to get operation log by ID: {}", id, e);
            throw new RuntimeException("Failed to get operation log", e);
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
    @Transactional(readOnly = true)
    public Page<OperationLog> searchOperationLogs(String username, String operationType, String target, String description, int page, int size) {
        try {
            logger.info("Searching operation logs: username={}, type={}, target={}, description={}", username, operationType, target, description);
            
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "operationTime"));
            Page<OperationLog> result;
            
            // 根据提供的参数进行不同的查询
            if (username != null && !username.trim().isEmpty()) {
                result = operationLogRepository.findByUsernameContainingOrderByOperationTimeDesc(username, pageable);
            } else if (operationType != null && !operationType.trim().isEmpty()) {
                result = operationLogRepository.findByOperationTypeContainingOrderByOperationTimeDesc(operationType, pageable);
            } else if (target != null && !target.trim().isEmpty()) {
                result = operationLogRepository.findByTargetContainingOrderByOperationTimeDesc(target, pageable);
            } else if (description != null && !description.trim().isEmpty()) {
                result = operationLogRepository.findByDescriptionContainingOrderByOperationTimeDesc(description, pageable);
            } else {
                result = operationLogRepository.findAllOrderByOperationTimeDesc(pageable);
            }
            
            logger.info("Successfully retrieved {} operation logs from search", result.getTotalElements());
            return result;
        } catch (Exception e) {
            logger.error("Failed to search operation logs", e);
            throw new RuntimeException("Failed to search operation logs", e);
        }
    }

    /**
     * 获取最近的N条操作记录
     *
     * @param limit 限制数量
     * @return 操作记录列表
     */
    @Transactional(readOnly = true)
    public List<OperationLog> getRecentOperationLogs(int limit) {
        try {
            logger.info("Getting recent {} operation logs", limit);
            List<OperationLog> result = operationLogRepository.findRecentOperationLogs(limit);
            logger.info("Successfully retrieved {} recent operation logs", result.size());
            return result;
        } catch (Exception e) {
            logger.error("Failed to get recent operation logs", e);
            throw new RuntimeException("Failed to get recent operation logs", e);
        }
    }


}
