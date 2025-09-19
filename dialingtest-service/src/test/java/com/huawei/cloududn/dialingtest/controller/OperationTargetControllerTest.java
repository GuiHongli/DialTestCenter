/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.cloududn.dialingtest.controller;

import com.huawei.cloududn.dialingtest.model.OperationTarget;
import com.huawei.cloududn.dialingtest.service.OperationTargetService;

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
 * 操作目标控制器测试类
 *
 * @author g00940940
 * @since 2025-09-19
 */
@RunWith(MockitoJUnitRunner.class)
public class OperationTargetControllerTest {

    @Mock
    private OperationTargetService operationTargetService;

    @InjectMocks
    private OperationTargetController operationTargetController;

    private OperationTarget testOperationTarget;

    @Before
    public void setUp() {
        testOperationTarget = new OperationTarget();
        testOperationTarget.setCode("USER");
        testOperationTarget.setNameZh("用户");
        testOperationTarget.setNameEn("User");
        testOperationTarget.setDescriptionZh("用户相关操作");
        testOperationTarget.setDescriptionEn("User related operations");
    }

    @Test
    public void testOperationTargetsGet_Success_ReturnsOperationTargets() {
        // Arrange
        List<OperationTarget> mockTargets = Arrays.asList(testOperationTarget);
        when(operationTargetService.getAllOperationTargets()).thenReturn(mockTargets);

        // Act
        ResponseEntity<List<OperationTarget>> response = operationTargetController.operationTargetsGet();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("USER", response.getBody().get(0).getCode());
        verify(operationTargetService).getAllOperationTargets();
    }

    @Test
    public void testOperationTargetsGet_ServiceException_ReturnsInternalServerError() {
        // Arrange
        when(operationTargetService.getAllOperationTargets()).thenThrow(new RuntimeException("Service error"));

        // Act
        ResponseEntity<List<OperationTarget>> response = operationTargetController.operationTargetsGet();

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    public void testOperationTargetsGet_EmptyList_ReturnsEmptyList() {
        // Arrange
        when(operationTargetService.getAllOperationTargets()).thenReturn(Arrays.asList());

        // Act
        ResponseEntity<List<OperationTarget>> response = operationTargetController.operationTargetsGet();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
        verify(operationTargetService).getAllOperationTargets();
    }

    @Test
    public void testOperationTargetsGet_MultipleTargets_ReturnsAllTargets() {
        // Arrange
        OperationTarget target2 = new OperationTarget();
        target2.setCode("SYSTEM");
        target2.setNameZh("系统");
        target2.setNameEn("System");
        
        List<OperationTarget> mockTargets = Arrays.asList(testOperationTarget, target2);
        when(operationTargetService.getAllOperationTargets()).thenReturn(mockTargets);

        // Act
        ResponseEntity<List<OperationTarget>> response = operationTargetController.operationTargetsGet();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertEquals("USER", response.getBody().get(0).getCode());
        assertEquals("SYSTEM", response.getBody().get(1).getCode());
        verify(operationTargetService).getAllOperationTargets();
    }
}
