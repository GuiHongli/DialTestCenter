/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.dialtest.center.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.lenient;

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

import com.huawei.dialtest.center.entity.TestCaseSet;
import com.huawei.dialtest.center.mapper.TestCaseSetMapper;
import com.huawei.dialtest.center.service.ArchiveParseService;
import com.huawei.dialtest.center.service.ExcelParseService;
import com.huawei.dialtest.center.service.ScriptMatchService;
import com.huawei.dialtest.center.service.TestCaseService;
import com.huawei.dialtest.center.service.TestCaseSetService;

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
    private TestCaseSetMapper testCaseSetMapper;

    @Mock
    private TestCaseService testCaseService;

    @Mock
    private ArchiveParseService archiveParseService;

    @Mock
    private ExcelParseService excelParseService;

    @Mock
    private ScriptMatchService scriptMatchService;

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
        testCaseSet.setFileContent("test content".getBytes());
        testCaseSet.setCreator("admin");
        testCaseSet.setFileSize(179L);
        testCaseSet.setSha512("sha512_hash_test");
        testCaseSet.setBusiness("VPN阻断业务");
        testCaseSet.setDescription("Test description");

        mockFile = mock(MultipartFile.class);
    }

    @Test
    public void testGetTestCaseSets() {
        // Given
        Page<TestCaseSet> mockPage = new PageImpl<>(java.util.Arrays.asList(testCaseSet));
        when(testCaseSetMapper.findAllByOrderByCreatedTimeDesc(anyInt(), anyInt()))
                .thenReturn(java.util.Arrays.asList(testCaseSet));
        when(testCaseSetMapper.count()).thenReturn(1L);

        // When
        Page<TestCaseSet> result = testCaseSetService.getTestCaseSets(1, 10);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(testCaseSet.getName(), result.getContent().get(0).getName());
        //verify(testCaseSetMapper).findAllByOrderByCreatedTimeDesc(any(Pageable.class));
    }

    @Test
    public void testGetTestCaseSetById() {
        // Given
        when(testCaseSetMapper.findById(1L)).thenReturn(testCaseSet);

        // When
        Optional<TestCaseSet> result = testCaseSetService.getTestCaseSetById(1L);

        // Then
        assertTrue(result.isPresent());
        assertEquals(testCaseSet.getName(), result.get().getName());
        verify(testCaseSetMapper).findById(1L);
    }

    @Test
    public void testGetTestCaseSetByIdNotFound() {
        // Given
        when(testCaseSetMapper.findById(999L)).thenReturn(null);

        // When
        Optional<TestCaseSet> result = testCaseSetService.getTestCaseSetById(999L);

        // Then
        assertFalse(result.isPresent());
        verify(testCaseSetMapper).findById(999L);
    }

    @Test
    public void testUploadTestCaseSetSuccess() throws IOException {
        // Given
        when(mockFile.getOriginalFilename()).thenReturn("test_v1.zip");
        when(mockFile.getBytes()).thenReturn("test content".getBytes());
        when(mockFile.getSize()).thenReturn(179L);
        when(mockFile.isEmpty()).thenReturn(false);
        when(testCaseSetMapper.existsByNameAndVersion("test", "v1")).thenReturn(false);
        when(testCaseSetMapper.insert(any(TestCaseSet.class))).thenReturn(1);
        
        // Mock archive validation
        ArchiveParseService.ArchiveValidationResult validationResult = 
            new ArchiveParseService.ArchiveValidationResult(true, true, 1);
        when(archiveParseService.validateArchive(any(byte[].class), anyString()))
            .thenReturn(validationResult);
        
        // Mock Excel extraction and parsing
        when(archiveParseService.extractCasesExcel(any(byte[].class), anyString()))
            .thenReturn("Excel content".getBytes());
        lenient().when(excelParseService.parseCasesExcel(any(byte[].class)))
            .thenReturn(new java.util.ArrayList<>());
        
        // Mock script extraction and matching
        lenient().when(archiveParseService.extractScriptFileNames(any(byte[].class), anyString()))
            .thenReturn(new java.util.ArrayList<>());
        lenient().when(scriptMatchService.matchScripts(any(), any()))
            .thenReturn(new ScriptMatchService.ScriptMatchResult(new java.util.HashMap<>(), new java.util.ArrayList<>(), new java.util.ArrayList<>()));
        
        // Mock test case service
        lenient().when(testCaseService.saveTestCases(any())).thenReturn(new java.util.ArrayList<>());

        // When
        TestCaseSet result = testCaseSetService.uploadTestCaseSet(mockFile, "Test description", "admin", "VPN阻断业务");

        // Then
        assertNotNull(result);
        assertEquals("test", result.getName());
        assertEquals("v1", result.getVersion());
        assertEquals("admin", result.getCreator());
        assertEquals("Test description", result.getDescription());
        verify(testCaseSetMapper).existsByNameAndVersion("test", "v1");
        verify(testCaseSetMapper).insert(any(TestCaseSet.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUploadTestCaseSetEmptyFile() throws IOException {
        // Given
        when(mockFile.isEmpty()).thenReturn(true);

        // When
        testCaseSetService.uploadTestCaseSet(mockFile, "Test description", "admin", "VPN阻断业务");

        // Then - exception expected
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUploadTestCaseSetFileTooLarge() throws IOException {
        // Given
        when(mockFile.isEmpty()).thenReturn(false);
        when(mockFile.getSize()).thenReturn(101L * 1024 * 1024); // 101MB

        // When
        testCaseSetService.uploadTestCaseSet(mockFile, "Test description", "admin", "VPN阻断业务");

        // Then - exception expected
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUploadTestCaseSetInvalidFileType() throws IOException {
        // Given
        when(mockFile.isEmpty()).thenReturn(false);
        when(mockFile.getSize()).thenReturn(179L);
        when(mockFile.getOriginalFilename()).thenReturn("test.txt");

        // When
        testCaseSetService.uploadTestCaseSet(mockFile, "Test description", "admin", "VPN阻断业务");

        // Then - exception expected
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUploadTestCaseSetInvalidFileNameFormat() throws IOException {
        // Given
        when(mockFile.isEmpty()).thenReturn(false);
        when(mockFile.getSize()).thenReturn(179L);
        when(mockFile.getOriginalFilename()).thenReturn("test.zip");

        // When
        testCaseSetService.uploadTestCaseSet(mockFile, "Test description", "admin", "VPN阻断业务");

        // Then - exception expected
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUploadTestCaseSetDuplicateNameVersion() throws IOException {
        // Given
        when(mockFile.isEmpty()).thenReturn(false);
        when(mockFile.getSize()).thenReturn(179L);
        when(mockFile.getOriginalFilename()).thenReturn("test_v1.zip");
        when(testCaseSetMapper.existsByNameAndVersion("test", "v1")).thenReturn(true);

        // When
        testCaseSetService.uploadTestCaseSet(mockFile, "Test description", "admin", "VPN阻断业务");

        // Then - exception expected
    }

    @Test
    public void testDeleteTestCaseSetSuccess() {
        // Given
        when(testCaseSetMapper.findById(1L)).thenReturn(testCaseSet);
        when(testCaseSetMapper.deleteById(1L)).thenReturn(1);

        // When
        testCaseSetService.deleteTestCaseSet(1L);

        // Then
        verify(testCaseSetMapper).findById(1L);
        verify(testCaseSetMapper).deleteById(1L);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDeleteTestCaseSetNotFound() {
        // Given
        when(testCaseSetMapper.findById(999L)).thenReturn(null);

        // When
        testCaseSetService.deleteTestCaseSet(999L);

        // Then - exception expected
    }

    @Test
    public void testUpdateTestCaseSetSuccess() {
        // Given
        when(testCaseSetMapper.findById(1L)).thenReturn(testCaseSet);
        when(testCaseSetMapper.existsByNameAndVersion("updated_test", "v2")).thenReturn(false);
        when(testCaseSetMapper.update(any(TestCaseSet.class))).thenReturn(1);

        // When
        TestCaseSet result = testCaseSetService.updateTestCaseSet(1L, "updated_test", "v2", "Updated description");

        // Then
        assertNotNull(result);
        verify(testCaseSetMapper).findById(1L);
        verify(testCaseSetMapper).existsByNameAndVersion("updated_test", "v2");
        verify(testCaseSetMapper).update(any(TestCaseSet.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdateTestCaseSetNotFound() {
        // Given
        when(testCaseSetMapper.findById(999L)).thenReturn(null);

        // When
        testCaseSetService.updateTestCaseSet(999L, "updated_test", "v2", "Updated description");

        // Then - exception expected
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdateTestCaseSetDuplicateNameVersion() {
        // Given
        when(testCaseSetMapper.findById(1L)).thenReturn(testCaseSet);
        when(testCaseSetMapper.existsByNameAndVersion("updated_test", "v2")).thenReturn(true);

        // When
        testCaseSetService.updateTestCaseSet(1L, "updated_test", "v2", "Updated description");

        // Then - exception expected
    }

    @Test
    public void testUpdateTestCaseSetSameNameVersion() {
        // Given
        when(testCaseSetMapper.findById(1L)).thenReturn(testCaseSet);
        when(testCaseSetMapper.update(any(TestCaseSet.class))).thenReturn(1);

        // When
        TestCaseSet result = testCaseSetService.updateTestCaseSet(1L, "test", "v1", "Updated description");

        // Then
        assertNotNull(result);
        verify(testCaseSetMapper).findById(1L);
        verify(testCaseSetMapper, never()).existsByNameAndVersion(anyString(), anyString());
        verify(testCaseSetMapper).update(any(TestCaseSet.class));
    }
}
