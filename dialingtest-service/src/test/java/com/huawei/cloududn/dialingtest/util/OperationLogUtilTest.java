/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.cloududn.dialingtest.util;

import com.huawei.cloududn.dialingtest.model.CreateOperationLogRequest;
import com.huawei.cloududn.dialingtest.model.DialUser;
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
        DialUser userDetails = new DialUser();
        userDetails.setUsername(targetUsername);
        userDetails.setPassword("password");

        // Act
        operationLogUtil.logUserCreate(operatorUsername, targetUsername, userDetails);

        // Assert
        verify(operationLogService).createOperationLog(argThat(request -> {
            return "admin".equals(request.getUsername()) &&
                   "CREATE".equals(request.getOperationType()) &&
                   "USER".equals(request.getOperationTarget()) &&
                   request.getOperationDescriptionZh().contains("创建用户: newuser") &&
                   request.getOperationDescriptionEn().contains("Create user: newuser");
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
        DialUser userDetails = new DialUser();
        userDetails.setUsername(targetUsername);

        // Act
        operationLogUtil.logUserCreate(operatorUsername, targetUsername, userDetails);

        // Assert
        verify(operationLogService).createOperationLog(argThat(request -> {
            return "admin".equals(request.getUsername()) &&
                   "CREATE".equals(request.getOperationType()) &&
                   "USER".equals(request.getOperationTarget()) &&
                   request.getOperationDescriptionZh().contains("创建用户: newuser") &&
                   request.getOperationDescriptionEn().contains("Create user: newuser");
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
        DialUser oldValues = new DialUser();
        oldValues.setUsername("olduser");
        oldValues.setPassword("oldpassword");
        DialUser newValues = new DialUser();
        newValues.setUsername("newuser");
        newValues.setPassword("newpassword");

        // Act
        operationLogUtil.logUserUpdate(operatorUsername, targetUsername, oldValues, newValues);

        // Assert
        verify(operationLogService).createOperationLog(argThat(request -> {
            return "admin".equals(request.getUsername()) &&
                   "UPDATE".equals(request.getOperationType()) &&
                   "USER".equals(request.getOperationTarget()) &&
                   request.getOperationDescriptionZh().contains("更新用户: user1") &&
                   request.getOperationDescriptionEn().contains("Update user: user1");
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
        DialUser userInfo = new DialUser();
        userInfo.setUsername(targetUsername);
        userInfo.setPassword("password");
        userInfo.setLastLoginTime("2025-09-19T10:00:00Z");

        // Act
        operationLogUtil.logUserDelete(operatorUsername, targetUsername, userInfo);

        // Assert
        verify(operationLogService).createOperationLog(argThat(request -> {
            return "admin".equals(request.getUsername()) &&
                   "DELETE".equals(request.getOperationType()) &&
                   "USER".equals(request.getOperationTarget()) &&
                   request.getOperationDescriptionZh().contains("删除用户: user1") &&
                   request.getOperationDescriptionEn().contains("Delete user: user1");
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
        DialUser userInfo = new DialUser();
        userInfo.setUsername(targetUsername);

        // Act
        operationLogUtil.logUserDelete(operatorUsername, targetUsername, userInfo);

        // Assert
        verify(operationLogService).createOperationLog(argThat(request -> {
            return "admin".equals(request.getUsername()) &&
                   "DELETE".equals(request.getOperationType()) &&
                   "USER".equals(request.getOperationTarget()) &&
                   request.getOperationDescriptionZh().contains("删除用户: user1") &&
                   request.getOperationDescriptionEn().contains("Delete user: user1");
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

}
