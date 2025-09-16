/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.dialtest.center.controller;

import com.huawei.dialtest.center.dto.ApiResponse;
import com.huawei.dialtest.center.dto.PagedResponse;
import com.huawei.dialtest.center.entity.SoftwarePackage;
import com.huawei.dialtest.center.service.SoftwarePackageService;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * SoftwarePackageController测试类
 *
 * @author g00940940
 * @since 2025-01-27
 */
@RunWith(MockitoJUnitRunner.class)
public class SoftwarePackageControllerTest {

    @Mock
    private SoftwarePackageService softwarePackageService;

    @Mock
    private MultipartFile multipartFile;

    @InjectMocks
    private SoftwarePackageController softwarePackageController;

    private SoftwarePackage testSoftwarePackage;
    private List<SoftwarePackage> testSoftwarePackages;

    @Before
    public void setUp() {
        testSoftwarePackage = new SoftwarePackage();
        testSoftwarePackage.setId(1L);
        testSoftwarePackage.setSoftwareName("TestApp");
        testSoftwarePackage.setPlatform("Android");
        testSoftwarePackage.setCreator("admin");
        testSoftwarePackage.setDescription("Test application");

        testSoftwarePackages = Arrays.asList(testSoftwarePackage);
    }

    @Test
    public void testGetSoftwarePackages_Success() {
        Page<SoftwarePackage> page = new PageImpl<>(testSoftwarePackages, PageRequest.of(0, 10), 1L);
        when(softwarePackageService.getSoftwarePackages(anyInt(), anyInt(), any(), any(), any()))
                .thenReturn(page);

        ResponseEntity<ApiResponse<PagedResponse<SoftwarePackage>>> response = softwarePackageController
                .getSoftwarePackages(1, 10, "Android", "admin", "TestApp");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertNotNull(response.getBody().getData());
        assertEquals(1, response.getBody().getData().getData().size());
        assertEquals(1L, response.getBody().getData().getTotal());
    }

    @Test
    public void testGetSoftwarePackages_ValidationError() {
        when(softwarePackageService.getSoftwarePackages(anyInt(), anyInt(), any(), any(), any()))
                .thenThrow(new IllegalArgumentException("Invalid parameters"));

        ResponseEntity<ApiResponse<PagedResponse<SoftwarePackage>>> response = softwarePackageController
                .getSoftwarePackages(1, 10, "Android", "admin", "TestApp");

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("VALIDATION_ERROR", response.getBody().getErrorCode());
    }

    @Test
    public void testGetSoftwarePackage_Success() {
        when(softwarePackageService.getSoftwarePackageById(1L)).thenReturn(Optional.of(testSoftwarePackage));

        ResponseEntity<ApiResponse<SoftwarePackage>> response = softwarePackageController.getSoftwarePackage(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals(testSoftwarePackage, response.getBody().getData());
    }

    @Test
    public void testGetSoftwarePackage_NotFound() {
        when(softwarePackageService.getSoftwarePackageById(1L)).thenReturn(Optional.empty());

        ResponseEntity<ApiResponse<SoftwarePackage>> response = softwarePackageController.getSoftwarePackage(1L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("NOT_FOUND", response.getBody().getErrorCode());
    }

    @Test
    public void testUploadSoftwarePackage_Success() throws IOException {
        when(multipartFile.getOriginalFilename()).thenReturn("TestApp.apk");
        when(softwarePackageService.uploadSoftwarePackage(any(), any(), any())).thenReturn(testSoftwarePackage);

        ResponseEntity<ApiResponse<SoftwarePackage>> response = softwarePackageController
                .uploadSoftwarePackage(multipartFile, "Test description");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals(testSoftwarePackage, response.getBody().getData());
        assertEquals("Upload successful", response.getBody().getMessage());
    }

    @Test
    public void testUploadSoftwarePackage_ValidationError() throws IOException {
        when(multipartFile.getOriginalFilename()).thenReturn("TestApp.apk");
        when(softwarePackageService.uploadSoftwarePackage(any(), any(), any()))
                .thenThrow(new IllegalArgumentException("Invalid file format"));

        ResponseEntity<ApiResponse<SoftwarePackage>> response = softwarePackageController
                .uploadSoftwarePackage(multipartFile, "Test description");

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("VALIDATION_ERROR", response.getBody().getErrorCode());
    }

    @Test
    public void testUploadSoftwarePackage_IOException() throws IOException {
        when(multipartFile.getOriginalFilename()).thenReturn("TestApp.apk");
        when(softwarePackageService.uploadSoftwarePackage(any(), any(), any()))
                .thenThrow(new IOException("File processing failed"));

        ResponseEntity<ApiResponse<SoftwarePackage>> response = softwarePackageController
                .uploadSoftwarePackage(multipartFile, "Test description");

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("FILE_PROCESSING_ERROR", response.getBody().getErrorCode());
    }

    @Test
    public void testUploadZipPackage_Success() throws IOException {
        when(multipartFile.getOriginalFilename()).thenReturn("packages.zip");
        when(softwarePackageService.uploadZipPackage(any(), any())).thenReturn(testSoftwarePackages);

        ResponseEntity<ApiResponse<List<SoftwarePackage>>> response = softwarePackageController
                .uploadZipPackage(multipartFile);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals(testSoftwarePackages, response.getBody().getData());
        assertEquals("ZIP package upload successful", response.getBody().getMessage());
    }

    @Test
    public void testUpdateSoftwarePackage_Success() {
        when(softwarePackageService.updateSoftwarePackage(anyLong(), any(), any())).thenReturn(testSoftwarePackage);

        Map<String, String> request = new HashMap<>();
        request.put("softwareName", "UpdatedApp");
        request.put("description", "Updated description");

        ResponseEntity<ApiResponse<SoftwarePackage>> response = softwarePackageController
                .updateSoftwarePackage(1L, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals(testSoftwarePackage, response.getBody().getData());
    }

    @Test
    public void testUpdateSoftwarePackage_ValidationError() {
        when(softwarePackageService.updateSoftwarePackage(anyLong(), any(), any()))
                .thenThrow(new IllegalArgumentException("Software package not found"));

        Map<String, String> request = new HashMap<>();
        request.put("softwareName", "UpdatedApp");
        request.put("description", "Updated description");

        ResponseEntity<ApiResponse<SoftwarePackage>> response = softwarePackageController
                .updateSoftwarePackage(1L, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("VALIDATION_ERROR", response.getBody().getErrorCode());
    }

    @Test
    public void testGetStatistics_Success() {
        Map<String, Long> statistics = new HashMap<>();
        statistics.put("Android", 5L);
        statistics.put("iOS", 3L);
        when(softwarePackageService.getPlatformStatistics()).thenReturn(statistics);

        ResponseEntity<ApiResponse<Map<String, Long>>> response = softwarePackageController.getStatistics();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals(statistics, response.getBody().getData());
    }
}