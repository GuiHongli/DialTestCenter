/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.cloududn.dialingtest.controller;

import com.huawei.cloududn.dialingtest.model.*;
import com.huawei.cloududn.dialingtest.service.SoftwarePackagesService;
import com.huawei.cloududn.dialingtest.service.UserRoleService;
import com.huawei.cloududn.dialingtest.util.OperationLogUtil;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;

import java.util.Arrays;
import java.util.List;
import java.util.Collections;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * SoftwarePackageController单元测试类
 * 测试软件包管理控制器的所有方法
 *
 * @author g00940940
 * @since 2025-01-27
 */
@RunWith(MockitoJUnitRunner.class)
public class SoftwarePackageControllerTest {

    @Mock
    private SoftwarePackagesService softwarePackageService;

    @Mock
    private UserRoleService userRoleService;

    @Mock
    private OperationLogUtil operationLogUtil;

    @InjectMocks
    private SoftwarePackageController softwarePackageController;

    private SoftwarePackageInfo mockPackageInfo;
    private SoftwarePackageListResponseData mockListData;
    private List<String> adminRoles;
    private List<String> operatorRoles;
    private UpdateSoftwarePackageBody mockUpdateRequest;
    private BatchDownloadRequest mockDownloadRequest;

    @Before
    public void setUp() {
        // 初始化测试数据
        mockPackageInfo = new SoftwarePackageInfo();
        mockPackageInfo.setId(1L);
        mockPackageInfo.setSoftwareName("test-app");
        mockPackageInfo.setFileSize(1024L);
        mockPackageInfo.setDescription("Test software package");

        mockListData = new SoftwarePackageListResponseData();
        mockListData.setData(Arrays.asList(mockPackageInfo));
        mockListData.setTotal(1);
        mockListData.setPageSize(1);
        mockListData.setPage(1);
        mockListData.setPageSize(10);

        adminRoles = Arrays.asList("ADMIN");
        operatorRoles = Arrays.asList("OPERATOR");

        mockUpdateRequest = new UpdateSoftwarePackageBody();
        mockUpdateRequest.setDescription("Updated description");

        mockDownloadRequest = new BatchDownloadRequest();
        mockDownloadRequest.setPackageIds(Arrays.asList(1L, 2L));
        mockDownloadRequest.setZipFileName("test-packages");
    }

    /**
     * 测试getSoftwarePackages方法 - 成功场景
     */
    @Test
    public void testGetSoftwarePackages_Success_ReturnsCorrectResponse() {
        // Arrange
        when(softwarePackageService.getSoftwarePackageList(1, 10, "test")).thenReturn(mockListData);

        // Act
        ResponseEntity<SoftwarePackageListResponse> response = softwarePackageController.getSoftwarePackages(1, 10, "test");

        // Assert
        assertNotNull("Response should not be null", response);
        assertEquals("Status should be OK", HttpStatus.OK, response.getStatusCode());
        assertNotNull("Response body should not be null", response.getBody());
        assertTrue("Response should be successful", response.getBody().isSuccess());
        assertEquals("Message should be correct", "获取软件包列表成功", response.getBody().getMessage());
        assertNotNull("Data should not be null", response.getBody().getData());
        assertEquals("Should contain 1 software package", 1, response.getBody().getData().getData().size());
        
        verify(softwarePackageService, times(1)).getSoftwarePackageList(1, 10, "test");
    }

    /**
     * 测试getSoftwarePackages方法 - 默认参数场景
     */
    @Test
    public void testGetSoftwarePackages_DefaultParameters_ReturnsCorrectResponse() {
        // Arrange
        when(softwarePackageService.getSoftwarePackageList(1, 10, null)).thenReturn(mockListData);

        // Act
        ResponseEntity<SoftwarePackageListResponse> response = softwarePackageController.getSoftwarePackages(null, null, null);

        // Assert
        assertNotNull("Response should not be null", response);
        assertEquals("Status should be OK", HttpStatus.OK, response.getStatusCode());
        assertTrue("Response should be successful", response.getBody().isSuccess());
        
        verify(softwarePackageService, times(1)).getSoftwarePackageList(1, 10, null);
    }

    /**
     * 测试getSoftwarePackages方法 - 异常场景
     */
    @Test
    public void testGetSoftwarePackages_Exception_ReturnsInternalServerError() {
        // Arrange
        when(softwarePackageService.getSoftwarePackageList(1, 10, "test"))
            .thenThrow(new RuntimeException("Database error"));

        // Act
        ResponseEntity<SoftwarePackageListResponse> response = softwarePackageController.getSoftwarePackages(1, 10, "test");

        // Assert
        assertNotNull("Response should not be null", response);
        assertEquals("Status should be INTERNAL_SERVER_ERROR", HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertFalse("Response should not be successful", response.getBody().isSuccess());
        assertTrue("Error message should contain failure info", 
                  response.getBody().getMessage().contains("获取软件包列表失败"));
    }

    /**
     * 测试getSoftwarePackageById方法 - 成功场景
     */
    @Test
    public void testGetSoftwarePackageById_Success_ReturnsCorrectResponse() {
        // Arrange
        when(softwarePackageService.getSoftwarePackageById(1L)).thenReturn(mockPackageInfo);

        // Act
        ResponseEntity<SoftwarePackagePayload> response = softwarePackageController.getSoftwarePackageById(1L);

        // Assert
        assertNotNull("Response should not be null", response);
        assertEquals("Status should be OK", HttpStatus.OK, response.getStatusCode());
        assertTrue("Response should be successful", response.getBody().isSuccess());
        assertEquals("Message should be correct", "获取软件包详情成功", response.getBody().getMessage());
        assertNotNull("Data should not be null", response.getBody().getData());
        assertEquals("ID should match", Long.valueOf(1L), response.getBody().getData().getId());
        
        verify(softwarePackageService, times(1)).getSoftwarePackageById(1L);
    }

    /**
     * 测试getSoftwarePackageById方法 - 软件包不存在场景
     */
    @Test
    public void testGetSoftwarePackageById_NotFound_ReturnsNotFound() {
        // Arrange
        when(softwarePackageService.getSoftwarePackageById(999L)).thenReturn(null);

        // Act
        ResponseEntity<SoftwarePackagePayload> response = softwarePackageController.getSoftwarePackageById(999L);

        // Assert
        assertNotNull("Response should not be null", response);
        assertEquals("Status should be NOT_FOUND", HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull("Response body should be null", response.getBody());
    }

    /**
     * 测试updateSoftwarePackage方法 - 成功场景
     */
    @Test
    public void testUpdateSoftwarePackage_Success_ReturnsCorrectResponse() {
        // Arrange
        SoftwarePackageInfo updatedPackageInfo = new SoftwarePackageInfo();
        updatedPackageInfo.setId(1L);
        updatedPackageInfo.setSoftwareName("test-app");
        updatedPackageInfo.setDescription("Updated description");
        
        when(userRoleService.getUserRolesByUsername("admin")).thenReturn(adminRoles);
        when(softwarePackageService.getSoftwarePackageById(1L)).thenReturn(mockPackageInfo);
        when(softwarePackageService.updateSoftwarePackage(1L, "Updated description")).thenReturn(updatedPackageInfo);

        // Act
        ResponseEntity<SoftwarePackagePayload> response = softwarePackageController.updateSoftwarePackage("admin", 1L, mockUpdateRequest);

        // Assert
        assertNotNull("Response should not be null", response);
        assertEquals("Status should be OK", HttpStatus.OK, response.getStatusCode());
        assertTrue("Response should be successful", response.getBody().isSuccess());
        assertEquals("Message should be correct", "更新软件包成功", response.getBody().getMessage());
        assertNotNull("Data should not be null", response.getBody().getData());
        assertEquals("Description should be updated", "Updated description", response.getBody().getData().getDescription());
        
        verify(userRoleService, times(1)).getUserRolesByUsername("admin");
        verify(softwarePackageService, times(1)).getSoftwarePackageById(1L);
        verify(softwarePackageService, times(1)).updateSoftwarePackage(1L, "Updated description");
        verify(operationLogUtil, times(1)).logSoftwarePackageUpdate("admin", mockPackageInfo, updatedPackageInfo);
    }

    /**
     * 测试updateSoftwarePackage方法 - 权限不足场景
     */
    @Test
    public void testUpdateSoftwarePackage_InsufficientPermission_ReturnsForbidden() {
        // Arrange
        when(userRoleService.getUserRolesByUsername("browser")).thenReturn(Arrays.asList("BROWSER"));

        // Act
        ResponseEntity<SoftwarePackagePayload> response = softwarePackageController.updateSoftwarePackage("browser", 1L, mockUpdateRequest);

        // Assert
        assertNotNull("Response should not be null", response);
        assertEquals("Status should be FORBIDDEN", HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNull("Response body should be null", response.getBody());
        
        verify(userRoleService, times(1)).getUserRolesByUsername("browser");
        verify(softwarePackageService, never()).updateSoftwarePackage(anyLong(), anyString());
    }

    /**
     * 测试updateSoftwarePackage方法 - 软件包不存在场景
     */
    @Test
    public void testUpdateSoftwarePackage_NotFound_ReturnsNotFound() {
        // Arrange
        when(userRoleService.getUserRolesByUsername("admin")).thenReturn(adminRoles);
        when(softwarePackageService.getSoftwarePackageById(999L)).thenReturn(null);

        // Act
        ResponseEntity<SoftwarePackagePayload> response = softwarePackageController.updateSoftwarePackage("admin", 999L, mockUpdateRequest);

        // Assert
        assertNotNull("Response should not be null", response);
        assertEquals("Status should be NOT_FOUND", HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull("Response body should be null", response.getBody());
        
        verify(userRoleService, times(1)).getUserRolesByUsername("admin");
        verify(softwarePackageService, times(1)).getSoftwarePackageById(999L);
        verify(softwarePackageService, never()).updateSoftwarePackage(anyLong(), anyString());
    }

    /**
     * 测试updateSoftwarePackage方法 - 空用户名场景
     */
    @Test
    public void testUpdateSoftwarePackage_NullUsername_UsesDefaultUsername() {
        // Arrange
        SoftwarePackageInfo updatedPackageInfo = new SoftwarePackageInfo();
        updatedPackageInfo.setId(1L);
        updatedPackageInfo.setSoftwareName("test-app");
        updatedPackageInfo.setDescription("Updated description");
        
        when(userRoleService.getUserRolesByUsername("admin")).thenReturn(adminRoles);
        when(softwarePackageService.getSoftwarePackageById(1L)).thenReturn(mockPackageInfo);
        when(softwarePackageService.updateSoftwarePackage(1L, "Updated description")).thenReturn(updatedPackageInfo);

        // Act
        ResponseEntity<SoftwarePackagePayload> response = softwarePackageController.updateSoftwarePackage(null, 1L, mockUpdateRequest);

        // Assert
        assertNotNull("Response should not be null", response);
        assertEquals("Status should be OK", HttpStatus.OK, response.getStatusCode());
        assertTrue("Response should be successful", response.getBody().isSuccess());
        
        verify(userRoleService, times(1)).getUserRolesByUsername("admin");
    }

    /**
     * 测试deleteSoftwarePackage方法 - 成功场景
     */
    @Test
    public void testDeleteSoftwarePackage_Success_ReturnsCorrectResponse() {
        // Arrange
        when(userRoleService.getUserRolesByUsername("admin")).thenReturn(adminRoles);
        when(softwarePackageService.getSoftwarePackageById(1L)).thenReturn(mockPackageInfo);
        when(softwarePackageService.isReferencedByTestCaseSet(1L)).thenReturn(false);
        when(softwarePackageService.deleteSoftwarePackage(1L)).thenReturn(true);

        // Act
        ResponseEntity<SuccessResponse> response = softwarePackageController.deleteSoftwarePackage(1L, "admin");

        // Assert
        assertNotNull("Response should not be null", response);
        assertEquals("Status should be OK", HttpStatus.OK, response.getStatusCode());
        assertTrue("Response should be successful", response.getBody().isSuccess());
        assertEquals("Message should be correct", "删除软件包成功", response.getBody().getMessage());
        
        verify(userRoleService, times(1)).getUserRolesByUsername("admin");
        verify(softwarePackageService, times(1)).getSoftwarePackageById(1L);
        verify(softwarePackageService, times(1)).isReferencedByTestCaseSet(1L);
        verify(softwarePackageService, times(1)).deleteSoftwarePackage(1L);
        verify(operationLogUtil, times(1)).logSoftwarePackageDelete("admin", mockPackageInfo);
    }

    /**
     * 测试deleteSoftwarePackage方法 - 权限不足场景
     */
    @Test
    public void testDeleteSoftwarePackage_InsufficientPermission_ReturnsForbidden() {
        // Arrange
        when(userRoleService.getUserRolesByUsername("browser")).thenReturn(Arrays.asList("BROWSER"));

        // Act
        ResponseEntity<SuccessResponse> response = softwarePackageController.deleteSoftwarePackage(1L, "browser");

        // Assert
        assertNotNull("Response should not be null", response);
        assertEquals("Status should be FORBIDDEN", HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNull("Response body should be null", response.getBody());
        
        verify(userRoleService, times(1)).getUserRolesByUsername("browser");
        verify(softwarePackageService, never()).deleteSoftwarePackage(anyLong());
    }

    /**
     * 测试deleteSoftwarePackage方法 - 软件包不存在场景
     */
    @Test
    public void testDeleteSoftwarePackage_NotFound_ReturnsNotFound() {
        // Arrange
        when(userRoleService.getUserRolesByUsername("admin")).thenReturn(adminRoles);
        when(softwarePackageService.getSoftwarePackageById(999L)).thenReturn(null);

        // Act
        ResponseEntity<SuccessResponse> response = softwarePackageController.deleteSoftwarePackage(999L, "admin");

        // Assert
        assertNotNull("Response should not be null", response);
        assertEquals("Status should be NOT_FOUND", HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull("Response body should be null", response.getBody());
        
        verify(userRoleService, times(1)).getUserRolesByUsername("admin");
        verify(softwarePackageService, times(1)).getSoftwarePackageById(999L);
        verify(softwarePackageService, never()).deleteSoftwarePackage(anyLong());
    }

    /**
     * 测试deleteSoftwarePackage方法 - 软件包被引用场景
     */
    @Test
    public void testDeleteSoftwarePackage_Referenced_ReturnsBadRequest() {
        // Arrange
        when(userRoleService.getUserRolesByUsername("admin")).thenReturn(adminRoles);
        when(softwarePackageService.getSoftwarePackageById(1L)).thenReturn(mockPackageInfo);
        when(softwarePackageService.isReferencedByTestCaseSet(1L)).thenReturn(true);

        // Act
        ResponseEntity<SuccessResponse> response = softwarePackageController.deleteSoftwarePackage(1L, "admin");

        // Assert
        assertNotNull("Response should not be null", response);
        assertEquals("Status should be BAD_REQUEST", HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNull("Response body should be null", response.getBody());
        
        verify(userRoleService, times(1)).getUserRolesByUsername("admin");
        verify(softwarePackageService, times(1)).getSoftwarePackageById(1L);
        verify(softwarePackageService, times(1)).isReferencedByTestCaseSet(1L);
        verify(softwarePackageService, never()).deleteSoftwarePackage(anyLong());
    }

    /**
     * 测试downloadSoftwarePackages方法 - 成功场景
     */
    @Test
    public void testDownloadSoftwarePackages_Success_ReturnsResource() {
        // Arrange
        Resource mockResource = mock(Resource.class);
        
        when(userRoleService.getUserRolesByUsername("admin")).thenReturn(adminRoles);
        when(softwarePackageService.downloadSoftwarePackages(Arrays.asList(1L, 2L), "test-packages")).thenReturn(mockResource);
        when(softwarePackageService.getSoftwarePackageNameById(1L)).thenReturn("test-app.apk");

        // Act
        ResponseEntity<Resource> response = softwarePackageController.downloadSoftwarePackages("csrf-token", "admin", mockDownloadRequest);

        // Assert
        assertNotNull("Response should not be null", response);
        assertEquals("Status should be OK", HttpStatus.OK, response.getStatusCode());
        assertNotNull("Resource should not be null", response.getBody());
        assertEquals("Resource should match", mockResource, response.getBody());
        
        verify(userRoleService, times(1)).getUserRolesByUsername("admin");
        verify(softwarePackageService, times(1)).downloadSoftwarePackages(Arrays.asList(1L, 2L), "test-packages");
    }

    /**
     * 测试downloadSoftwarePackages方法 - 权限不足场景
     */
    @Test
    public void testDownloadSoftwarePackages_InsufficientPermission_ReturnsForbidden() {
        // Arrange
        when(userRoleService.getUserRolesByUsername("browser")).thenReturn(Arrays.asList("BROWSER"));

        // Act
        ResponseEntity<Resource> response = softwarePackageController.downloadSoftwarePackages("csrf-token", "browser", mockDownloadRequest);

        // Assert
        assertNotNull("Response should not be null", response);
        assertEquals("Status should be FORBIDDEN", HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNull("Resource should be null", response.getBody());
        
        verify(userRoleService, times(1)).getUserRolesByUsername("browser");
        verify(softwarePackageService, never()).downloadSoftwarePackages(anyList(), anyString());
    }

    /**
     * 测试downloadSoftwarePackages方法 - 空包ID列表场景
     */
    @Test
    public void testDownloadSoftwarePackages_EmptyPackageIds_ReturnsBadRequest() {
        BatchDownloadRequest emptyRequest = new BatchDownloadRequest();
        emptyRequest.setPackageIds(Arrays.asList());
        
        when(userRoleService.getUserRolesByUsername("admin")).thenReturn(adminRoles);

        // Act
        ResponseEntity<Resource> response = softwarePackageController.downloadSoftwarePackages("csrf-token", "admin", emptyRequest);

        // Assert
        assertNotNull("Response should not be null", response);
        assertEquals("Status should be BAD_REQUEST", HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNull("Resource should be null", response.getBody());
        
        verify(userRoleService, times(1)).getUserRolesByUsername("admin");
        verify(softwarePackageService, never()).downloadSoftwarePackages(anyList(), anyString());
    }

    /**
     * 测试downloadSoftwarePackages方法 - 单个文件下载场景
     */
    @Test
    public void testDownloadSoftwarePackages_SingleFile_ReturnsCorrectHeaders() {
        // Arrange
        Resource mockResource = mock(Resource.class);
        BatchDownloadRequest singleRequest = new BatchDownloadRequest();
        singleRequest.setPackageIds(Arrays.asList(1L));
        
        when(userRoleService.getUserRolesByUsername("admin")).thenReturn(adminRoles);
        when(softwarePackageService.downloadSoftwarePackages(Arrays.asList(1L), null)).thenReturn(mockResource);
        when(softwarePackageService.getSoftwarePackageNameById(1L)).thenReturn("test-app.apk");

        // Act
        ResponseEntity<Resource> response = softwarePackageController.downloadSoftwarePackages("csrf-token", "admin", singleRequest);

        // Assert
        assertNotNull("Response should not be null", response);
        assertEquals("Status should be OK", HttpStatus.OK, response.getStatusCode());
        assertNotNull("Resource should not be null", response.getBody());
        assertTrue("Content-Disposition should contain filename", 
                  response.getHeaders().getFirst("Content-Disposition").contains("test-app.apk"));
        
        verify(userRoleService, times(1)).getUserRolesByUsername("admin");
        verify(softwarePackageService, times(1)).downloadSoftwarePackages(Arrays.asList(1L), null);
    }

    /**
     * 测试downloadSoftwarePackages方法 - 异常场景
     */
    @Test
    public void testDownloadSoftwarePackages_Exception_ReturnsInternalServerError() {
        // Arrange
        when(userRoleService.getUserRolesByUsername("admin")).thenReturn(adminRoles);
        when(softwarePackageService.downloadSoftwarePackages(Arrays.asList(1L, 2L), "test-packages"))
            .thenThrow(new RuntimeException("Download failed"));

        // Act
        ResponseEntity<Resource> response = softwarePackageController.downloadSoftwarePackages("csrf-token", "admin", mockDownloadRequest);

        // Assert
        assertNotNull("Response should not be null", response);
        assertEquals("Status should be INTERNAL_SERVER_ERROR", HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNull("Resource should be null", response.getBody());
    }

    /**
     * SoftwarePackageController 单包下载 LLT
     */
    @RunWith(MockitoJUnitRunner.class)
    public static class SoftwarePackageControllerSingleDownloadLLT {

        @Mock
        private SoftwarePackagesService softwarePackagesService;

        @Mock
        private UserRoleService userRoleService;

        @InjectMocks
        private SoftwarePackageController controller;

        private BatchDownloadRequest request;

        @Before
        public void setUp() {
            request = new BatchDownloadRequest();
        }

        /**
         * 成功下载单个软件包
         */
        @Test
        public void testDownloadSoftwarePackages_Single_Success() {
            String xUsername = "admin";
            when(userRoleService.getUserRolesByUsername(xUsername)).thenReturn(Arrays.asList("ADMIN"));
            request.setPackageIds(Collections.singletonList(1L));

            SoftwarePackageInfo info = new SoftwarePackageInfo();
            info.setId(1L);
            info.setSoftwareName("TestApp_1.0.0.apk");
            when(softwarePackagesService.getSoftwarePackageById(1L)).thenReturn(info);
            when(softwarePackagesService.getSoftwarePackageFileContent(1L)).thenReturn(new byte[]{1,2,3});

            ResponseEntity<org.springframework.core.io.Resource> resp = controller.downloadSoftwarePackages("csrf-token", xUsername, request);
            assertEquals(200, resp.getStatusCodeValue());
            Resource body = resp.getBody();
            assertNotNull(body);
            HttpHeaders headers = resp.getHeaders();
            assertTrue(headers.getFirst(HttpHeaders.CONTENT_DISPOSITION).contains("TestApp_1.0.0.apk"));
            assertEquals("application/octet-stream", headers.getFirst(HttpHeaders.CONTENT_TYPE));
        }

        /**
         * 用户名为空
         */
        @Test
        public void testDownloadSoftwarePackages_EmptyUsername_BadRequest() {
            request.setPackageIds(Collections.singletonList(1L));
            ResponseEntity<Resource> resp = controller.downloadSoftwarePackages("csrf-token", " ", request);
            assertEquals(400, resp.getStatusCodeValue());
        }

        /**
         * 权限不足
         */
        @Test
        public void testDownloadSoftwarePackages_NoPermission_Forbidden() {
            String xUsername = "user";
            when(userRoleService.getUserRolesByUsername(xUsername)).thenReturn(Arrays.asList("VIEWER"));
            request.setPackageIds(Collections.singletonList(1L));
            ResponseEntity<Resource> resp = controller.downloadSoftwarePackages("csrf-token", xUsername, request);
            assertEquals(403, resp.getStatusCodeValue());
        }

        /**
         * packageIds 为空
         */
        @Test
        public void testDownloadSoftwarePackages_EmptyIds_BadRequest() {
            String xUsername = "admin";
            when(userRoleService.getUserRolesByUsername(xUsername)).thenReturn(Arrays.asList("ADMIN"));
            request.setPackageIds(Collections.emptyList());
            ResponseEntity<Resource> resp = controller.downloadSoftwarePackages("csrf-token", xUsername, request);
            assertEquals(400, resp.getStatusCodeValue());
        }

        /**
         * 软件包不存在
         */
        @Test
        public void testDownloadSoftwarePackages_PackageNotFound_NotFound() {
            String xUsername = "admin";
            when(userRoleService.getUserRolesByUsername(xUsername)).thenReturn(Arrays.asList("ADMIN"));
            request.setPackageIds(Collections.singletonList(99L));
            when(softwarePackagesService.getSoftwarePackageById(99L)).thenReturn(null);
            ResponseEntity<Resource> resp = controller.downloadSoftwarePackages("csrf-token", xUsername, request);
            assertEquals(404, resp.getStatusCodeValue());
        }

        /**
         * 文件内容为空
         */
        @Test
        public void testDownloadSoftwarePackages_FileContentNull_NotFound() {
            String xUsername = "admin";
            when(userRoleService.getUserRolesByUsername(xUsername)).thenReturn(Arrays.asList("ADMIN"));
            request.setPackageIds(Collections.singletonList(1L));

            SoftwarePackageInfo info = new SoftwarePackageInfo();
            info.setId(1L);
            info.setSoftwareName("TestApp_1.0.0.apk");
            when(softwarePackagesService.getSoftwarePackageById(1L)).thenReturn(info);
            when(softwarePackagesService.getSoftwarePackageFileContent(1L)).thenReturn(null);

            ResponseEntity<Resource> resp = controller.downloadSoftwarePackages("csrf-token", xUsername, request);
            assertEquals(404, resp.getStatusCodeValue());
        }
    }
}

