/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.cloududn.dialingtest.util;

import com.huawei.cloududn.dialingtest.model.CreateOperationLogRequest;
import com.huawei.cloududn.dialingtest.service.OperationLogService;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 操作记录工具类测试
 *
 * @author g00940940
 * @since 2025-09-19
 */
@RunWith(MockitoJUnitRunner.class)
public class OperationLogUtilTest {

    @Mock
    private OperationLogService operationLogService;

    @InjectMocks
    private OperationLogUtil operationLogUtil;

    @Before
    public void setUp() {
        // Mock successful service calls by default
        doNothing().when(operationLogService).createOperationLog(any(CreateOperationLogRequest.class));
    }

    @Test
    public void testLogUserCreate_Success_CallsServiceWithCorrectParameters() {
        // Arrange
        String operatorUsername = "admin";
        String targetUsername = "newuser";
        String userDetails = "用户名:newuser, 密码:已设置";

        // Act
        operationLogUtil.logUserCreate(operatorUsername, targetUsername, userDetails);

        // Assert
        verify(operationLogService).createOperationLog(argThat(request -> {
            return "admin".equals(request.getUsername()) &&
                   "CREATE".equals(request.getOperationType()) &&
                   "USER".equals(request.getOperationTarget()) &&
                   request.getOperationDescriptionZh().contains("创建用户: newuser") &&
                   request.getOperationDescriptionZh().contains("详细信息: " + userDetails) &&
                   request.getOperationDescriptionEn().contains("Create user: newuser") &&
                   request.getOperationDescriptionEn().contains("details: " + userDetails);
        }));
    }

    @Test
    public void testLogUserCreate_SimplifiedVersion_CallsServiceWithDefaultDetails() {
        // Arrange
        String operatorUsername = "admin";
        String targetUsername = "newuser";

        // Act
        operationLogUtil.logUserCreate(operatorUsername, targetUsername);

        // Assert
        verify(operationLogService).createOperationLog(argThat(request -> {
            return "admin".equals(request.getUsername()) &&
                   "CREATE".equals(request.getOperationType()) &&
                   "USER".equals(request.getOperationTarget()) &&
                   request.getOperationDescriptionZh().contains("创建用户: newuser") &&
                   request.getOperationDescriptionZh().contains("详细信息: 新用户") &&
                   request.getOperationDescriptionEn().contains("Create user: newuser") &&
                   request.getOperationDescriptionEn().contains("details: 新用户");
        }));
    }

    @Test
    public void testLogUserUpdate_Success_CallsServiceWithCorrectParameters() {
        // Arrange
        String operatorUsername = "admin";
        String targetUsername = "user1";
        String oldValues = "用户名:olduser";
        String newValues = "用户名:newuser";

        // Act
        operationLogUtil.logUserUpdate(operatorUsername, targetUsername, oldValues, newValues);

        // Assert
        verify(operationLogService).createOperationLog(argThat(request -> {
            return "admin".equals(request.getUsername()) &&
                   "UPDATE".equals(request.getOperationType()) &&
                   "USER".equals(request.getOperationTarget()) &&
                   request.getOperationDescriptionZh().contains("更新用户: user1") &&
                   request.getOperationDescriptionZh().contains("由 " + oldValues + " 更改为 " + newValues) &&
                   request.getOperationDescriptionEn().contains("Update user: user1") &&
                   request.getOperationDescriptionEn().contains("changed from " + oldValues + " to " + newValues);
        }));
    }

    @Test
    public void testLogUserUpdate_SimplifiedVersion_CallsServiceWithDefaultValues() {
        // Arrange
        String operatorUsername = "admin";
        String targetUsername = "user1";

        // Act
        operationLogUtil.logUserUpdate(operatorUsername, targetUsername);

        // Assert
        verify(operationLogService).createOperationLog(argThat(request -> {
            return "admin".equals(request.getUsername()) &&
                   "UPDATE".equals(request.getOperationType()) &&
                   "USER".equals(request.getOperationTarget()) &&
                   request.getOperationDescriptionZh().contains("更新用户: user1") &&
                   request.getOperationDescriptionZh().contains("由 原值 更改为 新值") &&
                   request.getOperationDescriptionEn().contains("Update user: user1") &&
                   request.getOperationDescriptionEn().contains("changed from 原值 to 新值");
        }));
    }

    @Test
    public void testLogUserDelete_Success_CallsServiceWithCorrectParameters() {
        // Arrange
        String operatorUsername = "admin";
        String targetUsername = "user1";
        String userInfo = "用户名:user1, 最后登录:2025-09-19T10:00:00Z";

        // Act
        operationLogUtil.logUserDelete(operatorUsername, targetUsername, userInfo);

        // Assert
        verify(operationLogService).createOperationLog(argThat(request -> {
            return "admin".equals(request.getUsername()) &&
                   "DELETE".equals(request.getOperationType()) &&
                   "USER".equals(request.getOperationTarget()) &&
                   request.getOperationDescriptionZh().contains("删除用户: user1") &&
                   request.getOperationDescriptionZh().contains("用户信息: " + userInfo) &&
                   request.getOperationDescriptionEn().contains("Delete user: user1") &&
                   request.getOperationDescriptionEn().contains("user info: " + userInfo);
        }));
    }

    @Test
    public void testLogUserDelete_SimplifiedVersion_CallsServiceWithDefaultInfo() {
        // Arrange
        String operatorUsername = "admin";
        String targetUsername = "user1";

        // Act
        operationLogUtil.logUserDelete(operatorUsername, targetUsername);

        // Assert
        verify(operationLogService).createOperationLog(argThat(request -> {
            return "admin".equals(request.getUsername()) &&
                   "DELETE".equals(request.getOperationType()) &&
                   "USER".equals(request.getOperationTarget()) &&
                   request.getOperationDescriptionZh().contains("删除用户: user1") &&
                   request.getOperationDescriptionZh().contains("用户信息: 用户已删除") &&
                   request.getOperationDescriptionEn().contains("Delete user: user1") &&
                   request.getOperationDescriptionEn().contains("user info: 用户已删除");
        }));
    }

    @Test
    public void testLogUserView_Success_CallsServiceWithCorrectParameters() {
        // Arrange
        String operatorUsername = "admin";
        String targetUsername = "user1";

        // Act
        operationLogUtil.logUserView(operatorUsername, targetUsername);

        // Assert
        verify(operationLogService).createOperationLog(argThat(request -> {
            return "admin".equals(request.getUsername()) &&
                   "VIEW".equals(request.getOperationType()) &&
                   "USER".equals(request.getOperationTarget()) &&
                   request.getOperationDescriptionZh().contains("查看用户: user1") &&
                   request.getOperationDescriptionEn().contains("View user: user1");
        }));
    }

    @Test
    public void testLogUserLogin_Success_CallsServiceWithCorrectParameters() {
        // Arrange
        String operatorUsername = "user1";

        // Act
        operationLogUtil.logUserLogin(operatorUsername);

        // Assert
        verify(operationLogService).createOperationLog(argThat(request -> {
            return "user1".equals(request.getUsername()) &&
                   "LOGIN".equals(request.getOperationType()) &&
                   "USER".equals(request.getOperationTarget()) &&
                   request.getOperationDescriptionZh().contains("用户登录: user1") &&
                   request.getOperationDescriptionEn().contains("User login: user1");
        }));
    }

    @Test
    public void testLogUserLogout_Success_CallsServiceWithCorrectParameters() {
        // Arrange
        String operatorUsername = "user1";

        // Act
        operationLogUtil.logUserLogout(operatorUsername);

        // Assert
        verify(operationLogService).createOperationLog(argThat(request -> {
            return "user1".equals(request.getUsername()) &&
                   "LOGOUT".equals(request.getOperationType()) &&
                   "USER".equals(request.getOperationTarget()) &&
                   request.getOperationDescriptionZh().contains("用户登出: user1") &&
                   request.getOperationDescriptionEn().contains("User logout: user1");
        }));
    }

    @Test
    public void testLogOperation_Success_CallsServiceWithCorrectParameters() {
        // Arrange
        String operatorUsername = "admin";
        String operationType = "CUSTOM";
        String operationTarget = "SYSTEM";
        String descriptionZh = "自定义操作";
        String descriptionEn = "Custom operation";

        // Act
        operationLogUtil.logOperation(operatorUsername, operationType, operationTarget, descriptionZh, descriptionEn);

        // Assert
        verify(operationLogService).createOperationLog(argThat(request -> {
            return "admin".equals(request.getUsername()) &&
                   "CUSTOM".equals(request.getOperationType()) &&
                   "SYSTEM".equals(request.getOperationTarget()) &&
                   descriptionZh.equals(request.getOperationDescriptionZh()) &&
                   descriptionEn.equals(request.getOperationDescriptionEn());
        }));
    }

    @Test
    public void testLogUserCreate_ServiceException_HandlesExceptionGracefully() {
        // Arrange
        doThrow(new RuntimeException("Service error")).when(operationLogService).createOperationLog(any(CreateOperationLogRequest.class));
        String operatorUsername = "admin";
        String targetUsername = "newuser";

        // Act
        operationLogUtil.logUserCreate(operatorUsername, targetUsername);

        // Assert
        verify(operationLogService).createOperationLog(any(CreateOperationLogRequest.class));
        // Should not throw exception, just log warning
    }

    @Test
    public void testLogUserUpdate_ServiceException_HandlesExceptionGracefully() {
        // Arrange
        doThrow(new RuntimeException("Service error")).when(operationLogService).createOperationLog(any(CreateOperationLogRequest.class));
        String operatorUsername = "admin";
        String targetUsername = "user1";

        // Act
        operationLogUtil.logUserUpdate(operatorUsername, targetUsername);

        // Assert
        verify(operationLogService).createOperationLog(any(CreateOperationLogRequest.class));
        // Should not throw exception, just log warning
    }
}
