/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.cloududn.dialingtest.service;

import com.huawei.cloududn.dialingtest.dao.OperationLogDao;
import com.huawei.cloududn.dialingtest.model.CreateOperationLogRequest;
import com.huawei.cloududn.dialingtest.model.OperationLog;
import com.huawei.cloududn.dialingtest.model.OperationLogPageResponse;
import com.huawei.cloududn.dialingtest.model.OperationLogStatistics;
import com.huawei.cloududn.dialingtest.util.ExcelUtil;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

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

    @Mock
    private ExcelUtil excelUtil;

    @InjectMocks
    private OperationLogService operationLogService;

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
    public void testGetOperationLogs_Success_ReturnsPageResponse() {
        // Arrange
        List<OperationLog> mockLogs = Arrays.asList(testOperationLog);
        when(operationLogDao.findOperationLogsWithPagination(anyString(), anyString(), anyString(), anyString(), anyString(), anyInt(), anyInt()))
                .thenReturn(mockLogs);
        when(operationLogDao.countOperationLogs(anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(1L);

        // Act
        OperationLogPageResponse response = operationLogService.getOperationLogs(
                "testuser", "CREATE", "USER", "2025-09-19T00:00:00Z", "2025-09-19T23:59:59Z", 0, 20);

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertNotNull(response.getData());
        assertEquals(1, response.getData().getContent().size());
        assertEquals("testuser", response.getData().getContent().get(0).getUsername());
        verify(operationLogDao).findOperationLogsWithPagination("testuser", "CREATE", "USER", 
                "2025-09-19T00:00:00Z", "2025-09-19T23:59:59Z", 0, 20);
        verify(operationLogDao).countOperationLogs("testuser", "CREATE", "USER", 
                "2025-09-19T00:00:00Z", "2025-09-19T23:59:59Z");
    }

    @Test
    public void testGetOperationLogs_DaoException_ReturnsErrorResponse() {
        // Arrange
        when(operationLogDao.findOperationLogsWithPagination(anyString(), anyString(), anyString(), anyString(), anyString(), anyInt(), anyInt()))
                .thenThrow(new RuntimeException("Database error"));

        // Act
        OperationLogPageResponse response = operationLogService.getOperationLogs(
                "testuser", "CREATE", "USER", "2025-09-19T00:00:00Z", "2025-09-19T23:59:59Z", 0, 20);

        // Assert
        assertNotNull(response);
        assertFalse(response.isSuccess());
        assertNotNull(response.getMessage());
        assertTrue(response.getMessage().contains("查询操作记录失败"));
    }

    @Test
    public void testCreateOperationLog_Success_ReturnsCreatedLog() {
        // Arrange
        when(operationLogDao.save(any(OperationLog.class))).thenReturn(1);

        // Act
        OperationLog result = operationLogService.createOperationLog(testRequest);

        // Assert
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        assertEquals("CREATE", result.getOperationType());
        assertEquals("USER", result.getOperationTarget());
        assertEquals("创建用户: testuser", result.getOperationDescriptionZh());
        assertEquals("Create user: testuser", result.getOperationDescriptionEn());
        verify(operationLogDao).save(any(OperationLog.class));
    }

    @Test
    public void testCreateOperationLog_InvalidRequest_ThrowsException() {
        // Arrange
        CreateOperationLogRequest invalidRequest = new CreateOperationLogRequest();
        invalidRequest.setUsername("");

        // Act & Assert
        try {
            operationLogService.createOperationLog(invalidRequest);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("用户名不能为空"));
        }
    }

    @Test
    public void testGetOperationLogById_Success_ReturnsOperationLog() {
        // Arrange
        when(operationLogDao.findById(1)).thenReturn(testOperationLog);

        // Act
        OperationLog result = operationLogService.getOperationLogById(1);

        // Assert
        assertNotNull(result);
        assertEquals(Integer.valueOf(1), result.getId());
        assertEquals("testuser", result.getUsername());
        verify(operationLogDao).findById(1);
    }

    @Test
    public void testGetOperationLogById_NotFound_ReturnsNull() {
        // Arrange
        when(operationLogDao.findById(999)).thenReturn(null);

        // Act
        OperationLog result = operationLogService.getOperationLogById(999);

        // Assert
        assertNull(result);
        verify(operationLogDao).findById(999);
    }

    @Test
    public void testGetOperationLogStatistics_Success_ReturnsStatistics() {
        // Arrange
        when(operationLogDao.getStatistics()).thenReturn(new OperationLogStatistics());

        // Act
        OperationLogStatistics result = operationLogService.getOperationLogStatistics();

        // Assert
        assertNotNull(result);
        verify(operationLogDao).getStatistics();
    }

    @Test
    public void testExportOperationLogs_Success_ReturnsExcelData() {
        // Arrange
        List<OperationLog> mockLogs = Arrays.asList(testOperationLog);
        byte[] mockExcelData = "mock excel data".getBytes();
        when(operationLogDao.findOperationLogsForExport(anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(mockLogs);
        when(excelUtil.generateOperationLogsExcel(anyList())).thenReturn(mockExcelData);

        // Act
        byte[] result = operationLogService.exportOperationLogs(
                "testuser", "CREATE", "USER", "2025-09-19T00:00:00Z", "2025-09-19T23:59:59Z");

        // Assert
        assertNotNull(result);
        assertArrayEquals(mockExcelData, result);
        verify(operationLogDao).findOperationLogsForExport("testuser", "CREATE", "USER", 
                "2025-09-19T00:00:00Z", "2025-09-19T23:59:59Z");
        verify(excelUtil).generateOperationLogsExcel(mockLogs);
    }
}
