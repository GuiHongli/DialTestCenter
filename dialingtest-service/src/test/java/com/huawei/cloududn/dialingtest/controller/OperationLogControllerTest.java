/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.cloududn.dialingtest.controller;

import com.huawei.cloududn.dialingtest.model.CreateOperationLogRequest;
import com.huawei.cloududn.dialingtest.model.OperationLog;
import com.huawei.cloududn.dialingtest.model.OperationLogPageResponse;
import com.huawei.cloududn.dialingtest.model.OperationLogStatistics;
import com.huawei.cloududn.dialingtest.service.OperationLogService;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 操作记录控制器测试类
 *
 * @author g00940940
 * @since 2025-09-19
 */
@RunWith(MockitoJUnitRunner.class)
public class OperationLogControllerTest {

    @Mock
    private OperationLogService operationLogService;

    @InjectMocks
    private OperationLogController operationLogController;

    private OperationLog testOperationLog;
    private CreateOperationLogRequest testRequest;

    @Before
    public void setUp() {
        testOperationLog = new OperationLog();
        testOperationLog.setId(1);
        testOperationLog.setUsername("testuser");
        testOperationLog.setOperationType("CREATE");
        testOperationLog.setOperationTarget("USER");
        testOperationLog.setOperationDescriptionZh("创建用户: testuser");
        testOperationLog.setOperationDescriptionEn("Create user: testuser");
        testOperationLog.setOperationTime("2025-09-19T10:00:00Z");

        testRequest = new CreateOperationLogRequest();
        testRequest.setUsername("testuser");
        testRequest.setOperationType("CREATE");
        testRequest.setOperationTarget("USER");
        testRequest.setOperationDescriptionZh("创建用户: testuser");
        testRequest.setOperationDescriptionEn("Create user: testuser");
    }

    @Test
    public void testOperationLogsGet_Success_ReturnsOperationLogs() {
        // Arrange
        OperationLogPageResponse mockResponse = new OperationLogPageResponse();
        mockResponse.setSuccess(true);
        when(operationLogService.getOperationLogs(anyString(), anyString(), anyString(), anyString(), anyString(), anyInt(), anyInt()))
                .thenReturn(mockResponse);

        // Act
        ResponseEntity<OperationLogPageResponse> response = operationLogController.operationLogsGet(
                "testuser", "CREATE", "USER", "2025-09-19T00:00:00Z", "2025-09-19T23:59:59Z", 0, 20);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        verify(operationLogService).getOperationLogs("testuser", "CREATE", "USER", 
                "2025-09-19T00:00:00Z", "2025-09-19T23:59:59Z", 0, 20);
    }

    @Test
    public void testOperationLogsGet_ServiceException_ReturnsInternalServerError() {
        // Arrange
        when(operationLogService.getOperationLogs(anyString(), anyString(), anyString(), anyString(), anyString(), anyInt(), anyInt()))
                .thenThrow(new RuntimeException("Service error"));

        // Act
        ResponseEntity<OperationLogPageResponse> response = operationLogController.operationLogsGet(
                "testuser", "CREATE", "USER", "2025-09-19T00:00:00Z", "2025-09-19T23:59:59Z", 0, 20);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
    }

    @Test
    public void testOperationLogsPost_Success_ReturnsCreatedOperationLog() {
        // Arrange
        when(operationLogService.createOperationLog(any(CreateOperationLogRequest.class)))
                .thenReturn(testOperationLog);

        // Act
        ResponseEntity<OperationLog> response = operationLogController.operationLogsPost(testRequest);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(Integer.valueOf(1), response.getBody().getId());
        assertEquals("testuser", response.getBody().getUsername());
        verify(operationLogService).createOperationLog(testRequest);
    }

    @Test
    public void testOperationLogsPost_ServiceException_ReturnsInternalServerError() {
        // Arrange
        when(operationLogService.createOperationLog(any(CreateOperationLogRequest.class)))
                .thenThrow(new RuntimeException("Service error"));

        // Act
        ResponseEntity<OperationLog> response = operationLogController.operationLogsPost(testRequest);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    public void testOperationLogsIdGet_Success_ReturnsOperationLog() {
        // Arrange
        when(operationLogService.getOperationLogById(1)).thenReturn(testOperationLog);

        // Act
        ResponseEntity<OperationLog> response = operationLogController.operationLogsIdGet(1);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(Integer.valueOf(1), response.getBody().getId());
        verify(operationLogService).getOperationLogById(1);
    }

    @Test
    public void testOperationLogsIdGet_NotFound_ReturnsNotFound() {
        // Arrange
        when(operationLogService.getOperationLogById(999)).thenReturn(null);

        // Act
        ResponseEntity<OperationLog> response = operationLogController.operationLogsIdGet(999);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    public void testOperationLogsStatisticsGet_Success_ReturnsStatistics() {
        // Arrange
        OperationLogStatistics mockStats = new OperationLogStatistics();
        mockStats.setSuccess(true);
        when(operationLogService.getOperationLogStatistics()).thenReturn(mockStats);

        // Act
        ResponseEntity<OperationLogStatistics> response = operationLogController.operationLogsStatisticsGet();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        verify(operationLogService).getOperationLogStatistics();
    }

    @Test
    public void testOperationLogsExportGet_Success_ReturnsExcelFile() {
        // Arrange
        byte[] mockExcelData = "mock excel data".getBytes();
        when(operationLogService.exportOperationLogs(anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(mockExcelData);

        // Act
        ResponseEntity<byte[]> response = operationLogController.operationLogsExportGet(
                "testuser", "CREATE", "USER", "2025-09-19T00:00:00Z", "2025-09-19T23:59:59Z");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertArrayEquals(mockExcelData, response.getBody());
        verify(operationLogService).exportOperationLogs("testuser", "CREATE", "USER", 
                "2025-09-19T00:00:00Z", "2025-09-19T23:59:59Z");
    }
}
