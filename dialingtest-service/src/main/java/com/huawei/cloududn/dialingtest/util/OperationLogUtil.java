/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.cloududn.dialingtest.util;

import com.huawei.cloududn.dialingtest.model.CreateOperationLogRequest;
import com.huawei.cloududn.dialingtest.model.DialUser;
import com.huawei.cloududn.dialingtest.model.OperationLog;
import com.huawei.cloududn.dialingtest.model.TestCaseSet;
import com.huawei.cloududn.dialingtest.model.UserRole;
import com.huawei.cloududn.dialingtest.service.OperationLogService;

import java.time.Instant;
import java.util.Map;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

/**
 * 操作记录工具类
 * 用于在业务操作中自动记录操作历史
 * 支持用户操作、用例集操作、用户角色操作等
 *
 * @author g00940940
 * @since 2025-01-15
 */
@Component
public class OperationLogUtil {
    private static final Logger logger = LoggerFactory.getLogger(OperationLogUtil.class);
    
    @Autowired
    private OperationLogService operationLogService;
    
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final ObjectWriter objectWriter = objectMapper.writer();
    
    /**
     * 将对象转换为JSON字符串
     *
     * @param obj 要转换的对象
     * @return JSON字符串
     */
    private String objectToJson(Object obj) {
        try {
            return objectWriter.writeValueAsString(obj);
        } catch (Exception e) {
            logger.warn("Failed to convert object to JSON: {}", e.getMessage());
            return "{}";
        }
    }
    
    /**
     * 通用的操作记录方法
     *
     * @param operatorUsername 操作用户名
     * @param operationType 操作类型
     * @param operationTarget 操作目标
     * @param descriptionZh 中文描述
     * @param descriptionEn 英文描述
     * @param operationDataBuilder 操作数据构建器函数
     * @param debugMessage 调试日志消息
     */
    private void logOperation(String operatorUsername, String operationType, String operationTarget,
                            String descriptionZh, String descriptionEn,
                            Function<OperationDataBuilder, OperationDataBuilder> operationDataBuilder,
                            String debugMessage) {
        try {
            CreateOperationLogRequest request = new CreateOperationLogRequest();
            request.setUsername(operatorUsername);
            request.setOperationType(operationType);
            request.setOperationTarget(operationTarget);
            request.setOperationDescriptionZh(descriptionZh);
            request.setOperationDescriptionEn(descriptionEn);
            
            Map<String, Object> operationData = operationDataBuilder.apply(new OperationDataBuilder()).build();
            request.setOperationData(objectToJson(operationData));
            
            operationLogService.createOperationLog(request);
            logger.debug(debugMessage);
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid parameters for {} operation: {}", operationType, e.getMessage());
        } catch (RuntimeException e) {
            logger.warn("Failed to log {} operation: {}", operationType, e.getMessage());
        }
    }
    
    /**
     * 记录用户创建操作
     *
     * @param operatorUsername 操作用户名
     * @param targetUsername 目标用户名
     * @param userDetails 用户详细信息对象
     */
    public void logUserCreate(String operatorUsername, String targetUsername, DialUser userDetails) {
        logOperation(operatorUsername, "CREATE", "USER",
            "创建用户: " + targetUsername,
            "Create user: " + targetUsername,
            builder -> builder.userCreate(userDetails),
            "Logged user create operation for user: " + targetUsername);
    }
    

    
    /**
     * 记录用户更新操作
     *
     * @param operatorUsername 操作用户名
     * @param targetUsername 目标用户名
     * @param oldValues 更新前的DialUser对象
     * @param newValues 更新后的DialUser对象
     */
    public void logUserUpdate(String operatorUsername, String targetUsername, DialUser oldValues, DialUser newValues) {
        logOperation(operatorUsername, "UPDATE", "USER",
            "更新用户: " + targetUsername,
            "Update user: " + targetUsername,
            builder -> builder.userUpdate(oldValues, newValues),
            "Logged user update operation for user: " + targetUsername);
    }
    

    
    /**
     * 记录用户删除操作
     *
     * @param operatorUsername 操作用户名
     * @param targetUsername 目标用户名
     * @param userInfo 被删除用户的DialUser对象
     */
    public void logUserDelete(String operatorUsername, String targetUsername, DialUser userInfo) {
        logOperation(operatorUsername, "DELETE", "USER",
            "删除用户: " + targetUsername,
            "Delete user: " + targetUsername,
            builder -> builder.userDelete(userInfo),
            "Logged user delete operation for user: " + targetUsername);
    }
    
  
    
    /**
     * 记录用例集上传操作
     *
     * @param operatorUsername 操作用户名
     * @param testCaseSet 用例集信息
     */
    public void logTestCaseSetUpload(String operatorUsername, TestCaseSet testCaseSet) {
        logOperation(operatorUsername, "CREATE", "TEST_CASE_SET",
            "上传用例集: " + testCaseSet.getName() + " v" + testCaseSet.getVersion(),
            "Upload test case set: " + testCaseSet.getName() + " v" + testCaseSet.getVersion(),
            builder -> builder.testCaseSetCreate(testCaseSet),
            "Logged test case set upload: " + testCaseSet.getName() + " v" + testCaseSet.getVersion() + " by user: " + operatorUsername);
    }
    
    /**
     * 记录用例集更新操作
     *
     * @param operatorUsername 操作用户名
     * @param oldValues 更新前的TestCaseSet对象
     * @param newValues 更新后的TestCaseSet对象
     */
    public void logTestCaseSetUpdate(String operatorUsername, TestCaseSet oldValues, TestCaseSet newValues) {
        logOperation(operatorUsername, "UPDATE", "TEST_CASE_SET",
            "更新用例集: " + newValues.getName() + " v" + newValues.getVersion(),
            "Update test case set: " + newValues.getName() + " v" + newValues.getVersion(),
            builder -> builder.testCaseSetUpdate(oldValues, newValues),
            "Logged test case set update: " + newValues.getName() + " v" + newValues.getVersion() + " by user: " + operatorUsername);
    }
    
    /**
     * 记录用例集删除操作
     *
     * @param operatorUsername 操作用户名
     * @param testCaseSet 用例集信息
     */
    public void logTestCaseSetDelete(String operatorUsername, TestCaseSet testCaseSet) {
        logOperation(operatorUsername, "DELETE", "TEST_CASE_SET",
            "删除用例集: " + testCaseSet.getName() + " v" + testCaseSet.getVersion(),
            "Delete test case set: " + testCaseSet.getName() + " v" + testCaseSet.getVersion(),
            builder -> builder.testCaseSetDelete(testCaseSet),
            "Logged test case set delete: " + testCaseSet.getName() + " v" + testCaseSet.getVersion() + " by user: " + operatorUsername);
    }
    
    /**
     * 记录用户角色创建操作
     *
     * @param operatorUsername 操作用户名
     * @param userRole 用户角色对象
     */
    public void logUserRoleCreate(String operatorUsername, UserRole userRole) {
        logOperation(operatorUsername, "CREATE", "USER_ROLE",
            "创建用户角色: " + userRole.getUsername() + " -> " + userRole.getRole(),
            "Create user role: " + userRole.getUsername() + " -> " + userRole.getRole(),
            builder -> builder.userRoleCreate(userRole),
            "Logged user role create operation for user: " + userRole.getUsername() + " with role: " + userRole.getRole());
    }
    
    /**
     * 记录用户角色更新操作
     *
     * @param operatorUsername 操作用户名
     * @param oldValues 更新前的UserRole对象
     * @param newValues 更新后的UserRole对象
     */
    public void logUserRoleUpdate(String operatorUsername, UserRole oldValues, UserRole newValues) {
        logOperation(operatorUsername, "UPDATE", "USER_ROLE",
            "更新用户角色: " + oldValues.getUsername() + " " + oldValues.getRole() + " -> " + newValues.getRole(),
            "Update user role: " + oldValues.getUsername() + " " + oldValues.getRole() + " -> " + newValues.getRole(),
            builder -> builder.userRoleUpdate(oldValues, newValues),
            "Logged user role update operation: " + oldValues + " -> " + newValues);
    }
    
  /**
   * 记录用户角色删除操作
   *
   * @param operatorUsername 操作用户名
   * @param userRole 被删除的用户角色对象
   */
  public void logUserRoleDelete(String operatorUsername, UserRole userRole) {
    logOperation(operatorUsername, "DELETE", "USER_ROLE",
        "删除用户角色: " + userRole.getUsername() + " -> " + userRole.getRole(),
        "Delete user role: " + userRole.getUsername() + " -> " + userRole.getRole(),
        builder -> builder.userRoleDelete(userRole),
        "Logged user role delete operation for user: " + userRole.getUsername() + " with role: " + userRole.getRole());
  }

  /**
   * 记录用户登录操作
   *
   * @param username 用户名
   */
  public void logUserLogin(String username) {
    logOperation(username, "LOGIN", "SYSTEM",
        "用户登录: " + username,
        "User login: " + username,
        builder -> builder.userLogin(username),
        "Logged user login operation for user: " + username);
  }
}
