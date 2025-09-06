/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.dialtest.center.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import com.dialtest.center.entity.TestCaseSet;
import com.dialtest.center.repository.TestCaseSetRepository;

/**
 * 用例集服务测试类，测试TestCaseSetService的业务逻辑
 * 包括文件上传、下载、删除、查询等功能的测试
 * 使用Mockito模拟依赖，验证服务层的正确性
 * 
 * @author g00940940
 * @since 2025-09-06
 */
@RunWith(MockitoJUnitRunner.class)
public class TestCaseSetServiceTest {

    @Mock
    private TestCaseSetRepository testCaseSetRepository;

    @InjectMocks
    private TestCaseSetService testCaseSetService;

    private TestCaseSet testCaseSet;
    private MultipartFile mockFile;

    @Before
    public void setUp() {
        testCaseSet = new TestCaseSet();
        testCaseSet.setId(1L);
        testCaseSet.setName("test");
        testCaseSet.setVersion("v1");
        testCaseSet.setZipFile("test content".getBytes());
        testCaseSet.setCreator("admin");
        testCaseSet.setFileSize(179L);
        testCaseSet.setDescription("Test description");
        testCaseSet.setCreatedTime(LocalDateTime.now());
        testCaseSet.setUpdatedTime(LocalDateTime.now());

        mockFile = mock(MultipartFile.class);
    }

    @Test
    public void testGetTestCaseSets() {
        // Given
        Page<TestCaseSet> mockPage = new PageImpl<>(java.util.Arrays.asList(testCaseSet));
        when(testCaseSetRepository.findAllByOrderByCreatedTimeDesc(any(Pageable.class)))
                .thenReturn(mockPage);

        // When
        Page<TestCaseSet> result = testCaseSetService.getTestCaseSets(1, 10);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(testCaseSet.getName(), result.getContent().get(0).getName());
        verify(testCaseSetRepository).findAllByOrderByCreatedTimeDesc(any(Pageable.class));
    }

    @Test
    public void testGetTestCaseSetById() {
        // Given
        when(testCaseSetRepository.findById(1L)).thenReturn(Optional.of(testCaseSet));

        // When
        Optional<TestCaseSet> result = testCaseSetService.getTestCaseSetById(1L);

        // Then
        assertTrue(result.isPresent());
        assertEquals(testCaseSet.getName(), result.get().getName());
        verify(testCaseSetRepository).findById(1L);
    }

    @Test
    public void testGetTestCaseSetByIdNotFound() {
        // Given
        when(testCaseSetRepository.findById(999L)).thenReturn(Optional.empty());

        // When
        Optional<TestCaseSet> result = testCaseSetService.getTestCaseSetById(999L);

        // Then
        assertFalse(result.isPresent());
        verify(testCaseSetRepository).findById(999L);
    }

    @Test
    public void testUploadTestCaseSetSuccess() throws IOException {
        // Given
        when(mockFile.getOriginalFilename()).thenReturn("test_v1.zip");
        when(mockFile.getBytes()).thenReturn("test content".getBytes());
        when(mockFile.getSize()).thenReturn(179L);
        when(mockFile.isEmpty()).thenReturn(false);
        when(testCaseSetRepository.existsByNameAndVersion("test", "v1")).thenReturn(false);
        when(testCaseSetRepository.save(any(TestCaseSet.class))).thenReturn(testCaseSet);

        // When
        TestCaseSet result = testCaseSetService.uploadTestCaseSet(mockFile, "Test description", "admin");

        // Then
        assertNotNull(result);
        assertEquals("test", result.getName());
        assertEquals("v1", result.getVersion());
        assertEquals("admin", result.getCreator());
        assertEquals("Test description", result.getDescription());
        verify(testCaseSetRepository).existsByNameAndVersion("test", "v1");
        verify(testCaseSetRepository).save(any(TestCaseSet.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUploadTestCaseSetEmptyFile() throws IOException {
        // Given
        when(mockFile.isEmpty()).thenReturn(true);

        // When
        testCaseSetService.uploadTestCaseSet(mockFile, "Test description", "admin");

        // Then - exception expected
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUploadTestCaseSetFileTooLarge() throws IOException {
        // Given
        when(mockFile.isEmpty()).thenReturn(false);
        when(mockFile.getSize()).thenReturn(101L * 1024 * 1024); // 101MB

        // When
        testCaseSetService.uploadTestCaseSet(mockFile, "Test description", "admin");

        // Then - exception expected
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUploadTestCaseSetInvalidFileType() throws IOException {
        // Given
        when(mockFile.isEmpty()).thenReturn(false);
        when(mockFile.getSize()).thenReturn(179L);
        when(mockFile.getOriginalFilename()).thenReturn("test.txt");

        // When
        testCaseSetService.uploadTestCaseSet(mockFile, "Test description", "admin");

        // Then - exception expected
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUploadTestCaseSetInvalidFileNameFormat() throws IOException {
        // Given
        when(mockFile.isEmpty()).thenReturn(false);
        when(mockFile.getSize()).thenReturn(179L);
        when(mockFile.getOriginalFilename()).thenReturn("test.zip");

        // When
        testCaseSetService.uploadTestCaseSet(mockFile, "Test description", "admin");

        // Then - exception expected
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUploadTestCaseSetDuplicateNameVersion() throws IOException {
        // Given
        when(mockFile.isEmpty()).thenReturn(false);
        when(mockFile.getSize()).thenReturn(179L);
        when(mockFile.getOriginalFilename()).thenReturn("test_v1.zip");
        when(testCaseSetRepository.existsByNameAndVersion("test", "v1")).thenReturn(true);

        // When
        testCaseSetService.uploadTestCaseSet(mockFile, "Test description", "admin");

        // Then - exception expected
    }

    @Test
    public void testDeleteTestCaseSetSuccess() {
        // Given
        when(testCaseSetRepository.findById(1L)).thenReturn(Optional.of(testCaseSet));
        doNothing().when(testCaseSetRepository).deleteById(1L);

        // When
        testCaseSetService.deleteTestCaseSet(1L);

        // Then
        verify(testCaseSetRepository).findById(1L);
        verify(testCaseSetRepository).deleteById(1L);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDeleteTestCaseSetNotFound() {
        // Given
        when(testCaseSetRepository.findById(999L)).thenReturn(Optional.empty());

        // When
        testCaseSetService.deleteTestCaseSet(999L);

        // Then - exception expected
    }

    @Test
    public void testUpdateTestCaseSetSuccess() {
        // Given
        when(testCaseSetRepository.findById(1L)).thenReturn(Optional.of(testCaseSet));
        when(testCaseSetRepository.existsByNameAndVersion("updated_test", "v2")).thenReturn(false);
        when(testCaseSetRepository.save(any(TestCaseSet.class))).thenReturn(testCaseSet);

        // When
        TestCaseSet result = testCaseSetService.updateTestCaseSet(1L, "updated_test", "v2", "Updated description");

        // Then
        assertNotNull(result);
        verify(testCaseSetRepository).findById(1L);
        verify(testCaseSetRepository).existsByNameAndVersion("updated_test", "v2");
        verify(testCaseSetRepository).save(any(TestCaseSet.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdateTestCaseSetNotFound() {
        // Given
        when(testCaseSetRepository.findById(999L)).thenReturn(Optional.empty());

        // When
        testCaseSetService.updateTestCaseSet(999L, "updated_test", "v2", "Updated description");

        // Then - exception expected
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdateTestCaseSetDuplicateNameVersion() {
        // Given
        when(testCaseSetRepository.findById(1L)).thenReturn(Optional.of(testCaseSet));
        when(testCaseSetRepository.existsByNameAndVersion("updated_test", "v2")).thenReturn(true);

        // When
        testCaseSetService.updateTestCaseSet(1L, "updated_test", "v2", "Updated description");

        // Then - exception expected
    }

    @Test
    public void testUpdateTestCaseSetSameNameVersion() {
        // Given
        when(testCaseSetRepository.findById(1L)).thenReturn(Optional.of(testCaseSet));
        when(testCaseSetRepository.save(any(TestCaseSet.class))).thenReturn(testCaseSet);

        // When
        TestCaseSet result = testCaseSetService.updateTestCaseSet(1L, "test", "v1", "Updated description");

        // Then
        assertNotNull(result);
        verify(testCaseSetRepository).findById(1L);
        verify(testCaseSetRepository, never()).existsByNameAndVersion(anyString(), anyString());
        verify(testCaseSetRepository).save(any(TestCaseSet.class));
    }
}
