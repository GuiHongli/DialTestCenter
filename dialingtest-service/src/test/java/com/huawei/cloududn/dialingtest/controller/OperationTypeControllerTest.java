/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.cloududn.dialingtest.controller;

import com.huawei.cloududn.dialingtest.model.OperationType;
import com.huawei.cloududn.dialingtest.service.OperationTypeService;

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
    private OperationTypeService operationTypeService;

    @InjectMocks
    private OperationTypeController operationTypeController;

    private OperationType testOperationType;

    @Before
    public void setUp() {
        testOperationType = new OperationType();
        testOperationType.setCode("CREATE");
        testOperationType.setNameZh("创建");
        testOperationType.setNameEn("Create");
        testOperationType.setDescriptionZh("创建操作");
        testOperationType.setDescriptionEn("Create operation");
    }

    @Test
    public void testOperationTypesGet_Success_ReturnsOperationTypes() {
        // Arrange
        List<OperationType> mockTypes = Arrays.asList(testOperationType);
        when(operationTypeService.getAllOperationTypes()).thenReturn(mockTypes);

        // Act
        ResponseEntity<List<OperationType>> response = operationTypeController.operationTypesGet();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("CREATE", response.getBody().get(0).getCode());
        verify(operationTypeService).getAllOperationTypes();
    }

    @Test
    public void testOperationTypesGet_ServiceException_ReturnsInternalServerError() {
        // Arrange
        when(operationTypeService.getAllOperationTypes()).thenThrow(new RuntimeException("Service error"));

        // Act
        ResponseEntity<List<OperationType>> response = operationTypeController.operationTypesGet();

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    public void testOperationTypesGet_EmptyList_ReturnsEmptyList() {
        // Arrange
        when(operationTypeService.getAllOperationTypes()).thenReturn(Arrays.asList());

        // Act
        ResponseEntity<List<OperationType>> response = operationTypeController.operationTypesGet();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
        verify(operationTypeService).getAllOperationTypes();
    }

    @Test
    public void testOperationTypesGet_MultipleTypes_ReturnsAllTypes() {
        // Arrange
        OperationType type2 = new OperationType();
        type2.setCode("UPDATE");
        type2.setNameZh("更新");
        type2.setNameEn("Update");
        
        List<OperationType> mockTypes = Arrays.asList(testOperationType, type2);
        when(operationTypeService.getAllOperationTypes()).thenReturn(mockTypes);

        // Act
        ResponseEntity<List<OperationType>> response = operationTypeController.operationTypesGet();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertEquals("CREATE", response.getBody().get(0).getCode());
        assertEquals("UPDATE", response.getBody().get(1).getCode());
        verify(operationTypeService).getAllOperationTypes();
    }
}
