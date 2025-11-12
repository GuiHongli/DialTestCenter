/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.cloududn.dialingtestapp.controller;

import com.huawei.cloududn.dialingtest.model.*;
import com.huawei.cloududn.dialingtestapp.service.UserRoleService;
import com.huawei.cloududn.dialingtestapp.util.OperationLogUtil;
import com.huawei.cloududn.dialingtestapp.util.PermissionValidator;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;

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
    private OperationLogUtil operationLogUtil;

    @Mock
    private PermissionValidator permissionValidator;

    @InjectMocks
    private UserRoleController userRoleController;

    private UserRole testUserRole;
    private CreateUserRoleRequest testCreateRequest;
    private UpdateUserRoleRequest testUpdateRequest;

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
        ResponseEntity<UserRolePageResponse> response = userRoleController.getUserRoles(Integer.valueOf(0), Integer.valueOf(10), null);

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
        ResponseEntity<UserRolePageResponse> response = userRoleController.getUserRoles(Integer.valueOf(0), Integer.valueOf(10), null);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        verify(userRoleService).getUserRolesWithPagination(0, 10, null);
    }

    @Test
    public void testUserRolesGet_ServiceException_ReturnsError() {
        // Arrange
        when(userRoleService.getUserRolesWithPagination(0, 10, null)).thenThrow(new RuntimeException("Database error"));

        // Act
        ResponseEntity<UserRolePageResponse> response = userRoleController.getUserRoles(Integer.valueOf(0), Integer.valueOf(10), null);

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
        ResponseEntity<UserRolePageResponse> response = userRoleController.getUserRoles(Integer.valueOf(0), Integer.valueOf(10), null);

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
        String username = "admin";
        PermissionValidator.PermissionValidationResult successResult = 
            PermissionValidator.PermissionValidationResult.success();
        when(permissionValidator.checkAdmin(username, "创建用户角色"))
            .thenReturn(successResult);
        
        when(userRoleService.createUserRole("testuser", "ADMIN", username)).thenReturn(testUserRole);

        // Act
        ResponseEntity<UserRoleResponse> response = userRoleController.createUserRole("csrf-token", username, testCreateRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("创建用户角色成功", response.getBody().getMessage());
        assertEquals(testUserRole, response.getBody().getData());
        verify(permissionValidator).checkAdmin(username, "创建用户角色");
        verify(userRoleService).createUserRole("testuser", "ADMIN", username);
    }

    @Test
    public void testUserRolesPost_NoPermission_ReturnsForbidden() {
        // Arrange
        String username = "operator";
        PermissionValidator.PermissionValidationResult failureResult = 
            PermissionValidator.PermissionValidationResult.failure("权限不足，创建用户角色需要管理员权限");
        when(permissionValidator.checkAdmin(username, "创建用户角色"))
            .thenReturn(failureResult);

        // Act
        ResponseEntity<UserRoleResponse> response = userRoleController.createUserRole("csrf-token", username, testCreateRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertTrue(response.getBody().getMessage().contains("权限不足"));
        verify(permissionValidator).checkAdmin(username, "创建用户角色");
        verify(userRoleService, never()).createUserRole(anyString(), anyString(), anyString());
    }

    @Test
    public void testUserRolesPost_IllegalArgument_ReturnsBadRequest() {
        // Arrange
        String username = "admin";
        PermissionValidator.PermissionValidationResult successResult = 
            PermissionValidator.PermissionValidationResult.success();
        when(permissionValidator.checkAdmin(username, "创建用户角色"))
            .thenReturn(successResult);
        
        when(userRoleService.createUserRole("testuser", "ADMIN", username))
            .thenThrow(new IllegalArgumentException("用户角色关系已存在"));

        // Act
        ResponseEntity<UserRoleResponse> response = userRoleController.createUserRole("csrf-token", username, testCreateRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("用户角色关系已存在", response.getBody().getMessage());
        verify(permissionValidator).checkAdmin(username, "创建用户角色");
        verify(userRoleService).createUserRole("testuser", "ADMIN", username);
    }

    @Test
    public void testUserRolesIdPut_AdminUser_Success() {
        // Arrange
        String username = "admin";
        PermissionValidator.PermissionValidationResult successResult = 
            PermissionValidator.PermissionValidationResult.success();
        when(permissionValidator.checkAdmin(username, "update user role"))
            .thenReturn(successResult);
        
        when(userRoleService.updateUserRole(1, "testuser", "ADMIN", username)).thenReturn(testUserRole);

        // Act
        ResponseEntity<UserRoleResponse> response = userRoleController.updateUserRole(username, Integer.valueOf(1), testUpdateRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Update user role successful", response.getBody().getMessage());
        assertEquals(testUserRole, response.getBody().getData());
        verify(permissionValidator).checkAdmin(username, "update user role");
        verify(userRoleService).updateUserRole(1, "testuser", "ADMIN", username);
    }

    @Test
    public void testUserRolesIdPut_NoPermission_ReturnsForbidden() {
        // Arrange
        String username = "operator";
        PermissionValidator.PermissionValidationResult failureResult = 
            PermissionValidator.PermissionValidationResult.failure("权限不足，更新用户角色需要管理员权限");
        when(permissionValidator.checkAdmin(username, "update user role"))
            .thenReturn(failureResult);

        // Act
        ResponseEntity<UserRoleResponse> response = userRoleController.updateUserRole(username, Integer.valueOf(1), testUpdateRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertTrue(response.getBody().getMessage().contains("权限不足"));
        verify(permissionValidator).checkAdmin(username, "更新用户角色");
        verify(userRoleService, never()).updateUserRole(anyInt(), anyString(), anyString(), anyString());
    }

    @Test
    public void testUserRolesIdPut_NotFound_ReturnsNotFound() {
        // Arrange
        String username = "admin";
        PermissionValidator.PermissionValidationResult successResult = 
            PermissionValidator.PermissionValidationResult.success();
        when(permissionValidator.checkAdmin(username, "update user role"))
            .thenReturn(successResult);
        
        when(userRoleService.updateUserRole(1, "testuser", "ADMIN", username))
            .thenThrow(new IllegalArgumentException("User role relationship does not exist"));

        // Act
        ResponseEntity<UserRoleResponse> response = userRoleController.updateUserRole(username, Integer.valueOf(1), testUpdateRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("User role relationship does not exist", response.getBody().getMessage());
        verify(permissionValidator).checkAdmin(username, "update user role");
        verify(userRoleService).updateUserRole(1, "testuser", "ADMIN", username);
    }

    @Test
    public void testUserRolesIdPut_UserRoleAlreadyExists_ReturnsBadRequest() {
        // Arrange
        String username = "admin";
        PermissionValidator.PermissionValidationResult successResult = 
            PermissionValidator.PermissionValidationResult.success();
        when(permissionValidator.checkAdmin(username, "update user role"))
            .thenReturn(successResult);
        
        when(userRoleService.updateUserRole(1, "newuser", "ADMIN", username))
            .thenThrow(new IllegalArgumentException("User role already exists"));

        // Act
        ResponseEntity<UserRoleResponse> response = userRoleController.updateUserRole(username, Integer.valueOf(1), testUpdateRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("User role already exists", response.getBody().getMessage());
        verify(permissionValidator).checkAdmin(username, "update user role");
        verify(userRoleService).updateUserRole(1, "testuser", "ADMIN", username);
    }

    @Test
    public void testUserRolesIdPut_DataIntegrityViolation_ReturnsBadRequest() {
        // Arrange
        String username = "admin";
        PermissionValidator.PermissionValidationResult successResult = 
            PermissionValidator.PermissionValidationResult.success();
        when(permissionValidator.checkAdmin(username, "update user role"))
            .thenReturn(successResult);
        
        org.springframework.dao.DataIntegrityViolationException exception = 
            new org.springframework.dao.DataIntegrityViolationException("duplicate key value violates unique constraint 'user_roles_username_role_key'");
        when(userRoleService.updateUserRole(1, "testuser", "ADMIN", username))
            .thenThrow(exception);

        // Act
        ResponseEntity<UserRoleResponse> response = userRoleController.updateUserRole(username, Integer.valueOf(1), testUpdateRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("User role already exists", response.getBody().getMessage());
        verify(permissionValidator).checkAdmin(username, "update user role");
        verify(userRoleService).updateUserRole(1, "testuser", "ADMIN", username);
    }

    @Test
    public void testUserRolesIdDelete_AdminUser_Success() {
        // Arrange
        String username = "admin";
        PermissionValidator.PermissionValidationResult successResult = 
            PermissionValidator.PermissionValidationResult.success();
        when(permissionValidator.checkAdmin(username, "删除用户角色"))
            .thenReturn(successResult);

        // Act
        ResponseEntity<Void> response = userRoleController.deleteUserRole(Integer.valueOf(1), username);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(permissionValidator).checkAdmin(username, "删除用户角色");
        verify(userRoleService).deleteUserRole(1, username);
    }

    @Test
    public void testUserRolesIdDelete_NoPermission_ReturnsForbidden() {
        // Arrange
        String username = "operator";
        PermissionValidator.PermissionValidationResult failureResult = 
            PermissionValidator.PermissionValidationResult.failure("权限不足，删除用户角色需要管理员权限");
        when(permissionValidator.checkAdmin(username, "删除用户角色"))
            .thenReturn(failureResult);

        // Act
        ResponseEntity<Void> response = userRoleController.deleteUserRole(Integer.valueOf(1), username);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        verify(permissionValidator).checkAdmin(username, "删除用户角色");
        verify(userRoleService, never()).deleteUserRole(anyInt(), anyString());
    }

    @Test
    public void testUserRolesIdDelete_NotFound_ReturnsNotFound() {
        // Arrange
        String username = "admin";
        PermissionValidator.PermissionValidationResult successResult = 
            PermissionValidator.PermissionValidationResult.success();
        when(permissionValidator.checkAdmin(username, "删除用户角色"))
            .thenReturn(successResult);
        
        doThrow(new IllegalArgumentException("用户角色关系不存在")).when(userRoleService).deleteUserRole(1, username);

        // Act
        ResponseEntity<Void> response = userRoleController.deleteUserRole(Integer.valueOf(1), username);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(permissionValidator).checkAdmin(username, "删除用户角色");
        verify(userRoleService).deleteUserRole(1, username);
    }

    @Test
    public void testUserRolesPermissionGet_Success() {
        // Arrange
        when(userRoleService.getUserRolesByUsername("testuser")).thenReturn(Arrays.asList("ADMIN"));

        // Act
        ResponseEntity<UserPermissionResponse> response = userRoleController.getUserPermission("testuser");

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
        ResponseEntity<UserPermissionResponse> response = userRoleController.getUserPermission("testuser");

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertTrue(response.getBody().getMessage().contains("获取用户权限信息失败"));
        verify(userRoleService).getUserRolesByUsername("testuser");
    }
    
    @Test
    public void testUserRolesExecutorCountGet_Success_ReturnsExecutorCount() {
        // Arrange
        when(userRoleService.getExecutorCount()).thenReturn(5);

        // Act
        ResponseEntity<ExecutorCountResponse> response = userRoleController.getExecutorCount();

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
        ResponseEntity<ExecutorCountResponse> response = userRoleController.getExecutorCount();

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertTrue(response.getBody().getMessage().contains("获取执行机数量失败"));
        verify(userRoleService).getExecutorCount();
    }
}
