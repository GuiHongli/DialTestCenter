/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.cloududn.dialingtest.util;

import com.huawei.cloududn.dialingtest.model.CreateOperationLogRequest;
import com.huawei.cloududn.dialingtest.model.OperationLog;
import com.huawei.cloududn.dialingtest.service.OperationLogService;

import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 操作记录工具类
 * 用于在业务操作中自动记录操作历史
 *
 * @author g00940940
 * @since 2025-01-15
 */
@Component
public class OperationLogUtil {
    private static final Logger logger = LoggerFactory.getLogger(OperationLogUtil.class);
    
    @Autowired
    private OperationLogService operationLogService;
    
    /**
     * 记录用户创建操作
     *
     * @param operatorUsername 操作用户名
     * @param targetUsername 目标用户名
     * @param userDetails 用户详细信息
     */
    public void logUserCreate(String operatorUsername, String targetUsername, String userDetails) {
        try {
            CreateOperationLogRequest request = new CreateOperationLogRequest();
            request.setUsername(operatorUsername);
            request.setOperationType("CREATE");
            request.setOperationTarget("USER");
            request.setOperationDescriptionZh("创建用户: " + targetUsername + "，详细信息: " + userDetails);
            request.setOperationDescriptionEn("Create user: " + targetUsername + ", details: " + userDetails);
            request.setOperationData("{\"targetUsername\": \"" + targetUsername + "\", \"userDetails\": \"" + userDetails + "\"}");
            
            operationLogService.createOperationLog(request);
            logger.debug("Logged user create operation for user: {}", targetUsername);
        } catch (Exception e) {
            logger.warn("Failed to log user create operation: {}", e.getMessage());
        }
    }
    
    /**
     * 记录用户创建操作（简化版本）
     *
     * @param operatorUsername 操作用户名
     * @param targetUsername 目标用户名
     */
    public void logUserCreate(String operatorUsername, String targetUsername) {
        logUserCreate(operatorUsername, targetUsername, "新用户");
    }
    
    /**
     * 记录用户更新操作
     *
     * @param operatorUsername 操作用户名
     * @param targetUsername 目标用户名
     * @param oldValues 更新前的值
     * @param newValues 更新后的值
     */
    public void logUserUpdate(String operatorUsername, String targetUsername, String oldValues, String newValues) {
        try {
            CreateOperationLogRequest request = new CreateOperationLogRequest();
            request.setUsername(operatorUsername);
            request.setOperationType("UPDATE");
            request.setOperationTarget("USER");
            request.setOperationDescriptionZh("更新用户: " + targetUsername + "，由 " + oldValues + " 更改为 " + newValues);
            request.setOperationDescriptionEn("Update user: " + targetUsername + ", changed from " + oldValues + " to " + newValues);
            request.setOperationData("{\"targetUsername\": \"" + targetUsername + "\", \"oldValues\": \"" + oldValues + "\", \"newValues\": \"" + newValues + "\"}");
            
            operationLogService.createOperationLog(request);
            logger.debug("Logged user update operation for user: {}", targetUsername);
        } catch (Exception e) {
            logger.warn("Failed to log user update operation: {}", e.getMessage());
        }
    }
    
    /**
     * 记录用户更新操作（简化版本）
     *
     * @param operatorUsername 操作用户名
     * @param targetUsername 目标用户名
     */
    public void logUserUpdate(String operatorUsername, String targetUsername) {
        logUserUpdate(operatorUsername, targetUsername, "原值", "新值");
    }
    
    /**
     * 记录用户删除操作
     *
     * @param operatorUsername 操作用户名
     * @param targetUsername 目标用户名
     * @param userInfo 被删除用户的信息
     */
    public void logUserDelete(String operatorUsername, String targetUsername, String userInfo) {
        try {
            CreateOperationLogRequest request = new CreateOperationLogRequest();
            request.setUsername(operatorUsername);
            request.setOperationType("DELETE");
            request.setOperationTarget("USER");
            request.setOperationDescriptionZh("删除用户: " + targetUsername + "，用户信息: " + userInfo);
            request.setOperationDescriptionEn("Delete user: " + targetUsername + ", user info: " + userInfo);
            request.setOperationData("{\"targetUsername\": \"" + targetUsername + "\", \"userInfo\": \"" + userInfo + "\"}");
            
            operationLogService.createOperationLog(request);
            logger.debug("Logged user delete operation for user: {}", targetUsername);
        } catch (Exception e) {
            logger.warn("Failed to log user delete operation: {}", e.getMessage());
        }
    }
    
    /**
     * 记录用户删除操作（简化版本）
     *
     * @param operatorUsername 操作用户名
     * @param targetUsername 目标用户名
     */
    public void logUserDelete(String operatorUsername, String targetUsername) {
        logUserDelete(operatorUsername, targetUsername, "用户已删除");
    }
    
    /**
     * 记录用户查看操作
     *
     */
    public void logUserView(String operatorUsername, String targetUsername) {
        try {
            CreateOperationLogRequest request = new CreateOperationLogRequest();
            request.setUsername(operatorUsername);
            request.setOperationType("VIEW");
            request.setOperationTarget("USER");
            request.setOperationDescriptionZh("查看用户: " + targetUsername);
            request.setOperationDescriptionEn("View user: " + targetUsername);
            request.setOperationData("{\"targetUsername\": \"" + targetUsername + "\"}");
            
            operationLogService.createOperationLog(request);
            logger.debug("Logged user view operation for user: {}", targetUsername);
        } catch (Exception e) {
            logger.warn("Failed to log user view operation: {}", e.getMessage());
        }
    }
    
    /**
     * 记录用户登录操作
     *
     */
    public void logUserLogin(String username) {
        try {
            CreateOperationLogRequest request = new CreateOperationLogRequest();
            request.setUsername(username);
            request.setOperationType("LOGIN");
            request.setOperationTarget("SYSTEM");
            request.setOperationDescriptionZh("用户登录: " + username);
            request.setOperationDescriptionEn("User login: " + username);
            request.setOperationData("{\"username\": \"" + username + "\"}");
            
            operationLogService.createOperationLog(request);
            logger.debug("Logged user login operation for user: {}", username);
        } catch (Exception e) {
            logger.warn("Failed to log user login operation: {}", e.getMessage());
        }
    }
    
    /**
     * 记录用户登出操作
     *
     */
    public void logUserLogout(String username) {
        try {
            CreateOperationLogRequest request = new CreateOperationLogRequest();
            request.setUsername(username);
            request.setOperationType("LOGOUT");
            request.setOperationTarget("SYSTEM");
            request.setOperationDescriptionZh("用户登出: " + username);
            request.setOperationDescriptionEn("User logout: " + username);
            request.setOperationData("{\"username\": \"" + username + "\"}");
            
            operationLogService.createOperationLog(request);
            logger.debug("Logged user logout operation for user: {}", username);
        } catch (Exception e) {
            logger.warn("Failed to log user logout operation: {}", e.getMessage());
        }
    }
    
    /**
     * 记录通用操作
     *
     * @param operatorUsername 操作用户名
     * @param operationType 操作类型
     * @param operationTarget 操作目标
     */
    public void logOperation(String operatorUsername, String operationType, String operationTarget,
                           String descriptionZh, String descriptionEn, String operationData) {
        try {
            CreateOperationLogRequest request = new CreateOperationLogRequest();
            request.setUsername(operatorUsername);
            request.setOperationType(operationType);
            request.setOperationTarget(operationTarget);
            request.setOperationDescriptionZh(descriptionZh);
            request.setOperationDescriptionEn(descriptionEn);
            request.setOperationData(operationData);
            
            operationLogService.createOperationLog(request);
            logger.debug("Logged operation: {} {} for user: {}", operationType, operationTarget, operatorUsername);
        } catch (Exception e) {
            logger.warn("Failed to log operation: {}", e.getMessage());
        }
    }
}
