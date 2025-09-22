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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 用户角色操作记录工具类测试
 *
 * @author g00940940
 * @since 2025-01-27
 */
@RunWith(MockitoJUnitRunner.class)
public class UserRoleOperationLogUtilTest {

    @Mock
    private OperationLogService operationLogService;

    @InjectMocks
    private UserRoleOperationLogUtil userRoleOperationLogUtil;

    private OperationLogResponse successResponse;

    @Before
    public void setUp() {
        // 初始化测试数据
        successResponse = new OperationLogResponse();
        successResponse.setSuccess(true);
        successResponse.setMessage("创建成功");
    }

    @Test
    public void testLogUserRoleCreate_Success_LogsOperation() {
        // Arrange
        when(operationLogService.createOperationLog(any(CreateOperationLogRequest.class))).thenReturn(successResponse);

        // Act
        userRoleOperationLogUtil.logUserRoleCreate("admin", "testuser", "ADMIN");

        // Assert
        verify(operationLogService).createOperationLog(argThat(request -> {
            return "admin".equals(request.getUsername()) &&
                   "CREATE".equals(request.getOperationType()) &&
                   "USER_ROLE".equals(request.getOperationTarget()) &&
                   ((String) request.getOperationDescriptionZh()).contains("创建用户角色: testuser -> ADMIN") &&
                   ((String) request.getOperationDescriptionEn()).contains("Create user role: testuser -> ADMIN") &&
                   ((String) request.getOperationData()).contains("\"targetUsername\": \"testuser\"") &&
                   ((String) request.getOperationData()).contains("\"role\": \"ADMIN\"");
        }));
    }

    @Test
    public void testLogUserRoleCreate_ServiceException_LogsWarning() {
        // Arrange
        when(operationLogService.createOperationLog(any(CreateOperationLogRequest.class)))
            .thenThrow(new RuntimeException("Database error"));

        // Act
        userRoleOperationLogUtil.logUserRoleCreate("admin", "testuser", "ADMIN");

        // Assert
        verify(operationLogService).createOperationLog(any(CreateOperationLogRequest.class));
        // 验证没有抛出异常，说明异常被捕获并处理了
    }

    @Test
    public void testLogUserRoleUpdate_Success_LogsOperation() {
        // Arrange
        when(operationLogService.createOperationLog(any(CreateOperationLogRequest.class))).thenReturn(successResponse);

        // Act
        userRoleOperationLogUtil.logUserRoleUpdate("admin", "testuser", "testuser", "OPERATOR", "ADMIN");

        // Assert
        verify(operationLogService).createOperationLog(argThat(request -> {
            return "admin".equals(request.getUsername()) &&
                   "UPDATE".equals(request.getOperationType()) &&
                   "USER_ROLE".equals(request.getOperationTarget()) &&
                   ((String) request.getOperationDescriptionZh()).contains("更新角色: OPERATOR -> ADMIN") &&
                   ((String) request.getOperationDescriptionEn()).contains("Update role: OPERATOR -> ADMIN") &&
                   ((String) request.getOperationData()).contains("\"oldRole\": \"OPERATOR\"") &&
                   ((String) request.getOperationData()).contains("\"newRole\": \"ADMIN\"");
        }));
    }

    @Test
    public void testLogUserRoleUpdate_ServiceException_LogsWarning() {
        // Arrange
        when(operationLogService.createOperationLog(any(CreateOperationLogRequest.class)))
            .thenThrow(new RuntimeException("Database error"));

        // Act
        userRoleOperationLogUtil.logUserRoleUpdate("admin", "testuser", "testuser", "OPERATOR", "ADMIN");

        // Assert
        verify(operationLogService).createOperationLog(any(CreateOperationLogRequest.class));
        // 验证没有抛出异常，说明异常被捕获并处理了
    }

    @Test
    public void testLogUserRoleDelete_Success_LogsOperation() {
        // Arrange
        when(operationLogService.createOperationLog(any(CreateOperationLogRequest.class))).thenReturn(successResponse);

        // Act
        userRoleOperationLogUtil.logUserRoleDelete("admin", "testuser", "ADMIN");

        // Assert
        verify(operationLogService).createOperationLog(argThat(request -> {
            return "admin".equals(request.getUsername()) &&
                   "DELETE".equals(request.getOperationType()) &&
                   "USER_ROLE".equals(request.getOperationTarget()) &&
                   ((String) request.getOperationDescriptionZh()).contains("删除用户角色: testuser -> ADMIN") &&
                   ((String) request.getOperationDescriptionEn()).contains("Delete user role: testuser -> ADMIN") &&
                   ((String) request.getOperationData()).contains("\"targetUsername\": \"testuser\"") &&
                   ((String) request.getOperationData()).contains("\"role\": \"ADMIN\"");
        }));
    }

    @Test
    public void testLogUserRoleDelete_ServiceException_LogsWarning() {
        // Arrange
        when(operationLogService.createOperationLog(any(CreateOperationLogRequest.class)))
            .thenThrow(new RuntimeException("Database error"));

        // Act
        userRoleOperationLogUtil.logUserRoleDelete("admin", "testuser", "ADMIN");

        // Assert
        verify(operationLogService).createOperationLog(any(CreateOperationLogRequest.class));
        // 验证没有抛出异常，说明异常被捕获并处理了
    }

    @Test
    public void testLogUserRoleCreate_WithSpecialCharacters_HandlesCorrectly() {
        // Arrange
        when(operationLogService.createOperationLog(any(CreateOperationLogRequest.class))).thenReturn(successResponse);

        // Act
        userRoleOperationLogUtil.logUserRoleCreate("admin", "test@user", "OPERATOR");

        // Assert
        verify(operationLogService).createOperationLog(argThat(request -> {
            return ((String) request.getOperationDescriptionZh()).contains("test@user") &&
                   ((String) request.getOperationDescriptionEn()).contains("test@user") &&
                   ((String) request.getOperationData()).contains("\"targetUsername\": \"test@user\"");
        }));
    }

    @Test
    public void testLogUserRoleUpdate_WithEmptyValues_HandlesCorrectly() {
        // Arrange
        when(operationLogService.createOperationLog(any(CreateOperationLogRequest.class))).thenReturn(successResponse);

        // Act
        userRoleOperationLogUtil.logUserRoleUpdate("admin", "", "", "", "");

        // Assert
        verify(operationLogService).createOperationLog(argThat(request -> {
            return ((String) request.getOperationDescriptionZh()).contains("更新用户角色:  (无变化)") &&
                   ((String) request.getOperationDescriptionEn()).contains("Update user role:  (no changes)");
        }));
    }

    @Test
    public void testLogUserRoleDelete_WithNullValues_HandlesCorrectly() {
        // Arrange
        when(operationLogService.createOperationLog(any(CreateOperationLogRequest.class))).thenReturn(successResponse);

        // Act
        userRoleOperationLogUtil.logUserRoleDelete("admin", null, null);

        // Assert
        verify(operationLogService).createOperationLog(argThat(request -> {
            return ((String) request.getOperationDescriptionZh()).contains("删除用户角色: null -> null") &&
                   ((String) request.getOperationDescriptionEn()).contains("Delete user role: null -> null");
        }));
    }

    @Test
    public void testLogUserRoleCreate_OperationDataFormat_IsValidJson() {
        // Arrange
        when(operationLogService.createOperationLog(any(CreateOperationLogRequest.class))).thenReturn(successResponse);

        // Act
        userRoleOperationLogUtil.logUserRoleCreate("admin", "testuser", "ADMIN");

        // Assert
        verify(operationLogService).createOperationLog(argThat(request -> {
            String operationData = (String) request.getOperationData();
            return operationData.contains("\"targetUsername\": \"testuser\"") &&
                   operationData.contains("\"role\": \"ADMIN\"") &&
                   operationData.startsWith("{") &&
                   operationData.endsWith("}");
        }));
    }

    @Test
    public void testLogUserRoleUpdate_OperationDataFormat_IsValidJson() {
        // Arrange
        when(operationLogService.createOperationLog(any(CreateOperationLogRequest.class))).thenReturn(successResponse);

        // Act
        userRoleOperationLogUtil.logUserRoleUpdate("admin", "testuser", "testuser", "OPERATOR", "ADMIN");

        // Assert
        verify(operationLogService).createOperationLog(argThat(request -> {
            String operationData = (String) request.getOperationData();
            return operationData.contains("\"targetUsername\": \"testuser\"") &&
                   operationData.contains("\"oldRole\": \"OPERATOR\"") &&
                   operationData.contains("\"newRole\": \"ADMIN\"") &&
                   operationData.startsWith("{") &&
                   operationData.endsWith("}");
        }));
    }

    @Test
    public void testLogUserRoleDelete_OperationDataFormat_IsValidJson() {
        // Arrange
        when(operationLogService.createOperationLog(any(CreateOperationLogRequest.class))).thenReturn(successResponse);

        // Act
        userRoleOperationLogUtil.logUserRoleDelete("admin", "testuser", "ADMIN");

        // Assert
        verify(operationLogService).createOperationLog(argThat(request -> {
            String operationData = (String) request.getOperationData();
            return operationData.contains("\"targetUsername\": \"testuser\"") &&
                   operationData.contains("\"role\": \"ADMIN\"") &&
                   operationData.startsWith("{") &&
                   operationData.endsWith("}");
        }));
    }

    @Test
    public void testLogUserRoleUpdate_UsernameChange_LogsCorrectly() {
        // Arrange
        when(operationLogService.createOperationLog(any(CreateOperationLogRequest.class))).thenReturn(successResponse);

        // Act
        userRoleOperationLogUtil.logUserRoleUpdate("admin", "olduser", "newuser", "ADMIN", "ADMIN");

        // Assert
        verify(operationLogService).createOperationLog(argThat(request -> {
            return "admin".equals(request.getUsername()) &&
                   "UPDATE".equals(request.getOperationType()) &&
                   "USER_ROLE".equals(request.getOperationTarget()) &&
                   ((String) request.getOperationDescriptionZh()).contains("更新用户名: olduser -> newuser") &&
                   ((String) request.getOperationDescriptionEn()).contains("Update username: olduser -> newuser") &&
                   ((String) request.getOperationData()).contains("\"oldUsername\": \"olduser\"") &&
                   ((String) request.getOperationData()).contains("\"newUsername\": \"newuser\"");
        }));
    }

    @Test
    public void testLogUserRoleUpdate_BothUsernameAndRoleChange_LogsCorrectly() {
        // Arrange
        when(operationLogService.createOperationLog(any(CreateOperationLogRequest.class))).thenReturn(successResponse);

        // Act
        userRoleOperationLogUtil.logUserRoleUpdate("admin", "olduser", "newuser", "OPERATOR", "ADMIN");

        // Assert
        verify(operationLogService).createOperationLog(argThat(request -> {
            return "admin".equals(request.getUsername()) &&
                   "UPDATE".equals(request.getOperationType()) &&
                   "USER_ROLE".equals(request.getOperationTarget()) &&
                   ((String) request.getOperationDescriptionZh()).contains("更新用户名: olduser -> newuser") &&
                   ((String) request.getOperationDescriptionZh()).contains("更新角色: OPERATOR -> ADMIN") &&
                   ((String) request.getOperationDescriptionEn()).contains("Update username: olduser -> newuser") &&
                   ((String) request.getOperationDescriptionEn()).contains("Update role: OPERATOR -> ADMIN") &&
                   ((String) request.getOperationData()).contains("\"oldUsername\": \"olduser\"") &&
                   ((String) request.getOperationData()).contains("\"newUsername\": \"newuser\"") &&
                   ((String) request.getOperationData()).contains("\"oldRole\": \"OPERATOR\"") &&
                   ((String) request.getOperationData()).contains("\"newRole\": \"ADMIN\"");
        }));
    }

    @Test
    public void testLogUserRoleUpdate_NoChanges_LogsCorrectly() {
        // Arrange
        when(operationLogService.createOperationLog(any(CreateOperationLogRequest.class))).thenReturn(successResponse);

        // Act
        userRoleOperationLogUtil.logUserRoleUpdate("admin", "testuser", "testuser", "ADMIN", "ADMIN");

        // Assert
        verify(operationLogService).createOperationLog(argThat(request -> {
            return "admin".equals(request.getUsername()) &&
                   "UPDATE".equals(request.getOperationType()) &&
                   "USER_ROLE".equals(request.getOperationTarget()) &&
                   ((String) request.getOperationDescriptionZh()).contains("更新用户角色: testuser (无变化)") &&
                   ((String) request.getOperationDescriptionEn()).contains("Update user role: testuser (no changes)") &&
                   ((String) request.getOperationData()).contains("\"username\": \"testuser\"") &&
                   ((String) request.getOperationData()).contains("\"role\": \"ADMIN\"") &&
                   ((String) request.getOperationData()).contains("\"changes\": \"none\"");
        }));
    }

    @Test
    public void testLogUserRoleUpdate_RealScenario_UsernameChange() {
        // Arrange - 模拟您的实际场景：用户名从 ghl_test 改为 ghl_test123，角色保持 ADMIN
        when(operationLogService.createOperationLog(any(CreateOperationLogRequest.class))).thenReturn(successResponse);

        // Act
        userRoleOperationLogUtil.logUserRoleUpdate("admin", "ghl_test", "ghl_test123", "ADMIN", "ADMIN");

        // Assert
        verify(operationLogService).createOperationLog(argThat(request -> {
            String descriptionZh = (String) request.getOperationDescriptionZh();
            String descriptionEn = (String) request.getOperationDescriptionEn();
            String operationData = (String) request.getOperationData();
            
            // 验证中文描述包含用户名变化
            boolean zhCorrect = descriptionZh.contains("更新用户名: ghl_test -> ghl_test123");
            // 验证英文描述包含用户名变化
            boolean enCorrect = descriptionEn.contains("Update username: ghl_test -> ghl_test123");
            // 验证操作数据包含用户名变化
            boolean dataCorrect = operationData.contains("\"oldUsername\": \"ghl_test\"") && 
                                operationData.contains("\"newUsername\": \"ghl_test123\"");
            
            return zhCorrect && enCorrect && dataCorrect;
        }));
    }
}
