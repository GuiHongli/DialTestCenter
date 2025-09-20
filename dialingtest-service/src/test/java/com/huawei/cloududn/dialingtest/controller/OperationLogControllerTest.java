/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.cloududn.dialingtest.controller;

import com.huawei.cloududn.dialingtest.model.*;
import com.huawei.cloududn.dialingtest.service.OperationLogService;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * OperationLogController单元测试类
 * 测试操作记录控制器的所有REST API接口
 *
 * @author g00940940
 * @since 2025-01-15
 */
@RunWith(MockitoJUnitRunner.class)
public class OperationLogControllerTest {
    
    @Mock
    private OperationLogService operationLogService;
    
    @InjectMocks
    private OperationLogController operationLogController;
    
    private OperationLog testOperationLog;
    private CreateOperationLogRequest testCreateRequest;
    private OperationLogPageResponse testPageResponse;
    private OperationLogStatistics testStatistics;
    
    @Before
    public void setUp() {
        // 初始化测试数据
        testOperationLog = new OperationLog();
        testOperationLog.setId(1);
        testOperationLog.setUsername("testuser");
        testOperationLog.setOperationType("CREATE");
        testOperationLog.setOperationTarget("USER");
        testOperationLog.setOperationDescriptionZh("创建用户");
        testOperationLog.setOperationDescriptionEn("Create user");
        testOperationLog.setOperationData("{\"username\": \"testuser\"}");
        testOperationLog.setOperationTime("2025-01-15T10:00:00");
        
        testCreateRequest = new CreateOperationLogRequest();
        testCreateRequest.setUsername("testuser");
        testCreateRequest.setOperationType("CREATE");
        testCreateRequest.setOperationTarget("USER");
        testCreateRequest.setOperationDescriptionZh("创建用户");
        testCreateRequest.setOperationDescriptionEn("Create user");
        testCreateRequest.setOperationData("{\"username\": \"testuser\"}");
        
        testPageResponse = new OperationLogPageResponse();
        testPageResponse.setSuccess(true);
        testPageResponse.setMessage("查询成功");
        
        OperationLogPageResponseData pageData = new OperationLogPageResponseData();
        pageData.setContent(Arrays.asList(testOperationLog));
        pageData.setTotalElements(1);
        pageData.setTotalPages(1);
        pageData.setSize(20);
        pageData.setNumber(0);
        testPageResponse.setData(pageData);
        
        testStatistics = new OperationLogStatistics();
        testStatistics.setTotalCount(100);
        testStatistics.setTodayCount(10);
        testStatistics.setActiveUsers(5);
        testStatistics.setOperationTypes(3);
    }
    
    /**
     * 测试分页查询操作记录 - 成功场景
     */
    @Test
    public void testOperationLogsGet_Success_ReturnsOkResponse() {
        // Arrange
        when(operationLogService.getOperationLogs(anyInt(), anyInt(), anyString(), 
                anyString(), anyString(), anyString(), anyString()))
                .thenReturn(testPageResponse);
        
        // Act
        ResponseEntity<OperationLogPageResponse> response = operationLogController.operationLogsGet(
                0, 20, "testuser", "CREATE", "USER", "2025-01-01", "2025-01-31");
        
        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("查询成功", response.getBody().getMessage());
        assertNotNull(response.getBody().getData());
        assertEquals(1, response.getBody().getData().getTotalElements().intValue());
        
        verify(operationLogService, times(1)).getOperationLogs(0, 20, "testuser", 
                "CREATE", "USER", "2025-01-01", "2025-01-31");
    }
    
    /**
     * 测试分页查询操作记录 - 数据库异常
     */
    @Test
    public void testOperationLogsGet_DatabaseError_ReturnsInternalServerError() {
        // Arrange
        when(operationLogService.getOperationLogs(anyInt(), anyInt(), anyString(), 
                anyString(), anyString(), anyString(), anyString()))
                .thenThrow(new RuntimeException("Database connection failed"));
        
        // Act
        ResponseEntity<OperationLogPageResponse> response = operationLogController.operationLogsGet(
                0, 20, "testuser", "CREATE", "USER", "2025-01-01", "2025-01-31");
        
        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Internal server error", response.getBody().getMessage());
        
        verify(operationLogService, times(1)).getOperationLogs(0, 20, "testuser", 
                "CREATE", "USER", "2025-01-01", "2025-01-31");
    }
    
    /**
     * 测试分页查询操作记录 - 服务器内部错误
     */
    @Test
    public void testOperationLogsGet_ServerError_ReturnsInternalServerError() {
        // Arrange
        when(operationLogService.getOperationLogs(anyInt(), anyInt(), anyString(), 
                anyString(), anyString(), anyString(), anyString()))
                .thenThrow(new RuntimeException("Database error"));
        
        // Act
        ResponseEntity<OperationLogPageResponse> response = operationLogController.operationLogsGet(
                0, 20, "testuser", "CREATE", "USER", "2025-01-01", "2025-01-31");
        
        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Internal server error", response.getBody().getMessage());
        
        verify(operationLogService, times(1)).getOperationLogs(0, 20, "testuser", 
                "CREATE", "USER", "2025-01-01", "2025-01-31");
    }
    
    /**
     * 测试创建操作记录 - 成功场景
     */
    @Test
    public void testOperationLogsPost_Success_ReturnsCreatedResponse() {
        // Arrange
        OperationLogResponse createResponse = new OperationLogResponse();
        createResponse.setSuccess(true);
        createResponse.setMessage("创建成功");
        createResponse.setData(testOperationLog);
        
        when(operationLogService.createOperationLog(any(CreateOperationLogRequest.class)))
                .thenReturn(createResponse);
        
        // Act
        ResponseEntity<OperationLogResponse> response = operationLogController.operationLogsPost(testCreateRequest);
        
        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("创建成功", response.getBody().getMessage());
        assertNotNull(response.getBody().getData());
        assertEquals(Integer.valueOf(1), response.getBody().getData().getId());
        
        verify(operationLogService, times(1)).createOperationLog(testCreateRequest);
    }
    
    /**
     * 测试创建操作记录 - 参数验证失败
     */
    @Test
    public void testOperationLogsPost_InvalidParameters_ReturnsBadRequest() {
        // Arrange
        when(operationLogService.createOperationLog(any(CreateOperationLogRequest.class)))
                .thenThrow(new IllegalArgumentException("Username is required"));
        
        // Act
        ResponseEntity<OperationLogResponse> response = operationLogController.operationLogsPost(testCreateRequest);
        
        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertTrue(response.getBody().getMessage().contains("Invalid request parameters"));
        
        verify(operationLogService, times(1)).createOperationLog(testCreateRequest);
    }
    
    /**
     * 测试创建操作记录 - 状态异常
     */
    @Test
    public void testOperationLogsPost_IllegalState_ReturnsInternalServerError() {
        // Arrange
        when(operationLogService.createOperationLog(any(CreateOperationLogRequest.class)))
                .thenThrow(new IllegalStateException("Database operation failed"));
        
        // Act
        ResponseEntity<OperationLogResponse> response = operationLogController.operationLogsPost(testCreateRequest);
        
        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Failed to create operation log", response.getBody().getMessage());
        
        verify(operationLogService, times(1)).createOperationLog(testCreateRequest);
    }
    
    /**
     * 测试创建操作记录 - 服务器内部错误
     */
    @Test
    public void testOperationLogsPost_ServerError_ReturnsInternalServerError() {
        // Arrange
        when(operationLogService.createOperationLog(any(CreateOperationLogRequest.class)))
                .thenThrow(new RuntimeException("Unexpected error"));
        
        // Act
        ResponseEntity<OperationLogResponse> response = operationLogController.operationLogsPost(testCreateRequest);
        
        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Internal server error", response.getBody().getMessage());
        
        verify(operationLogService, times(1)).createOperationLog(testCreateRequest);
    }
    
    /**
     * 测试根据ID查询操作记录详情 - 成功场景
     */
    @Test
    public void testOperationLogsIdGet_Success_ReturnsOkResponse() {
        // Arrange
        OperationLogResponse getResponse = new OperationLogResponse();
        getResponse.setSuccess(true);
        getResponse.setMessage("查询成功");
        getResponse.setData(testOperationLog);
        
        when(operationLogService.getOperationLogById(anyInt())).thenReturn(getResponse);
        
        // Act
        ResponseEntity<OperationLogResponse> response = operationLogController.operationLogsIdGet(1);
        
        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("查询成功", response.getBody().getMessage());
        assertNotNull(response.getBody().getData());
        assertEquals(Integer.valueOf(1), response.getBody().getData().getId());
        
        verify(operationLogService, times(1)).getOperationLogById(1);
    }
    
    /**
     * 测试根据ID查询操作记录详情 - 无效ID
     */
    @Test
    public void testOperationLogsIdGet_InvalidId_ReturnsBadRequest() {
        // Arrange
        when(operationLogService.getOperationLogById(anyInt()))
                .thenThrow(new IllegalArgumentException("Invalid operation log ID"));
        
        // Act
        ResponseEntity<OperationLogResponse> response = operationLogController.operationLogsIdGet(0);
        
        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Invalid operation log ID", response.getBody().getMessage());
        
        verify(operationLogService, times(1)).getOperationLogById(0);
    }
    
    /**
     * 测试根据ID查询操作记录详情 - 记录不存在
     */
    @Test
    public void testOperationLogsIdGet_NotFound_ReturnsNotFound() {
        // Arrange
        when(operationLogService.getOperationLogById(anyInt()))
                .thenThrow(new RuntimeException("Operation log not found"));
        
        // Act
        ResponseEntity<OperationLogResponse> response = operationLogController.operationLogsIdGet(999);
        
        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Operation log not found", response.getBody().getMessage());
        
        verify(operationLogService, times(1)).getOperationLogById(999);
    }
    
    /**
     * 测试获取操作记录统计信息 - 成功场景
     */
    @Test
    public void testOperationLogsStatisticsGet_Success_ReturnsOkResponse() {
        // Arrange
        OperationLogStatisticsResponse statisticsResponse = new OperationLogStatisticsResponse();
        statisticsResponse.setSuccess(true);
        statisticsResponse.setMessage("查询成功");
        statisticsResponse.setData(testStatistics);
        
        when(operationLogService.getOperationLogStatistics(anyString(), anyString()))
                .thenReturn(statisticsResponse);
        
        // Act
        ResponseEntity<OperationLogStatisticsResponse> response = operationLogController.operationLogsStatisticsGet(
                "2025-01-01", "2025-01-31");
        
        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("查询成功", response.getBody().getMessage());
        assertNotNull(response.getBody().getData());
        assertEquals(Integer.valueOf(100), response.getBody().getData().getTotalCount());
        
        verify(operationLogService, times(1)).getOperationLogStatistics("2025-01-01", "2025-01-31");
    }
    
    /**
     * 测试获取操作记录统计信息 - 服务器内部错误
     */
    @Test
    public void testOperationLogsStatisticsGet_ServerError_ReturnsInternalServerError() {
        // Arrange
        when(operationLogService.getOperationLogStatistics(anyString(), anyString()))
                .thenThrow(new RuntimeException("Database error"));
        
        // Act
        ResponseEntity<OperationLogStatisticsResponse> response = operationLogController.operationLogsStatisticsGet(
                "2025-01-01", "2025-01-31");
        
        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Internal server error", response.getBody().getMessage());
        
        verify(operationLogService, times(1)).getOperationLogStatistics("2025-01-01", "2025-01-31");
    }
    
    /**
     * 测试导出操作记录 - 成功场景
     */
    @Test
    public void testOperationLogsExportGet_Success_ReturnsOkResponse() {
        // Arrange
        Resource mockResource = mock(Resource.class);
        when(operationLogService.exportOperationLogs(anyString(), anyString(), anyString(), 
                anyString(), anyString())).thenReturn(mockResource);
        
        // Act
        ResponseEntity<Resource> response = operationLogController.operationLogsExportGet(
                "testuser", "CREATE", "USER", "2025-01-01", "2025-01-31");
        
        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_OCTET_STREAM, response.getHeaders().getContentType());
        assertNotNull(response.getBody());
        assertEquals(mockResource, response.getBody());
        
        verify(operationLogService, times(1)).exportOperationLogs("testuser", "CREATE", 
                "USER", "2025-01-01", "2025-01-31");
    }
    
    /**
     * 测试导出操作记录 - 服务器内部错误
     */
    @Test
    public void testOperationLogsExportGet_ServerError_ReturnsInternalServerError() {
        // Arrange
        when(operationLogService.exportOperationLogs(anyString(), anyString(), anyString(), 
                anyString(), anyString())).thenThrow(new RuntimeException("Export failed"));
        
        // Act
        ResponseEntity<Resource> response = operationLogController.operationLogsExportGet(
                "testuser", "CREATE", "USER", "2025-01-01", "2025-01-31");
        
        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNull(response.getBody());
        
        verify(operationLogService, times(1)).exportOperationLogs("testuser", "CREATE", 
                "USER", "2025-01-01", "2025-01-31");
    }
    
}
