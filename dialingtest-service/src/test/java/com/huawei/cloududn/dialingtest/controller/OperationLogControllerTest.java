/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.cloududn.dialingtest.controller;

import com.huawei.cloududn.dialingtest.model.CreateOperationLogRequest;
import com.huawei.cloududn.dialingtest.model.OperationLogPageResponse;
import com.huawei.cloududn.dialingtest.model.OperationLogResponse;
import com.huawei.cloududn.dialingtest.model.OperationLogStatistics;
import com.huawei.cloududn.dialingtest.model.OperationLogStatisticsResponse;
import com.huawei.cloududn.dialingtest.service.OperationLogService;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.Assert.*;
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

    private CreateOperationLogRequest testRequest;
    private OperationLogResponse testResponse;
    private OperationLogStatistics testStatistics;

    @Before
    public void setUp() {
        // 设置测试请求
        testRequest = new CreateOperationLogRequest();
        testRequest.setUsername("testuser");
        testRequest.setOperationType("CREATE");
        testRequest.setOperationTarget("USER");
        testRequest.setOperationDescriptionZh("创建用户");
        testRequest.setOperationDescriptionEn("Create user");

        // 设置测试响应
        testResponse = new OperationLogResponse();
        testResponse.setSuccess(true);
        testResponse.setMessage("操作成功");

        // 设置测试统计
        testStatistics = new OperationLogStatistics();
        testStatistics.setTotalCount(10);
        testStatistics.setTodayCount(5);
        testStatistics.setActiveUsers(3);
    }

    @Test
    public void testOperationLogsPost_Success_ReturnsCreated() {
        // Arrange
        when(operationLogService.createOperationLog(any(CreateOperationLogRequest.class))).thenReturn(testResponse);

        // Act
        ResponseEntity<OperationLogResponse> response = operationLogController.operationLogsPost(testRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        verify(operationLogService).createOperationLog(any(CreateOperationLogRequest.class));
    }

    @Test
    public void testOperationLogsPost_ServiceException_ReturnsInternalServerError() {
        // Arrange
        when(operationLogService.createOperationLog(any(CreateOperationLogRequest.class)))
            .thenThrow(new RuntimeException("Service error"));

        // Act
        ResponseEntity<OperationLogResponse> response = operationLogController.operationLogsPost(testRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        verify(operationLogService).createOperationLog(any(CreateOperationLogRequest.class));
    }

    @Test
    public void testOperationLogsGet_Success_ReturnsOk() {
        // Arrange
        OperationLogPageResponse mockPageResponse = new OperationLogPageResponse();
        mockPageResponse.setSuccess(true);
        when(operationLogService.getOperationLogs(anyInt(), anyInt(), anyString(), anyString(), anyString(), anyString(), anyString()))
            .thenReturn(mockPageResponse);

        // Act
        ResponseEntity<OperationLogPageResponse> response = operationLogController.operationLogsGet(
            0, 10, "testuser", "CREATE", "USER", "2023-01-01", "2023-12-31");

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        verify(operationLogService).getOperationLogs(anyInt(), anyInt(), anyString(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    public void testOperationLogsGet_ServiceException_ReturnsInternalServerError() {
        // Arrange
        when(operationLogService.getOperationLogs(anyInt(), anyInt(), anyString(), anyString(), anyString(), anyString(), anyString()))
            .thenThrow(new RuntimeException("Service error"));

        // Act
        ResponseEntity<OperationLogPageResponse> response = operationLogController.operationLogsGet(
            0, 10, "testuser", "CREATE", "USER", "2023-01-01", "2023-12-31");

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        verify(operationLogService).getOperationLogs(anyInt(), anyInt(), anyString(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    public void testOperationLogsStatisticsGet_Success_ReturnsOk() {
        // Arrange
        OperationLogStatisticsResponse mockStatsResponse = new OperationLogStatisticsResponse();
        mockStatsResponse.setSuccess(true);
        mockStatsResponse.setData(testStatistics);
        when(operationLogService.getOperationLogStatistics(anyString(), anyString())).thenReturn(mockStatsResponse);

        // Act
        ResponseEntity<OperationLogStatisticsResponse> response = operationLogController.operationLogsStatisticsGet("2023-01-01", "2023-12-31");

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        verify(operationLogService).getOperationLogStatistics(anyString(), anyString());
    }

    @Test
    public void testOperationLogsStatisticsGet_ServiceException_ReturnsInternalServerError() {
        // Arrange
        when(operationLogService.getOperationLogStatistics(anyString(), anyString()))
            .thenThrow(new RuntimeException("Service error"));

        // Act
        ResponseEntity<OperationLogStatisticsResponse> response = operationLogController.operationLogsStatisticsGet("2023-01-01", "2023-12-31");

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        verify(operationLogService).getOperationLogStatistics(anyString(), anyString());
    }

    @Test
    public void testOperationLogsExportGet_Success_ReturnsOk() {
        // Arrange
        Resource mockResource = mock(Resource.class);
        when(operationLogService.exportOperationLogs(anyString(), anyString(), anyString(), anyString(), anyString()))
            .thenReturn(mockResource);

        // Act
        ResponseEntity<Resource> response = operationLogController.operationLogsExportGet(
            "testuser", "CREATE", "USER", "2023-01-01", "2023-12-31");

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(mockResource, response.getBody());
        verify(operationLogService).exportOperationLogs(anyString(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    public void testOperationLogsExportGet_ServiceException_ReturnsInternalServerError() {
        // Arrange
        when(operationLogService.exportOperationLogs(anyString(), anyString(), anyString(), anyString(), anyString()))
            .thenThrow(new RuntimeException("Service error"));

        // Act
        ResponseEntity<Resource> response = operationLogController.operationLogsExportGet(
            "testuser", "CREATE", "USER", "2023-01-01", "2023-12-31");

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNull(response.getBody());
        verify(operationLogService).exportOperationLogs(anyString(), anyString(), anyString(), anyString(), anyString());
    }
}