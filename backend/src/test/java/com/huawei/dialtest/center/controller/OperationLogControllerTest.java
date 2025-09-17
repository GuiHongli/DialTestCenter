/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.dialtest.center.controller;

import com.huawei.dialtest.center.dto.BaseApiResponse;
import com.huawei.dialtest.center.dto.PagedResponse;
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
import org.springframework.data.domain.PageRequest;
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
 * @since 2025-01-27
 */
@RunWith(MockitoJUnitRunner.class)
public class OperationLogControllerTest {

    @Mock
    private OperationLogService operationLogService;

    @InjectMocks
    private OperationLogController operationLogController;

    private OperationLog testOperationLog;
    private List<OperationLog> testOperationLogs;

    @Before
    public void setUp() {
        testOperationLog = new OperationLog();
        testOperationLog.setId(1L);
        testOperationLog.setUsername("testuser");
        testOperationLog.setOperationType("CREATE");
        testOperationLog.setTarget("TestCaseSet");
        testOperationLog.setDescription("Created test case set");
        // Note: timestamp is set automatically by the entity

        testOperationLogs = Arrays.asList(testOperationLog);
    }

    @Test
    public void testGetOperationLogsByConditions_Success() {
        Page<OperationLog> page = new PageImpl<>(testOperationLogs, PageRequest.of(0, 20), 1L);
        when(operationLogService.getOperationLogsByConditions(any(), any(), any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(page);

        ResponseEntity<BaseApiResponse<PagedResponse<OperationLog>>> response = operationLogController
                .getOperationLogsByConditions("testuser", "CREATE", "TestCaseSet", null, null, 0, 20);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertNotNull(response.getBody().getData());
        assertEquals(1, response.getBody().getData().getData().size());
        assertEquals(1L, response.getBody().getData().getTotal());
    }

    @Test
    public void testGetOperationLogsByConditions_ValidationError() {
        when(operationLogService.getOperationLogsByConditions(any(), any(), any(), any(), any(), anyInt(), anyInt()))
                .thenThrow(new IllegalArgumentException("Invalid parameters"));

        ResponseEntity<BaseApiResponse<PagedResponse<OperationLog>>> response = operationLogController
                .getOperationLogsByConditions("testuser", "CREATE", "TestCaseSet", null, null, 0, 20);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("VALIDATION_ERROR", response.getBody().getErrorCode());
    }

    @Test
    public void testGetOperationLogById_Success() {
        when(operationLogService.getOperationLogById(1L)).thenReturn(Optional.of(testOperationLog));

        ResponseEntity<BaseApiResponse<OperationLog>> response = operationLogController.getOperationLogById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals(testOperationLog, response.getBody().getData());
    }

    @Test
    public void testGetOperationLogById_NotFound() {
        when(operationLogService.getOperationLogById(1L)).thenReturn(Optional.empty());

        ResponseEntity<BaseApiResponse<OperationLog>> response = operationLogController.getOperationLogById(1L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("NOT_FOUND", response.getBody().getErrorCode());
    }

    @Test
    public void testSearchOperationLogs_Success() {
        Page<OperationLog> page = new PageImpl<>(testOperationLogs, PageRequest.of(0, 20), 1L);
        when(operationLogService.searchOperationLogs(any(), any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(page);

        ResponseEntity<BaseApiResponse<PagedResponse<OperationLog>>> response = operationLogController
                .searchOperationLogs("testuser", "CREATE", "TestCaseSet", "test", 0, 20);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertNotNull(response.getBody().getData());
        assertEquals(1, response.getBody().getData().getData().size());
    }

    @Test
    public void testGetRecentOperationLogs_Success() {
        when(operationLogService.getRecentOperationLogs(10)).thenReturn(testOperationLogs);

        ResponseEntity<BaseApiResponse<List<OperationLog>>> response = operationLogController.getRecentOperationLogs(10);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals(testOperationLogs, response.getBody().getData());
    }

    @Test
    public void testLogOperation_Success() {
        when(operationLogService.logOperation(any(), any(), any(), any())).thenReturn(testOperationLog);

        Map<String, String> request = new HashMap<>();
        request.put("username", "testuser");
        request.put("operationType", "CREATE");
        request.put("target", "TestCaseSet");
        request.put("description", "Created test case set");

        ResponseEntity<BaseApiResponse<OperationLog>> response = operationLogController.logOperation(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals(testOperationLog, response.getBody().getData());
    }

    @Test
    public void testLogOperation_MissingUsername() {
        Map<String, String> request = new HashMap<>();
        request.put("operationType", "CREATE");
        request.put("target", "TestCaseSet");

        ResponseEntity<BaseApiResponse<OperationLog>> response = operationLogController.logOperation(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("VALIDATION_ERROR", response.getBody().getErrorCode());
    }

    @Test
    public void testLogOperation_MissingOperationType() {
        Map<String, String> request = new HashMap<>();
        request.put("username", "testuser");
        request.put("target", "TestCaseSet");

        ResponseEntity<BaseApiResponse<OperationLog>> response = operationLogController.logOperation(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("VALIDATION_ERROR", response.getBody().getErrorCode());
    }

    @Test
    public void testLogOperation_MissingTarget() {
        Map<String, String> request = new HashMap<>();
        request.put("username", "testuser");
        request.put("operationType", "CREATE");

        ResponseEntity<BaseApiResponse<OperationLog>> response = operationLogController.logOperation(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("VALIDATION_ERROR", response.getBody().getErrorCode());
    }
}