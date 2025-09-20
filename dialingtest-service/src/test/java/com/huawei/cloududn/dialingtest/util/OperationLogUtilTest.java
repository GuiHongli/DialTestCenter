/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.cloududn.dialingtest.util;

import com.huawei.cloududn.dialingtest.model.CreateOperationLogRequest;
import com.huawei.cloududn.dialingtest.model.OperationLogResponse;
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
 * @since 2025-01-15
 */
@RunWith(MockitoJUnitRunner.class)
public class OperationLogUtilTest {

    @Mock
    private OperationLogService operationLogService;

    @InjectMocks
    private OperationLogUtil operationLogUtil;

    @Before
    public void setUp() {
        OperationLogResponse mockResponse = new OperationLogResponse();
        mockResponse.setSuccess(true);
        mockResponse.setMessage("操作记录创建成功");
        when(operationLogService.createOperationLog(any(CreateOperationLogRequest.class))).thenReturn(mockResponse);
    }

    /**
     * 测试用户创建操作记录 - 成功场景
     *
     * @throws Exception 测试异常
     */
    @Test
    public void testLogUserCreate_Success_CallsServiceWithCorrectParameters() throws Exception {
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

    /**
     * 测试用户创建操作记录 - 简化版本
     *
     * @throws Exception 测试异常
     */
    @Test
    public void testLogUserCreate_SimplifiedVersion_CallsServiceWithDefaultDetails() throws Exception {
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

    /**
     * 测试用户更新操作记录 - 成功场景
     *
     * @throws Exception 测试异常
     */
    @Test
    public void testLogUserUpdate_Success_CallsServiceWithCorrectParameters() throws Exception {
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

    /**
     * 测试用户更新操作记录 - 简化版本
     *
     * @throws Exception 测试异常
     */
    @Test
    public void testLogUserUpdate_SimplifiedVersion_CallsServiceWithDefaultValues() throws Exception {
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

    /**
     * 测试用户删除操作记录 - 成功场景
     *
     * @throws Exception 测试异常
     */
    @Test
    public void testLogUserDelete_Success_CallsServiceWithCorrectParameters() throws Exception {
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

    /**
     * 测试用户删除操作记录 - 简化版本
     *
     * @throws Exception 测试异常
     */
    @Test
    public void testLogUserDelete_SimplifiedVersion_CallsServiceWithDefaultInfo() throws Exception {
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

    /**
     * 测试用户查看操作记录 - 成功场景
     *
     * @throws Exception 测试异常
     */
    @Test
    public void testLogUserView_Success_CallsServiceWithCorrectParameters() throws Exception {
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

    /**
     * 测试用户登录操作记录 - 成功场景
     *
     * @throws Exception 测试异常
     */
    @Test
    public void testLogUserLogin_Success_CallsServiceWithCorrectParameters() throws Exception {
        // Arrange
        String operatorUsername = "user1";

        // Act
        operationLogUtil.logUserLogin(operatorUsername);

        // Assert
        verify(operationLogService).createOperationLog(argThat(request -> {
            return "user1".equals(request.getUsername()) &&
                   "LOGIN".equals(request.getOperationType()) &&
                   "SYSTEM".equals(request.getOperationTarget()) &&
                   request.getOperationDescriptionZh().contains("用户登录: user1") &&
                   request.getOperationDescriptionEn().contains("User login: user1");
        }));
    }

    /**
     * 测试用户登出操作记录 - 成功场景
     *
     * @throws Exception 测试异常
     */
    @Test
    public void testLogUserLogout_Success_CallsServiceWithCorrectParameters() throws Exception {
        // Arrange
        String operatorUsername = "user1";

        // Act
        operationLogUtil.logUserLogout(operatorUsername);

        // Assert
        verify(operationLogService).createOperationLog(argThat(request -> {
            return "user1".equals(request.getUsername()) &&
                   "LOGOUT".equals(request.getOperationType()) &&
                   "SYSTEM".equals(request.getOperationTarget()) &&
                   request.getOperationDescriptionZh().contains("用户登出: user1") &&
                   request.getOperationDescriptionEn().contains("User logout: user1");
        }));
    }

    /**
     * 测试通用操作记录 - 成功场景
     *
     * @throws Exception 测试异常
     */
    @Test
    public void testLogOperation_Success_CallsServiceWithCorrectParameters() throws Exception {
        // Arrange
        String operatorUsername = "admin";
        String operationType = "CUSTOM";
        String operationTarget = "SYSTEM";
        String descriptionZh = "自定义操作";
        String descriptionEn = "Custom operation";
        String operationData = "操作数据";

        // Act
        operationLogUtil.logOperation(operatorUsername, operationType, operationTarget, 
                                    descriptionZh, descriptionEn, operationData);

        // Assert
        verify(operationLogService).createOperationLog(argThat(request -> {
            return "admin".equals(request.getUsername()) &&
                   "CUSTOM".equals(request.getOperationType()) &&
                   "SYSTEM".equals(request.getOperationTarget()) &&
                   descriptionZh.equals(request.getOperationDescriptionZh()) &&
                   descriptionEn.equals(request.getOperationDescriptionEn()) &&
                   operationData.equals(request.getOperationData());
        }));
    }

    /**
     * 测试用户创建操作记录 - 服务异常处理
     *
     * @throws Exception 测试异常
     */
    @Test
    public void testLogUserCreate_ServiceException_HandlesExceptionGracefully() throws Exception {
        // Arrange
        doThrow(new RuntimeException("Service error"))
            .when(operationLogService).createOperationLog(any(CreateOperationLogRequest.class));
        String operatorUsername = "admin";
        String targetUsername = "newuser";

        // Act
        operationLogUtil.logUserCreate(operatorUsername, targetUsername);

        // Assert
        verify(operationLogService).createOperationLog(any(CreateOperationLogRequest.class));
    }

    /**
     * 测试用户更新操作记录 - 服务异常处理
     *
     * @throws Exception 测试异常
     */
    @Test
    public void testLogUserUpdate_ServiceException_HandlesExceptionGracefully() throws Exception {
        // Arrange
        doThrow(new RuntimeException("Service error"))
            .when(operationLogService).createOperationLog(any(CreateOperationLogRequest.class));
        String operatorUsername = "admin";
        String targetUsername = "user1";

        // Act
        operationLogUtil.logUserUpdate(operatorUsername, targetUsername);

        // Assert
        verify(operationLogService).createOperationLog(any(CreateOperationLogRequest.class));
    }
}
