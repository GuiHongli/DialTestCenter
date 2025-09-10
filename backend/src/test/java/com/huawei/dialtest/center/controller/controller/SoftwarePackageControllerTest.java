/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.dialtest.center.controller.controller;

import com.huawei.dialtest.center.entity.SoftwarePackage;
import com.huawei.dialtest.center.service.SoftwarePackageService;
import com.huawei.dialtest.center.controller.SoftwarePackageController;

import org.junit.Test;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * SoftwarePackageController控制器测试
 * 测试软件包控制器的REST API接口，包括HTTP请求处理、响应格式等
 *
 * @author g00940940
 * @since 2025-09-09
 */
@RunWith(MockitoJUnitRunner.class)
public class SoftwarePackageControllerTest {

    @Mock
    private SoftwarePackageService softwarePackageService;

    @InjectMocks
    private SoftwarePackageController softwarePackageController;

    private SoftwarePackage testSoftwarePackage;
    private MultipartFile testFile;

    @Before
    public void setUp() {
        testSoftwarePackage = new SoftwarePackage();
        testSoftwarePackage.setId(1L);
        testSoftwarePackage.setSoftwareName("TestApp_1.0.0.apk");
        testSoftwarePackage.setFileContent("test content".getBytes());
        testSoftwarePackage.setFileFormat("apk");
        testSoftwarePackage.setSha512("test_sha512_hash");
        testSoftwarePackage.setPlatform("android");
        testSoftwarePackage.setCreator("admin");
        testSoftwarePackage.setFileSize(1024L);
        testSoftwarePackage.setDescription("Test description");

        testFile = mock(MultipartFile.class);
        when(testFile.getOriginalFilename()).thenReturn("TestApp_1.0.0.apk");
    }

    @Test
    public void testGetSoftwarePackages_Success() {
        // Arrange
        List<SoftwarePackage> packages = Arrays.asList(testSoftwarePackage);
        Page<SoftwarePackage> page = new PageImpl<>(packages);
        when(softwarePackageService.getSoftwarePackages(1, 10, null, null, null)).thenReturn(page);

        // Act
        ResponseEntity<Map<String, Object>> response = softwarePackageController.getSoftwarePackages(1, 10, null, null, null);

        // Assert
        assertEquals("Status should be OK", HttpStatus.OK, response.getStatusCode());
        assertNotNull("Response body should not be null", response.getBody());
        assertTrue("Response should contain data", response.getBody().containsKey("data"));
        assertTrue("Response should contain total", response.getBody().containsKey("total"));
        assertTrue("Response should contain page", response.getBody().containsKey("page"));
        assertTrue("Response should contain pageSize", response.getBody().containsKey("pageSize"));
        verify(softwarePackageService).getSoftwarePackages(1, 10, null, null, null);
    }

    @Test
    public void testGetSoftwarePackages_WithFilters() {
        // Arrange
        List<SoftwarePackage> packages = Arrays.asList(testSoftwarePackage);
        Page<SoftwarePackage> page = new PageImpl<>(packages);
        when(softwarePackageService.getSoftwarePackages(1, 10, "android", "admin", "TestApp")).thenReturn(page);

        // Act
        ResponseEntity<Map<String, Object>> response = softwarePackageController.getSoftwarePackages(1, 10, "android", "admin", "TestApp");

        // Assert
        assertEquals("Status should be OK", HttpStatus.OK, response.getStatusCode());
        verify(softwarePackageService).getSoftwarePackages(1, 10, "android", "admin", "TestApp");
    }

    @Test
    public void testGetSoftwarePackages_InvalidParameters() {
        // Arrange
        when(softwarePackageService.getSoftwarePackages(1, 10, null, null, null))
                .thenThrow(new IllegalArgumentException("Invalid parameters"));

        // Act
        ResponseEntity<Map<String, Object>> response = softwarePackageController.getSoftwarePackages(1, 10, null, null, null);

        // Assert
        assertEquals("Status should be Bad Request", HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void testGetSoftwarePackage_Success() {
        // Arrange
        when(softwarePackageService.getSoftwarePackageById(1L)).thenReturn(Optional.of(testSoftwarePackage));

        // Act
        ResponseEntity<SoftwarePackage> response = softwarePackageController.getSoftwarePackage(1L);

        // Assert
        assertEquals("Status should be OK", HttpStatus.OK, response.getStatusCode());
        assertNotNull("Response body should not be null", response.getBody());
        assertEquals("Should return correct package", testSoftwarePackage.getId(), response.getBody().getId());
        verify(softwarePackageService).getSoftwarePackageById(1L);
    }

    @Test
    public void testGetSoftwarePackage_NotFound() {
        // Arrange
        when(softwarePackageService.getSoftwarePackageById(999L)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<SoftwarePackage> response = softwarePackageController.getSoftwarePackage(999L);

        // Assert
        assertEquals("Status should be Not Found", HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(softwarePackageService).getSoftwarePackageById(999L);
    }

    @Test
    public void testUploadSoftwarePackage_Success() throws IOException {
        // Arrange
        when(softwarePackageService.uploadSoftwarePackage(testFile, "Test description", "admin"))
                .thenReturn(testSoftwarePackage);

        // Act
        ResponseEntity<Map<String, Object>> response = softwarePackageController.uploadSoftwarePackage(testFile, "Test description");

        // Assert
        assertEquals("Status should be OK", HttpStatus.OK, response.getStatusCode());
        assertNotNull("Response body should not be null", response.getBody());
        assertTrue("Response should indicate success", (Boolean) response.getBody().get("success"));
        assertEquals("Response should contain success message", "Upload successful", response.getBody().get("message"));
        verify(softwarePackageService).uploadSoftwarePackage(testFile, "Test description", "admin");
    }

    @Test
    public void testUploadSoftwarePackage_InvalidFile() throws IOException {
        // Arrange
        when(softwarePackageService.uploadSoftwarePackage(testFile, "Test description", "admin"))
                .thenThrow(new IllegalArgumentException("Invalid file format"));

        // Act
        ResponseEntity<Map<String, Object>> response = softwarePackageController.uploadSoftwarePackage(testFile, "Test description");

        // Assert
        assertEquals("Status should be Bad Request", HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull("Response body should not be null", response.getBody());
        assertFalse("Response should indicate failure", (Boolean) response.getBody().get("success"));
        assertEquals("Response should contain error message", "Invalid file format", response.getBody().get("message"));
    }

    @Test
    public void testUploadSoftwarePackage_IOException() throws IOException {
        // Arrange
        when(softwarePackageService.uploadSoftwarePackage(testFile, "Test description", "admin"))
                .thenThrow(new IOException("File processing failed"));

        // Act
        ResponseEntity<Map<String, Object>> response = softwarePackageController.uploadSoftwarePackage(testFile, "Test description");

        // Assert
        assertEquals("Status should be Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull("Response body should not be null", response.getBody());
        assertFalse("Response should indicate failure", (Boolean) response.getBody().get("success"));
        assertEquals("Response should contain error message", "File processing failed", response.getBody().get("message"));
    }

    @Test
    public void testUploadZipPackage_Success() throws IOException {
        // Arrange
        List<SoftwarePackage> packages = Arrays.asList(testSoftwarePackage);
        when(softwarePackageService.uploadZipPackage(testFile, "admin")).thenReturn(packages);

        // Act
        ResponseEntity<Map<String, Object>> response = softwarePackageController.uploadZipPackage(testFile);

        // Assert
        assertEquals("Status should be OK", HttpStatus.OK, response.getStatusCode());
        assertNotNull("Response body should not be null", response.getBody());
        assertTrue("Response should indicate success", (Boolean) response.getBody().get("success"));
        assertEquals("Response should contain success message", "ZIP package upload successful", response.getBody().get("message"));
        assertTrue("Response should contain data", response.getBody().containsKey("data"));
        assertTrue("Response should contain count", response.getBody().containsKey("count"));
        verify(softwarePackageService).uploadZipPackage(testFile, "admin");
    }

    @Test
    public void testUploadZipPackage_InvalidFile() throws IOException {
        // Arrange
        when(softwarePackageService.uploadZipPackage(testFile, "admin"))
                .thenThrow(new IllegalArgumentException("Invalid ZIP file format"));

        // Act
        ResponseEntity<Map<String, Object>> response = softwarePackageController.uploadZipPackage(testFile);

        // Assert
        assertEquals("Status should be Bad Request", HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull("Response body should not be null", response.getBody());
        assertFalse("Response should indicate failure", (Boolean) response.getBody().get("success"));
        assertEquals("Response should contain error message", "Invalid ZIP file format", response.getBody().get("message"));
    }

    @Test
    public void testDownloadSoftwarePackage_Success() {
        // Arrange
        when(softwarePackageService.getSoftwarePackageById(1L)).thenReturn(Optional.of(testSoftwarePackage));

        // Act
        ResponseEntity<org.springframework.core.io.Resource> response = softwarePackageController.downloadSoftwarePackage(1L);

        // Assert
        assertEquals("Status should be OK", HttpStatus.OK, response.getStatusCode());
        assertNotNull("Response body should not be null", response.getBody());
        verify(softwarePackageService).getSoftwarePackageById(1L);
    }

    @Test
    public void testDownloadSoftwarePackage_NotFound() {
        // Arrange
        when(softwarePackageService.getSoftwarePackageById(999L)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<org.springframework.core.io.Resource> response = softwarePackageController.downloadSoftwarePackage(999L);

        // Assert
        assertEquals("Status should be Not Found", HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(softwarePackageService).getSoftwarePackageById(999L);
    }

    @Test
    public void testDeleteSoftwarePackage_Success() {
        // Arrange
        // No need to mock anything for successful deletion

        // Act
        ResponseEntity<Void> response = softwarePackageController.deleteSoftwarePackage(1L);

        // Assert
        assertEquals("Status should be No Content", HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(softwarePackageService).deleteSoftwarePackage(1L);
    }

    @Test
    public void testDeleteSoftwarePackage_NotFound() {
        // Arrange
        doThrow(new IllegalArgumentException("Software package does not exist"))
                .when(softwarePackageService).deleteSoftwarePackage(999L);

        // Act
        ResponseEntity<Void> response = softwarePackageController.deleteSoftwarePackage(999L);

        // Assert
        assertEquals("Status should be Not Found", HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(softwarePackageService).deleteSoftwarePackage(999L);
    }

    @Test
    public void testUpdateSoftwarePackage_Success() {
        // Arrange
        Map<String, String> request = new HashMap<>();
        request.put("softwareName", "NewApp_2.0.0.apk");
        request.put("description", "New description");

        when(softwarePackageService.updateSoftwarePackage(1L, "NewApp_2.0.0.apk", "New description"))
                .thenReturn(testSoftwarePackage);

        // Act
        ResponseEntity<SoftwarePackage> response = softwarePackageController.updateSoftwarePackage(1L, request);

        // Assert
        assertEquals("Status should be OK", HttpStatus.OK, response.getStatusCode());
        assertNotNull("Response body should not be null", response.getBody());
        assertEquals("Should return updated package", testSoftwarePackage.getId(), response.getBody().getId());
        verify(softwarePackageService).updateSoftwarePackage(1L, "NewApp_2.0.0.apk", "New description");
    }

    @Test
    public void testUpdateSoftwarePackage_InvalidParameters() {
        // Arrange
        Map<String, String> request = new HashMap<>();
        request.put("softwareName", "NewApp_2.0.0.apk");
        request.put("description", "New description");

        when(softwarePackageService.updateSoftwarePackage(1L, "NewApp_2.0.0.apk", "New description"))
                .thenThrow(new IllegalArgumentException("Invalid parameters"));

        // Act
        ResponseEntity<SoftwarePackage> response = softwarePackageController.updateSoftwarePackage(1L, request);

        // Assert
        assertEquals("Status should be Bad Request", HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void testGetStatistics_Success() {
        // Arrange
        Map<String, Long> statistics = new HashMap<>();
        statistics.put("android", 5L);
        statistics.put("ios", 3L);
        statistics.put("total", 8L);
        when(softwarePackageService.getPlatformStatistics()).thenReturn(statistics);

        // Act
        ResponseEntity<Map<String, Object>> response = softwarePackageController.getStatistics();

        // Assert
        assertEquals("Status should be OK", HttpStatus.OK, response.getStatusCode());
        assertNotNull("Response body should not be null", response.getBody());
        assertTrue("Response should indicate success", (Boolean) response.getBody().get("success"));
        assertTrue("Response should contain data", response.getBody().containsKey("data"));
        verify(softwarePackageService).getPlatformStatistics();
    }

    @Test
    public void testGetStatistics_Error() {
        // Arrange
        when(softwarePackageService.getPlatformStatistics())
                .thenThrow(new org.springframework.dao.DataAccessException("Database error") {});

        // Act
        ResponseEntity<Map<String, Object>> response = softwarePackageController.getStatistics();

        // Assert
        assertEquals("Status should be Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull("Response body should not be null", response.getBody());
        assertFalse("Response should indicate failure", (Boolean) response.getBody().get("success"));
        assertEquals("Response should contain error message", "Failed to get statistics", response.getBody().get("message"));
    }
}
