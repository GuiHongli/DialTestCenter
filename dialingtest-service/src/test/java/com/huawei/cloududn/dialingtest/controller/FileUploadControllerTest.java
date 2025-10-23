/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.cloududn.dialingtest.controller;

import com.huawei.cloududn.dialingtest.entity.SoftwarePackage;
import com.huawei.cloududn.dialingtest.model.TestCaseSet;
import com.huawei.cloududn.dialingtest.model.TestCaseSetUploadResponse;
import com.huawei.cloududn.dialingtest.service.SoftwarePackagesService;
import com.huawei.cloududn.dialingtest.service.TestCaseSetService;
import com.huawei.cloududn.dialingtest.service.UserRoleService;
import com.huawei.cloududn.dialingtest.service.PreprocessRuleService;
import com.huawei.cloududn.dialingtest.util.OperationLogUtil;

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
import java.util.ArrayList;
import java.util.List;

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
    private UserRoleService userRoleService;
    
    @Mock
    private PreprocessRuleService preprocessRuleService;
    
    @Mock
    private OperationLogUtil operationLogUtil;
    
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
    
    @Test
    public void testUploadTestCaseSet_Success() {
        // Arrange
        mockRequest.addFile(mockFile);
        mockRequest.addParameter("description", "Test description");
        mockRequest.addParameter("businessZh", "测试业务");
        mockRequest.addParameter("overwrite", "false");
        mockRequest.addHeader("X-Username", "testuser");
        
        // Mock权限验证 - 用户有ADMIN角色
        List<String> userRoles = new ArrayList<>();
        userRoles.add("ADMIN");
        when(userRoleService.getUserRolesByUsername("testuser")).thenReturn(userRoles);
        
        when(testCaseSetService.uploadTestCaseSet(
            any(MultipartFile.class), 
            anyString(), 
            anyString(), 
            anyString(), 
            anyBoolean(), 
            anyString()
        )).thenReturn(mockTestCaseSet);
        
        // Act
        ResponseEntity<TestCaseSetUploadResponse> response = fileUploadController.uploadTestCaseSet(
            mockRequest, "Test description", "测试业务", "TEST_BUSINESS", "false", "testuser");
        
        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("上传用例集成功", response.getBody().getMessage());
        assertEquals(mockTestCaseSet, response.getBody().getData());
        
        verify(userRoleService).getUserRolesByUsername("testuser");
        verify(testCaseSetService).uploadTestCaseSet(
            any(MultipartFile.class), 
            eq("Test description"), 
            eq("测试业务"), 
            anyString(), 
            eq(false), 
            eq("testuser")
        );
    }
    
    @Test
    public void testUploadTestCaseSet_WithOverwrite() {
        // Arrange
        mockRequest.addFile(mockFile);
        mockRequest.addParameter("overwrite", "true");
        mockRequest.addHeader("X-Username", "testuser");
        
        // Mock权限验证 - 用户有OPERATOR角色
        List<String> userRoles = new ArrayList<>();
        userRoles.add("OPERATOR");
        when(userRoleService.getUserRolesByUsername("testuser")).thenReturn(userRoles);
        
        when(testCaseSetService.uploadTestCaseSet(
            any(MultipartFile.class), 
            anyString(), 
            anyString(), 
            anyString(), 
            anyBoolean(), 
            anyString()
        )).thenReturn(mockTestCaseSet);
        
        // Act
        ResponseEntity<TestCaseSetUploadResponse> response = fileUploadController.uploadTestCaseSet(
            mockRequest, "Test description", "测试业务", "TEST_BUSINESS", "true", "testuser");
        
        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
        assertEquals("覆盖更新用例集成功", response.getBody().getMessage());
        
        verify(userRoleService).getUserRolesByUsername("testuser");
        verify(testCaseSetService).uploadTestCaseSet(
            any(MultipartFile.class), 
            anyString(), 
            anyString(), 
            anyString(), 
            eq(true), 
            eq("testuser")
        );
    }
    
    @Test
    public void testUploadTestCaseSet_NoFile() {
        // Arrange
        MockHttpServletRequest nonMultipartRequest = new MockHttpServletRequest();
        nonMultipartRequest.addHeader("X-Username", "testuser");
        
        // Mock权限验证 - 用户有ADMIN角色
        List<String> userRoles = new ArrayList<>();
        userRoles.add("ADMIN");
        when(userRoleService.getUserRolesByUsername("testuser")).thenReturn(userRoles);
        
        // Act
        ResponseEntity<TestCaseSetUploadResponse> response = fileUploadController.uploadTestCaseSet(
            nonMultipartRequest, null, null, null, "false", "testuser");
        
        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("未提供上传文件", response.getBody().getMessage());
        
        verify(userRoleService).getUserRolesByUsername("testuser");
        verify(testCaseSetService, never()).uploadTestCaseSet(
            any(MultipartFile.class), 
            anyString(), 
            anyString(), 
            anyBoolean(), 
            anyString()
        );
    }
    
    @Test
    public void testUploadTestCaseSet_EmptyFile() {
        // Arrange
        MockMultipartFile emptyFile = new MockMultipartFile(
            "file", 
            "empty.zip", 
            "application/zip", 
            new byte[0]
        );
        mockRequest.addFile(emptyFile);
        mockRequest.addHeader("X-Username", "testuser");
        
        // Mock权限验证 - 用户有ADMIN角色
        List<String> userRoles = new ArrayList<>();
        userRoles.add("ADMIN");
        when(userRoleService.getUserRolesByUsername("testuser")).thenReturn(userRoles);
        
        // Act
        ResponseEntity<TestCaseSetUploadResponse> response = fileUploadController.uploadTestCaseSet(
            mockRequest, "Test description", "测试业务", "TEST_BUSINESS", "false", "testuser");
        
        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("未提供上传文件", response.getBody().getMessage());
        
        verify(userRoleService).getUserRolesByUsername("testuser");
        verify(testCaseSetService, never()).uploadTestCaseSet(
            any(MultipartFile.class), 
            anyString(), 
            anyString(), 
            anyBoolean(), 
            anyString()
        );
    }
    
    @Test
    public void testUploadTestCaseSet_DefaultUsername() {
        // Arrange
        mockRequest.addFile(mockFile);
        mockRequest.addHeader("X-Username", "admin");
        
        // Mock权限验证 - 用户有ADMIN角色
        List<String> userRoles = new ArrayList<>();
        userRoles.add("ADMIN");
        when(userRoleService.getUserRolesByUsername("admin")).thenReturn(userRoles);
        
        when(testCaseSetService.uploadTestCaseSet(
            any(MultipartFile.class), 
            anyString(), 
            anyString(), 
            anyString(), 
            anyBoolean(), 
            anyString()
        )).thenReturn(mockTestCaseSet);
        
        // Act
        ResponseEntity<TestCaseSetUploadResponse> response = fileUploadController.uploadTestCaseSet(
            mockRequest, "Test description", "测试业务", "TEST_BUSINESS", "false", "admin");
        
        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
        
        verify(userRoleService).getUserRolesByUsername("admin");
        verify(testCaseSetService).uploadTestCaseSet(
            any(MultipartFile.class), 
            anyString(), 
            anyString(), 
            anyString(), 
            anyBoolean(), 
            eq("admin")
        );
    }
    
    @Test
    public void testUploadTestCaseSet_InsufficientPermission_ReturnsForbidden() {
        // Arrange
        mockRequest.addFile(mockFile);
        mockRequest.addHeader("X-Username", "viewer");
        
        // Mock权限验证 - 用户只有VIEWER角色，没有上传权限
        List<String> userRoles = new ArrayList<>();
        userRoles.add("VIEWER");
        when(userRoleService.getUserRolesByUsername("viewer")).thenReturn(userRoles);
        
        // Act
        ResponseEntity<TestCaseSetUploadResponse> response = fileUploadController.uploadTestCaseSet(
            mockRequest, "Test description", "测试业务", "TEST_BUSINESS", "false", "viewer");
        
        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("权限不足，只有管理员和操作员可以上传用例集", response.getBody().getMessage());
        
        verify(userRoleService).getUserRolesByUsername("viewer");
        verify(testCaseSetService, never()).uploadTestCaseSet(any(), any(), any(), any(), anyBoolean(), any());
    }
    
    @Test
    public void testUploadTestCaseSet_ValidationException() {
        // Arrange
        mockRequest.addFile(mockFile);
        mockRequest.addHeader("X-Username", "testuser");
        
        // Mock权限验证 - 用户有ADMIN角色
        List<String> userRoles = new ArrayList<>();
        userRoles.add("ADMIN");
        when(userRoleService.getUserRolesByUsername("testuser")).thenReturn(userRoles);
        
        when(testCaseSetService.uploadTestCaseSet(
            any(MultipartFile.class), 
            anyString(), 
            anyString(), 
            anyString(), 
            anyBoolean(), 
            anyString()
        )).thenThrow(new IllegalArgumentException("Invalid file format"));
        
        // Act
        ResponseEntity<TestCaseSetUploadResponse> response = fileUploadController.uploadTestCaseSet(
            mockRequest, "Test description", "测试业务", "TEST_BUSINESS", "false", "testuser");
        
        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("上传失败: Invalid file format", response.getBody().getMessage());
    }
    
    @Test
    public void testUploadTestCaseSet_GeneralException() {
        // Arrange
        mockRequest.addFile(mockFile);
        mockRequest.addHeader("X-Username", "testuser");
        
        // Mock权限验证 - 用户有ADMIN角色
        List<String> userRoles = new ArrayList<>();
        userRoles.add("ADMIN");
        when(userRoleService.getUserRolesByUsername("testuser")).thenReturn(userRoles);
        
        when(testCaseSetService.uploadTestCaseSet(
            any(MultipartFile.class), 
            anyString(), 
            anyString(), 
            anyString(), 
            anyBoolean(), 
            anyString()
        )).thenThrow(new RuntimeException("Unexpected error"));
        
        // Act
        ResponseEntity<TestCaseSetUploadResponse> response = fileUploadController.uploadTestCaseSet(
            mockRequest, "Test description", "测试业务", "TEST_BUSINESS", "false", "testuser");
        
        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("上传失败: Unexpected error", response.getBody().getMessage());
    }
    
    // ==================== 软件包管理相关测试 ====================
    
    /**
     * 测试上传单个软件包 - 正常情况
     */
    @Test
    public void testUploadSoftwarePackage_NormalCase_ShouldReturnSuccess() throws IOException {
        // Arrange
        MockMultipartFile apkFile = new MockMultipartFile(
            "file", "TestApp_1.0.0.apk", "application/vnd.android.package-archive", "test content".getBytes());
        
        mockRequest.addFile(apkFile);
        mockRequest.addParameter("description", "Test application package");
        mockRequest.addParameter("overwrite", "false");
        mockRequest.addHeader("X-Username", "testuser");
        
        SoftwarePackage mockPackage = new SoftwarePackage();
        mockPackage.setId(1L);
        mockPackage.setSoftwareName("TestApp_1.0.0.apk");
        mockPackage.setDescription("Test application package");
        
        List<String> adminRoles = new ArrayList<>();
        adminRoles.add("ADMIN");
        when(userRoleService.getUserRolesByUsername("testuser")).thenReturn(adminRoles);
        when(softwarePackagesService.uploadSinglePackage(any(MultipartFile.class), anyString(), anyBoolean(), anyString()))
            .thenReturn(mockPackage);
        
        // Act
        ResponseEntity<?> response = fileUploadController.uploadSoftwarePackage(mockRequest, "Test application package", "false", "testuser");
        
        // Assert
        assertEquals("HTTP状态码应为200", HttpStatus.OK, response.getStatusCode());
        assertNotNull("响应体不应为null", response.getBody());
        
        verify(userRoleService).getUserRolesByUsername("testuser");
        verify(softwarePackagesService).uploadSinglePackage(any(MultipartFile.class), anyString(), anyBoolean(), anyString());
    }
    
    /**
     * 测试上传单个软件包 - 权限不足
     */
    @Test
    public void testUploadSoftwarePackage_InsufficientPermission_ShouldReturnForbidden() throws IOException {
        // Arrange
        MockMultipartFile apkFile = new MockMultipartFile(
            "file", "TestApp_1.0.0.apk", "application/vnd.android.package-archive", "test content".getBytes());
        
        mockRequest.addFile(apkFile);
        mockRequest.addHeader("X-Username", "testuser");
        
        List<String> browserRoles = new ArrayList<>();
        browserRoles.add("BROWSER");
        when(userRoleService.getUserRolesByUsername("testuser")).thenReturn(browserRoles);
        
        // Act
        ResponseEntity<?> response = fileUploadController.uploadSoftwarePackage(mockRequest, "Test description", "false", "testuser");
        
        // Assert
        assertEquals("HTTP状态码应为403", HttpStatus.FORBIDDEN, response.getStatusCode());
        
        verify(userRoleService).getUserRolesByUsername("testuser");
        verify(softwarePackagesService, never()).uploadSinglePackage(any(), anyString(), anyBoolean(), anyString());
    }
    
    /**
     * 测试上传单个软件包 - 文件格式不支持
     */
    @Test
    public void testUploadSoftwarePackage_UnsupportedFileFormat_ShouldReturnBadRequest() throws IOException {
        // Arrange
        MockMultipartFile txtFile = new MockMultipartFile(
            "file", "test.txt", "text/plain", "test content".getBytes());
        
        mockRequest.addFile(txtFile);
        mockRequest.addHeader("X-Username", "testuser");
        
        List<String> adminRoles = new ArrayList<>();
        adminRoles.add("ADMIN");
        when(userRoleService.getUserRolesByUsername("testuser")).thenReturn(adminRoles);
        
        // Act
        ResponseEntity<?> response = fileUploadController.uploadSoftwarePackage(mockRequest, "Test description", "false", "testuser");
        
        // Assert
        assertEquals("HTTP状态码应为400", HttpStatus.BAD_REQUEST, response.getStatusCode());
        
        verify(userRoleService).getUserRolesByUsername("testuser");
        verify(softwarePackagesService, never()).uploadSinglePackage(any(), anyString(), anyBoolean(), anyString());
    }
    
    /**
     * 测试上传ZIP包 - 正常情况
     */
    @Test
    public void testUploadZipPackage_NormalCase_ShouldReturnSuccess() throws IOException {
        // Arrange
        byte[] zipContent = createTestZipFile();
        MockMultipartFile zipFile = new MockMultipartFile(
            "file", "test.zip", "application/zip", zipContent);
        
        mockRequest.addFile(zipFile);
        mockRequest.addParameter("description", "Test ZIP package");
        mockRequest.addParameter("overwrite", "false");
        mockRequest.addHeader("X-Username", "testuser");
        
        SoftwarePackage mockPackage1 = new SoftwarePackage();
        mockPackage1.setId(1L);
        mockPackage1.setSoftwareName("TestApp_1.0.0.apk");
        
        SoftwarePackage mockPackage2 = new SoftwarePackage();
        mockPackage2.setId(2L);
        mockPackage2.setSoftwareName("TestApp_1.0.0.ipa");
        
        List<SoftwarePackage> mockPackages = new ArrayList<>();
        mockPackages.add(mockPackage1);
        mockPackages.add(mockPackage2);
        
        List<String> adminRoles = new ArrayList<>();
        adminRoles.add("ADMIN");
        when(userRoleService.getUserRolesByUsername("testuser")).thenReturn(adminRoles);
        when(softwarePackagesService.uploadZipPackage(any(MultipartFile.class), anyBoolean(), anyString(), anyString()))
            .thenReturn(mockPackages);
        
        // Act
        ResponseEntity<?> response = fileUploadController.uploadSoftwarePackage(mockRequest, "Test ZIP package", "false", "testuser");
        
        // Assert
        assertEquals("HTTP状态码应为200", HttpStatus.OK, response.getStatusCode());
        assertNotNull("响应体不应为null", response.getBody());
        
        verify(userRoleService).getUserRolesByUsername("testuser");
        verify(softwarePackagesService).uploadZipPackage(any(MultipartFile.class), anyBoolean(), anyString(), anyString());
    }
    
    /**
     * 测试上传ZIP包 - 权限不足
     */
    @Test
    public void testUploadZipPackage_InsufficientPermission_ShouldReturnForbidden() throws IOException {
        // Arrange
        byte[] zipContent = createTestZipFile();
        MockMultipartFile zipFile = new MockMultipartFile(
            "file", "test.zip", "application/zip", zipContent);
        
        mockRequest.addFile(zipFile);
        mockRequest.addHeader("X-Username", "testuser");
        
        List<String> browserRoles = new ArrayList<>();
        browserRoles.add("BROWSER");
        when(userRoleService.getUserRolesByUsername("testuser")).thenReturn(browserRoles);
        
        // Act
        ResponseEntity<?> response = fileUploadController.uploadSoftwarePackage(mockRequest, "Test description", "false", "testuser");
        
        // Assert
        assertEquals("HTTP状态码应为403", HttpStatus.FORBIDDEN, response.getStatusCode());
        
        verify(userRoleService).getUserRolesByUsername("testuser");
        verify(softwarePackagesService, never()).uploadZipPackage(any(), anyBoolean(), anyString(), anyString());
    }
    
    /**
     * 测试上传软件包 - 服务异常
     */
    @Test
    public void testUploadSoftwarePackage_ServiceException_ShouldReturnInternalServerError() throws IOException {
        // Arrange
        MockMultipartFile apkFile = new MockMultipartFile(
            "file", "TestApp_1.0.0.apk", "application/vnd.android.package-archive", "test content".getBytes());
        
        mockRequest.addFile(apkFile);
        mockRequest.addHeader("X-Username", "testuser");
        
        List<String> adminRoles = new ArrayList<>();
        adminRoles.add("ADMIN");
        when(userRoleService.getUserRolesByUsername("testuser")).thenReturn(adminRoles);
        when(softwarePackagesService.uploadSinglePackage(any(MultipartFile.class), anyString(), anyBoolean(), anyString()))
            .thenThrow(new RuntimeException("Service error"));
        
        // Act
        ResponseEntity<?> response = fileUploadController.uploadSoftwarePackage(mockRequest, "Test description", "false", "testuser");
        
        // Assert
        assertEquals("HTTP状态码应为500", HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        
        verify(userRoleService).getUserRolesByUsername("testuser");
        verify(softwarePackagesService).uploadSinglePackage(any(MultipartFile.class), anyString(), anyBoolean(), anyString());
    }
    
    // ==================== 预处理规则包上传测试用例 ====================
    
    /**
     * 测试上传预处理规则包 - 成功场景
     */
    @Test
    public void testUploadPreprocessRulePackage_Success_ShouldReturnOk() throws IOException {
        // Arrange
        MockMultipartFile zipFile = new MockMultipartFile(
            "file", "preprocess-rule-v1.0.zip", "application/zip", "test zip content".getBytes());
        
        mockRequest.addFile(zipFile);
        mockRequest.addHeader("X-Username", "testuser");
        
        // Mock权限验证 - 用户有ADMIN角色
        List<String> adminRoles = new ArrayList<>();
        adminRoles.add("ADMIN");
        when(userRoleService.getUserRolesByUsername("testuser")).thenReturn(adminRoles);
        
        // Mock服务层返回成功结果
        String expectedResult = "预处理规则包上传成功";
        when(preprocessRuleService.uploadPreprocessRulePackage(
            any(MultipartFile.class), 
            anyString(), 
            anyString(), 
            anyString(), 
            anyString(), 
            anyBoolean()
        )).thenReturn(expectedResult);
        
        // Act
        ResponseEntity<String> response = fileUploadController.uploadPreprocessRulePackage(
            mockRequest, "测试业务", "TEST_BUSINESS", "测试描述", "false", "testuser");
        
        // Assert
        assertNotNull("响应不应为空", response);
        assertEquals("HTTP状态码应为200", HttpStatus.OK, response.getStatusCode());
        assertEquals("响应内容应匹配", expectedResult, response.getBody());
        
        verify(userRoleService).getUserRolesByUsername("testuser");
        verify(preprocessRuleService).uploadPreprocessRulePackage(
            any(MultipartFile.class), 
            eq("测试业务"), 
            eq("TEST_BUSINESS"), 
            eq("测试描述"), 
            eq("testuser"), 
            eq(false)
        );
    }
    
    /**
     * 测试上传预处理规则包 - 带覆盖标志
     */
    @Test
    public void testUploadPreprocessRulePackage_WithForceOverwrite_ShouldReturnOk() throws IOException {
        // Arrange
        MockMultipartFile zipFile = new MockMultipartFile(
            "file", "preprocess-rule-v2.0.zip", "application/zip", "test zip content".getBytes());
        
        mockRequest.addFile(zipFile);
        mockRequest.addHeader("X-Username", "testuser");
        
        // Mock权限验证 - 用户有OPERATOR角色
        List<String> operatorRoles = new ArrayList<>();
        operatorRoles.add("OPERATOR");
        when(userRoleService.getUserRolesByUsername("testuser")).thenReturn(operatorRoles);
        
        // Mock服务层返回成功结果
        String expectedResult = "预处理规则包上传成功（覆盖）";
        when(preprocessRuleService.uploadPreprocessRulePackage(
            any(MultipartFile.class), 
            anyString(), 
            anyString(), 
            anyString(), 
            anyString(), 
            anyBoolean()
        )).thenReturn(expectedResult);
        
        // Act
        ResponseEntity<String> response = fileUploadController.uploadPreprocessRulePackage(
            mockRequest, "测试业务", "TEST_BUSINESS", "测试描述", "true", "testuser");
        
        // Assert
        assertNotNull("响应不应为空", response);
        assertEquals("HTTP状态码应为200", HttpStatus.OK, response.getStatusCode());
        assertEquals("响应内容应匹配", expectedResult, response.getBody());
        
        verify(userRoleService).getUserRolesByUsername("testuser");
        verify(preprocessRuleService).uploadPreprocessRulePackage(
            any(MultipartFile.class), 
            eq("测试业务"), 
            eq("TEST_BUSINESS"), 
            eq("测试描述"), 
            eq("testuser"), 
            eq(true)
        );
    }
    
    /**
     * 测试上传预处理规则包 - 权限不足
     */
    @Test
    public void testUploadPreprocessRulePackage_InsufficientPermission_ShouldReturnForbidden() {
        // Arrange
        MockMultipartFile zipFile = new MockMultipartFile(
            "file", "preprocess-rule-v1.0.zip", "application/zip", "test zip content".getBytes());
        
        mockRequest.addFile(zipFile);
        mockRequest.addHeader("X-Username", "testuser");
        
        // Mock权限验证 - 用户只有VIEWER角色
        List<String> viewerRoles = new ArrayList<>();
        viewerRoles.add("VIEWER");
        when(userRoleService.getUserRolesByUsername("testuser")).thenReturn(viewerRoles);
        
        // Act
        ResponseEntity<String> response = fileUploadController.uploadPreprocessRulePackage(
            mockRequest, "测试业务", "TEST_BUSINESS", "测试描述", "false", "testuser");
        
        // Assert
        assertNotNull("响应不应为空", response);
        assertEquals("HTTP状态码应为403", HttpStatus.FORBIDDEN, response.getStatusCode());
        assertTrue("响应内容应包含权限不足信息", 
            response.getBody().contains("权限不足") || response.getBody().contains("权限"));
        
        verify(userRoleService).getUserRolesByUsername("testuser");
        verify(preprocessRuleService, never()).uploadPreprocessRulePackage(
            any(MultipartFile.class), anyString(), anyString(), anyString(), anyString(), anyBoolean());
    }
    
    /**
     * 测试上传预处理规则包 - 无角色用户
     */
    @Test
    public void testUploadPreprocessRulePackage_NoRoles_ShouldReturnForbidden() {
        // Arrange
        MockMultipartFile zipFile = new MockMultipartFile(
            "file", "preprocess-rule-v1.0.zip", "application/zip", "test zip content".getBytes());
        
        mockRequest.addFile(zipFile);
        mockRequest.addHeader("X-Username", "testuser");
        
        // Mock权限验证 - 用户无任何角色
        List<String> emptyRoles = new ArrayList<>();
        when(userRoleService.getUserRolesByUsername("testuser")).thenReturn(emptyRoles);
        
        // Act
        ResponseEntity<String> response = fileUploadController.uploadPreprocessRulePackage(
            mockRequest, "测试业务", "TEST_BUSINESS", "测试描述", "false", "testuser");
        
        // Assert
        assertNotNull("响应不应为空", response);
        assertEquals("HTTP状态码应为403", HttpStatus.FORBIDDEN, response.getStatusCode());
        assertTrue("响应内容应包含权限不足信息", 
            response.getBody().contains("权限不足") || response.getBody().contains("权限"));
        
        verify(userRoleService).getUserRolesByUsername("testuser");
        verify(preprocessRuleService, never()).uploadPreprocessRulePackage(
            any(MultipartFile.class), anyString(), anyString(), anyString(), anyString(), anyBoolean());
    }
    
    /**
     * 测试上传预处理规则包 - 无上传文件
     */
    @Test
    public void testUploadPreprocessRulePackage_NoFile_ShouldReturnBadRequest() {
        // Arrange
        mockRequest.addHeader("X-Username", "testuser");
        
        // Mock权限验证 - 用户有ADMIN角色
        List<String> adminRoles = new ArrayList<>();
        adminRoles.add("ADMIN");
        when(userRoleService.getUserRolesByUsername("testuser")).thenReturn(adminRoles);
        
        // Act
        ResponseEntity<String> response = fileUploadController.uploadPreprocessRulePackage(
            mockRequest, "测试业务", "TEST_BUSINESS", "测试描述", "false", "testuser");
        
        // Assert
        assertNotNull("响应不应为空", response);
        assertEquals("HTTP状态码应为400", HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue("响应内容应包含文件未找到信息", 
            response.getBody().contains("未找到上传文件") || response.getBody().contains("文件"));
        
        verify(userRoleService).getUserRolesByUsername("testuser");
        verify(preprocessRuleService, never()).uploadPreprocessRulePackage(
            any(MultipartFile.class), anyString(), anyString(), anyString(), anyString(), anyBoolean());
    }
    
    /**
     * 测试上传预处理规则包 - 非ZIP格式文件
     */
    @Test
    public void testUploadPreprocessRulePackage_NonZipFile_ShouldReturnBadRequest() {
        // Arrange
        MockMultipartFile txtFile = new MockMultipartFile(
            "file", "preprocess-rule.txt", "text/plain", "test content".getBytes());
        
        mockRequest.addFile(txtFile);
        mockRequest.addHeader("X-Username", "testuser");
        
        // Mock权限验证 - 用户有ADMIN角色
        List<String> adminRoles = new ArrayList<>();
        adminRoles.add("ADMIN");
        when(userRoleService.getUserRolesByUsername("testuser")).thenReturn(adminRoles);
        
        // Act
        ResponseEntity<String> response = fileUploadController.uploadPreprocessRulePackage(
            mockRequest, "测试业务", "TEST_BUSINESS", "测试描述", "false", "testuser");
        
        // Assert
        assertNotNull("响应不应为空", response);
        assertEquals("HTTP状态码应为400", HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue("响应内容应包含格式错误信息", 
            response.getBody().contains("ZIP格式") || response.getBody().contains("格式"));
        
        verify(userRoleService).getUserRolesByUsername("testuser");
        verify(preprocessRuleService, never()).uploadPreprocessRulePackage(
            any(MultipartFile.class), anyString(), anyString(), anyString(), anyString(), anyBoolean());
    }
    
    /**
     * 测试上传预处理规则包 - 业务类型中文为空
     */
    @Test
    public void testUploadPreprocessRulePackage_EmptyBusinessZh_ShouldReturnBadRequest() {
        // Arrange
        MockMultipartFile zipFile = new MockMultipartFile(
            "file", "preprocess-rule-v1.0.zip", "application/zip", "test zip content".getBytes());
        
        mockRequest.addFile(zipFile);
        mockRequest.addHeader("X-Username", "testuser");
        
        // Mock权限验证 - 用户有ADMIN角色
        List<String> adminRoles = new ArrayList<>();
        adminRoles.add("ADMIN");
        when(userRoleService.getUserRolesByUsername("testuser")).thenReturn(adminRoles);
        
        // Act
        ResponseEntity<String> response = fileUploadController.uploadPreprocessRulePackage(
            mockRequest, "", "TEST_BUSINESS", "测试描述", "false", "testuser");
        
        // Assert
        assertNotNull("响应不应为空", response);
        assertEquals("HTTP状态码应为400", HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue("响应内容应包含业务类型中文名称不能为空信息", 
            response.getBody().contains("业务类型中文名称不能为空"));
        
        verify(userRoleService).getUserRolesByUsername("testuser");
        verify(preprocessRuleService, never()).uploadPreprocessRulePackage(
            any(MultipartFile.class), anyString(), anyString(), anyString(), anyString(), anyBoolean());
    }
    
    /**
     * 测试上传预处理规则包 - 业务类型英文为空
     */
    @Test
    public void testUploadPreprocessRulePackage_EmptyBusinessEn_ShouldReturnBadRequest() {
        // Arrange
        MockMultipartFile zipFile = new MockMultipartFile(
            "file", "preprocess-rule-v1.0.zip", "application/zip", "test zip content".getBytes());
        
        mockRequest.addFile(zipFile);
        mockRequest.addHeader("X-Username", "testuser");
        
        // Mock权限验证 - 用户有ADMIN角色
        List<String> adminRoles = new ArrayList<>();
        adminRoles.add("ADMIN");
        when(userRoleService.getUserRolesByUsername("testuser")).thenReturn(adminRoles);
        
        // Act
        ResponseEntity<String> response = fileUploadController.uploadPreprocessRulePackage(
            mockRequest, "测试业务", "", "测试描述", "false", "testuser");
        
        // Assert
        assertNotNull("响应不应为空", response);
        assertEquals("HTTP状态码应为400", HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue("响应内容应包含业务类型英文名称不能为空信息", 
            response.getBody().contains("业务类型英文名称不能为空"));
        
        verify(userRoleService).getUserRolesByUsername("testuser");
        verify(preprocessRuleService, never()).uploadPreprocessRulePackage(
            any(MultipartFile.class), anyString(), anyString(), anyString(), anyString(), anyBoolean());
    }
    
    /**
     * 测试上传预处理规则包 - 业务类型中文为null
     */
    @Test
    public void testUploadPreprocessRulePackage_NullBusinessZh_ShouldReturnBadRequest() {
        // Arrange
        MockMultipartFile zipFile = new MockMultipartFile(
            "file", "preprocess-rule-v1.0.zip", "application/zip", "test zip content".getBytes());
        
        mockRequest.addFile(zipFile);
        mockRequest.addHeader("X-Username", "testuser");
        
        // Mock权限验证 - 用户有ADMIN角色
        List<String> adminRoles = new ArrayList<>();
        adminRoles.add("ADMIN");
        when(userRoleService.getUserRolesByUsername("testuser")).thenReturn(adminRoles);
        
        // Act
        ResponseEntity<String> response = fileUploadController.uploadPreprocessRulePackage(
            mockRequest, null, "TEST_BUSINESS", "测试描述", "false", "testuser");
        
        // Assert
        assertNotNull("响应不应为空", response);
        assertEquals("HTTP状态码应为400", HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue("响应内容应包含业务类型中文名称不能为空信息", 
            response.getBody().contains("业务类型中文名称不能为空"));
        
        verify(userRoleService).getUserRolesByUsername("testuser");
        verify(preprocessRuleService, never()).uploadPreprocessRulePackage(
            any(MultipartFile.class), anyString(), anyString(), anyString(), anyString(), anyBoolean());
    }
    
    /**
     * 测试上传预处理规则包 - 业务类型英文为null
     */
    @Test
    public void testUploadPreprocessRulePackage_NullBusinessEn_ShouldReturnBadRequest() {
        // Arrange
        MockMultipartFile zipFile = new MockMultipartFile(
            "file", "preprocess-rule-v1.0.zip", "application/zip", "test zip content".getBytes());
        
        mockRequest.addFile(zipFile);
        mockRequest.addHeader("X-Username", "testuser");
        
        // Mock权限验证 - 用户有ADMIN角色
        List<String> adminRoles = new ArrayList<>();
        adminRoles.add("ADMIN");
        when(userRoleService.getUserRolesByUsername("testuser")).thenReturn(adminRoles);
        
        // Act
        ResponseEntity<String> response = fileUploadController.uploadPreprocessRulePackage(
            mockRequest, "测试业务", null, "测试描述", "false", "testuser");
        
        // Assert
        assertNotNull("响应不应为空", response);
        assertEquals("HTTP状态码应为400", HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue("响应内容应包含业务类型英文名称不能为空信息", 
            response.getBody().contains("业务类型英文名称不能为空"));
        
        verify(userRoleService).getUserRolesByUsername("testuser");
        verify(preprocessRuleService, never()).uploadPreprocessRulePackage(
            any(MultipartFile.class), anyString(), anyString(), anyString(), anyString(), anyBoolean());
    }
    
    /**
     * 测试上传预处理规则包 - 服务层IllegalArgumentException异常
     */
    @Test
    public void testUploadPreprocessRulePackage_IllegalArgumentException_ShouldReturnBadRequest() throws IOException {
        // Arrange
        MockMultipartFile zipFile = new MockMultipartFile(
            "file", "preprocess-rule-v1.0.zip", "application/zip", "test zip content".getBytes());
        
        mockRequest.addFile(zipFile);
        mockRequest.addHeader("X-Username", "testuser");
        
        // Mock权限验证 - 用户有ADMIN角色
        List<String> adminRoles = new ArrayList<>();
        adminRoles.add("ADMIN");
        when(userRoleService.getUserRolesByUsername("testuser")).thenReturn(adminRoles);
        
        // Mock服务层抛出IllegalArgumentException
        when(preprocessRuleService.uploadPreprocessRulePackage(
            any(MultipartFile.class), 
            anyString(), 
            anyString(), 
            anyString(), 
            anyString(), 
            anyBoolean()
        )).thenThrow(new IllegalArgumentException("Invalid file format"));
        
        // Act
        ResponseEntity<String> response = fileUploadController.uploadPreprocessRulePackage(
            mockRequest, "测试业务", "TEST_BUSINESS", "测试描述", "false", "testuser");
        
        // Assert
        assertNotNull("响应不应为空", response);
        assertEquals("HTTP状态码应为400", HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue("响应内容应包含上传失败信息", 
            response.getBody().contains("上传失败") || response.getBody().contains("Invalid file format"));
        
        verify(userRoleService).getUserRolesByUsername("testuser");
        verify(preprocessRuleService).uploadPreprocessRulePackage(
            any(MultipartFile.class), anyString(), anyString(), anyString(), anyString(), anyBoolean());
    }
    
    /**
     * 测试上传预处理规则包 - 服务层IOException异常
     */
    @Test
    public void testUploadPreprocessRulePackage_IOException_ShouldReturnInternalServerError() throws IOException {
        // Arrange
        MockMultipartFile zipFile = new MockMultipartFile(
            "file", "preprocess-rule-v1.0.zip", "application/zip", "test zip content".getBytes());
        
        mockRequest.addFile(zipFile);
        mockRequest.addHeader("X-Username", "testuser");
        
        // Mock权限验证 - 用户有ADMIN角色
        List<String> adminRoles = new ArrayList<>();
        adminRoles.add("ADMIN");
        when(userRoleService.getUserRolesByUsername("testuser")).thenReturn(adminRoles);
        
        // Mock服务层抛出IOException
        when(preprocessRuleService.uploadPreprocessRulePackage(
            any(MultipartFile.class), 
            anyString(), 
            anyString(), 
            anyString(), 
            anyString(), 
            anyBoolean()
        )).thenThrow(new IOException("File processing error"));
        
        // Act
        ResponseEntity<String> response = fileUploadController.uploadPreprocessRulePackage(
            mockRequest, "测试业务", "TEST_BUSINESS", "测试描述", "false", "testuser");
        
        // Assert
        assertNotNull("响应不应为空", response);
        assertEquals("HTTP状态码应为500", HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue("响应内容应包含上传失败信息", 
            response.getBody().contains("上传失败") || response.getBody().contains("File processing error"));
        
        verify(userRoleService).getUserRolesByUsername("testuser");
        verify(preprocessRuleService).uploadPreprocessRulePackage(
            any(MultipartFile.class), anyString(), anyString(), anyString(), anyString(), anyBoolean());
    }
    
    /**
     * 测试上传预处理规则包 - 服务层RuntimeException异常
     */
    @Test
    public void testUploadPreprocessRulePackage_RuntimeException_ShouldReturnInternalServerError() throws IOException {
        // Arrange
        MockMultipartFile zipFile = new MockMultipartFile(
            "file", "preprocess-rule-v1.0.zip", "application/zip", "test zip content".getBytes());
        
        mockRequest.addFile(zipFile);
        mockRequest.addHeader("X-Username", "testuser");
        
        // Mock权限验证 - 用户有ADMIN角色
        List<String> adminRoles = new ArrayList<>();
        adminRoles.add("ADMIN");
        when(userRoleService.getUserRolesByUsername("testuser")).thenReturn(adminRoles);
        
        // Mock服务层抛出RuntimeException
        when(preprocessRuleService.uploadPreprocessRulePackage(
            any(MultipartFile.class), 
            anyString(), 
            anyString(), 
            anyString(), 
            anyString(), 
            anyBoolean()
        )).thenThrow(new RuntimeException("Service error"));
        
        // Act
        ResponseEntity<String> response = fileUploadController.uploadPreprocessRulePackage(
            mockRequest, "测试业务", "TEST_BUSINESS", "测试描述", "false", "testuser");
        
        // Assert
        assertNotNull("响应不应为空", response);
        assertEquals("HTTP状态码应为500", HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue("响应内容应包含上传失败信息", 
            response.getBody().contains("上传失败") || response.getBody().contains("Service error"));
        
        verify(userRoleService).getUserRolesByUsername("testuser");
        verify(preprocessRuleService).uploadPreprocessRulePackage(
            any(MultipartFile.class), anyString(), anyString(), anyString(), anyString(), anyBoolean());
    }
    
    /**
     * 测试上传预处理规则包 - 文件名包含大写ZIP扩展名
     */
    @Test
    public void testUploadPreprocessRulePackage_UppercaseZipExtension_ShouldReturnOk() throws IOException {
        // Arrange
        MockMultipartFile zipFile = new MockMultipartFile(
            "file", "preprocess-rule-v1.0.ZIP", "application/zip", "test zip content".getBytes());
        
        mockRequest.addFile(zipFile);
        mockRequest.addHeader("X-Username", "testuser");
        
        // Mock权限验证 - 用户有ADMIN角色
        List<String> adminRoles = new ArrayList<>();
        adminRoles.add("ADMIN");
        when(userRoleService.getUserRolesByUsername("testuser")).thenReturn(adminRoles);
        
        // Mock服务层返回成功结果
        String expectedResult = "预处理规则包上传成功";
        when(preprocessRuleService.uploadPreprocessRulePackage(
            any(MultipartFile.class), 
            anyString(), 
            anyString(), 
            anyString(), 
            anyString(), 
            anyBoolean()
        )).thenReturn(expectedResult);
        
        // Act
        ResponseEntity<String> response = fileUploadController.uploadPreprocessRulePackage(
            mockRequest, "测试业务", "TEST_BUSINESS", "测试描述", "false", "testuser");
        
        // Assert
        assertNotNull("响应不应为空", response);
        assertEquals("HTTP状态码应为200", HttpStatus.OK, response.getStatusCode());
        assertEquals("响应内容应匹配", expectedResult, response.getBody());
        
        verify(userRoleService).getUserRolesByUsername("testuser");
        verify(preprocessRuleService).uploadPreprocessRulePackage(
            any(MultipartFile.class), 
            eq("测试业务"), 
            eq("TEST_BUSINESS"), 
            eq("测试描述"), 
            eq("testuser"), 
            eq(false)
        );
    }
    
    /**
     * 创建测试ZIP文件
     */
    private byte[] createTestZipFile() throws IOException {
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        try (java.util.zip.ZipOutputStream zos = new java.util.zip.ZipOutputStream(baos)) {
            // 添加APK文件
            java.util.zip.ZipEntry apkEntry = new java.util.zip.ZipEntry("TestApp_1.0.0.apk");
            zos.putNextEntry(apkEntry);
            zos.write("test apk content".getBytes());
            zos.closeEntry();
            
            // 添加IPA文件
            java.util.zip.ZipEntry ipaEntry = new java.util.zip.ZipEntry("TestApp_1.0.0.ipa");
            zos.putNextEntry(ipaEntry);
            zos.write("test ipa content".getBytes());
            zos.closeEntry();
        }
        return baos.toByteArray();
    }
}



