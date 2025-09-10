/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.dialtest.center.service;

import com.huawei.dialtest.center.entity.SoftwarePackage;
import com.huawei.dialtest.center.repository.SoftwarePackageRepository;
import com.huawei.dialtest.center.service.SoftwarePackageService;

import org.junit.Test;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * SoftwarePackageService服务类测试
 * 测试软件包服务的业务逻辑，包括上传、下载、删除、查询等功能
 *
 * @author g00940940
 * @since 2025-09-09
 */
@RunWith(MockitoJUnitRunner.class)
public class SoftwarePackageServiceTest {

    @Mock
    private SoftwarePackageRepository softwarePackageRepository;

    @InjectMocks
    private SoftwarePackageService softwarePackageService;

    private SoftwarePackage testSoftwarePackage;
    private MultipartFile testFile;
    private byte[] testFileContent;

    @Before
    public void setUp() {
        testFileContent = "test file content".getBytes();
        
        testSoftwarePackage = new SoftwarePackage();
        testSoftwarePackage.setId(1L);
        testSoftwarePackage.setSoftwareName("TestApp_1.0.0.apk");
        testSoftwarePackage.setFileContent(testFileContent);
        testSoftwarePackage.setFileFormat("apk");
        testSoftwarePackage.setSha512("test_sha512_hash");
        testSoftwarePackage.setPlatform("android");
        testSoftwarePackage.setCreator("admin");
        testSoftwarePackage.setFileSize(1024L);
        testSoftwarePackage.setDescription("Test description");

        testFile = mock(MultipartFile.class);
        when(testFile.getOriginalFilename()).thenReturn("TestApp_1.0.0.apk");
        when(testFile.getSize()).thenReturn(1024L);
        when(testFile.isEmpty()).thenReturn(false);
    }

    @Test
    public void testGetSoftwarePackages_Success() {
        // Arrange
        List<SoftwarePackage> packages = Arrays.asList(testSoftwarePackage);
        Page<SoftwarePackage> page = new PageImpl<>(packages);
        when(softwarePackageRepository.findAllByOrderByCreatedTimeDesc(any(Pageable.class))).thenReturn(page);

        // Act
        Page<SoftwarePackage> result = softwarePackageService.getSoftwarePackages(1, 10, null, null, null);

        // Assert
        assertNotNull("Result should not be null", result);
        assertEquals("Should return 1 package", 1, result.getContent().size());
        assertEquals("Should return correct package", testSoftwarePackage.getSoftwareName(), 
                    result.getContent().get(0).getSoftwareName());
        verify(softwarePackageRepository).findAllByOrderByCreatedTimeDesc(any(Pageable.class));
    }

    @Test
    public void testGetSoftwarePackages_WithFilters() {
        // Arrange
        List<SoftwarePackage> packages = Arrays.asList(testSoftwarePackage);
        Page<SoftwarePackage> page = new PageImpl<>(packages);
        when(softwarePackageRepository.findByConditions(eq("android"), eq("admin"), eq("TestApp"), any(Pageable.class)))
                .thenReturn(page);

        // Act
        Page<SoftwarePackage> result = softwarePackageService.getSoftwarePackages(1, 10, "android", "admin", "TestApp");

        // Assert
        assertNotNull("Result should not be null", result);
        assertEquals("Should return 1 package", 1, result.getContent().size());
        verify(softwarePackageRepository).findByConditions(eq("android"), eq("admin"), eq("TestApp"), any(Pageable.class));
    }

    @Test
    public void testGetSoftwarePackageById_Success() {
        // Arrange
        when(softwarePackageRepository.findById(1L)).thenReturn(Optional.of(testSoftwarePackage));

        // Act
        Optional<SoftwarePackage> result = softwarePackageService.getSoftwarePackageById(1L);

        // Assert
        assertTrue("Result should be present", result.isPresent());
        assertEquals("Should return correct package", testSoftwarePackage.getId(), result.get().getId());
        verify(softwarePackageRepository).findById(1L);
    }

    @Test
    public void testGetSoftwarePackageById_NotFound() {
        // Arrange
        when(softwarePackageRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        Optional<SoftwarePackage> result = softwarePackageService.getSoftwarePackageById(999L);

        // Assert
        assertFalse("Result should not be present", result.isPresent());
        verify(softwarePackageRepository).findById(999L);
    }

    @Test
    public void testUploadSoftwarePackage_APK_Success() throws IOException {
        // Arrange
        when(testFile.getBytes()).thenReturn(testFileContent);
        when(softwarePackageRepository.existsBySoftwareName("TestApp_1.0.0.apk")).thenReturn(false);
        when(softwarePackageRepository.existsBySha512(anyString())).thenReturn(false);
        when(softwarePackageRepository.save(any(SoftwarePackage.class))).thenReturn(testSoftwarePackage);

        // Act
        SoftwarePackage result = softwarePackageService.uploadSoftwarePackage(testFile, "Test description", "admin");

        // Assert
        assertNotNull("Result should not be null", result);
        assertEquals("Should return correct package", testSoftwarePackage.getId(), result.getId());
        verify(softwarePackageRepository).existsBySoftwareName("TestApp_1.0.0.apk");
        verify(softwarePackageRepository).existsBySha512(anyString());
        verify(softwarePackageRepository).save(any(SoftwarePackage.class));
    }

    @Test
    public void testUploadSoftwarePackage_IPA_Success() throws IOException {
        // Arrange
        when(testFile.getOriginalFilename()).thenReturn("TestApp_1.0.0.ipa");
        when(testFile.getBytes()).thenReturn(testFileContent);
        when(softwarePackageRepository.existsBySoftwareName("TestApp_1.0.0.ipa")).thenReturn(false);
        when(softwarePackageRepository.existsBySha512(anyString())).thenReturn(false);
        when(softwarePackageRepository.save(any(SoftwarePackage.class))).thenReturn(testSoftwarePackage);

        // Act
        SoftwarePackage result = softwarePackageService.uploadSoftwarePackage(testFile, "Test description", "admin");

        // Assert
        assertNotNull("Result should not be null", result);
        verify(softwarePackageRepository).existsBySoftwareName("TestApp_1.0.0.ipa");
        verify(softwarePackageRepository).save(any(SoftwarePackage.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUploadSoftwarePackage_EmptyFile() throws IOException {
        // Arrange
        when(testFile.isEmpty()).thenReturn(true);

        // Act
        softwarePackageService.uploadSoftwarePackage(testFile, "Test description", "admin");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUploadSoftwarePackage_UnsupportedFormat() throws IOException {
        // Arrange
        when(testFile.getOriginalFilename()).thenReturn("TestApp.txt");

        // Act
        softwarePackageService.uploadSoftwarePackage(testFile, "Test description", "admin");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUploadSoftwarePackage_FileTooLarge() throws IOException {
        // Arrange
        when(testFile.getSize()).thenReturn(600L * 1024 * 1024); // 600MB

        // Act
        softwarePackageService.uploadSoftwarePackage(testFile, "Test description", "admin");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUploadSoftwarePackage_FileNameExists() throws IOException {
        // Arrange
        when(softwarePackageRepository.existsBySoftwareName("TestApp_1.0.0.apk")).thenReturn(true);

        // Act
        softwarePackageService.uploadSoftwarePackage(testFile, "Test description", "admin");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUploadSoftwarePackage_SHA512Exists() throws IOException {
        // Arrange
        when(testFile.getBytes()).thenReturn(testFileContent);
        when(softwarePackageRepository.existsBySoftwareName("TestApp_1.0.0.apk")).thenReturn(false);
        when(softwarePackageRepository.existsBySha512(anyString())).thenReturn(true);

        // Act
        softwarePackageService.uploadSoftwarePackage(testFile, "Test description", "admin");
    }

    @Test
    public void testDeleteSoftwarePackage_Success() {
        // Arrange
        when(softwarePackageRepository.findById(1L)).thenReturn(Optional.of(testSoftwarePackage));

        // Act
        softwarePackageService.deleteSoftwarePackage(1L);

        // Assert
        verify(softwarePackageRepository).findById(1L);
        verify(softwarePackageRepository).deleteById(1L);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDeleteSoftwarePackage_NotFound() {
        // Arrange
        when(softwarePackageRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        softwarePackageService.deleteSoftwarePackage(999L);
    }

    @Test
    public void testUpdateSoftwarePackage_Success() {
        // Arrange
        when(softwarePackageRepository.findById(1L)).thenReturn(Optional.of(testSoftwarePackage));
        when(softwarePackageRepository.save(any(SoftwarePackage.class))).thenReturn(testSoftwarePackage);

        // Act
        SoftwarePackage result = softwarePackageService.updateSoftwarePackage(1L, "NewApp_2.0.0.apk", "New description");

        // Assert
        assertNotNull("Result should not be null", result);
        verify(softwarePackageRepository).findById(1L);
        verify(softwarePackageRepository).save(any(SoftwarePackage.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdateSoftwarePackage_NotFound() {
        // Arrange
        when(softwarePackageRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        softwarePackageService.updateSoftwarePackage(999L, "NewApp_2.0.0.apk", "New description");
    }

    @Test
    public void testGetPlatformStatistics() {
        // Arrange
        when(softwarePackageRepository.countByPlatform("android")).thenReturn(5L);
        when(softwarePackageRepository.countByPlatform("ios")).thenReturn(3L);
        when(softwarePackageRepository.count()).thenReturn(8L);

        // Act
        Map<String, Long> result = softwarePackageService.getPlatformStatistics();

        // Assert
        assertNotNull("Result should not be null", result);
        assertEquals("Android count should be 5", Long.valueOf(5L), result.get("android"));
        assertEquals("iOS count should be 3", Long.valueOf(3L), result.get("ios"));
        assertEquals("Total count should be 8", Long.valueOf(8L), result.get("total"));
        verify(softwarePackageRepository).countByPlatform("android");
        verify(softwarePackageRepository).countByPlatform("ios");
        verify(softwarePackageRepository).count();
    }

    @Test
    public void testUploadZipPackage_Success() throws IOException {
        // Arrange
        MultipartFile zipFile = mock(MultipartFile.class);
        when(zipFile.getOriginalFilename()).thenReturn("packages.zip");
        when(zipFile.getSize()).thenReturn(1024L);
        when(zipFile.isEmpty()).thenReturn(false);
        when(zipFile.getInputStream()).thenReturn(new java.io.ByteArrayInputStream(
            createTestZipContent()
        ));
        when(softwarePackageRepository.existsBySoftwareName(anyString())).thenReturn(false);
        when(softwarePackageRepository.existsBySha512(anyString())).thenReturn(false);
        when(softwarePackageRepository.save(any(SoftwarePackage.class))).thenReturn(testSoftwarePackage);

        // Act
        List<SoftwarePackage> result = softwarePackageService.uploadZipPackage(zipFile, "admin");

        // Assert
        assertNotNull("Result should not be null", result);
        // Note: The actual number depends on the test ZIP content
        verify(softwarePackageRepository, atLeastOnce()).save(any(SoftwarePackage.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUploadZipPackage_NotZipFile() throws IOException {
        // Arrange
        MultipartFile zipFile = mock(MultipartFile.class);
        when(zipFile.getOriginalFilename()).thenReturn("packages.tar.gz");
        when(zipFile.isEmpty()).thenReturn(false);

        // Act
        softwarePackageService.uploadZipPackage(zipFile, "admin");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUploadZipPackage_FileTooLarge() throws IOException {
        // Arrange
        MultipartFile zipFile = mock(MultipartFile.class);
        when(zipFile.getOriginalFilename()).thenReturn("packages.zip");
        when(zipFile.getSize()).thenReturn(2L * 1024 * 1024 * 1024); // 2GB
        when(zipFile.isEmpty()).thenReturn(false);

        // Act
        softwarePackageService.uploadZipPackage(zipFile, "admin");
    }

    /**
     * 创建测试用的ZIP文件内容
     * 这里创建一个简单的ZIP文件，包含一个APK文件
     */
    private byte[] createTestZipContent() {
        try {
            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            java.util.zip.ZipOutputStream zos = new java.util.zip.ZipOutputStream(baos);
            
            // 添加一个APK文件到ZIP中
            java.util.zip.ZipEntry apkEntry = new java.util.zip.ZipEntry("TestApp_1.0.0.apk");
            zos.putNextEntry(apkEntry);
            zos.write("Test APK content".getBytes());
            zos.closeEntry();
            
            // 添加一个IPA文件到ZIP中
            java.util.zip.ZipEntry ipaEntry = new java.util.zip.ZipEntry("TestApp_2.0.0.ipa");
            zos.putNextEntry(ipaEntry);
            zos.write("Test IPA content".getBytes());
            zos.closeEntry();
            
            zos.close();
            return baos.toByteArray();
        } catch (Exception e) {
            // 如果创建失败，返回一个基本的ZIP文件头
            return new byte[]{
                0x50, 0x4B, 0x03, 0x04, // ZIP file signature
                0x14, 0x00, 0x00, 0x00, // Version needed to extract
                0x00, 0x00, 0x00, 0x00, // General purpose bit flag
                0x00, 0x00, 0x00, 0x00, // Compression method
                0x00, 0x00, 0x00, 0x00, // Last mod file time
                0x00, 0x00, 0x00, 0x00, // Last mod file date
                0x00, 0x00, 0x00, 0x00, // CRC-32
                0x00, 0x00, 0x00, 0x00, // Compressed size
                0x00, 0x00, 0x00, 0x00, // Uncompressed size
                0x00, 0x00, 0x00, 0x00, // File name length
                0x00, 0x00, 0x00, 0x00  // Extra field length
            };
        }
    }
}
