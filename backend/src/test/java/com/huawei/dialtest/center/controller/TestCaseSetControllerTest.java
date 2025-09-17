/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.dialtest.center.controller;

import com.huawei.dialtest.center.dto.BaseApiResponse;
import com.huawei.dialtest.center.dto.PagedResponse;
import com.huawei.dialtest.center.entity.TestCase;
import com.huawei.dialtest.center.entity.TestCaseSet;
import com.huawei.dialtest.center.service.TestCaseSetService;

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
 * TestCaseSetController测试类
 *
 * @author g00940940
 * @since 2025-01-27
 */
@RunWith(MockitoJUnitRunner.class)
public class TestCaseSetControllerTest {

    @Mock
    private TestCaseSetService testCaseSetService;

    @Mock
    private MultipartFile multipartFile;

    @InjectMocks
    private TestCaseSetController testCaseSetController;

    private TestCaseSet testTestCaseSet;
    private TestCase testCase;
    private List<TestCaseSet> testTestCaseSets;
    private List<TestCase> testCases;

    @Before
    public void setUp() {
        testTestCaseSet = new TestCaseSet();
        testTestCaseSet.setId(1L);
        testTestCaseSet.setName("TestCaseSet");
        testTestCaseSet.setVersion("1.0.0");
        testTestCaseSet.setDescription("Test case set description");
        testTestCaseSet.setCreator("admin");
        testTestCaseSet.setBusiness("VPN阻断业务");

        testCase = new TestCase();
        testCase.setId(1L);
        testCase.setCaseNumber("TC001");
        testCase.setCaseName("Test Case 1");
        // Note: testCaseSetId is set automatically by the entity

        testTestCaseSets = Arrays.asList(testTestCaseSet);
        testCases = Arrays.asList(testCase);
    }

    @Test
    public void testGetTestCaseSets_Success() {
        Page<TestCaseSet> page = new PageImpl<>(testTestCaseSets, PageRequest.of(0, 10), 1L);
        when(testCaseSetService.getTestCaseSets(anyInt(), anyInt())).thenReturn(page);

        ResponseEntity<BaseApiResponse<PagedResponse<TestCaseSet>>> response = testCaseSetController
                .getTestCaseSets(1, 10);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertNotNull(response.getBody().getData());
        assertEquals(1, response.getBody().getData().getData().size());
        assertEquals(1L, response.getBody().getData().getTotal());
    }

    @Test
    public void testGetTestCaseSets_ValidationError() {
        when(testCaseSetService.getTestCaseSets(anyInt(), anyInt()))
                .thenThrow(new IllegalArgumentException("Invalid parameters"));

        ResponseEntity<BaseApiResponse<PagedResponse<TestCaseSet>>> response = testCaseSetController
                .getTestCaseSets(1, 10);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("VALIDATION_ERROR", response.getBody().getErrorCode());
    }

    @Test
    public void testGetTestCaseSet_Success() {
        when(testCaseSetService.getTestCaseSetById(1L)).thenReturn(Optional.of(testTestCaseSet));

        ResponseEntity<BaseApiResponse<TestCaseSet>> response = testCaseSetController.getTestCaseSet(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals(testTestCaseSet, response.getBody().getData());
    }

    @Test
    public void testGetTestCaseSet_NotFound() {
        when(testCaseSetService.getTestCaseSetById(1L)).thenReturn(Optional.empty());

        ResponseEntity<BaseApiResponse<TestCaseSet>> response = testCaseSetController.getTestCaseSet(1L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("NOT_FOUND", response.getBody().getErrorCode());
    }

    @Test
    public void testUploadTestCaseSet_Success() throws IOException {
        when(multipartFile.getOriginalFilename()).thenReturn("TestCaseSet.zip");
        when(testCaseSetService.uploadTestCaseSet(any(), any(), any(), any())).thenReturn(testTestCaseSet);

        ResponseEntity<BaseApiResponse<TestCaseSet>> response = testCaseSetController
                .uploadTestCaseSet(multipartFile, "Test description", "VPN阻断业务");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals(testTestCaseSet, response.getBody().getData());
        assertEquals("Upload successful", response.getBody().getMessage());
    }

    @Test
    public void testUploadTestCaseSet_ValidationError() throws IOException {
        when(multipartFile.getOriginalFilename()).thenReturn("TestCaseSet.zip");
        when(testCaseSetService.uploadTestCaseSet(any(), any(), any(), any()))
                .thenThrow(new IllegalArgumentException("Invalid file format"));

        ResponseEntity<BaseApiResponse<TestCaseSet>> response = testCaseSetController
                .uploadTestCaseSet(multipartFile, "Test description", "VPN阻断业务");

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("VALIDATION_ERROR", response.getBody().getErrorCode());
    }

    @Test
    public void testUploadTestCaseSet_IOException() throws IOException {
        when(multipartFile.getOriginalFilename()).thenReturn("TestCaseSet.zip");
        when(testCaseSetService.uploadTestCaseSet(any(), any(), any(), any()))
                .thenThrow(new IOException("File processing failed"));

        ResponseEntity<BaseApiResponse<TestCaseSet>> response = testCaseSetController
                .uploadTestCaseSet(multipartFile, "Test description", "VPN阻断业务");

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("FILE_PROCESSING_ERROR", response.getBody().getErrorCode());
    }

    @Test
    public void testUpdateTestCaseSet_Success() {
        when(testCaseSetService.updateTestCaseSet(anyLong(), any(), any(), any())).thenReturn(testTestCaseSet);

        Map<String, String> request = new HashMap<>();
        request.put("name", "UpdatedTestCaseSet");
        request.put("version", "2.0.0");
        request.put("description", "Updated description");

        ResponseEntity<BaseApiResponse<TestCaseSet>> response = testCaseSetController
                .updateTestCaseSet(1L, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals(testTestCaseSet, response.getBody().getData());
    }

    @Test
    public void testUpdateTestCaseSet_ValidationError() {
        when(testCaseSetService.updateTestCaseSet(anyLong(), any(), any(), any()))
                .thenThrow(new IllegalArgumentException("Test case set not found"));

        Map<String, String> request = new HashMap<>();
        request.put("name", "UpdatedTestCaseSet");
        request.put("version", "2.0.0");
        request.put("description", "Updated description");

        ResponseEntity<BaseApiResponse<TestCaseSet>> response = testCaseSetController
                .updateTestCaseSet(1L, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("VALIDATION_ERROR", response.getBody().getErrorCode());
    }

    @Test
    public void testGetTestCases_Success() {
        Page<TestCase> page = new PageImpl<>(testCases, PageRequest.of(0, 10), 1L);
        when(testCaseSetService.getTestCases(anyLong(), anyInt(), anyInt())).thenReturn(page);

        ResponseEntity<BaseApiResponse<PagedResponse<TestCase>>> response = testCaseSetController
                .getTestCases(1L, 1, 10);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertNotNull(response.getBody().getData());
        assertEquals(1, response.getBody().getData().getData().size());
        assertEquals(1L, response.getBody().getData().getTotal());
    }

    @Test
    public void testGetMissingScripts_Success() {
        when(testCaseSetService.getMissingScripts(1L)).thenReturn(testCases);
        when(testCaseSetService.countMissingScripts(1L)).thenReturn(1L);

        ResponseEntity<BaseApiResponse<Map<String, Object>>> response = testCaseSetController.getMissingScripts(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertNotNull(response.getBody().getData());
        
        @SuppressWarnings("unchecked")
        Map<String, Object> responseData = (Map<String, Object>) response.getBody().getData();
        assertEquals(testCases, responseData.get("testCases"));
        assertEquals(1L, responseData.get("count"));
    }

    @Test
    public void testGetMissingScripts_ValidationError() {
        when(testCaseSetService.getMissingScripts(1L))
                .thenThrow(new IllegalArgumentException("Test case set not found"));

        ResponseEntity<BaseApiResponse<Map<String, Object>>> response = testCaseSetController.getMissingScripts(1L);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("VALIDATION_ERROR", response.getBody().getErrorCode());
    }
}