/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.dialtest.center.service;

import com.huawei.dialtest.center.entity.OperationLog;
import com.huawei.dialtest.center.mapper.OperationLogMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * OperationLogService测试类
 *
 * @author g00940940
 * @since 2025-09-09
 */
@RunWith(MockitoJUnitRunner.class)
public class OperationLogServiceTest {

    @Mock
    private OperationLogMapper operationLogMapper;

    @InjectMocks
    private OperationLogService operationLogService;

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
    public void testLogOperation_Success() {
        // Arrange
        String username = "testuser";
        String operationType = "CREATE";
        String target = "用户管理";
        String description = "创建新用户";
        

        when(operationLogMapper.insert(any(OperationLog.class))).thenReturn(1);

        // Act
        OperationLog result = operationLogService.logOperation(username, operationType, target, description);

        // Assert
        assertNotNull("Result should not be null", result);
        assertEquals("Username should match", username, result.getUsername());
        assertEquals("Operation type should match", operationType, result.getOperationType());
        assertEquals("Target should match", target, result.getTarget());
        assertEquals("Description should match", description, result.getDescription());
        assertNotNull("Operation time should be set", result.getOperationTime());

        verify(operationLogMapper).insert(any(OperationLog.class));
    }

    @Test
    public void testLogOperation_WithoutDescription() {
        // Arrange
        String username = "testuser";
        String operationType = "UPDATE";
        String target = "角色管理";

        OperationLog expectedResult = new OperationLog();
        expectedResult.setId(1L);
        expectedResult.setUsername(username);
        expectedResult.setOperationType(operationType);
        expectedResult.setTarget(target);
        expectedResult.setDescription(null);

        when(operationLogMapper.insert(any(OperationLog.class))).thenReturn(1);

        // Act
        OperationLog result = operationLogService.logOperation(username, operationType, target);

        // Assert
        assertNotNull("Result should not be null", result);
        assertEquals("Username should match", username, result.getUsername());
        assertEquals("Operation type should match", operationType, result.getOperationType());
        assertEquals("Target should match", target, result.getTarget());

        verify(operationLogMapper).insert(any(OperationLog.class));
    }

    @Test(expected = RuntimeException.class)
    public void testLogOperation_Error() {
        // Arrange
        String username = "testuser";
        String operationType = "CREATE";
        String target = "用户管理";
        String description = "创建新用户";

        when(operationLogMapper.insert(any(OperationLog.class)))
                .thenThrow(new RuntimeException("Database error"));

        // Act
        operationLogService.logOperation(username, operationType, target, description);
    }

    @Test
    public void testGetOperationLogsByConditions_Success() {
        // Arrange
        String username = "testuser";
        String operationType = "CREATE";
        String target = "用户管理";
        LocalDateTime startTime = LocalDateTime.now().minusDays(1);
        LocalDateTime endTime = LocalDateTime.now();
        List<OperationLog> operationLogs = Arrays.asList(testOperationLog);
        Page<OperationLog> page = new PageImpl<>(operationLogs);

        when(operationLogMapper.findByConditions(eq(username), eq(operationType), eq(target), eq(startTime), eq(endTime), anyInt(), anyInt())).thenReturn(operationLogs);
        when(operationLogMapper.countByConditions(eq(username), eq(operationType), eq(target), eq(startTime), eq(endTime))).thenReturn(1L);

        // Act
        Page<OperationLog> result = operationLogService.getOperationLogsByConditions(username, operationType, target, startTime, endTime, 0, 20);

        // Assert
        assertNotNull("Result should not be null", result);
        assertEquals("Total elements should match", 1, result.getTotalElements());
        assertEquals("Content size should match", 1, result.getContent().size());

        verify(operationLogMapper).findByConditions(eq(username), eq(operationType), eq(target), eq(startTime), eq(endTime), anyInt(), anyInt());
        verify(operationLogMapper).countByConditions(eq(username), eq(operationType), eq(target), eq(startTime), eq(endTime));
    }

    @Test(expected = RuntimeException.class)
    public void testGetOperationLogsByConditions_Error() {
        // Arrange
        String username = "testuser";
        String operationType = "CREATE";
        String target = "用户管理";
        LocalDateTime startTime = LocalDateTime.now().minusDays(1);
        LocalDateTime endTime = LocalDateTime.now();
        when(operationLogMapper.findByConditions(eq(username), eq(operationType), eq(target), eq(startTime), eq(endTime), anyInt(), anyInt()))
                .thenThrow(new RuntimeException("Database error"));

        // Act
        operationLogService.getOperationLogsByConditions(username, operationType, target, startTime, endTime, 0, 20);
    }

    @Test
    public void testGetOperationLogById_Success() {
        // Arrange
        Long id = 1L;
        when(operationLogMapper.findById(id)).thenReturn(testOperationLog);

        // Act
        Optional<OperationLog> result = operationLogService.getOperationLogById(id);

        // Assert
        assertTrue("Result should be present", result.isPresent());
        assertEquals("ID should match", id, result.get().getId());

        verify(operationLogMapper).findById(id);
    }

    @Test
    public void testGetOperationLogById_NotFound() {
        // Arrange
        Long id = 999L;
        when(operationLogMapper.findById(id)).thenReturn(null);

        // Act
        Optional<OperationLog> result = operationLogService.getOperationLogById(id);

        // Assert
        assertFalse("Result should not be present", result.isPresent());

        verify(operationLogMapper).findById(id);
    }

    @Test(expected = RuntimeException.class)
    public void testGetOperationLogById_Error() {
        // Arrange
        Long id = 1L;
        when(operationLogMapper.findById(id))
                .thenThrow(new RuntimeException("Database error"));

        // Act
        operationLogService.getOperationLogById(id);
    }




    @Test
    public void testGetRecentOperationLogs_Success() {
        // Arrange
        List<OperationLog> operationLogs = Arrays.asList(testOperationLog);
        when(operationLogMapper.findRecentOperationLogs(10)).thenReturn(operationLogs);

        // Act
        List<OperationLog> result = operationLogService.getRecentOperationLogs(10);

        // Assert
        assertNotNull("Result should not be null", result);
        assertEquals("Size should match", 1, result.size());

        verify(operationLogMapper).findRecentOperationLogs(10);
    }

    @Test(expected = RuntimeException.class)
    public void testGetRecentOperationLogs_Error() {
        // Arrange
        when(operationLogMapper.findRecentOperationLogs(10))
                .thenThrow(new RuntimeException("Database error"));

        // Act
        operationLogService.getRecentOperationLogs(10);
    }


}
