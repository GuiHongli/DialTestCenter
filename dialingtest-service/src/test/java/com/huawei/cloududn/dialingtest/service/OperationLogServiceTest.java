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
import com.huawei.cloududn.dialingtest.util.ExcelUtil;

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

    @Mock
    private ExcelUtil excelUtil;

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
        when(operationLogDao.save(any(OperationLog.class))).thenReturn(testOperationLog);

        // Act
        OperationLogResponse response = operationLogService.createOperationLog(testRequest);

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());
        verify(operationLogDao).save(any(OperationLog.class));
    }

    @Test
    public void testCreateOperationLog_DaoException_ReturnsErrorResponse() {
        // Arrange
        when(operationLogDao.save(any(OperationLog.class))).thenThrow(new RuntimeException("DAO error"));

        // Act
        OperationLogResponse response = operationLogService.createOperationLog(testRequest);

        // Assert
        assertNotNull(response);
        assertFalse(response.isSuccess());
        verify(operationLogDao).save(any(OperationLog.class));
    }

    @Test
    public void testGetOperationLogs_Success_ReturnsPageResponse() {
        // Arrange
        when(operationLogDao.findOperationLogsForExport(anyString(), anyString(), anyString(), anyString(), anyString()))
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
        verify(operationLogDao).findOperationLogsForExport(anyString(), anyString(), anyString(), anyString(), anyString());
        verify(operationLogDao).countOperationLogs(anyString(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    public void testGetOperationLogs_DaoException_ReturnsErrorResponse() {
        // Arrange
        when(operationLogDao.findOperationLogsForExport(anyString(), anyString(), anyString(), anyString(), anyString()))
            .thenThrow(new RuntimeException("DAO error"));

        // Act
        OperationLogPageResponse response = operationLogService.getOperationLogs(
            0, 10, "testuser", "CREATE", "USER", "2023-01-01", "2023-12-31");

        // Assert
        assertNotNull(response);
        assertFalse(response.isSuccess());
        verify(operationLogDao).findOperationLogsForExport(anyString(), anyString(), anyString(), anyString(), anyString());
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
    public void testGetOperationLogStatistics_DaoException_ReturnsErrorResponse() {
        // Arrange
        when(operationLogDao.getStatistics(anyString(), anyString())).thenThrow(new RuntimeException("DAO error"));

        // Act
        OperationLogStatisticsResponse response = operationLogService.getOperationLogStatistics("2023-01-01", "2023-12-31");

        // Assert
        assertNotNull(response);
        assertFalse(response.isSuccess());
        verify(operationLogDao).getStatistics(anyString(), anyString());
    }

    @Test
    public void testExportOperationLogs_Success_ReturnsResource() {
        // Arrange
        Resource mockResource = mock(Resource.class);
        when(operationLogDao.findOperationLogsForExport(anyString(), anyString(), anyString(), anyString(), anyString()))
            .thenReturn(Arrays.asList(testOperationLog));
        when(excelUtil.generateOperationLogsExcel(any())).thenReturn(mockResource);

        // Act
        Resource result = operationLogService.exportOperationLogs(
            "testuser", "CREATE", "USER", "2023-01-01", "2023-12-31");

        // Assert
        assertNotNull(result);
        assertEquals(mockResource, result);
        verify(operationLogDao).findOperationLogsForExport(anyString(), anyString(), anyString(), anyString(), anyString());
        verify(excelUtil).generateOperationLogsExcel(anyList());
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
        verify(excelUtil, never()).generateOperationLogsExcel(any());
    }
}