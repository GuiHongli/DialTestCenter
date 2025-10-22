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
        
        verify(testCaseSetService).uploadTestCaseSet(
            any(MultipartFile.class), 
            eq("Test description"), 
            eq("测试业务"), 
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
        
        verify(testCaseSetService).uploadTestCaseSet(
            any(MultipartFile.class), 
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
        
        // Act
        ResponseEntity<TestCaseSetUploadResponse> response = fileUploadController.uploadTestCaseSet(
            nonMultipartRequest, null, null, null, "false", null);
        
        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("请求类型错误，必须是multipart/form-data", response.getBody().getMessage());
        
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
        
        // Act
        ResponseEntity<TestCaseSetUploadResponse> response = fileUploadController.uploadTestCaseSet(
            mockRequest, "Test description", "测试业务", "TEST_BUSINESS", "false", "testuser");
        
        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("未提供上传文件", response.getBody().getMessage());
        
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
        // 不设置X-Username header，应该使用默认值"admin"
        
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
            mockRequest, "Test description", "测试业务", "TEST_BUSINESS", "false", null);
        
        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
        
        verify(testCaseSetService).uploadTestCaseSet(
            any(MultipartFile.class), 
            anyString(), 
            anyString(), 
            anyBoolean(), 
            eq("admin")
        );
    }
    
    @Test
    public void testUploadTestCaseSet_ValidationException() {
        // Arrange
        mockRequest.addFile(mockFile);
        mockRequest.addHeader("X-Username", "testuser");
        
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
