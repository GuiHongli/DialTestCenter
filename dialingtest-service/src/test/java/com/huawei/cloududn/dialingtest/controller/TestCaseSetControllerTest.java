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
import com.huawei.cloududn.dialingtest.model.MissingScriptsResponse;
import com.huawei.cloududn.dialingtest.model.MissingScriptsResponseData;
import com.huawei.cloududn.dialingtest.service.TestCaseSetService;
import com.huawei.cloududn.dialingtest.util.OperationLogUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.*;

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
    private OperationLogUtil operationLogUtil;

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
        Map<String, Object> result = new HashMap<>();
        result.put("page", 1);
        result.put("pageSize", 10);
        result.put("total", 1L);
        result.put("data", testTestCaseSetList);

        when(testCaseSetService.getTestCaseSets(1, 10)).thenReturn(result);

        // Act
        ResponseEntity<TestCaseSetListResponse> response = testCaseSetController.testCaseSetsGet(1, 10);

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
        Map<String, Object> result = new HashMap<>();
        result.put("page", 1);
        result.put("pageSize", 10);
        result.put("total", 1L);
        result.put("data", testTestCaseSetList);

        when(testCaseSetService.getTestCaseSets(1, 10)).thenReturn(result);

        // Act
        ResponseEntity<TestCaseSetListResponse> response = testCaseSetController.testCaseSetsGet(null, null);

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
        ResponseEntity<TestCaseSetListResponse> response = testCaseSetController.testCaseSetsGet(1, 10);

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
        ResponseEntity<TestCaseSetResponse> response = testCaseSetController.testCaseSetsIdGet(1L);

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
        ResponseEntity<TestCaseSetResponse> response = testCaseSetController.testCaseSetsIdGet(1L);

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
        ResponseEntity<TestCaseSetResponse> response = testCaseSetController.testCaseSetsIdGet(1L);

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
        when(testCaseSetService.getTestCaseSetById(1L)).thenReturn(testTestCaseSet);

        // Act
        ResponseEntity<Resource> response = testCaseSetController.testCaseSetsIdDownloadGet(1L);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        verify(testCaseSetService, times(1)).getTestCaseSetById(1L);
    }

    /**
     * 测试下载用例集 - 用例集不存在
     */
    @Test
    public void testTestCaseSetsIdDownloadGet_NotFound_ReturnsNotFound() {
        // Arrange
        when(testCaseSetService.getTestCaseSetById(1L)).thenReturn(null);

        // Act
        ResponseEntity<Resource> response = testCaseSetController.testCaseSetsIdDownloadGet(1L);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

        verify(testCaseSetService, times(1)).getTestCaseSetById(1L);
    }

    /**
     * 测试更新用例集 - 成功场景
     */
    @Test
    public void testTestCaseSetsIdPut_Success_ReturnsOk() {
        // Arrange
        UpdateTestCaseSetRequest request = new UpdateTestCaseSetRequest();
        request.setDescription("Updated description");
        request.setBusinessZh("Updated business");

        when(testCaseSetService.updateTestCaseSet(1L, request, "admin")).thenReturn(testTestCaseSet);

        // Act
        ResponseEntity<TestCaseSetResponse> response = testCaseSetController.testCaseSetsIdPut("admin", 1L, request);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("更新用例集成功", response.getBody().getMessage());
        assertEquals(testTestCaseSet, response.getBody().getData());

        verify(testCaseSetService, times(1)).updateTestCaseSet(1L, request, "admin");
    }

    /**
     * 测试更新用例集 - 用例集不存在
     */
    @Test
    public void testTestCaseSetsIdPut_NotFound_ReturnsNotFound() {
        // Arrange
        UpdateTestCaseSetRequest request = new UpdateTestCaseSetRequest();
        when(testCaseSetService.updateTestCaseSet(1L, request, "admin")).thenReturn(null);

        // Act
        ResponseEntity<TestCaseSetResponse> response = testCaseSetController.testCaseSetsIdPut("admin", 1L, request);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("用例集不存在", response.getBody().getMessage());

        verify(testCaseSetService, times(1)).updateTestCaseSet(1L, request, "admin");
    }

    /**
     * 测试删除用例集 - 成功场景
     */
    @Test
    public void testTestCaseSetsIdDelete_Success_ReturnsOk() {
        // Arrange
        when(testCaseSetService.deleteTestCaseSet(1L, "admin")).thenReturn(true);

        // Act
        ResponseEntity<SuccessResponse> response = testCaseSetController.testCaseSetsIdDelete(1L, "admin");

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("删除用例集成功", response.getBody().getMessage());

        verify(testCaseSetService, times(1)).deleteTestCaseSet(1L, "admin");
    }

    /**
     * 测试删除用例集 - 用例集不存在
     */
    @Test
    public void testTestCaseSetsIdDelete_NotFound_ReturnsNotFound() {
        // Arrange
        when(testCaseSetService.deleteTestCaseSet(1L, "admin")).thenReturn(false);

        // Act
        ResponseEntity<SuccessResponse> response = testCaseSetController.testCaseSetsIdDelete(1L, "admin");

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("用例集不存在", response.getBody().getMessage());

        verify(testCaseSetService, times(1)).deleteTestCaseSet(1L, "admin");
    }

    /**
     * 测试获取测试用例列表 - 成功场景
     */
    @Test
    public void testTestCaseSetsIdTestCasesGet_Success_ReturnsOk() {
        // Arrange
        Map<String, Object> result = new HashMap<>();
        result.put("page", 1);
        result.put("pageSize", 10);
        result.put("total", 1L);
        result.put("data", testTestCaseList);

        when(testCaseSetService.getTestCases(1L, 1, 10)).thenReturn(result);

        // Act
        ResponseEntity<TestCaseListResponse> response = testCaseSetController.testCaseSetsIdTestCasesGet(1L, 1, 10);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("获取测试用例列表成功", response.getBody().getMessage());

        verify(testCaseSetService, times(1)).getTestCases(1L, 1, 10);
    }

    /**
     * 测试获取缺失脚本列表 - 成功场景
     */
    @Test
    public void testTestCaseSetsIdMissingScriptsGet_Success_ReturnsOk() {
        // Arrange
        when(testCaseSetService.getMissingScripts(1L)).thenReturn(testTestCaseList);

        // Act
        ResponseEntity<MissingScriptsResponse> response = testCaseSetController.testCaseSetsIdMissingScriptsGet(1L);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("获取缺失脚本列表成功", response.getBody().getMessage());
        assertNotNull(response.getBody().getData());
        assertEquals(Integer.valueOf(1), Integer.valueOf(response.getBody().getData().getCount()));

        verify(testCaseSetService, times(1)).getMissingScripts(1L);
    }
}
