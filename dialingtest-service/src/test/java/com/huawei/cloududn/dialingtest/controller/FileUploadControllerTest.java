/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.cloududn.dialingtest.controller;

import com.huawei.cloududn.dialingtest.entity.SoftwarePackage;
import com.huawei.cloududn.dialingtest.model.TestCaseSet;
import com.huawei.cloududn.dialingtest.model.TestCaseSetUploadResponse;
import com.huawei.cloududn.dialingtest.service.SoftwarePackagesService;
import com.huawei.cloududn.dialingtest.service.TestCaseSetService;
import com.huawei.cloududn.dialingtest.service.TestCaseValidationService;
import com.huawei.cloududn.dialingtest.service.PreprocessRuleService;
import com.huawei.cloududn.dialingtest.util.OperationLogUtil;
import com.huawei.cloududn.dialingtest.util.PermissionValidator;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockMultipartHttpServletRequest;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * FileUploadController单元测试类
 *
 * @author g00940940
 * @since 2025-01-27
 */
@RunWith(MockitoJUnitRunner.class)
public class FileUploadControllerTest {
    
    @Mock
    private TestCaseSetService testCaseSetService;
    
    @Mock
    private SoftwarePackagesService softwarePackagesService;
    
    @Mock
    private PreprocessRuleService preprocessRuleService;
    
    @Mock
    private OperationLogUtil operationLogUtil;
    
    @Mock
    private TestCaseValidationService testCaseValidationService;
    
    @Mock
    private PermissionValidator permissionValidator;
    
    @InjectMocks
    private FileUploadController fileUploadController;
    
    private MockMultipartHttpServletRequest mockRequest;
    private MockMultipartFile mockFile;
    private TestCaseSet mockTestCaseSet;
    
    @Before
    public void setUp() {
        mockRequest = new MockMultipartHttpServletRequest();
        mockFile = new MockMultipartFile(
            "file", 
            "test-case-v1.0.zip", 
            "application/zip", 
            "test file content".getBytes()
        );
        
        mockTestCaseSet = new TestCaseSet();
        mockTestCaseSet.setId(1L);
        mockTestCaseSet.setName("test-case");
        mockTestCaseSet.setVersion("v1.0");
        mockTestCaseSet.setDescription("Test case set");
    }
    
    /**
     * 测试上传用例集 - 成功场景（ADMIN用户）
     */
    @Test
    public void testUploadTestCaseSet_AdminUser_Success() {
        // Arrange
        mockRequest.addFile(mockFile);
        String username = "admin";
        
        PermissionValidator.PermissionValidationResult successResult = 
            PermissionValidator.PermissionValidationResult.success();
        when(permissionValidator.checkAdminOrOperator(username, "上传用例集"))
            .thenReturn(successResult);
        
        when(testCaseSetService.uploadTestCaseSet(
            any(MultipartFile.class), 
            anyString(), 
            anyString(), 
            anyString(), 
            anyBoolean(), 
            anyString()
        )).thenReturn(mockTestCaseSet);
        
        when(testCaseValidationService.triggerValidation(anyLong()))
            .thenReturn(mock(TestCaseValidationService.ValidationTaskInfo.class));
        
        // Act
        ResponseEntity<TestCaseSetUploadResponse> response = fileUploadController.uploadTestCaseSet(
            mockRequest, "Test description", "测试业务", "TEST_BUSINESS", "false", username);
        
        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("上传用例集成功", response.getBody().getMessage());
        
        verify(permissionValidator).checkAdminOrOperator(username, "上传用例集");
        verify(testCaseSetService).uploadTestCaseSet(any(), anyString(), anyString(), anyString(), eq(false), eq(username));
    }
    
    /**
     * 测试上传用例集 - 成功场景（OPERATOR用户）
     */
    @Test
    public void testUploadTestCaseSet_OperatorUser_Success() {
        // Arrange
        mockRequest.addFile(mockFile);
        String username = "operator";
        
        PermissionValidator.PermissionValidationResult successResult = 
            PermissionValidator.PermissionValidationResult.success();
        when(permissionValidator.checkAdminOrOperator(username, "上传用例集"))
            .thenReturn(successResult);
        
        when(testCaseSetService.uploadTestCaseSet(any(), anyString(), anyString(), anyString(), anyBoolean(), anyString()))
            .thenReturn(mockTestCaseSet);
        
        when(testCaseValidationService.triggerValidation(anyLong()))
            .thenReturn(mock(TestCaseValidationService.ValidationTaskInfo.class));
        
        // Act
        ResponseEntity<TestCaseSetUploadResponse> response = fileUploadController.uploadTestCaseSet(
            mockRequest, "Test description", "测试业务", "TEST_BUSINESS", "true", username);
        
        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
        assertEquals("覆盖更新用例集成功", response.getBody().getMessage());
        
        verify(permissionValidator).checkAdminOrOperator(username, "上传用例集");
    }
    
    /**
     * 测试上传用例集 - 权限不足
     */
    @Test
    public void testUploadTestCaseSet_InsufficientPermission_ReturnsForbidden() {
        // Arrange
        mockRequest.addFile(mockFile);
        String username = "browser";
        
        PermissionValidator.PermissionValidationResult failureResult = 
            PermissionValidator.PermissionValidationResult.failure("权限不足，上传用例集需要管理员或操作员权限");
        when(permissionValidator.checkAdminOrOperator(username, "上传用例集"))
            .thenReturn(failureResult);
        
        // Act
        ResponseEntity<TestCaseSetUploadResponse> response = fileUploadController.uploadTestCaseSet(
            mockRequest, "Test description", "测试业务", "TEST_BUSINESS", "false", username);
        
        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertTrue(response.getBody().getMessage().contains("权限不足"));
        
        verify(permissionValidator).checkAdminOrOperator(username, "上传用例集");
        verify(testCaseSetService, never()).uploadTestCaseSet(any(), anyString(), anyString(), anyString(), anyBoolean(), anyString());
    }
    
    /**
     * 测试上传用例集 - 用户名为null
     */
    @Test
    public void testUploadTestCaseSet_NullUsername_ReturnsUnauthorized() {
        // Arrange
        mockRequest.addFile(mockFile);
        String username = null;
        
        PermissionValidator.PermissionValidationResult failureResult = 
            PermissionValidator.PermissionValidationResult.failure("未提供用户名");
        when(permissionValidator.checkAdminOrOperator(username, "上传用例集"))
            .thenReturn(failureResult);
        
        // Act
        ResponseEntity<TestCaseSetUploadResponse> response = fileUploadController.uploadTestCaseSet(
            mockRequest, "Test description", "测试业务", "TEST_BUSINESS", "false", username);
        
        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        
        verify(permissionValidator).checkAdminOrOperator(username, "上传用例集");
        verify(testCaseSetService, never()).uploadTestCaseSet(any(), anyString(), anyString(), anyString(), anyBoolean(), anyString());
    }
    
    /**
     * 测试上传用例集 - 未提供文件
     */
    @Test
    public void testUploadTestCaseSet_NoFile_ReturnsBadRequest() {
        // Arrange
        MockHttpServletRequest nonMultipartRequest = new MockHttpServletRequest();
        String username = "admin";
        
        PermissionValidator.PermissionValidationResult successResult = 
            PermissionValidator.PermissionValidationResult.success();
        when(permissionValidator.checkAdminOrOperator(username, "上传用例集"))
            .thenReturn(successResult);
        
        // Act
        ResponseEntity<TestCaseSetUploadResponse> response = fileUploadController.uploadTestCaseSet(
            nonMultipartRequest, null, null, null, "false", username);
        
        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("未提供上传文件", response.getBody().getMessage());
        
        verify(permissionValidator).checkAdminOrOperator(username, "上传用例集");
        verify(testCaseSetService, never()).uploadTestCaseSet(any(), anyString(), anyString(), anyString(), anyBoolean(), anyString());
    }
    
    /**
     * 测试上传软件包 - 成功场景
     */
    @Test
    public void testUploadSoftwarePackage_AdminUser_Success() throws IOException {
        // Arrange
        MockMultipartFile apkFile = new MockMultipartFile(
            "file", "TestApp_1.0.0.apk", "application/vnd.android.package-archive", "test content".getBytes());
        mockRequest.addFile(apkFile);
        String username = "admin";
        
        PermissionValidator.PermissionValidationResult successResult = 
            PermissionValidator.PermissionValidationResult.success();
        when(permissionValidator.checkAdminOrOperator(username, "上传软件包"))
            .thenReturn(successResult);
        
        SoftwarePackage mockPackage = new SoftwarePackage();
        mockPackage.setId(1L);
        mockPackage.setSoftwareName("TestApp_1.0.0.apk");
        
        when(softwarePackagesService.uploadSinglePackage(any(), anyString(), anyBoolean(), anyString()))
            .thenReturn(mockPackage);
        
        // Act
        ResponseEntity<?> response = fileUploadController.uploadSoftwarePackage(
            mockRequest, "Test description", "false", username);
        
        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        
        verify(permissionValidator).checkAdminOrOperator(username, "上传软件包");
        verify(softwarePackagesService).uploadSinglePackage(any(), anyString(), anyBoolean(), anyString());
    }
    
    /**
     * 测试上传软件包 - 权限不足
     */
    @Test
    public void testUploadSoftwarePackage_InsufficientPermission_ReturnsForbidden() throws IOException {
        // Arrange
        MockMultipartFile apkFile = new MockMultipartFile(
            "file", "TestApp_1.0.0.apk", "application/vnd.android.package-archive", "test content".getBytes());
        mockRequest.addFile(apkFile);
        String username = "browser";
        
        PermissionValidator.PermissionValidationResult failureResult = 
            PermissionValidator.PermissionValidationResult.failure("权限不足，上传软件包需要管理员或操作员权限");
        when(permissionValidator.checkAdminOrOperator(username, "上传软件包"))
            .thenReturn(failureResult);
        
        // Act
        ResponseEntity<?> response = fileUploadController.uploadSoftwarePackage(
            mockRequest, "Test description", "false", username);
        
        // Assert
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        
        verify(permissionValidator).checkAdminOrOperator(username, "上传软件包");
        verify(softwarePackagesService, never()).uploadSinglePackage(any(), anyString(), anyBoolean(), anyString());
    }
    
    /**
     * 测试上传预处理规则包 - 成功场景
     */
    @Test
    public void testUploadPreprocessRulePackage_Success() throws IOException {
        // Arrange
        MockMultipartFile zipFile = new MockMultipartFile(
            "file", "preprocess-rule-v1.0.zip", "application/zip", "test zip content".getBytes());
        mockRequest.addFile(zipFile);
        String username = "admin";
        
        PermissionValidator.PermissionValidationResult successResult = 
            PermissionValidator.PermissionValidationResult.success();
        when(permissionValidator.checkAdminOrOperator(username, "上传预处理规则包"))
            .thenReturn(successResult);
        
        String expectedResult = "{\"success\":true,\"message\":\"预处理规则包上传成功\"}";
        when(preprocessRuleService.uploadPreprocessRulePackage(
            any(), anyString(), anyString(), anyString(), anyString(), anyBoolean()))
            .thenReturn(expectedResult);
        
        // Act
        ResponseEntity<String> response = fileUploadController.uploadPreprocessRulePackage(
            mockRequest, "测试业务", "TEST_BUSINESS", "测试描述", "false", username);
        
        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedResult, response.getBody());
        
        verify(permissionValidator).checkAdminOrOperator(username, "上传预处理规则包");
        verify(preprocessRuleService).uploadPreprocessRulePackage(
            any(), eq("测试业务"), eq("TEST_BUSINESS"), eq("测试描述"), eq(username), eq(false));
    }
    
    /**
     * 测试上传预处理规则包 - 权限不足
     */
    @Test
    public void testUploadPreprocessRulePackage_InsufficientPermission_ReturnsForbidden() throws IOException {
        // Arrange
        MockMultipartFile zipFile = new MockMultipartFile(
            "file", "preprocess-rule-v1.0.zip", "application/zip", "test zip content".getBytes());
        mockRequest.addFile(zipFile);
        String username = "browser";
        
        PermissionValidator.PermissionValidationResult failureResult = 
            PermissionValidator.PermissionValidationResult.failure("权限不足，上传预处理规则包需要管理员或操作员权限");
        when(permissionValidator.checkAdminOrOperator(username, "上传预处理规则包"))
            .thenReturn(failureResult);
        
        // Act
        ResponseEntity<String> response = fileUploadController.uploadPreprocessRulePackage(
            mockRequest, "测试业务", "TEST_BUSINESS", "测试描述", "false", username);
        
        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertTrue(response.getBody().contains("权限不足"));
        
        verify(permissionValidator).checkAdminOrOperator(username, "上传预处理规则包");
        verify(preprocessRuleService, never()).uploadPreprocessRulePackage(
            any(), anyString(), anyString(), anyString(), anyString(), anyBoolean());
    }
    
    /**
     * 测试上传预处理规则包 - 业务类型中文为空
     */
    @Test
    public void testUploadPreprocessRulePackage_EmptyBusinessZh_ReturnsBadRequest() throws IOException {
        // Arrange
        MockMultipartFile zipFile = new MockMultipartFile(
            "file", "preprocess-rule-v1.0.zip", "application/zip", "test zip content".getBytes());
        mockRequest.addFile(zipFile);
        String username = "admin";
        
        PermissionValidator.PermissionValidationResult successResult = 
            PermissionValidator.PermissionValidationResult.success();
        when(permissionValidator.checkAdminOrOperator(username, "上传预处理规则包"))
            .thenReturn(successResult);
        
        // Act
        ResponseEntity<String> response = fileUploadController.uploadPreprocessRulePackage(
            mockRequest, "", "TEST_BUSINESS", "测试描述", "false", username);
        
        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().contains("业务类型中文名称不能为空"));
        
        verify(permissionValidator).checkAdminOrOperator(username, "上传预处理规则包");
        verify(preprocessRuleService, never()).uploadPreprocessRulePackage(
            any(), anyString(), anyString(), anyString(), anyString(), anyBoolean());
    }
    
    /**
     * 测试上传预处理规则包 - 业务类型英文为空
     */
    @Test
    public void testUploadPreprocessRulePackage_EmptyBusinessEn_ReturnsBadRequest() throws IOException {
        // Arrange
        MockMultipartFile zipFile = new MockMultipartFile(
            "file", "preprocess-rule-v1.0.zip", "application/zip", "test zip content".getBytes());
        mockRequest.addFile(zipFile);
        String username = "admin";
        
        PermissionValidator.PermissionValidationResult successResult = 
            PermissionValidator.PermissionValidationResult.success();
        when(permissionValidator.checkAdminOrOperator(username, "上传预处理规则包"))
            .thenReturn(successResult);
        
        // Act
        ResponseEntity<String> response = fileUploadController.uploadPreprocessRulePackage(
            mockRequest, "测试业务", "", "测试描述", "false", username);
        
        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().contains("业务类型英文名称不能为空"));
        
        verify(permissionValidator).checkAdminOrOperator(username, "上传预处理规则包");
        verify(preprocessRuleService, never()).uploadPreprocessRulePackage(
            any(), anyString(), anyString(), anyString(), anyString(), anyBoolean());
    }
}
