/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.dialtest.center.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import com.huawei.dialtest.center.controller.TestCaseSetController;
import com.huawei.dialtest.center.entity.TestCaseSet;
import com.huawei.dialtest.center.service.TestCaseSetService;

/**
 * 用例集控制器测试类，测试TestCaseSetController的REST API接口
 * 包括文件上传、下载、查询、删除等HTTP端点的测试
 * 使用Mockito模拟服务层依赖，验证控制器层的正确性
 * 
 * @author g00940940
 * @since 2025-09-06
 */
@RunWith(MockitoJUnitRunner.class)
public class TestCaseSetControllerTest {

    @Mock
    private TestCaseSetService testCaseSetService;

    @InjectMocks
    private TestCaseSetController testCaseSetController;

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
        testCaseSet.setFileFormat("zip");
        testCaseSet.setSha512("sha512_hash_test");
        testCaseSet.setBusiness("VPN阻断业务");
        testCaseSet.setDescription("Test description");
        testCaseSet.setCreatedTime(LocalDateTime.now());
        testCaseSet.setUpdatedTime(LocalDateTime.now());

        mockFile = mock(MultipartFile.class);
    }

    @Test
    public void testGetTestCaseSetsSuccess() {
        // Given
        Page<TestCaseSet> mockPage = new PageImpl<>(java.util.Arrays.asList(testCaseSet));
        when(testCaseSetService.getTestCaseSets(1, 10)).thenReturn(mockPage);

        // When
        ResponseEntity<Map<String, Object>> response = testCaseSetController.getTestCaseSets(1, 10);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        Map<String, Object> body = response.getBody();
        assertEquals(1L, body.get("total"));
        assertEquals(1, body.get("page"));
        assertEquals(10, body.get("pageSize"));
        assertNotNull(body.get("data"));
        verify(testCaseSetService).getTestCaseSets(1, 10);
    }

    @Test
    public void testGetTestCaseSetsWithException() {
        // Given
        when(testCaseSetService.getTestCaseSets(1, 10)).thenThrow(new org.springframework.dao.DataAccessException("Database error") {});

        // When
        ResponseEntity<Map<String, Object>> response = testCaseSetController.getTestCaseSets(1, 10);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(testCaseSetService).getTestCaseSets(1, 10);
    }

    @Test
    public void testGetTestCaseSetSuccess() {
        // Given
        when(testCaseSetService.getTestCaseSetById(1L)).thenReturn(Optional.of(testCaseSet));

        // When
        ResponseEntity<TestCaseSet> response = testCaseSetController.getTestCaseSet(1L);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(testCaseSet.getName(), response.getBody().getName());
        verify(testCaseSetService).getTestCaseSetById(1L);
    }

    @Test
    public void testGetTestCaseSetNotFound() {
        // Given
        when(testCaseSetService.getTestCaseSetById(999L)).thenReturn(Optional.empty());

        // When
        ResponseEntity<TestCaseSet> response = testCaseSetController.getTestCaseSet(999L);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(testCaseSetService).getTestCaseSetById(999L);
    }

    @Test
    public void testGetTestCaseSetWithException() {
        // Given
        when(testCaseSetService.getTestCaseSetById(1L)).thenThrow(new org.springframework.dao.DataAccessException("Database error") {});

        // When
        ResponseEntity<TestCaseSet> response = testCaseSetController.getTestCaseSet(1L);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(testCaseSetService).getTestCaseSetById(1L);
    }

    @Test
    public void testUploadTestCaseSetSuccess() throws Exception {
        // Given
        when(testCaseSetService.uploadTestCaseSet(any(MultipartFile.class), anyString(), anyString(), anyString()))
                .thenReturn(testCaseSet);

        // When
        ResponseEntity<Map<String, Object>> response = testCaseSetController.uploadTestCaseSet(mockFile, "Test description", "VPN阻断业务");

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        Map<String, Object> body = response.getBody();
        assertEquals(true, body.get("success"));
        assertEquals("Upload successful", body.get("message"));
        assertNotNull(body.get("data"));
        verify(testCaseSetService).uploadTestCaseSet(mockFile, "Test description", "admin", "VPN阻断业务");
    }

    @Test
    public void testUploadTestCaseSetWithIllegalArgumentException() throws Exception {
        // Given
        when(testCaseSetService.uploadTestCaseSet(any(MultipartFile.class), anyString(), anyString(), anyString()))
                .thenThrow(new IllegalArgumentException("文件格式错误"));

        // When
        ResponseEntity<Map<String, Object>> response = testCaseSetController.uploadTestCaseSet(mockFile, "Test description", "VPN阻断业务");

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        Map<String, Object> body = response.getBody();
        assertEquals(false, body.get("success"));
        assertEquals("文件格式错误", body.get("message"));
        verify(testCaseSetService).uploadTestCaseSet(mockFile, "Test description", "admin", "VPN阻断业务");
    }

    @Test
    public void testUploadTestCaseSetWithException() throws Exception {
        // Given
        when(testCaseSetService.uploadTestCaseSet(any(MultipartFile.class), anyString(), anyString(), anyString()))
                .thenThrow(new org.springframework.dao.DataAccessException("Upload failed") {});

        // When
        ResponseEntity<Map<String, Object>> response = testCaseSetController.uploadTestCaseSet(mockFile, "Test description", "VPN阻断业务");

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        Map<String, Object> body = response.getBody();
        assertEquals(false, body.get("success"));
        assertEquals("Upload failed", body.get("message"));
        verify(testCaseSetService).uploadTestCaseSet(mockFile, "Test description", "admin", "VPN阻断业务");
    }

    @Test
    public void testDownloadTestCaseSetSuccess() {
        // Given
        when(testCaseSetService.getTestCaseSetById(1L)).thenReturn(Optional.of(testCaseSet));

        // When
        ResponseEntity<Resource> response = testCaseSetController.downloadTestCaseSet(1L);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof ByteArrayResource);
        verify(testCaseSetService).getTestCaseSetById(1L);
    }

    @Test
    public void testDownloadTestCaseSetNotFound() {
        // Given
        when(testCaseSetService.getTestCaseSetById(999L)).thenReturn(Optional.empty());

        // When
        ResponseEntity<Resource> response = testCaseSetController.downloadTestCaseSet(999L);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(testCaseSetService).getTestCaseSetById(999L);
    }

    @Test
    public void testDownloadTestCaseSetEmptyFile() {
        // Given
        TestCaseSet emptyTestCaseSet = new TestCaseSet();
        emptyTestCaseSet.setId(1L);
        emptyTestCaseSet.setName("test");
        emptyTestCaseSet.setVersion("v1");
        emptyTestCaseSet.setFileContent(null);
        emptyTestCaseSet.setSha512("empty_sha512");
        emptyTestCaseSet.setBusiness("VPN阻断业务");
        when(testCaseSetService.getTestCaseSetById(1L)).thenReturn(Optional.of(emptyTestCaseSet));

        // When
        ResponseEntity<Resource> response = testCaseSetController.downloadTestCaseSet(1L);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(testCaseSetService).getTestCaseSetById(1L);
    }

    @Test
    public void testDownloadTestCaseSetWithException() {
        // Given
        when(testCaseSetService.getTestCaseSetById(1L)).thenThrow(new org.springframework.dao.DataAccessException("Download failed") {});

        // When
        ResponseEntity<Resource> response = testCaseSetController.downloadTestCaseSet(1L);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(testCaseSetService).getTestCaseSetById(1L);
    }

    @Test
    public void testDeleteTestCaseSetSuccess() {
        // Given
        doNothing().when(testCaseSetService).deleteTestCaseSet(1L);

        // When
        ResponseEntity<Void> response = testCaseSetController.deleteTestCaseSet(1L);

        // Then
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(testCaseSetService).deleteTestCaseSet(1L);
    }

    @Test
    public void testDeleteTestCaseSetWithIllegalArgumentException() {
        // Given
        doThrow(new IllegalArgumentException("用例集不存在")).when(testCaseSetService).deleteTestCaseSet(999L);

        // When
        ResponseEntity<Void> response = testCaseSetController.deleteTestCaseSet(999L);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(testCaseSetService).deleteTestCaseSet(999L);
    }

    @Test
    public void testDeleteTestCaseSetWithException() {
        // Given
        doThrow(new org.springframework.dao.DataAccessException("Delete failed") {}).when(testCaseSetService).deleteTestCaseSet(1L);

        // When
        ResponseEntity<Void> response = testCaseSetController.deleteTestCaseSet(1L);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(testCaseSetService).deleteTestCaseSet(1L);
    }

    @Test
    public void testUpdateTestCaseSetSuccess() {
        // Given
        Map<String, String> request = new HashMap<>();
        request.put("name", "updated_test");
        request.put("version", "v2");
        request.put("description", "Updated description");

        when(testCaseSetService.updateTestCaseSet(1L, "updated_test", "v2", "Updated description"))
                .thenReturn(testCaseSet);

        // When
        ResponseEntity<TestCaseSet> response = testCaseSetController.updateTestCaseSet(1L, request);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(testCaseSet.getName(), response.getBody().getName());
        verify(testCaseSetService).updateTestCaseSet(1L, "updated_test", "v2", "Updated description");
    }

    @Test
    public void testUpdateTestCaseSetWithIllegalArgumentException() {
        // Given
        Map<String, String> request = new HashMap<>();
        request.put("name", "updated_test");
        request.put("version", "v2");
        request.put("description", "Updated description");

        when(testCaseSetService.updateTestCaseSet(1L, "updated_test", "v2", "Updated description"))
                .thenThrow(new IllegalArgumentException("用例集不存在"));

        // When
        ResponseEntity<TestCaseSet> response = testCaseSetController.updateTestCaseSet(1L, request);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(testCaseSetService).updateTestCaseSet(1L, "updated_test", "v2", "Updated description");
    }

    @Test
    public void testUpdateTestCaseSetWithException() {
        // Given
        Map<String, String> request = new HashMap<>();
        request.put("name", "updated_test");
        request.put("version", "v2");
        request.put("description", "Updated description");

        when(testCaseSetService.updateTestCaseSet(1L, "updated_test", "v2", "Updated description"))
                .thenThrow(new org.springframework.dao.DataAccessException("Update failed") {});

        // When
        ResponseEntity<TestCaseSet> response = testCaseSetController.updateTestCaseSet(1L, request);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(testCaseSetService).updateTestCaseSet(1L, "updated_test", "v2", "Updated description");
    }
}
