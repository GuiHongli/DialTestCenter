/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.dialtest.center.controller;

import com.huawei.dialtest.center.entity.OperationLog;
import com.huawei.dialtest.center.service.OperationLogService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * OperationLogController测试类
 *
 * @author g00940940
 * @since 2025-09-09
 */
@RunWith(MockitoJUnitRunner.class)
public class OperationLogControllerTest {

    @Mock
    private OperationLogService operationLogService;

    @InjectMocks
    private OperationLogController operationLogController;

    private OperationLog testOperationLog;

    @Before
    public void setUp() {
        testOperationLog = new OperationLog();
        testOperationLog.setId(1L);
        testOperationLog.setUsername("testuser");
        testOperationLog.setOperationTime("2025-09-16 10:30:00");
        testOperationLog.setOperationType("CREATE");
        testOperationLog.setTarget("用户管理");
        testOperationLog.setDescription("创建新用户");
    }


    @Test
    public void testGetOperationLogById_Success() {
        // Arrange
        Long id = 1L;
        when(operationLogService.getOperationLogById(id)).thenReturn(Optional.of(testOperationLog));

        // Act
        ResponseEntity<?> response = operationLogController.getOperationLogById(id);

        // Assert
        assertEquals("Status should be OK", HttpStatus.OK, response.getStatusCode());
        assertNotNull("Response body should not be null", response.getBody());

        verify(operationLogService).getOperationLogById(id);
    }

    @Test
    public void testGetOperationLogById_NotFound() {
        // Arrange
        Long id = 999L;
        when(operationLogService.getOperationLogById(id)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<?> response = operationLogController.getOperationLogById(id);

        // Assert
        assertEquals("Status should be 404", HttpStatus.NOT_FOUND, response.getStatusCode());

        verify(operationLogService).getOperationLogById(id);
    }

    @Test
    public void testGetOperationLogById_Error() {
        // Arrange
        Long id = 1L;
        when(operationLogService.getOperationLogById(id))
                .thenThrow(new RuntimeException("Service error"));

        // Act
        ResponseEntity<?> response = operationLogController.getOperationLogById(id);

        // Assert
        assertEquals("Status should be 500", HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());

        verify(operationLogService).getOperationLogById(id);
    }




    @Test
    public void testGetRecentOperationLogs_Success() {
        // Arrange
        List<OperationLog> operationLogs = Arrays.asList(testOperationLog);
        when(operationLogService.getRecentOperationLogs(10)).thenReturn(operationLogs);

        // Act
        ResponseEntity<?> response = operationLogController.getRecentOperationLogs(10);

        // Assert
        assertEquals("Status should be OK", HttpStatus.OK, response.getStatusCode());
        assertNotNull("Response body should not be null", response.getBody());

        verify(operationLogService).getRecentOperationLogs(10);
    }

    @Test
    public void testGetRecentOperationLogs_Error() {
        // Arrange
        when(operationLogService.getRecentOperationLogs(10))
                .thenThrow(new RuntimeException("Service error"));

        // Act
        ResponseEntity<?> response = operationLogController.getRecentOperationLogs(10);

        // Assert
        assertEquals("Status should be 500", HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());

        verify(operationLogService).getRecentOperationLogs(10);
    }

    @Test
    public void testLogOperation_Success() {
        // Arrange
        Map<String, String> request = new HashMap<>();
        request.put("username", "testuser");
        request.put("operationType", "CREATE");
        request.put("target", "用户管理");
        request.put("description", "创建新用户");

        when(operationLogService.logOperation("testuser", "CREATE", "用户管理", "创建新用户"))
                .thenReturn(testOperationLog);

        // Act
        ResponseEntity<?> response = operationLogController.logOperation(request);

        // Assert
        assertEquals("Status should be OK", HttpStatus.OK, response.getStatusCode());
        assertNotNull("Response body should not be null", response.getBody());

        verify(operationLogService).logOperation("testuser", "CREATE", "用户管理", "创建新用户");
    }

    @Test
    public void testLogOperation_MissingUsername() {
        // Arrange
        Map<String, String> request = new HashMap<>();
        request.put("operationType", "CREATE");
        request.put("target", "用户管理");

        // Act
        ResponseEntity<?> response = operationLogController.logOperation(request);

        // Assert
        assertEquals("Status should be 400", HttpStatus.BAD_REQUEST, response.getStatusCode());

        verify(operationLogService, never()).logOperation(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    public void testLogOperation_MissingOperationType() {
        // Arrange
        Map<String, String> request = new HashMap<>();
        request.put("username", "testuser");
        request.put("target", "用户管理");

        // Act
        ResponseEntity<?> response = operationLogController.logOperation(request);

        // Assert
        assertEquals("Status should be 400", HttpStatus.BAD_REQUEST, response.getStatusCode());

        verify(operationLogService, never()).logOperation(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    public void testLogOperation_MissingTarget() {
        // Arrange
        Map<String, String> request = new HashMap<>();
        request.put("username", "testuser");
        request.put("operationType", "CREATE");

        // Act
        ResponseEntity<?> response = operationLogController.logOperation(request);

        // Assert
        assertEquals("Status should be 400", HttpStatus.BAD_REQUEST, response.getStatusCode());

        verify(operationLogService, never()).logOperation(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    public void testLogOperation_Error() {
        // Arrange
        Map<String, String> request = new HashMap<>();
        request.put("username", "testuser");
        request.put("operationType", "CREATE");
        request.put("target", "用户管理");
        request.put("description", "创建新用户");

        when(operationLogService.logOperation("testuser", "CREATE", "用户管理", "创建新用户"))
                .thenThrow(new RuntimeException("Service error"));

        // Act
        ResponseEntity<?> response = operationLogController.logOperation(request);

        // Assert
        assertEquals("Status should be 500", HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());

        verify(operationLogService).logOperation("testuser", "CREATE", "用户管理", "创建新用户");
    }


}
