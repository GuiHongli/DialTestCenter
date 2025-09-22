/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.cloududn.dialingtest.service;

import com.huawei.cloududn.dialingtest.dao.OperationLogDao;
import com.huawei.cloududn.dialingtest.model.CreateOperationLogRequest;
import com.huawei.cloududn.dialingtest.model.OperationLog;
import com.huawei.cloududn.dialingtest.model.OperationLogPageResponse;
import com.huawei.cloududn.dialingtest.model.OperationLogPageResponseData;
import com.huawei.cloududn.dialingtest.model.OperationLogResponse;
import com.huawei.cloududn.dialingtest.model.OperationLogStatistics;
import com.huawei.cloududn.dialingtest.model.OperationLogStatisticsResponse;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.io.Resource;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 操作记录服务测试类
 *
 * @author g00940940
 * @since 2025-09-19
 */
@RunWith(MockitoJUnitRunner.class)
public class OperationLogServiceTest {

    @Mock
    private OperationLogDao operationLogDao;

    @InjectMocks
    private OperationLogService operationLogService;

    private OperationLog testOperationLog;
    private CreateOperationLogRequest testRequest;
    private OperationLogStatistics testStatistics;

    @Before
    public void setUp() {
        // 设置测试操作记录
        testOperationLog = new OperationLog();
        testOperationLog.setId(1);
        testOperationLog.setUsername("testuser");
        testOperationLog.setOperationType("CREATE");
        testOperationLog.setOperationTarget("USER");
        testOperationLog.setOperationDescriptionZh("创建用户");
        testOperationLog.setOperationDescriptionEn("Create user");

        // 设置测试请求
        testRequest = new CreateOperationLogRequest();
        testRequest.setUsername("testuser");
        testRequest.setOperationType("CREATE");
        testRequest.setOperationTarget("USER");
        testRequest.setOperationDescriptionZh("创建用户");
        testRequest.setOperationDescriptionEn("Create user");

        // 设置测试统计
        testStatistics = new OperationLogStatistics();
        testStatistics.setTotalCount(10);
        testStatistics.setTodayCount(5);
        testStatistics.setActiveUsers(3);
    }

    @Test
    public void testCreateOperationLog_Success_ReturnsResponse() {
        // Arrange
        when(operationLogDao.save(any(OperationLog.class))).thenReturn(1);

        // Act
        OperationLogResponse response = operationLogService.createOperationLog(testRequest);

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());
        verify(operationLogDao).save(any(OperationLog.class));
    }

    @Test
    public void testCreateOperationLog_DaoException_ThrowsException() {
        // Arrange
        when(operationLogDao.save(any(OperationLog.class))).thenThrow(new RuntimeException("DAO error"));

        // Act & Assert
        try {
            operationLogService.createOperationLog(testRequest);
            fail("Expected RuntimeException");
        } catch (RuntimeException e) {
            assertEquals("DAO error", e.getMessage());
        }
        verify(operationLogDao).save(any(OperationLog.class));
    }

    @Test
    public void testCreateOperationLog_SaveReturnsZero_ThrowsException() {
        // Arrange
        when(operationLogDao.save(any(OperationLog.class))).thenReturn(0);

        // Act & Assert
        try {
            operationLogService.createOperationLog(testRequest);
            fail("Expected IllegalStateException");
        } catch (IllegalStateException e) {
            assertEquals("创建操作记录失败，数据库操作未生效", e.getMessage());
        }
        verify(operationLogDao).save(any(OperationLog.class));
    }

    @Test
    public void testCreateOperationLog_EmptyUsername_ThrowsException() {
        // Arrange
        CreateOperationLogRequest request = new CreateOperationLogRequest();
        request.setUsername("");
        request.setOperationType("CREATE");
        request.setOperationTarget("USER");

        // Act & Assert
        try {
            operationLogService.createOperationLog(request);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("Username is required", e.getMessage());
        }
        verify(operationLogDao, never()).save(any(OperationLog.class));
    }

    @Test
    public void testCreateOperationLog_EmptyOperationType_ThrowsException() {
        // Arrange
        CreateOperationLogRequest request = new CreateOperationLogRequest();
        request.setUsername("testuser");
        request.setOperationType("");
        request.setOperationTarget("USER");

        // Act & Assert
        try {
            operationLogService.createOperationLog(request);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("Operation type is required", e.getMessage());
        }
        verify(operationLogDao, never()).save(any(OperationLog.class));
    }

    @Test
    public void testCreateOperationLog_EmptyOperationTarget_ThrowsException() {
        // Arrange
        CreateOperationLogRequest request = new CreateOperationLogRequest();
        request.setUsername("testuser");
        request.setOperationType("CREATE");
        request.setOperationTarget("");

        // Act & Assert
        try {
            operationLogService.createOperationLog(request);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("Operation target is required", e.getMessage());
        }
        verify(operationLogDao, never()).save(any(OperationLog.class));
    }

    @Test
    public void testGetOperationLogs_Success_ReturnsPageResponse() {
        // Arrange
        when(operationLogDao.findOperationLogsWithPagination(anyInt(), anyInt(), anyString(), anyString(), anyString(), anyString(), anyString()))
            .thenReturn(Arrays.asList(testOperationLog));
        when(operationLogDao.countOperationLogs(anyString(), anyString(), anyString(), anyString(), anyString()))
            .thenReturn(1L);

        // Act
        OperationLogPageResponse response = operationLogService.getOperationLogs(
            0, 10, "testuser", "CREATE", "USER", "2023-01-01", "2023-12-31");

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertNotNull(response.getData());
        assertEquals(1, response.getData().getContent().size());
        verify(operationLogDao).findOperationLogsWithPagination(anyInt(), anyInt(), anyString(), anyString(), anyString(), anyString(), anyString());
        verify(operationLogDao).countOperationLogs(anyString(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    public void testGetOperationLogs_DaoException_ThrowsException() {
        // Arrange
        when(operationLogDao.findOperationLogsWithPagination(anyInt(), anyInt(), anyString(), anyString(), anyString(), anyString(), anyString()))
            .thenThrow(new RuntimeException("DAO error"));

        // Act & Assert
        try {
            operationLogService.getOperationLogs(0, 10, "testuser", "CREATE", "USER", "2023-01-01", "2023-12-31");
            fail("Expected RuntimeException");
        } catch (RuntimeException e) {
            assertEquals("DAO error", e.getMessage());
        }
        verify(operationLogDao).findOperationLogsWithPagination(anyInt(), anyInt(), anyString(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    public void testGetOperationLogStatistics_Success_ReturnsStatisticsResponse() {
        // Arrange
        when(operationLogDao.getStatistics(anyString(), anyString())).thenReturn(testStatistics);

        // Act
        OperationLogStatisticsResponse response = operationLogService.getOperationLogStatistics("2023-01-01", "2023-12-31");

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertNotNull(response.getData());
        assertEquals(Integer.valueOf(10), response.getData().getTotalCount());
        verify(operationLogDao).getStatistics(anyString(), anyString());
    }

    @Test
    public void testGetOperationLogStatistics_DaoException_ThrowsException() {
        // Arrange
        when(operationLogDao.getStatistics(anyString(), anyString())).thenThrow(new RuntimeException("DAO error"));

        // Act & Assert
        try {
            operationLogService.getOperationLogStatistics("2023-01-01", "2023-12-31");
            fail("Expected RuntimeException");
        } catch (RuntimeException e) {
            assertEquals("DAO error", e.getMessage());
        }
        verify(operationLogDao).getStatistics(anyString(), anyString());
    }

    @Test
    public void testGetOperationLogById_Success_ReturnsResponse() {
        // Arrange
        when(operationLogDao.findById(1)).thenReturn(testOperationLog);

        // Act
        OperationLogResponse response = operationLogService.getOperationLogById(1);

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertNotNull(response.getData());
        assertEquals(testOperationLog.getId(), response.getData().getId());
        verify(operationLogDao).findById(1);
    }

    @Test
    public void testGetOperationLogById_InvalidId_ThrowsException() {
        // Act & Assert
        try {
            operationLogService.getOperationLogById(0);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("Invalid operation log ID", e.getMessage());
        }
        verify(operationLogDao, never()).findById(anyInt());
    }

    @Test
    public void testGetOperationLogById_NotFound_ThrowsException() {
        // Arrange
        when(operationLogDao.findById(999)).thenReturn(null);

        // Act & Assert
        try {
            operationLogService.getOperationLogById(999);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("Operation log not found", e.getMessage());
        }
        verify(operationLogDao).findById(999);
    }

    @Test
    public void testExportOperationLogs_Success_ReturnsResource() {
        // Arrange
        when(operationLogDao.findOperationLogsForExport(anyString(), anyString(), anyString(), anyString(), anyString()))
            .thenReturn(Arrays.asList(testOperationLog));

        // Act
        Resource result = operationLogService.exportOperationLogs(
            "testuser", "CREATE", "USER", "2023-01-01", "2023-12-31");

        // Assert
        assertNotNull(result);
        verify(operationLogDao).findOperationLogsForExport(anyString(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    public void testExportOperationLogs_DaoException_ReturnsNull() {
        // Arrange
        when(operationLogDao.findOperationLogsForExport(anyString(), anyString(), anyString(), anyString(), anyString()))
            .thenThrow(new RuntimeException("DAO error"));

        // Act
        Resource result = operationLogService.exportOperationLogs(
            "testuser", "CREATE", "USER", "2023-01-01", "2023-12-31");

        // Assert
        assertNull(result);
        verify(operationLogDao).findOperationLogsForExport(anyString(), anyString(), anyString(), anyString(), anyString());
    }
}