/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.cloududn.dialingtest.controller;

import com.huawei.cloududn.dialingtest.model.*;
import com.huawei.cloududn.dialingtest.service.UserRoleService;
import com.huawei.cloududn.dialingtest.util.UserRoleOperationLogUtil;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 用户角色管理控制器测试类
 *
 * @author g00940940
 * @since 2025-01-27
 */
@RunWith(MockitoJUnitRunner.class)
public class UserRoleControllerTest {

    @Mock
    private UserRoleService userRoleService;

    @Mock
    private UserRoleOperationLogUtil operationLogUtil;

    @InjectMocks
    private UserRoleController userRoleController;

    private UserRole testUserRole;
    private CreateUserRoleRequest testCreateRequest;
    private UpdateUserRoleRequest testUpdateRequest;
    private Role testRole;

    @Before
    public void setUp() {
        // 初始化测试数据
        testUserRole = new UserRole();
        testUserRole.setId(1);
        testUserRole.setUsername("testuser");
        testUserRole.setRole(UserRole.RoleEnum.ADMIN);

        testCreateRequest = new CreateUserRoleRequest();
        testCreateRequest.setUsername("testuser");
        testCreateRequest.setRole(CreateUserRoleRequest.RoleEnum.ADMIN);

        testUpdateRequest = new UpdateUserRoleRequest();
        testUpdateRequest.setUsername("testuser");
        testUpdateRequest.setRole(UpdateUserRoleRequest.RoleEnum.ADMIN);

        testRole = new Role();
        testRole.setId(1);
        testRole.setCode("ADMIN");
        testRole.setNameZh("管理员");
        testRole.setNameEn("Administrator");
    }

    @Test
    public void testUserRolesGet_Success_ReturnsUserRoleList() {
        // Arrange
        UserRolePageResponseData pageData = new UserRolePageResponseData();
        pageData.setContent(Arrays.asList(testUserRole));
        pageData.setTotalElements(1);
        pageData.setTotalPages(1);
        pageData.setNumber(0);
        pageData.setSize(10);
        pageData.setFirst(true);
        pageData.setLast(true);

        when(userRoleService.getUserRolesWithPagination(0, 10, null)).thenReturn(pageData);

        // Act
        ResponseEntity<UserRolePageResponse> response = userRoleController.userRolesGet(Integer.valueOf(0), Integer.valueOf(10), null);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("获取用户角色列表成功", response.getBody().getMessage());
        verify(userRoleService).getUserRolesWithPagination(0, 10, null);
    }

    @Test
    public void testUserRolesGet_WithSearch_Success() {
        // Arrange
        UserRolePageResponseData pageData = new UserRolePageResponseData();
        pageData.setContent(Arrays.asList(testUserRole));
        pageData.setTotalElements(1);
        pageData.setTotalPages(1);
        pageData.setNumber(0);
        pageData.setSize(10);
        pageData.setFirst(true);
        pageData.setLast(true);

        when(userRoleService.getUserRolesWithPagination(0, 10, null)).thenReturn(pageData);

        // Act
        ResponseEntity<UserRolePageResponse> response = userRoleController.userRolesGet(Integer.valueOf(0), Integer.valueOf(10), null);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        verify(userRoleService).getUserRolesByUsername("operator");
        verify(userRoleService).getUserRolesWithPagination(0, 10, null);
    }

    @Test
    public void testUserRolesGet_ServiceException_ReturnsError() {
        // Arrange
        when(userRoleService.getUserRolesWithPagination(0, 10, null)).thenThrow(new RuntimeException("Database error"));

        // Act
        ResponseEntity<UserRolePageResponse> response = userRoleController.userRolesGet(Integer.valueOf(0), Integer.valueOf(10), null);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertTrue(response.getBody().getMessage().contains("获取用户角色列表失败"));
        
        verify(userRoleService).getUserRolesWithPagination(0, 10, null);
    }

    @Test
    public void testUserRolesGet_ServiceException_ReturnsInternalServerError() {
        // Arrange
        when(userRoleService.getUserRolesByUsername("admin")).thenReturn(Arrays.asList("ADMIN"));
        when(userRoleService.getUserRolesWithPagination(0, 10, null)).thenThrow(new RuntimeException("Database error"));

        // Act
        ResponseEntity<UserRolePageResponse> response = userRoleController.userRolesGet(Integer.valueOf(0), Integer.valueOf(10), null);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertTrue(response.getBody().getMessage().contains("获取用户角色列表失败"));
        verify(userRoleService).getUserRolesWithPagination(0, 10, null);
    }

    @Test
    public void testUserRolesPost_AdminUser_Success() {
        // Arrange
        when(userRoleService.getUserRolesByUsername("admin")).thenReturn(Arrays.asList("ADMIN"));
        when(userRoleService.createUserRole("testuser", "ADMIN", "admin")).thenReturn(testUserRole);

        // Act
        ResponseEntity<UserRoleResponse> response = userRoleController.userRolesPost("admin", testCreateRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("创建用户角色成功", response.getBody().getMessage());
        assertEquals(testUserRole, response.getBody().getData());
        verify(userRoleService).getUserRolesByUsername("admin");
        verify(userRoleService).createUserRole("testuser", "ADMIN", "admin");
    }

    @Test
    public void testUserRolesPost_NoPermission_ReturnsForbidden() {
        // Arrange
        when(userRoleService.getUserRolesByUsername("operator")).thenReturn(Arrays.asList("OPERATOR"));

        // Act
        ResponseEntity<UserRoleResponse> response = userRoleController.userRolesPost("operator", testCreateRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("权限不足，需要ADMIN权限", response.getBody().getMessage());
        verify(userRoleService).getUserRolesByUsername("operator");
        verify(userRoleService, never()).createUserRole(anyString(), anyString(), anyString());
    }

    @Test
    public void testUserRolesPost_IllegalArgument_ReturnsBadRequest() {
        // Arrange
        when(userRoleService.getUserRolesByUsername("admin")).thenReturn(Arrays.asList("ADMIN"));
        when(userRoleService.createUserRole("testuser", "ADMIN", "admin"))
            .thenThrow(new IllegalArgumentException("用户角色关系已存在"));

        // Act
        ResponseEntity<UserRoleResponse> response = userRoleController.userRolesPost("admin", testCreateRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("用户角色关系已存在", response.getBody().getMessage());
        verify(userRoleService).getUserRolesByUsername("admin");
        verify(userRoleService).createUserRole("testuser", "ADMIN", "admin");
    }

    @Test
    public void testUserRolesIdPut_AdminUser_Success() {
        // Arrange
        when(userRoleService.getUserRolesByUsername("admin")).thenReturn(Arrays.asList("ADMIN"));
        when(userRoleService.updateUserRole(1, "testuser", "ADMIN", "admin")).thenReturn(testUserRole);

        // Act
        ResponseEntity<UserRoleResponse> response = userRoleController.userRolesIdPut("admin", Integer.valueOf(1), testUpdateRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("更新用户角色成功", response.getBody().getMessage());
        assertEquals(testUserRole, response.getBody().getData());
        verify(userRoleService).getUserRolesByUsername("admin");
        verify(userRoleService).updateUserRole(1, "testuser", "ADMIN", "admin");
    }

    @Test
    public void testUserRolesIdPut_NoPermission_ReturnsForbidden() {
        // Arrange
        when(userRoleService.getUserRolesByUsername("operator")).thenReturn(Arrays.asList("OPERATOR"));

        // Act
        ResponseEntity<UserRoleResponse> response = userRoleController.userRolesIdPut("operator", Integer.valueOf(1), testUpdateRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("权限不足，需要ADMIN权限", response.getBody().getMessage());
        verify(userRoleService).getUserRolesByUsername("operator");
        verify(userRoleService, never()).updateUserRole(anyInt(), anyString(), anyString(), anyString());
    }

    @Test
    public void testUserRolesIdPut_NotFound_ReturnsNotFound() {
        // Arrange
        when(userRoleService.getUserRolesByUsername("admin")).thenReturn(Arrays.asList("ADMIN"));
        when(userRoleService.updateUserRole(1, "testuser", "ADMIN", "admin"))
            .thenThrow(new IllegalArgumentException("用户角色关系不存在"));

        // Act
        ResponseEntity<UserRoleResponse> response = userRoleController.userRolesIdPut("admin", Integer.valueOf(1), testUpdateRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("用户角色关系不存在", response.getBody().getMessage());
        verify(userRoleService).getUserRolesByUsername("admin");
        verify(userRoleService).updateUserRole(1, "testuser", "ADMIN", "admin");
    }

    @Test
    public void testUserRolesIdDelete_AdminUser_Success() {
        // Arrange
        when(userRoleService.getUserRolesByUsername("admin")).thenReturn(Arrays.asList("ADMIN"));

        // Act
        ResponseEntity<Void> response = userRoleController.userRolesIdDelete(Integer.valueOf(1), "admin");

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(userRoleService).getUserRolesByUsername("admin");
        verify(userRoleService).deleteUserRole(1, "admin");
    }

    @Test
    public void testUserRolesIdDelete_NoPermission_ReturnsForbidden() {
        // Arrange
        when(userRoleService.getUserRolesByUsername("operator")).thenReturn(Arrays.asList("OPERATOR"));

        // Act
        ResponseEntity<Void> response = userRoleController.userRolesIdDelete(Integer.valueOf(1), "operator");

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        verify(userRoleService).getUserRolesByUsername("operator");
        verify(userRoleService, never()).deleteUserRole(anyInt(), anyString());
    }

    @Test
    public void testUserRolesIdDelete_NotFound_ReturnsNotFound() {
        // Arrange
        when(userRoleService.getUserRolesByUsername("admin")).thenReturn(Arrays.asList("ADMIN"));
        doThrow(new IllegalArgumentException("用户角色关系不存在")).when(userRoleService).deleteUserRole(1, "admin");

        // Act
        ResponseEntity<Void> response = userRoleController.userRolesIdDelete(Integer.valueOf(1), "admin");

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(userRoleService).getUserRolesByUsername("admin");
        verify(userRoleService).deleteUserRole(1, "admin");
    }

    @Test
    public void testUserRolesPermissionGet_Success() {
        // Arrange
        when(userRoleService.getUserRolesByUsername("testuser")).thenReturn(Arrays.asList("ADMIN"));

        // Act
        ResponseEntity<UserPermissionResponse> response = userRoleController.userRolesPermissionGet("testuser");

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("获取用户权限信息成功", response.getBody().getMessage());
        assertNotNull(response.getBody().getData());
        assertEquals("testuser", response.getBody().getData().getUsername());
        assertEquals(Arrays.asList("ADMIN"), response.getBody().getData().getRoles());
        assertNotNull(response.getBody().getData().getPagePermissions());
        verify(userRoleService).getUserRolesByUsername("testuser");
    }

    @Test
    public void testUserRolesPermissionGet_ServiceException_ReturnsInternalServerError() {
        // Arrange
        when(userRoleService.getUserRolesByUsername("testuser")).thenThrow(new RuntimeException("Database error"));

        // Act
        ResponseEntity<UserPermissionResponse> response = userRoleController.userRolesPermissionGet("testuser");

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertTrue(response.getBody().getMessage().contains("获取用户权限信息失败"));
        verify(userRoleService).getUserRolesByUsername("testuser");
    }

    @Test
    public void testUserRolesRolesGet_Success() {
        // Arrange
        List<Role> roles = Arrays.asList(testRole);
        when(userRoleService.getAllRoles()).thenReturn(roles);

        // Act
        ResponseEntity<List<RoleResponse>> response = userRoleController.userRolesRolesGet();

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertTrue(response.getBody().get(0).isSuccess());
        assertEquals("获取角色信息成功", response.getBody().get(0).getMessage());
        assertEquals(testRole, response.getBody().get(0).getData());
        verify(userRoleService).getAllRoles();
    }

    @Test
    public void testUserRolesRolesGet_ServiceException_ReturnsInternalServerError() {
        // Arrange
        when(userRoleService.getAllRoles()).thenThrow(new RuntimeException("Database error"));

        // Act
        ResponseEntity<List<RoleResponse>> response = userRoleController.userRolesRolesGet();

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertFalse(response.getBody().get(0).isSuccess());
        assertTrue(response.getBody().get(0).getMessage().contains("获取角色列表失败"));
        verify(userRoleService).getAllRoles();
    }
    
    @Test
    public void testUserRolesExecutorCountGet_Success_ReturnsExecutorCount() {
        // Arrange
        when(userRoleService.getExecutorCount()).thenReturn(5);

        // Act
        ResponseEntity<ExecutorCountResponse> response = userRoleController.userRolesExecutorCountGet();

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals(Integer.valueOf(5), response.getBody().getData());
        assertEquals("获取执行机数量成功", response.getBody().getMessage());
        verify(userRoleService).getExecutorCount();
    }
    
    @Test
    public void testUserRolesExecutorCountGet_ServiceException_ReturnsInternalServerError() {
        // Arrange
        when(userRoleService.getExecutorCount()).thenThrow(new RuntimeException("Database error"));

        // Act
        ResponseEntity<ExecutorCountResponse> response = userRoleController.userRolesExecutorCountGet();

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertTrue(response.getBody().getMessage().contains("获取执行机数量失败"));
        verify(userRoleService).getExecutorCount();
    }
}
