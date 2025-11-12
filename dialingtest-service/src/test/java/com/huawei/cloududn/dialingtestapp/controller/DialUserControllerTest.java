/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.cloududn.dialingtestapp.controller;

import com.huawei.cloududn.dialingtest.model.*;
import com.huawei.cloududn.dialingtestapp.service.DialUserService;
import com.huawei.cloududn.dialingtestapp.service.UserRoleService;
import com.huawei.cloududn.dialingtestapp.util.PermissionValidator;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 拨测用户控制器测试类
 *
 * @author Generated
 * @since 2025-09-18
 */
@RunWith(MockitoJUnitRunner.class)
public class DialUserControllerTest {

    @Mock
    private DialUserService dialUserService;

    @Mock
    private UserRoleService userRoleService;

    @Mock
    private PermissionValidator permissionValidator;

    @InjectMocks
    private DialUserController dialUserController;

    @Test
    public void testDialusersGet_Success_ReturnsUserList() {
        // Arrange
        DialUser user1 = createTestUser(1, "user1", "password1");
        DialUser user2 = createTestUser(2, "user2", "password2");
        List<DialUser> users = Arrays.asList(user1, user2);
        
        when(dialUserService.findUsersWithPagination(0, 10, "testuser")).thenReturn(users);
        when(dialUserService.countUsers("testuser")).thenReturn(2L);

        // Act
        ResponseEntity<DialUserPageResponse> response = dialUserController.getDialUsers(0, 10, "testuser");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Query successful", response.getBody().getMessage());
        assertEquals(2, response.getBody().getData().getContent().size());
        assertEquals(2, response.getBody().getData().getTotalElements().intValue());
    }

    @Test
    public void testDialusersGet_WithUsernameFilter_ReturnsFilteredList() {
        // Arrange
        DialUser user = createTestUser(1, "testuser", "password");
        List<DialUser> users = Arrays.asList(user);
        
        when(dialUserService.findUsersWithPagination(0, 10, "test")).thenReturn(users);
        when(dialUserService.countUsers("test")).thenReturn(1L);

        // Act
        ResponseEntity<DialUserPageResponse> response = dialUserController.getDialUsers(0, 10, "test");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
        assertEquals(1, response.getBody().getData().getContent().size());
        assertEquals("testuser", response.getBody().getData().getContent().get(0).getUsername());
    }

    @Test
    public void testDialusersGet_WithNullParameters_UsesDefaults() {
        // Arrange
        List<DialUser> users = Arrays.asList();
        
        when(dialUserService.findUsersWithPagination(0, 10, "testuser")).thenReturn(users);
        when(dialUserService.countUsers("testuser")).thenReturn(0L);

        // Act
        ResponseEntity<DialUserPageResponse> response = dialUserController.getDialUsers(null, null, "testuser");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
        assertEquals(0, response.getBody().getData().getContent().size());
    }

    @Test
    public void testDialusersGet_ServiceException_ReturnsError() {
        // Arrange
        when(dialUserService.findUsersWithPagination(0, 10, "testuser")).thenThrow(new RuntimeException("Database error"));

        // Act
        ResponseEntity<DialUserPageResponse> response = dialUserController.getDialUsers(0, 10, "testuser");

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
        assertTrue(response.getBody().getMessage().contains("Query") || response.getBody().getMessage().contains("查询"));
    }

    @Test
    public void testDialusersGet_NullUsername_ReturnsOk() {
        // Note: getDialUsers方法不需要权限校验，可以接受null作为用户名过滤条件
        // Arrange
        List<DialUser> users = Arrays.asList();
        when(dialUserService.findUsersWithPagination(0, 10, null)).thenReturn(users);
        when(dialUserService.countUsers(null)).thenReturn(0L);
        
        // Act
        ResponseEntity<DialUserPageResponse> response = dialUserController.getDialUsers(0, 10, null);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
    }
    

    @Test
    public void testDialusersIdGet_Success_ReturnsUser() {
        // Arrange
        DialUser user = createTestUser(1, "testuser", "password");
        when(dialUserService.findById(1)).thenReturn(user);

        // Act
        ResponseEntity<DialUserResponse> response = dialUserController.getDialUserById(1);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Query successful", response.getBody().getMessage());
        assertEquals(user, response.getBody().getData());
    }

    @Test
    public void testDialusersIdGet_UserNotFound_ReturnsNotFound() {
        // Arrange
        when(dialUserService.findById(999)).thenReturn(null);

        // Act
        ResponseEntity<DialUserResponse> response = dialUserController.getDialUserById(999);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
        assertEquals("User not found", response.getBody().getMessage());
    }

    @Test
    public void testDialusersIdGet_UserNotFound_WhenServiceReturnsNull() {
        // Arrange
        when(dialUserService.findById(1)).thenReturn(null);

        // Act
        ResponseEntity<DialUserResponse> response = dialUserController.getDialUserById(1);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
        assertEquals("User not found", response.getBody().getMessage());
    }

    @Test
    public void testDialusersIdPut_Success_ReturnsUpdatedUser() {
        // Arrange
        UpdateDialUserRequest request = new UpdateDialUserRequest();
        request.setUsername("newuser");
        request.setPassword("newpassword");
        
        DialUser updatedUser = createTestUser(1, "newuser", "newpassword");
        String username = "admin";
        PermissionValidator.PermissionValidationResult successResult = 
            PermissionValidator.PermissionValidationResult.success();
        when(permissionValidator.checkAdmin(username, "update dial user"))
            .thenReturn(successResult);
        
        when(dialUserService.updateUser(1, "newuser", "newpassword", username)).thenReturn(updatedUser);

        // Act
        ResponseEntity<DialUserResponse> response = dialUserController.updateDialUser(username, 1, request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Update successful", response.getBody().getMessage());
        assertEquals(updatedUser, response.getBody().getData());
        
        verify(permissionValidator).checkAdmin(username, "update dial user");
    }

    @Test
    public void testDialusersIdPut_NoAdminRole_ReturnsForbidden() {
        // Arrange
        UpdateDialUserRequest request = new UpdateDialUserRequest();
        request.setUsername("newuser");
        request.setPassword("newpassword");
        
        String username = "operator";
        PermissionValidator.PermissionValidationResult failureResult = 
            PermissionValidator.PermissionValidationResult.failure("权限不足，update dial user需要管理员权限");
        when(permissionValidator.checkAdmin(username, "update dial user"))
            .thenReturn(failureResult);

        // Act
        ResponseEntity<DialUserResponse> response = dialUserController.updateDialUser(username, 1, request);

        // Assert
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
        assertTrue(response.getBody().getMessage().contains("权限不足"));
        
        verify(permissionValidator).checkAdmin(username, "update dial user");
    }

    @Test
    public void testDialusersIdPut_EmptyUsername_ReturnsUnauthorized() {
        // Arrange
        UpdateDialUserRequest request = new UpdateDialUserRequest();
        request.setUsername("newuser");
        request.setPassword("newpassword");
        
        String username = "";
        PermissionValidator.PermissionValidationResult failureResult = 
            PermissionValidator.PermissionValidationResult.failure("未提供用户名");
        when(permissionValidator.checkAdmin(username, "update dial user"))
            .thenReturn(failureResult);

        // Act
        ResponseEntity<DialUserResponse> response = dialUserController.updateDialUser(username, 1, request);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
        assertEquals("未提供用户名", response.getBody().getMessage());
        
        verify(permissionValidator).checkAdmin(username, "update dial user");
    }

    @Test
    public void testDialusersIdPut_UserNotFound_ReturnsNotFound() {
        // Arrange
        UpdateDialUserRequest request = new UpdateDialUserRequest();
        request.setUsername("newuser");
        request.setPassword("newpassword");
        
        String username = "admin";
        PermissionValidator.PermissionValidationResult successResult = 
            PermissionValidator.PermissionValidationResult.success();
        when(permissionValidator.checkAdmin(username, "update dial user"))
            .thenReturn(successResult);
        
        when(dialUserService.updateUser(999, "newuser", "newpassword", username))
                .thenThrow(new IllegalArgumentException("User not found"));

        // Act
        ResponseEntity<DialUserResponse> response = dialUserController.updateDialUser(username, 999, request);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
        assertTrue(response.getBody().getMessage().contains("not found") || response.getBody().getMessage().contains("不存在"));
        
        verify(permissionValidator).checkAdmin(username, "update dial user");
    }

    @Test
    public void testDialusersIdPut_UsernameExists_ReturnsConflict() {
        // Arrange
        UpdateDialUserRequest request = new UpdateDialUserRequest();
        request.setUsername("existinguser");
        request.setPassword("password");
        
        String username = "admin";
        PermissionValidator.PermissionValidationResult successResult = 
            PermissionValidator.PermissionValidationResult.success();
        when(permissionValidator.checkAdmin(username, "update dial user"))
            .thenReturn(successResult);
        
        when(dialUserService.updateUser(1, "existinguser", "password", username))
                .thenThrow(new IllegalArgumentException("Username already exists"));

        // Act
        ResponseEntity<DialUserResponse> response = dialUserController.updateDialUser(username, 1, request);

        // Assert
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
        assertTrue(response.getBody().getMessage().contains("already exists") || response.getBody().getMessage().contains("已存在"));
        
        verify(permissionValidator).checkAdmin(username, "update dial user");
    }

    @Test
    public void testDialusersIdDelete_Success_ReturnsNoContent() {
        // Arrange
        String username = "admin";
        PermissionValidator.PermissionValidationResult successResult = 
            PermissionValidator.PermissionValidationResult.success();
        when(permissionValidator.checkAdmin(username, "delete dial user"))
            .thenReturn(successResult);
        
        doNothing().when(dialUserService).deleteUser(1, username);

        // Act
        ResponseEntity<Void> response = dialUserController.deleteDialUser(1, username);

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(permissionValidator).checkAdmin(username, "delete dial user");
        verify(dialUserService).deleteUser(1, username);
    }

    @Test
    public void testDialusersIdDelete_NoAdminRole_ReturnsForbidden() {
        // Arrange
        String username = "operator";
        PermissionValidator.PermissionValidationResult failureResult = 
            PermissionValidator.PermissionValidationResult.failure("权限不足，delete dial user需要管理员权限");
        when(permissionValidator.checkAdmin(username, "delete dial user"))
            .thenReturn(failureResult);

        // Act
        ResponseEntity<Void> response = dialUserController.deleteDialUser(1, username);

        // Assert
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        verify(permissionValidator).checkAdmin(username, "delete dial user");
        verify(dialUserService, never()).deleteUser(anyInt(), anyString());
    }

    @Test
    public void testDialusersIdDelete_EmptyUsername_ReturnsUnauthorized() {
        // Arrange
        String username = "";
        PermissionValidator.PermissionValidationResult failureResult = 
            PermissionValidator.PermissionValidationResult.failure("未提供用户名");
        when(permissionValidator.checkAdmin(username, "delete dial user"))
            .thenReturn(failureResult);

        // Act
        ResponseEntity<Void> response = dialUserController.deleteDialUser(1, username);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        verify(permissionValidator).checkAdmin(username, "delete dial user");
        verify(dialUserService, never()).deleteUser(anyInt(), anyString());
    }

    @Test
    public void testDialusersIdDelete_UserNotFound_ReturnsNotFound() {
        // Arrange
        String username = "admin";
        PermissionValidator.PermissionValidationResult successResult = 
            PermissionValidator.PermissionValidationResult.success();
        when(permissionValidator.checkAdmin(username, "delete dial user"))
            .thenReturn(successResult);
        
        doThrow(new IllegalArgumentException("User not found")).when(dialUserService).deleteUser(999, username);

        // Act
        ResponseEntity<Void> response = dialUserController.deleteDialUser(999, username);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(permissionValidator).checkAdmin(username, "delete dial user");
    }

    @Test
    public void testDialusersIdDelete_ServiceException_ReturnsError() {
        // Arrange
        String username = "admin";
        PermissionValidator.PermissionValidationResult successResult = 
            PermissionValidator.PermissionValidationResult.success();
        when(permissionValidator.checkAdmin(username, "delete dial user"))
            .thenReturn(successResult);
        
        doThrow(new RuntimeException("Database error")).when(dialUserService).deleteUser(1, username);

        // Act
        ResponseEntity<Void> response = dialUserController.deleteDialUser(1, username);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(permissionValidator).checkAdmin(username, "delete dial user");
    }

    @Test
    public void testDialusersPost_Success_ReturnsCreatedUser() {
        // Arrange
        CreateDialUserRequest request = new CreateDialUserRequest();
        request.setUsername("newuser");
        request.setPassword("password");
        
        DialUser createdUser = createTestUser(1, "newuser", "password");
        String username = "admin";
        PermissionValidator.PermissionValidationResult successResult = 
            PermissionValidator.PermissionValidationResult.success();
        when(permissionValidator.checkAdmin(username, "create dial user"))
            .thenReturn(successResult);
        
        when(dialUserService.createUser("newuser", "password", username)).thenReturn(createdUser);

        // Act
        ResponseEntity<DialUserResponse> response = dialUserController.createDialUser("csrf-token", username, request);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Create successful", response.getBody().getMessage());
        assertEquals(createdUser, response.getBody().getData());
        
        verify(permissionValidator).checkAdmin(username, "create dial user");
    }

    @Test
    public void testDialusersPost_NoAdminRole_ReturnsForbidden() {
        // Arrange
        CreateDialUserRequest request = new CreateDialUserRequest();
        request.setUsername("newuser");
        request.setPassword("password");
        
        String username = "operator";
        PermissionValidator.PermissionValidationResult failureResult = 
            PermissionValidator.PermissionValidationResult.failure("权限不足，create dial user需要管理员权限");
        when(permissionValidator.checkAdmin(username, "create dial user"))
            .thenReturn(failureResult);

        // Act
        ResponseEntity<DialUserResponse> response = dialUserController.createDialUser("csrf-token", username, request);

        // Assert
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
        assertTrue(response.getBody().getMessage().contains("权限不足"));
        
        verify(permissionValidator).checkAdmin(username, "create dial user");
    }

    @Test
    public void testDialusersPost_EmptyUsername_ReturnsUnauthorized() {
        // Arrange
        CreateDialUserRequest request = new CreateDialUserRequest();
        request.setUsername("newuser");
        request.setPassword("password");
        
        String username = "";
        PermissionValidator.PermissionValidationResult failureResult = 
            PermissionValidator.PermissionValidationResult.failure("未提供用户名");
        when(permissionValidator.checkAdmin(username, "create dial user"))
            .thenReturn(failureResult);

        // Act
        ResponseEntity<DialUserResponse> response = dialUserController.createDialUser("csrf-token", username, request);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
        assertEquals("未提供用户名", response.getBody().getMessage());
        
        verify(permissionValidator).checkAdmin(username, "create dial user");
    }

    @Test
    public void testDialusersPost_UsernameExists_ReturnsConflict() {
        // Arrange
        CreateDialUserRequest request = new CreateDialUserRequest();
        request.setUsername("existinguser");
        request.setPassword("password");
        
        String username = "admin";
        PermissionValidator.PermissionValidationResult successResult = 
            PermissionValidator.PermissionValidationResult.success();
        when(permissionValidator.checkAdmin(username, "create dial user"))
            .thenReturn(successResult);
        
        when(dialUserService.createUser("existinguser", "password", username))
                .thenThrow(new IllegalArgumentException("Username already exists"));

        // Act
        ResponseEntity<DialUserResponse> response = dialUserController.createDialUser("csrf-token", username, request);

        // Assert
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
        assertTrue(response.getBody().getMessage().contains("already exists") || response.getBody().getMessage().contains("已存在"));
        
        verify(permissionValidator).checkAdmin(username, "create dial user");
    }

    @Test
    public void testDialusersPost_InvalidRequest_ReturnsBadRequest() {
        // Arrange
        CreateDialUserRequest request = new CreateDialUserRequest();
        request.setUsername("");
        request.setPassword("password");
        
        String username = "admin";
        PermissionValidator.PermissionValidationResult successResult = 
            PermissionValidator.PermissionValidationResult.success();
        when(permissionValidator.checkAdmin(username, "create dial user"))
            .thenReturn(successResult);
        
        when(dialUserService.createUser("", "password", username))
                .thenThrow(new IllegalArgumentException("Username cannot be empty"));

        // Act
        ResponseEntity<DialUserResponse> response = dialUserController.createDialUser("csrf-token", username, request);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
        assertTrue(response.getBody().getMessage().contains("cannot be empty") || response.getBody().getMessage().contains("不能为空"));
        
        verify(permissionValidator).checkAdmin(username, "create dial user");
    }

    @Test
    public void testDialusersPost_ServiceException_ReturnsError() {
        // Arrange
        CreateDialUserRequest request = new CreateDialUserRequest();
        request.setUsername("newuser");
        request.setPassword("password");
        
        String username = "admin";
        PermissionValidator.PermissionValidationResult successResult = 
            PermissionValidator.PermissionValidationResult.success();
        when(permissionValidator.checkAdmin(username, "create dial user"))
            .thenReturn(successResult);
        
        when(dialUserService.createUser("newuser", "password", username))
                .thenThrow(new RuntimeException("Database error"));

        // Act
        ResponseEntity<DialUserResponse> response = dialUserController.createDialUser("csrf-token", username, request);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
        assertTrue(response.getBody().getMessage().contains("Create") || response.getBody().getMessage().contains("创建"));
        
        verify(permissionValidator).checkAdmin(username, "create dial user");
    }

    private DialUser createTestUser(Integer id, String username, String password) {
        DialUser user = new DialUser();
        user.setId(id);
        user.setUsername(username);
        user.setPassword(password);
        user.setLastLoginTime(LocalDateTime.now().toString());
        return user;
    }
}
