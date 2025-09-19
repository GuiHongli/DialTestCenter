/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.cloududn.dialingtest.controller;

import com.huawei.cloududn.dialingtest.model.OperationTypeListResponse;
import com.huawei.cloududn.dialingtest.dao.OperationTypeDao;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * 操作类型控制器测试类
 *
 * @author g00940940
 * @since 2025-09-19
 */
@RunWith(MockitoJUnitRunner.class)
public class OperationTypeControllerTest {

    @Mock
    private OperationTypeDao operationTypeDao;

    @InjectMocks
    private OperationTypeController operationTypeController;

    private OperationTypeListResponse testResponse;

    @Before
    public void setUp() {
        testResponse = new OperationTypeListResponse();
        testResponse.setSuccess(true);
        testResponse.setMessage("查询成功");
        testResponse.setData(Arrays.asList());
    }

    @Test
    public void testOperationTypesGet_Success_ReturnsOk() {
        // Arrange
        when(operationTypeDao.getAllOperationTypes()).thenReturn(testResponse);

        // Act
        ResponseEntity<OperationTypeListResponse> response = operationTypeController.operationTypesGet();

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        verify(operationTypeDao).getAllOperationTypes();
    }

    @Test
    public void testOperationTypesGet_ServiceException_ReturnsInternalServerError() {
        // Arrange
        when(operationTypeDao.getAllOperationTypes()).thenThrow(new RuntimeException("Service error"));

        // Act
        ResponseEntity<OperationTypeListResponse> response = operationTypeController.operationTypesGet();

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        verify(operationTypeDao).getAllOperationTypes();
    }
}