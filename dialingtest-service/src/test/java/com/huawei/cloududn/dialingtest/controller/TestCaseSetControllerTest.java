/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.cloududn.dialingtest.controller;

import com.huawei.cloududn.dialingtest.model.TestCaseSet;
import com.huawei.cloududn.dialingtest.model.TestCase;
import com.huawei.cloududn.dialingtest.model.TestCaseSetListResponse;
import com.huawei.cloududn.dialingtest.model.TestCaseSetListResponseData;
import com.huawei.cloududn.dialingtest.model.TestCaseSetResponse;
import com.huawei.cloududn.dialingtest.model.UpdateTestCaseSetRequest;
import com.huawei.cloududn.dialingtest.model.SuccessResponse;
import com.huawei.cloududn.dialingtest.model.TestCaseListResponse;
import com.huawei.cloududn.dialingtest.model.TestCaseListResponseData;
import com.huawei.cloududn.dialingtest.model.ValidationTaskResponse;
import com.huawei.cloududn.dialingtest.model.ValidationResponse;
import com.huawei.cloududn.dialingtest.entity.ValidationTask;
import com.huawei.cloududn.dialingtest.service.TestCaseSetService;
import com.huawei.cloududn.dialingtest.service.TestCaseValidationService;
import com.huawei.cloududn.dialingtest.service.UserRoleService;
import com.huawei.cloududn.dialingtest.util.OperationLogUtil;
import com.huawei.cloududn.dialingtest.util.PermissionValidator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * TestCaseSetController 单元测试
 * 
 * @author g00940940
 * @since 2025-01-27
 */
@RunWith(MockitoJUnitRunner.class)
public class TestCaseSetControllerTest {

    @Mock
    private TestCaseSetService testCaseSetService;

    @Mock
    private TestCaseValidationService testCaseValidationService;

    @Mock
    private UserRoleService userRoleService;

    @Mock
    private OperationLogUtil operationLogUtil;

    @Mock
    private PermissionValidator permissionValidator;

    @InjectMocks
    private TestCaseSetController testCaseSetController;

    private TestCaseSet testTestCaseSet;
    private List<TestCaseSet> testTestCaseSetList;
    private List<TestCase> testTestCaseList;

    @Before
    public void setUp() {
        // 初始化测试数据
        testTestCaseSet = new TestCaseSet();
        testTestCaseSet.setId(1L);
        testTestCaseSet.setName("test-case");
        testTestCaseSet.setVersion("v1.0");
        testTestCaseSet.setDescription("Test case set");
        testTestCaseSet.setFileContent("test content".getBytes());

        testTestCaseSetList = Arrays.asList(testTestCaseSet);

        TestCase testCase = new TestCase();
        testCase.setId(1L);
        testCase.setTestCaseSetId(1L);
        testCase.setCaseName("Test Case");
        testCase.setCaseNumber("TC001");
        testTestCaseList = Arrays.asList(testCase);
    }

    /**
     * 测试获取用例集列表 - 成功场景
     */
    @Test
    public void testTestCaseSetsGet_Success_ReturnsOk() {
        // Arrange
        TestCaseSetListResponseData data = new TestCaseSetListResponseData();
        data.setPage(1);
        data.setPageSize(10);
        data.setTotal(1);
        data.setData(testTestCaseSetList);

        when(testCaseSetService.getTestCaseSets(1, 10)).thenReturn(data);

        // Act
        ResponseEntity<TestCaseSetListResponse> response = testCaseSetController.getTestCaseSets(1, 10);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("获取用例集列表成功", response.getBody().getMessage());
        assertNotNull(response.getBody().getData());

        verify(testCaseSetService, times(1)).getTestCaseSets(1, 10);
    }

    /**
     * 测试获取用例集列表 - 默认参数
     */
    @Test
    public void testTestCaseSetsGet_DefaultParams_ReturnsOk() {
        // Arrange
        TestCaseSetListResponseData data = new TestCaseSetListResponseData();
        data.setPage(1);
        data.setPageSize(10);
        data.setTotal(1);
        data.setData(testTestCaseSetList);

        when(testCaseSetService.getTestCaseSets(1, 10)).thenReturn(data);

        // Act
        ResponseEntity<TestCaseSetListResponse> response = testCaseSetController.getTestCaseSets(null, null);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());

        verify(testCaseSetService, times(1)).getTestCaseSets(1, 10);
    }

    /**
     * 测试获取用例集列表 - 服务异常
     */
    @Test
    public void testTestCaseSetsGet_ServiceException_ReturnsInternalServerError() {
        // Arrange
        when(testCaseSetService.getTestCaseSets(1, 10)).thenThrow(new RuntimeException("Database error"));

        // Act
        ResponseEntity<TestCaseSetListResponse> response = testCaseSetController.getTestCaseSets(1, 10);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertTrue(response.getBody().getMessage().contains("获取用例集列表失败"));
    }

    /**
     * 测试获取用例集详情 - 成功场景
     */
    @Test
    public void testTestCaseSetsIdGet_Success_ReturnsOk() {
        // Arrange
        when(testCaseSetService.getTestCaseSetById(1L)).thenReturn(testTestCaseSet);

        // Act
        ResponseEntity<TestCaseSetResponse> response = testCaseSetController.getTestCaseSetById(1L);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("获取用例集详情成功", response.getBody().getMessage());
        assertEquals(testTestCaseSet, response.getBody().getData());

        verify(testCaseSetService, times(1)).getTestCaseSetById(1L);
    }

    /**
     * 测试获取用例集详情 - 用例集不存在
     */
    @Test
    public void testTestCaseSetsIdGet_NotFound_ReturnsNotFound() {
        // Arrange
        when(testCaseSetService.getTestCaseSetById(1L)).thenReturn(null);

        // Act
        ResponseEntity<TestCaseSetResponse> response = testCaseSetController.getTestCaseSetById(1L);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("用例集不存在", response.getBody().getMessage());

        verify(testCaseSetService, times(1)).getTestCaseSetById(1L);
    }

    /**
     * 测试获取用例集详情 - 服务异常
     */
    @Test
    public void testTestCaseSetsIdGet_ServiceException_ReturnsInternalServerError() {
        // Arrange
        when(testCaseSetService.getTestCaseSetById(1L)).thenThrow(new RuntimeException("Database error"));

        // Act
        ResponseEntity<TestCaseSetResponse> response = testCaseSetController.getTestCaseSetById(1L);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertTrue(response.getBody().getMessage().contains("获取用例集详情失败"));
    }

    /**
     * 测试下载用例集 - 成功场景
     */
    @Test
    public void testTestCaseSetsIdDownloadGet_Success_ReturnsOk() {
        // Arrange
        String username = "admin";
        PermissionValidator.PermissionValidationResult successResult = 
            PermissionValidator.PermissionValidationResult.success();
        when(permissionValidator.checkAdminOrOperator(username, "下载用例集"))
            .thenReturn(successResult);
        
        when(testCaseSetService.getTestCaseSetById(1L)).thenReturn(testTestCaseSet);

        // Act
        ResponseEntity<Resource> response = testCaseSetController.downloadTestCaseSet(1L, username);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        verify(permissionValidator).checkAdminOrOperator(username, "下载用例集");
        verify(testCaseSetService, times(1)).getTestCaseSetById(1L);
    }

    /**
     * 测试下载用例集 - 用例集不存在
     */
    @Test
    public void testTestCaseSetsIdDownloadGet_NotFound_ReturnsNotFound() {
        // Arrange
        String username = "admin";
        PermissionValidator.PermissionValidationResult successResult = 
            PermissionValidator.PermissionValidationResult.success();
        when(permissionValidator.checkAdminOrOperator(username, "下载用例集"))
            .thenReturn(successResult);
        
        when(testCaseSetService.getTestCaseSetById(1L)).thenReturn(null);

        // Act
        ResponseEntity<Resource> response = testCaseSetController.downloadTestCaseSet(1L, username);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

        verify(permissionValidator).checkAdminOrOperator(username, "下载用例集");
        verify(testCaseSetService, times(1)).getTestCaseSetById(1L);
    }

    /**
     * 测试更新用例集 - 成功场景
     */
    @Test
    public void testTestCaseSetsIdPut_Success_ReturnsOk() {
        // Arrange
        String username = "admin";
        UpdateTestCaseSetRequest request = new UpdateTestCaseSetRequest();
        request.setDescription("Updated description");
        request.setBusinessZh("Updated business");

        PermissionValidator.PermissionValidationResult successResult = 
            PermissionValidator.PermissionValidationResult.success();
        when(permissionValidator.checkAdminOrOperator(username, "更新用例集"))
            .thenReturn(successResult);

        when(testCaseSetService.updateTestCaseSet(1L, request, username)).thenReturn(testTestCaseSet);

        // Act
        ResponseEntity<TestCaseSetResponse> response = testCaseSetController.updateTestCaseSet(username, 1L, request);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("更新用例集成功", response.getBody().getMessage());
        assertEquals(testTestCaseSet, response.getBody().getData());

        verify(permissionValidator).checkAdminOrOperator(username, "更新用例集");
        verify(testCaseSetService, times(1)).updateTestCaseSet(1L, request, username);
    }

    /**
     * 测试更新用例集 - 用例集不存在
     */
    @Test
    public void testTestCaseSetsIdPut_NotFound_ReturnsNotFound() {
        // Arrange
        String username = "admin";
        UpdateTestCaseSetRequest request = new UpdateTestCaseSetRequest();
        
        PermissionValidator.PermissionValidationResult successResult = 
            PermissionValidator.PermissionValidationResult.success();
        when(permissionValidator.checkAdminOrOperator(username, "更新用例集"))
            .thenReturn(successResult);
        
        when(testCaseSetService.updateTestCaseSet(1L, request, username)).thenReturn(null);

        // Act
        ResponseEntity<TestCaseSetResponse> response = testCaseSetController.updateTestCaseSet(username, 1L, request);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("用例集不存在", response.getBody().getMessage());

        verify(permissionValidator).checkAdminOrOperator(username, "更新用例集");
        verify(testCaseSetService, times(1)).updateTestCaseSet(1L, request, username);
    }

    /**
     * 测试删除用例集 - 成功场景
     */
    @Test
    public void testTestCaseSetsIdDelete_Success_ReturnsOk() {
        // Arrange
        String username = "admin";
        PermissionValidator.PermissionValidationResult successResult = 
            PermissionValidator.PermissionValidationResult.success();
        when(permissionValidator.checkAdminOrOperator(username, "删除用例集"))
            .thenReturn(successResult);
        
        when(testCaseSetService.deleteTestCaseSet(1L, username)).thenReturn(true);

        // Act
        ResponseEntity<SuccessResponse> response = testCaseSetController.deleteTestCaseSet(1L, username);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("删除用例集成功", response.getBody().getMessage());

        verify(permissionValidator).checkAdminOrOperator(username, "删除用例集");
        verify(testCaseSetService, times(1)).deleteTestCaseSet(1L, username);
    }

    /**
     * 测试删除用例集 - 用例集不存在
     */
    @Test
    public void testTestCaseSetsIdDelete_NotFound_ReturnsNotFound() {
        // Arrange
        String username = "admin";
        PermissionValidator.PermissionValidationResult successResult = 
            PermissionValidator.PermissionValidationResult.success();
        when(permissionValidator.checkAdminOrOperator(username, "删除用例集"))
            .thenReturn(successResult);
        
        when(testCaseSetService.deleteTestCaseSet(1L, username)).thenReturn(false);

        // Act
        ResponseEntity<SuccessResponse> response = testCaseSetController.deleteTestCaseSet(1L, username);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("用例集不存在", response.getBody().getMessage());

        verify(permissionValidator).checkAdminOrOperator(username, "删除用例集");
        verify(testCaseSetService, times(1)).deleteTestCaseSet(1L, username);
    }

    /**
     * 测试获取测试用例列表 - 成功场景
     */
    @Test
    public void testTestCaseSetsIdTestCasesGet_Success_ReturnsOk() {
        // Arrange
        TestCaseListResponseData data = new TestCaseListResponseData();
        data.setPage(1);
        data.setPageSize(10);
        data.setTotal(1);
        data.setData(testTestCaseList);

        when(testCaseSetService.getTestCases(1L, 1, 10)).thenReturn(data);

        // Act
        ResponseEntity<TestCaseListResponse> response = testCaseSetController.getTestCasesByTestCaseSetId(1L, 1, 10);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("获取测试用例列表成功", response.getBody().getMessage());

        verify(testCaseSetService, times(1)).getTestCases(1L, 1, 10);
    }

    /**
     * 测试触发用例集校验 - 成功场景
     */
    @Test
    public void testTriggerTestCaseSetValidation_Success_ReturnsAccepted() {
        // Arrange
        String username = "admin";
        TestCaseValidationService.ValidationTaskInfo taskInfo = 
            new TestCaseValidationService.ValidationTaskInfo();
        taskInfo.setTaskId("task-123");
        taskInfo.setTestCaseSetId(1L);
        taskInfo.setStatus("PENDING");
        taskInfo.setEstimatedTime(5);

        PermissionValidator.PermissionValidationResult successResult = 
            PermissionValidator.PermissionValidationResult.success();
        when(permissionValidator.checkAdminOrOperator(username, "触发用例集校验"))
            .thenReturn(successResult);
        
        when(testCaseValidationService.triggerValidation(1L)).thenReturn(taskInfo);
        when(testCaseSetService.getTestCaseSetById(1L)).thenReturn(testTestCaseSet);
        doNothing().when(operationLogUtil).logTestCaseSetValidation(anyString(), any(TestCaseSet.class));

        // Act
        ResponseEntity<ValidationTaskResponse> response = 
            testCaseSetController.triggerTestCaseSetValidation(1L, "token", username);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("校验任务已提交", response.getBody().getMessage());
        assertNotNull(response.getBody().getData());
        assertEquals("task-123", response.getBody().getData().getTaskId());

        verify(permissionValidator).checkAdminOrOperator(username, "触发用例集校验");
        verify(testCaseValidationService, times(1)).triggerValidation(1L);
    }

    /**
     * 测试触发用例集校验 - 权限不足
     */
    @Test
    public void testTriggerTestCaseSetValidation_Unauthorized_ReturnsForbidden() {
        // Arrange
        String username = "user";
        PermissionValidator.PermissionValidationResult failureResult = 
            PermissionValidator.PermissionValidationResult.failure("权限不足，触发用例集校验需要管理员或操作员权限");
        when(permissionValidator.checkAdminOrOperator(username, "触发用例集校验"))
            .thenReturn(failureResult);

        // Act
        ResponseEntity<ValidationTaskResponse> response = 
            testCaseSetController.triggerTestCaseSetValidation(1L, "token", username);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertTrue(response.getBody().getMessage().contains("权限不足"));

        verify(permissionValidator).checkAdminOrOperator(username, "触发用例集校验");
        verify(testCaseValidationService, never()).triggerValidation(anyLong());
    }

    /**
     * 测试触发用例集校验 - 用例集不存在
     */
    @Test
    public void testTriggerTestCaseSetValidation_NotFound_ReturnsNotFound() {
        // Arrange
        String username = "admin";
        PermissionValidator.PermissionValidationResult successResult = 
            PermissionValidator.PermissionValidationResult.success();
        when(permissionValidator.checkAdminOrOperator(username, "触发用例集校验"))
            .thenReturn(successResult);
        
        when(testCaseValidationService.triggerValidation(1L))
            .thenThrow(new IllegalArgumentException("Test case set not found: 1"));

        // Act
        ResponseEntity<ValidationTaskResponse> response = 
            testCaseSetController.triggerTestCaseSetValidation(1L, "token", username);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("用例集不存在", response.getBody().getMessage());
        
        verify(permissionValidator).checkAdminOrOperator(username, "触发用例集校验");
    }

    /**
     * 测试触发用例集校验 - 用户名为null
     */
    @Test
    public void testTriggerTestCaseSetValidation_NullUsername_ReturnsUnauthorized() {
        // Arrange
        String username = null;
        PermissionValidator.PermissionValidationResult failureResult = 
            PermissionValidator.PermissionValidationResult.failure("未提供用户名");
        when(permissionValidator.checkAdminOrOperator(username, "触发用例集校验"))
            .thenReturn(failureResult);

        // Act
        ResponseEntity<ValidationTaskResponse> response = 
            testCaseSetController.triggerTestCaseSetValidation(1L, "token", username);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("未提供用户名", response.getBody().getMessage());

        verify(permissionValidator).checkAdminOrOperator(username, "触发用例集校验");
        verify(testCaseValidationService, never()).triggerValidation(anyLong());
    }

    /**
     * 测试触发用例集校验 - 服务异常
     */
    @Test
    public void testTriggerTestCaseSetValidation_ServiceException_ReturnsInternalServerError() {
        // Arrange
        String username = "admin";
        PermissionValidator.PermissionValidationResult successResult = 
            PermissionValidator.PermissionValidationResult.success();
        when(permissionValidator.checkAdminOrOperator(username, "触发用例集校验"))
            .thenReturn(successResult);
        
        when(testCaseValidationService.triggerValidation(1L))
            .thenThrow(new RuntimeException("Service error"));

        // Act
        ResponseEntity<ValidationTaskResponse> response = 
            testCaseSetController.triggerTestCaseSetValidation(1L, "token", username);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertTrue(response.getBody().getMessage().contains("触发校验任务失败"));

        verify(permissionValidator).checkAdminOrOperator(username, "触发用例集校验");
    }

    /**
     * 测试获取用例集校验结果 - 成功场景
     */
    @Test
    public void testGetTestCaseSetValidation_Success_ReturnsOk() {
        // Arrange
        com.huawei.cloududn.dialingtest.model.ValidationResult serviceResult = 
            new com.huawei.cloududn.dialingtest.model.ValidationResult();
        serviceResult.setTestCaseSetId(1L);
        serviceResult.setTestCaseSetName("测试用例集");
        serviceResult.setTotalCaseCount(2);
        serviceResult.setPassedCaseCount(1);
        serviceResult.setFailedCaseCount(1);
        serviceResult.setMatchRate(50.0);

        com.huawei.cloududn.dialingtest.model.CaseValidationResult caseResult = 
            new com.huawei.cloududn.dialingtest.model.CaseValidationResult();
        caseResult.setCaseNumber("TC001");
        caseResult.setCaseName("测试用例1");
        caseResult.setScriptMatchValid(true);
        serviceResult.addCaseResult(caseResult);

        ValidationTask task = new ValidationTask();
        task.setStatus("COMPLETED");
        task.setCreatedTime(java.time.LocalDateTime.now());

        when(testCaseValidationService.getValidationResult(1L)).thenReturn(serviceResult);
        when(testCaseValidationService.getTaskStatus(1L)).thenReturn(task);

        // Act
        ResponseEntity<ValidationResponse> response = 
            testCaseSetController.getTestCaseSetValidation(1L, "token", false);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertNotNull(response.getBody().getData());
        assertEquals(Long.valueOf(1L), response.getBody().getData().getTestCaseSetId());
        assertEquals(Integer.valueOf(2), response.getBody().getData().getTotalCaseCount());
    }

    /**
     * 测试获取用例集校验结果 - 强制刷新
     */
    @Test
    public void testGetTestCaseSetValidation_ForceRefresh_ReturnsOk() {
        // Arrange
        com.huawei.cloududn.dialingtest.model.ValidationResult serviceResult = 
            new com.huawei.cloududn.dialingtest.model.ValidationResult();
        serviceResult.setTestCaseSetId(1L);
        serviceResult.setTotalCaseCount(1);

        ValidationTask task = new ValidationTask();
        task.setStatus("COMPLETED");

        when(testCaseValidationService.validateTestCaseSet(1L)).thenReturn(serviceResult);
        when(testCaseValidationService.getTaskStatus(1L)).thenReturn(task);

        // Act
        ResponseEntity<ValidationResponse> response = 
            testCaseSetController.getTestCaseSetValidation(1L, "token", true);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(testCaseValidationService, times(1)).validateTestCaseSet(1L);
    }

    /**
     * 测试获取用例集校验结果 - 结果不存在
     */
    @Test
    public void testGetTestCaseSetValidation_NotFound_ReturnsNotFound() {
        // Arrange
        when(testCaseValidationService.getValidationResult(1L)).thenReturn(null);
        when(testCaseValidationService.getTaskStatus(1L)).thenReturn(null);

        // Act
        ResponseEntity<ValidationResponse> response = 
            testCaseSetController.getTestCaseSetValidation(1L, "token", false);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
    }

    /**
     * 测试获取用例集校验结果 - 任务正在执行中
     */
    @Test
    public void testGetTestCaseSetValidation_TaskRunning_ReturnsOk() {
        // Arrange
        ValidationTask task = new ValidationTask();
        task.setStatus("RUNNING");
        task.setCreatedTime(java.time.LocalDateTime.now());

        when(testCaseValidationService.getValidationResult(1L)).thenReturn(null);
        when(testCaseValidationService.getTaskStatus(1L)).thenReturn(task);

        // Act
        ResponseEntity<ValidationResponse> response = 
            testCaseSetController.getTestCaseSetValidation(1L, "token", false);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertTrue(response.getBody().getMessage().contains("校验任务正在执行中"));
    }

    /**
     * 测试获取用例集校验结果 - forceRefresh为null
     */
    @Test
    public void testGetTestCaseSetValidation_NullForceRefresh_ReturnsOk() {
        // Arrange
        com.huawei.cloududn.dialingtest.model.ValidationResult serviceResult = 
            new com.huawei.cloududn.dialingtest.model.ValidationResult();
        serviceResult.setTestCaseSetId(1L);
        serviceResult.setTotalCaseCount(1);

        ValidationTask task = new ValidationTask();
        task.setStatus("COMPLETED");

        when(testCaseValidationService.getValidationResult(1L)).thenReturn(serviceResult);
        when(testCaseValidationService.getTaskStatus(1L)).thenReturn(task);

        // Act
        ResponseEntity<ValidationResponse> response = 
            testCaseSetController.getTestCaseSetValidation(1L, "token", null);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        verify(testCaseValidationService, times(1)).getValidationResult(1L);
        verify(testCaseValidationService, never()).validateTestCaseSet(anyLong());
    }

    /**
     * 测试获取用例集校验结果 - IllegalArgumentException异常
     */
    @Test
    public void testGetTestCaseSetValidation_IllegalArgumentException_ReturnsNotFound() {
        // Arrange
        when(testCaseValidationService.getValidationResult(1L))
            .thenThrow(new IllegalArgumentException("Test case set not found"));

        // Act
        ResponseEntity<ValidationResponse> response = 
            testCaseSetController.getTestCaseSetValidation(1L, "token", false);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("用例集不存在", response.getBody().getMessage());
    }

    /**
     * 测试获取用例集校验结果 - 服务异常
     */
    @Test
    public void testGetTestCaseSetValidation_ServiceException_ReturnsInternalServerError() {
        // Arrange
        when(testCaseValidationService.getValidationResult(1L))
            .thenThrow(new RuntimeException("Service error"));

        // Act
        ResponseEntity<ValidationResponse> response = 
            testCaseSetController.getTestCaseSetValidation(1L, "token", false);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertTrue(response.getBody().getMessage().contains("获取校验结果失败"));
    }

    /**
     * 测试导出用例集校验结果 - 成功场景
     */
    @Test
    public void testExportTestCaseSetValidation_Success_ReturnsOk() {
        // Arrange
        String username = "admin";
        PermissionValidator.PermissionValidationResult successResult = 
            PermissionValidator.PermissionValidationResult.success();
        when(permissionValidator.checkAdminOrOperator(username, "导出用例集校验结果"))
            .thenReturn(successResult);
        
        com.huawei.cloududn.dialingtest.model.ValidationResult serviceResult = 
            new com.huawei.cloududn.dialingtest.model.ValidationResult();
        serviceResult.setTestCaseSetId(1L);
        serviceResult.setTestCaseSetName("测试用例集");
        serviceResult.setTestCaseSetVersion("v1.0");

        when(testCaseValidationService.getValidationResult(1L)).thenReturn(serviceResult);

        // Act
        ResponseEntity<Resource> response = 
            testCaseSetController.exportTestCaseSetValidation(1L, "token", username);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(permissionValidator).checkAdminOrOperator(username, "导出用例集校验结果");
        verify(testCaseValidationService, times(1)).getValidationResult(1L);
    }

    /**
     * 测试导出用例集校验结果 - 结果不存在
     */
    @Test
    public void testExportTestCaseSetValidation_NotFound_ReturnsNotFound() {
        // Arrange
        String username = "admin";
        PermissionValidator.PermissionValidationResult successResult = 
            PermissionValidator.PermissionValidationResult.success();
        when(permissionValidator.checkAdminOrOperator(username, "导出用例集校验结果"))
            .thenReturn(successResult);
        
        when(testCaseValidationService.getValidationResult(1L)).thenReturn(null);

        // Act
        ResponseEntity<Resource> response = 
            testCaseSetController.exportTestCaseSetValidation(1L, "token", username);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(permissionValidator).checkAdminOrOperator(username, "导出用例集校验结果");
    }

    /**
     * 测试导出用例集校验结果 - 权限不足
     */
    @Test
    public void testExportTestCaseSetValidation_NoPermission_ReturnsForbidden() {
        // Arrange
        String username = "user";
        PermissionValidator.PermissionValidationResult failureResult = 
            PermissionValidator.PermissionValidationResult.failure("权限不足，导出用例集校验结果需要管理员或操作员权限");
        when(permissionValidator.checkAdminOrOperator(username, "导出用例集校验结果"))
            .thenReturn(failureResult);

        // Act
        ResponseEntity<Resource> response = 
            testCaseSetController.exportTestCaseSetValidation(1L, "token", username);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        verify(permissionValidator).checkAdminOrOperator(username, "导出用例集校验结果");
        verify(testCaseValidationService, never()).getValidationResult(anyLong());
    }

    /**
     * 测试导出用例集校验结果 - 用户名为null
     */
    @Test
    public void testExportTestCaseSetValidation_NullUsername_ReturnsUnauthorized() {
        // Arrange
        String username = null;
        PermissionValidator.PermissionValidationResult failureResult = 
            PermissionValidator.PermissionValidationResult.failure("未提供用户名");
        when(permissionValidator.checkAdminOrOperator(username, "导出用例集校验结果"))
            .thenReturn(failureResult);

        // Act
        ResponseEntity<Resource> response = 
            testCaseSetController.exportTestCaseSetValidation(1L, "token", username);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        verify(permissionValidator).checkAdminOrOperator(username, "导出用例集校验结果");
        verify(testCaseValidationService, never()).getValidationResult(anyLong());
    }

    /**
     * 测试导出用例集校验结果 - IllegalArgumentException异常
     */
    @Test
    public void testExportTestCaseSetValidation_IllegalArgumentException_ReturnsNotFound() {
        // Arrange
        String username = "admin";
        PermissionValidator.PermissionValidationResult successResult = 
            PermissionValidator.PermissionValidationResult.success();
        when(permissionValidator.checkAdminOrOperator(username, "导出用例集校验结果"))
            .thenReturn(successResult);
        
        when(testCaseValidationService.getValidationResult(1L))
            .thenThrow(new IllegalArgumentException("Test case set not found"));

        // Act
        ResponseEntity<Resource> response = 
            testCaseSetController.exportTestCaseSetValidation(1L, "token", username);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(permissionValidator).checkAdminOrOperator(username, "导出用例集校验结果");
    }

    /**
     * 测试导出用例集校验结果 - 服务异常
     */
    @Test
    public void testExportTestCaseSetValidation_ServiceException_ReturnsInternalServerError() {
        // Arrange
        String username = "admin";
        PermissionValidator.PermissionValidationResult successResult = 
            PermissionValidator.PermissionValidationResult.success();
        when(permissionValidator.checkAdminOrOperator(username, "导出用例集校验结果"))
            .thenReturn(successResult);
        
        when(testCaseValidationService.getValidationResult(1L))
            .thenThrow(new RuntimeException("Service error"));

        // Act
        ResponseEntity<Resource> response = 
            testCaseSetController.exportTestCaseSetValidation(1L, "token", username);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(permissionValidator).checkAdminOrOperator(username, "导出用例集校验结果");
    }
}



