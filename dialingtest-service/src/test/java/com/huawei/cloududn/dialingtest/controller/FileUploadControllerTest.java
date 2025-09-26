/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.cloududn.dialingtest.controller;

import com.huawei.cloududn.dialingtest.model.TestCaseSet;
import com.huawei.cloududn.dialingtest.model.TestCaseSetUploadResponse;
import com.huawei.cloududn.dialingtest.service.TestCaseSetService;
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
}
