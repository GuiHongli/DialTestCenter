/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.cloududn.dialingtest.controller;

import com.huawei.cloududn.dialingtest.model.TestCaseSet;
import com.huawei.cloududn.dialingtest.model.TestCase;
import com.huawei.cloududn.dialingtest.model.TestCaseSetListResponse;
import com.huawei.cloududn.dialingtest.model.TestCaseSetListResponseData;
import com.huawei.cloududn.dialingtest.model.TestCaseSetResponse;
import com.huawei.cloududn.dialingtest.model.TestCaseSetUploadResponse;
import com.huawei.cloududn.dialingtest.model.TestCaseSetUploadRequest;
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
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * TestCaseSetController 单元测试
 * 
 * @author Generated
 * @since 2024-01-XX
 */
@RunWith(MockitoJUnitRunner.class)
public class TestCaseSetControllerTest {

    @Mock
    private TestCaseSetService testCaseSetService;

    @Mock
    private OperationLogUtil operationLogUtil;

    @Mock
    private MultipartFile mockFile;

    @InjectMocks
    private TestCaseSetController testCaseSetController;

    private TestCaseSet testTestCaseSet;
    private TestCase testTestCase;
    private Map<String, Object> mockResult;

    @Before
    public void setUp() {
        // 初始化测试数据
        testTestCaseSet = createTestTestCaseSet();
        testTestCase = createTestTestCase();
        mockResult = createMockResult();
    }

    /**
     * 测试获取用例集列表 - 成功场景
     */
    @Test
    public void testTestCaseSetsGet_Success_ReturnsOk() {
        // Arrange
        when(testCaseSetService.getTestCaseSets(1, 10)).thenReturn(mockResult);

        // Act
        ResponseEntity<TestCaseSetListResponse> response = testCaseSetController.testCaseSetsGet(1, 10);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("获取用例集列表成功", response.getBody().getMessage());
        assertNotNull(response.getBody().getData());
        assertEquals(1, response.getBody().getData().getPage().intValue());
        assertEquals(10, response.getBody().getData().getPageSize().intValue());
        assertEquals(2, response.getBody().getData().getTotal().intValue());
        assertEquals(2, response.getBody().getData().getData().size());

        verify(testCaseSetService, times(1)).getTestCaseSets(1, 10);
    }

    /**
     * 测试获取用例集列表 - 默认分页参数
     */
    @Test
    public void testTestCaseSetsGet_WithNullParams_UsesDefaults() {
        // Arrange
        when(testCaseSetService.getTestCaseSets(1, 10)).thenReturn(mockResult);

        // Act
        ResponseEntity<TestCaseSetListResponse> response = testCaseSetController.testCaseSetsGet(null, null);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(testCaseSetService, times(1)).getTestCaseSets(1, 10);
    }

    /**
     * 测试获取用例集列表 - 服务异常
     */
    @Test
    public void testTestCaseSetsGet_ServiceException_ReturnsInternalServerError() {
        // Arrange
        when(testCaseSetService.getTestCaseSets(1, 10)).thenThrow(new RuntimeException("数据库连接失败"));

        // Act
        ResponseEntity<TestCaseSetListResponse> response = testCaseSetController.testCaseSetsGet(1, 10);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertTrue(response.getBody().getMessage().contains("获取用例集列表失败"));
        assertTrue(response.getBody().getMessage().contains("数据库连接失败"));
    }

    /**
     * 测试根据ID获取用例集 - 成功场景
     */
    @Test
    public void testTestCaseSetsIdGet_Success_ReturnsOk() {
        // Arrange
        Long testId = 1L;
        when(testCaseSetService.getTestCaseSetById(testId)).thenReturn(testTestCaseSet);

        // Act
        ResponseEntity<TestCaseSetResponse> response = testCaseSetController.testCaseSetsIdGet(testId);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("获取用例集详情成功", response.getBody().getMessage());
        assertNotNull(response.getBody().getData());
        assertEquals(testTestCaseSet.getId(), response.getBody().getData().getId());

        verify(testCaseSetService, times(1)).getTestCaseSetById(testId);
    }

    /**
     * 测试根据ID获取用例集 - 用例集不存在
     */
    @Test
    public void testTestCaseSetsIdGet_TestCaseSetNotFound_ReturnsNotFound() {
        // Arrange
        Long testId = 999L;
        when(testCaseSetService.getTestCaseSetById(testId)).thenReturn(null);

        // Act
        ResponseEntity<TestCaseSetResponse> response = testCaseSetController.testCaseSetsIdGet(testId);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("用例集不存在", response.getBody().getMessage());
    }

    /**
     * 测试根据ID获取用例集 - 服务异常
     */
    @Test
    public void testTestCaseSetsIdGet_ServiceException_ReturnsInternalServerError() {
        // Arrange
        Long testId = 1L;
        when(testCaseSetService.getTestCaseSetById(testId)).thenThrow(new RuntimeException("数据库错误"));

        // Act
        ResponseEntity<TestCaseSetResponse> response = testCaseSetController.testCaseSetsIdGet(testId);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertTrue(response.getBody().getMessage().contains("获取用例集详情失败"));
    }

    /**
     * 测试上传用例集 - 成功场景
     */
    @Test
    public void testTestCaseSetsPost_Success_ReturnsOk() throws Exception {
        // Arrange
        String xUsername = "admin";
        String description = "测试用例集";
        String businessZh = "VPN阻断";
        String overwrite = "false";
        
        when(mockFile.getOriginalFilename()).thenReturn("testcaseset_v1.0.zip");
        when(mockFile.getSize()).thenReturn(1024L);
        when(mockFile.getBytes()).thenReturn("test content".getBytes());
        when(testCaseSetService.uploadTestCaseSet(any(MultipartFile.class), eq(description), eq(businessZh), eq(false)))
                .thenReturn(testTestCaseSet);

        // Act
        String uploadRequestJson = "{\"description\":\"" + description + "\",\"businessZh\":\"" + businessZh + "\",\"overwrite\":\"" + overwrite + "\"}";
        
        ResponseEntity<TestCaseSetUploadResponse> response = testCaseSetController.testCaseSetsPost(
                xUsername, mockFile, uploadRequestJson);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("上传用例集成功", response.getBody().getMessage());
        assertNotNull(response.getBody().getData());

        verify(testCaseSetService, times(1)).uploadTestCaseSet(any(MultipartFile.class), eq(description), eq(businessZh), eq(false));
    }

    /**
     * 测试上传用例集 - 覆盖模式
     */
    @Test
    public void testTestCaseSetsPost_WithOverwrite_ReturnsOk() throws Exception {
        // Arrange
        String xUsername = "admin";
        String description = "测试用例集";
        String businessZh = "VPN阻断";
        String overwrite = "true";
        
        when(mockFile.getOriginalFilename()).thenReturn("testcaseset_v1.0.zip");
        when(mockFile.getSize()).thenReturn(1024L);
        when(mockFile.getBytes()).thenReturn("test content".getBytes());
        when(testCaseSetService.uploadTestCaseSet(any(MultipartFile.class), eq(description), eq(businessZh), eq(true)))
                .thenReturn(testTestCaseSet);

        // Act
        String uploadRequestJson = "{\"description\":\"" + description + "\",\"businessZh\":\"" + businessZh + "\",\"overwrite\":\"" + overwrite + "\"}";
        
        ResponseEntity<TestCaseSetUploadResponse> response = testCaseSetController.testCaseSetsPost(
                xUsername, mockFile, uploadRequestJson);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("覆盖更新用例集成功", response.getBody().getMessage());

        verify(testCaseSetService, times(1)).uploadTestCaseSet(any(MultipartFile.class), eq(description), eq(businessZh), eq(true));
    }

    /**
     * 测试上传用例集 - 参数验证失败
     */
    @Test
    public void testTestCaseSetsPost_InvalidArgument_ReturnsBadRequest() throws Exception {
        // Arrange
        String xUsername = "admin";
        String description = "测试用例集";
        String businessZh = "VPN阻断";
        String overwrite = "false";
        
        when(testCaseSetService.uploadTestCaseSet(any(MultipartFile.class), eq(description), eq(businessZh), eq(false)))
                .thenThrow(new IllegalArgumentException("文件格式不支持"));

        // Act
        String uploadRequestJson = "{\"description\":\"" + description + "\",\"businessZh\":\"" + businessZh + "\",\"overwrite\":\"" + overwrite + "\"}";
        
        ResponseEntity<TestCaseSetUploadResponse> response = testCaseSetController.testCaseSetsPost(
                xUsername, mockFile, uploadRequestJson);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertTrue(response.getBody().getMessage().contains("上传失败"));
        assertTrue(response.getBody().getMessage().contains("文件格式不支持"));
    }

    /**
     * 测试上传用例集 - 服务异常
     */
    @Test
    public void testTestCaseSetsPost_ServiceException_ReturnsInternalServerError() throws Exception {
        // Arrange
        String xUsername = "admin";
        String description = "测试用例集";
        String businessZh = "VPN阻断";
        String overwrite = "false";
        
        when(testCaseSetService.uploadTestCaseSet(any(MultipartFile.class), eq(description), eq(businessZh), eq(false)))
                .thenThrow(new RuntimeException("文件处理失败"));

        // Act
        String uploadRequestJson = "{\"description\":\"" + description + "\",\"businessZh\":\"" + businessZh + "\",\"overwrite\":\"" + overwrite + "\"}";
        
        ResponseEntity<TestCaseSetUploadResponse> response = testCaseSetController.testCaseSetsPost(
                xUsername, mockFile, uploadRequestJson);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertTrue(response.getBody().getMessage().contains("上传失败"));
    }

    /**
     * 测试下载用例集 - 成功场景
     */
    @Test
    public void testTestCaseSetsIdDownloadGet_Success_ReturnsOk() {
        // Arrange
        Long testId = 1L;
        when(testCaseSetService.getTestCaseSetById(testId)).thenReturn(testTestCaseSet);

        // Act
        ResponseEntity<Resource> response = testCaseSetController.testCaseSetsIdDownloadGet(testId);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getHeaders().getContentDisposition());
        assertTrue(response.getHeaders().getContentDisposition().getFilename().contains("testcaseset_v1.0.zip"));

        verify(testCaseSetService, times(1)).getTestCaseSetById(testId);
    }

    /**
     * 测试下载用例集 - 用例集不存在
     */
    @Test
    public void testTestCaseSetsIdDownloadGet_TestCaseSetNotFound_ReturnsNotFound() {
        // Arrange
        Long testId = 999L;
        when(testCaseSetService.getTestCaseSetById(testId)).thenReturn(null);

        // Act
        ResponseEntity<Resource> response = testCaseSetController.testCaseSetsIdDownloadGet(testId);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    /**
     * 测试下载用例集 - 文件内容为空
     */
    @Test
    public void testTestCaseSetsIdDownloadGet_EmptyFileContent_ReturnsNotFound() {
        // Arrange
        Long testId = 1L;
        TestCaseSet testCaseSetWithEmptyContent = createTestTestCaseSet();
        testCaseSetWithEmptyContent.setFileContent(null);
        when(testCaseSetService.getTestCaseSetById(testId)).thenReturn(testCaseSetWithEmptyContent);

        // Act
        ResponseEntity<Resource> response = testCaseSetController.testCaseSetsIdDownloadGet(testId);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    /**
     * 测试下载用例集 - 服务异常
     */
    @Test
    public void testTestCaseSetsIdDownloadGet_ServiceException_ReturnsInternalServerError() {
        // Arrange
        Long testId = 1L;
        when(testCaseSetService.getTestCaseSetById(testId)).thenThrow(new RuntimeException("数据库错误"));

        // Act
        ResponseEntity<Resource> response = testCaseSetController.testCaseSetsIdDownloadGet(testId);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    /**
     * 测试更新用例集 - 成功场景
     */
    @Test
    public void testTestCaseSetsIdPut_Success_ReturnsOk() {
        // Arrange
        String xUsername = "admin";
        Long testId = 1L;
        UpdateTestCaseSetRequest request = new UpdateTestCaseSetRequest();
        request.setDescription("更新后的描述");
        request.setBusinessZh("更新后的业务类型");
        
        TestCaseSet updatedTestCaseSet = createTestTestCaseSet();
        updatedTestCaseSet.setDescription("更新后的描述");
        updatedTestCaseSet.setBusinessZh("更新后的业务类型");
        
        when(testCaseSetService.updateTestCaseSet(testId, request)).thenReturn(updatedTestCaseSet);

        // Act
        ResponseEntity<TestCaseSetResponse> response = testCaseSetController.testCaseSetsIdPut(xUsername, testId, request);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("更新用例集成功", response.getBody().getMessage());
        assertNotNull(response.getBody().getData());
        assertEquals("更新后的描述", response.getBody().getData().getDescription());

        verify(testCaseSetService, times(1)).updateTestCaseSet(testId, request);
    }

    /**
     * 测试更新用例集 - 用例集不存在
     */
    @Test
    public void testTestCaseSetsIdPut_TestCaseSetNotFound_ReturnsNotFound() {
        // Arrange
        String xUsername = "admin";
        Long testId = 999L;
        UpdateTestCaseSetRequest request = new UpdateTestCaseSetRequest();
        when(testCaseSetService.updateTestCaseSet(testId, request)).thenReturn(null);

        // Act
        ResponseEntity<TestCaseSetResponse> response = testCaseSetController.testCaseSetsIdPut(xUsername, testId, request);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("用例集不存在", response.getBody().getMessage());
    }

    /**
     * 测试更新用例集 - 参数验证失败
     */
    @Test
    public void testTestCaseSetsIdPut_InvalidArgument_ReturnsBadRequest() {
        // Arrange
        String xUsername = "admin";
        Long testId = 1L;
        UpdateTestCaseSetRequest request = new UpdateTestCaseSetRequest();
        when(testCaseSetService.updateTestCaseSet(testId, request))
                .thenThrow(new IllegalArgumentException("描述不能为空"));

        // Act
        ResponseEntity<TestCaseSetResponse> response = testCaseSetController.testCaseSetsIdPut(xUsername, testId, request);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertTrue(response.getBody().getMessage().contains("更新失败"));
        assertTrue(response.getBody().getMessage().contains("描述不能为空"));
    }

    /**
     * 测试删除用例集 - 成功场景
     */
    @Test
    public void testTestCaseSetsIdDelete_Success_ReturnsOk() {
        // Arrange
        Long testId = 1L;
        String xUsername = "admin";
        when(testCaseSetService.deleteTestCaseSet(testId)).thenReturn(true);

        // Act
        ResponseEntity<SuccessResponse> response = testCaseSetController.testCaseSetsIdDelete(testId, xUsername);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("删除用例集成功", response.getBody().getMessage());

        verify(testCaseSetService, times(1)).deleteTestCaseSet(testId);
    }

    /**
     * 测试删除用例集 - 用例集不存在
     */
    @Test
    public void testTestCaseSetsIdDelete_TestCaseSetNotFound_ReturnsNotFound() {
        // Arrange
        Long testId = 999L;
        String xUsername = "admin";
        when(testCaseSetService.deleteTestCaseSet(testId)).thenReturn(false);

        // Act
        ResponseEntity<SuccessResponse> response = testCaseSetController.testCaseSetsIdDelete(testId, xUsername);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("用例集不存在", response.getBody().getMessage());
    }

    /**
     * 测试删除用例集 - 服务异常
     */
    @Test
    public void testTestCaseSetsIdDelete_ServiceException_ReturnsInternalServerError() {
        // Arrange
        Long testId = 1L;
        String xUsername = "admin";
        when(testCaseSetService.deleteTestCaseSet(testId)).thenThrow(new RuntimeException("数据库错误"));

        // Act
        ResponseEntity<SuccessResponse> response = testCaseSetController.testCaseSetsIdDelete(testId, xUsername);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertTrue(response.getBody().getMessage().contains("删除失败"));
    }

    /**
     * 测试获取测试用例列表 - 成功场景
     */
    @Test
    public void testTestCaseSetsIdTestCasesGet_Success_ReturnsOk() {
        // Arrange
        Long testId = 1L;
        Map<String, Object> testCasesResult = createMockTestCasesResult();
        when(testCaseSetService.getTestCases(testId, 1, 10)).thenReturn(testCasesResult);

        // Act
        ResponseEntity<TestCaseListResponse> response = testCaseSetController.testCaseSetsIdTestCasesGet(testId, 1, 10);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("获取测试用例列表成功", response.getBody().getMessage());
        assertNotNull(response.getBody().getData());
        assertEquals(1, response.getBody().getData().getPage().intValue());
        assertEquals(10, response.getBody().getData().getPageSize().intValue());
        assertEquals(1, response.getBody().getData().getTotal().intValue());
        assertEquals(1, response.getBody().getData().getData().size());

        verify(testCaseSetService, times(1)).getTestCases(testId, 1, 10);
    }

    /**
     * 测试获取测试用例列表 - 默认分页参数
     */
    @Test
    public void testTestCaseSetsIdTestCasesGet_WithNullParams_UsesDefaults() {
        // Arrange
        Long testId = 1L;
        Map<String, Object> testCasesResult = createMockTestCasesResult();
        when(testCaseSetService.getTestCases(testId, 1, 10)).thenReturn(testCasesResult);

        // Act
        ResponseEntity<TestCaseListResponse> response = testCaseSetController.testCaseSetsIdTestCasesGet(testId, null, null);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(testCaseSetService, times(1)).getTestCases(testId, 1, 10);
    }

    /**
     * 测试获取测试用例列表 - 服务异常
     */
    @Test
    public void testTestCaseSetsIdTestCasesGet_ServiceException_ReturnsInternalServerError() {
        // Arrange
        Long testId = 1L;
        when(testCaseSetService.getTestCases(testId, 1, 10)).thenThrow(new RuntimeException("数据库错误"));

        // Act
        ResponseEntity<TestCaseListResponse> response = testCaseSetController.testCaseSetsIdTestCasesGet(testId, 1, 10);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertTrue(response.getBody().getMessage().contains("获取测试用例列表失败"));
    }

    /**
     * 测试获取缺失脚本列表 - 成功场景
     */
    @Test
    public void testTestCaseSetsIdMissingScriptsGet_Success_ReturnsOk() {
        // Arrange
        Long testId = 1L;
        List<TestCase> missingScripts = Arrays.asList(testTestCase);
        when(testCaseSetService.getMissingScripts(testId)).thenReturn(missingScripts);

        // Act
        ResponseEntity<MissingScriptsResponse> response = testCaseSetController.testCaseSetsIdMissingScriptsGet(testId);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("获取缺失脚本列表成功", response.getBody().getMessage());
        assertNotNull(response.getBody().getData());
        assertEquals(1, response.getBody().getData().getCount().intValue());
        assertEquals(1, response.getBody().getData().getTestCases().size());

        verify(testCaseSetService, times(1)).getMissingScripts(testId);
    }

    /**
     * 测试获取缺失脚本列表 - 服务异常
     */
    @Test
    public void testTestCaseSetsIdMissingScriptsGet_ServiceException_ReturnsInternalServerError() {
        // Arrange
        Long testId = 1L;
        when(testCaseSetService.getMissingScripts(testId)).thenThrow(new RuntimeException("数据库错误"));

        // Act
        ResponseEntity<MissingScriptsResponse> response = testCaseSetController.testCaseSetsIdMissingScriptsGet(testId);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertTrue(response.getBody().getMessage().contains("获取缺失脚本列表失败"));
    }

    // 辅助方法

    private TestCaseSet createTestTestCaseSet() {
        TestCaseSet testCaseSet = new TestCaseSet();
        testCaseSet.setId(1L);
        testCaseSet.setName("testcaseset");
        testCaseSet.setVersion("v1.0");
        testCaseSet.setDescription("测试用例集");
        testCaseSet.setBusinessZh("VPN阻断");
        testCaseSet.setBusinessEn("VPN_BLOCK");
        testCaseSet.setFileSize(1024L);
        testCaseSet.setSha256("testsha256hash");
        testCaseSet.setFileContent("test content".getBytes());
        return testCaseSet;
    }

    private TestCase createTestTestCase() {
        TestCase testCase = new TestCase();
        testCase.setId(1L);
        testCase.setTestCaseSetId(1L);
        testCase.setCaseName("测试用例1");
        testCase.setCaseNumber("TC001");
        testCase.setTestSteps("测试步骤");
        testCase.setExpectedResult("预期结果");
        testCase.setBusinessCategory("VPN");
        testCase.setAppName("TestApp");
        testCase.setScriptExists(true);
        return testCase;
    }

    private Map<String, Object> createMockResult() {
        Map<String, Object> result = new HashMap<>();
        result.put("page", 1);
        result.put("pageSize", 10);
        result.put("total", 2L);
        result.put("data", Arrays.asList(testTestCaseSet, testTestCaseSet));
        return result;
    }

    private Map<String, Object> createMockTestCasesResult() {
        Map<String, Object> result = new HashMap<>();
        result.put("page", 1);
        result.put("pageSize", 10);
        result.put("total", 1L);
        result.put("data", Arrays.asList(testTestCase));
        return result;
    }
}
