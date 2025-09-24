/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.cloududn.dialingtest.controller;

import com.huawei.cloududn.dialingtest.model.*;
import com.huawei.cloududn.dialingtest.service.DialUserService;
import com.huawei.cloududn.dialingtest.service.UserRoleService;

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

    @InjectMocks
    private DialUserController dialUserController;

    @Test
    public void testDialusersGet_Success_ReturnsUserList() {
        // Arrange
        DialUser user1 = createTestUser(1, "user1", "password1");
        DialUser user2 = createTestUser(2, "user2", "password2");
        List<DialUser> users = Arrays.asList(user1, user2);
        
        when(dialUserService.findUsersWithPagination(0, 10, null)).thenReturn(users);
        when(dialUserService.countUsers(null)).thenReturn(2L);

        // Act
        ResponseEntity<DialUserPageResponse> response = dialUserController.dialusersGet(0, 10, null);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("查询成功", response.getBody().getMessage());
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
        ResponseEntity<DialUserPageResponse> response = dialUserController.dialusersGet(0, 10, "test");

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
        
        when(dialUserService.findUsersWithPagination(0, 10, null)).thenReturn(users);
        when(dialUserService.countUsers(null)).thenReturn(0L);

        // Act
        ResponseEntity<DialUserPageResponse> response = dialUserController.dialusersGet(null, null, null);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
        assertEquals(0, response.getBody().getData().getContent().size());
    }

    @Test
    public void testDialusersGet_ServiceException_ReturnsError() {
        // Arrange
        when(dialUserService.findUsersWithPagination(0, 10, null)).thenThrow(new RuntimeException("Database error"));

        // Act
        ResponseEntity<DialUserPageResponse> response = dialUserController.dialusersGet(0, 10, null);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
        assertTrue(response.getBody().getMessage().contains("查询失败"));
    }

    @Test
    public void testDialusersGet_NullUsername_ReturnsUnauthorized() {
        // Act
        ResponseEntity<DialUserPageResponse> response = dialUserController.dialusersGet(0, 10, null);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
        assertEquals("未提供用户名", response.getBody().getMessage());
    }
    

    @Test
    public void testDialusersIdGet_Success_ReturnsUser() {
        // Arrange
        DialUser user = createTestUser(1, "testuser", "password");
        when(dialUserService.findById(1)).thenReturn(user);

        // Act
        ResponseEntity<DialUserResponse> response = dialUserController.dialusersIdGet(1);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
        assertEquals("查询成功", response.getBody().getMessage());
        assertEquals(user, response.getBody().getData());
    }

    @Test
    public void testDialusersIdGet_UserNotFound_ReturnsNotFound() {
        // Arrange
        when(dialUserService.findById(999)).thenReturn(null);

        // Act
        ResponseEntity<DialUserResponse> response = dialUserController.dialusersIdGet(999);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
        assertEquals("用户不存在", response.getBody().getMessage());
    }

    @Test
    public void testDialusersIdGet_NullUsername_ReturnsUnauthorized() {
        // Act
        ResponseEntity<DialUserResponse> response = dialUserController.dialusersIdGet(1);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
        assertEquals("未提供用户名", response.getBody().getMessage());
    }

    @Test
    public void testDialusersIdPut_Success_ReturnsUpdatedUser() {
        // Arrange
        UpdateDialUserRequest request = new UpdateDialUserRequest();
        request.setUsername("newuser");
        request.setPassword("newpassword");
        
        DialUser updatedUser = createTestUser(1, "newuser", "newpassword");
        when(userRoleService.getUserRolesByUsername("admin")).thenReturn(Arrays.asList("ADMIN"));
        when(dialUserService.updateUser(1, "newuser", "newpassword", "admin")).thenReturn(updatedUser);

        // Act
        ResponseEntity<DialUserResponse> response = dialUserController.dialusersIdPut("admin", 1, request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
        assertEquals("修改成功", response.getBody().getMessage());
        assertEquals(updatedUser, response.getBody().getData());
    }

    @Test
    public void testDialusersIdPut_NoAdminRole_ReturnsForbidden() {
        // Arrange
        UpdateDialUserRequest request = new UpdateDialUserRequest();
        request.setUsername("newuser");
        request.setPassword("newpassword");
        
        when(userRoleService.getUserRolesByUsername("operator")).thenReturn(Arrays.asList("OPERATOR"));

        // Act
        ResponseEntity<DialUserResponse> response = dialUserController.dialusersIdPut("operator", 1, request);

        // Assert
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
        assertEquals("权限不足，仅ADMIN用户可操作", response.getBody().getMessage());
    }

    @Test
    public void testDialusersIdPut_EmptyUsername_ReturnsUnauthorized() {
        // Arrange
        UpdateDialUserRequest request = new UpdateDialUserRequest();
        request.setUsername("newuser");
        request.setPassword("newpassword");

        // Act
        ResponseEntity<DialUserResponse> response = dialUserController.dialusersIdPut("", 1, request);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
        assertEquals("未提供用户名", response.getBody().getMessage());
    }

    @Test
    public void testDialusersIdPut_UserNotFound_ReturnsNotFound() {
        // Arrange
        UpdateDialUserRequest request = new UpdateDialUserRequest();
        request.setUsername("newuser");
        request.setPassword("newpassword");
        
        when(userRoleService.getUserRolesByUsername("admin")).thenReturn(Arrays.asList("ADMIN"));
        when(dialUserService.updateUser(999, "newuser", "newpassword", "admin"))
                .thenThrow(new IllegalArgumentException("用户不存在: 999"));

        // Act
        ResponseEntity<DialUserResponse> response = dialUserController.dialusersIdPut("admin", 999, request);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
        assertEquals("用户不存在: 999", response.getBody().getMessage());
    }

    @Test
    public void testDialusersIdPut_UsernameExists_ReturnsConflict() {
        // Arrange
        UpdateDialUserRequest request = new UpdateDialUserRequest();
        request.setUsername("existinguser");
        request.setPassword("password");
        
        when(userRoleService.getUserRolesByUsername("admin")).thenReturn(Arrays.asList("ADMIN"));
        when(dialUserService.updateUser(1, "existinguser", "password", "admin"))
                .thenThrow(new IllegalArgumentException("用户名已存在: existinguser"));

        // Act
        ResponseEntity<DialUserResponse> response = dialUserController.dialusersIdPut("admin", 1, request);

        // Assert
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
        assertEquals("用户名已存在: existinguser", response.getBody().getMessage());
    }

    @Test
    public void testDialusersIdDelete_Success_ReturnsNoContent() {
        // Arrange
        when(userRoleService.getUserRolesByUsername("admin")).thenReturn(Arrays.asList("ADMIN"));
        doNothing().when(dialUserService).deleteUser(1, "admin");

        // Act
        ResponseEntity<Void> response = dialUserController.dialusersIdDelete(1, "admin");

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(dialUserService).deleteUser(1, "admin");
    }

    @Test
    public void testDialusersIdDelete_NoAdminRole_ReturnsForbidden() {
        // Arrange
        when(userRoleService.getUserRolesByUsername("operator")).thenReturn(Arrays.asList("OPERATOR"));

        // Act
        ResponseEntity<Void> response = dialUserController.dialusersIdDelete(1, "operator");

        // Assert
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        verify(dialUserService, never()).deleteUser(anyInt(), anyString());
    }

    @Test
    public void testDialusersIdDelete_EmptyUsername_ReturnsUnauthorized() {
        // Act
        ResponseEntity<Void> response = dialUserController.dialusersIdDelete(1, "");

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        verify(dialUserService, never()).deleteUser(anyInt(), anyString());
    }

    @Test
    public void testDialusersIdDelete_UserNotFound_ReturnsNotFound() {
        // Arrange
        when(userRoleService.getUserRolesByUsername("admin")).thenReturn(Arrays.asList("ADMIN"));
        doThrow(new IllegalArgumentException("用户不存在: 999")).when(dialUserService).deleteUser(999, "admin");

        // Act
        ResponseEntity<Void> response = dialUserController.dialusersIdDelete(999, "admin");

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void testDialusersIdDelete_ServiceException_ReturnsError() {
        // Arrange
        when(userRoleService.getUserRolesByUsername("admin")).thenReturn(Arrays.asList("ADMIN"));
        doThrow(new RuntimeException("Database error")).when(dialUserService).deleteUser(1, "admin");

        // Act
        ResponseEntity<Void> response = dialUserController.dialusersIdDelete(1, "admin");

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    public void testDialusersPost_Success_ReturnsCreatedUser() {
        // Arrange
        CreateDialUserRequest request = new CreateDialUserRequest();
        request.setUsername("newuser");
        request.setPassword("password");
        
        DialUser createdUser = createTestUser(1, "newuser", "password");
        when(userRoleService.getUserRolesByUsername("admin")).thenReturn(Arrays.asList("ADMIN"));
        when(dialUserService.createUser("newuser", "password", "admin")).thenReturn(createdUser);

        // Act
        ResponseEntity<DialUserResponse> response = dialUserController.dialusersPost("admin", request);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
        assertEquals("创建成功", response.getBody().getMessage());
        assertEquals(createdUser, response.getBody().getData());
    }

    @Test
    public void testDialusersPost_NoAdminRole_ReturnsForbidden() {
        // Arrange
        CreateDialUserRequest request = new CreateDialUserRequest();
        request.setUsername("newuser");
        request.setPassword("password");
        
        when(userRoleService.getUserRolesByUsername("operator")).thenReturn(Arrays.asList("OPERATOR"));

        // Act
        ResponseEntity<DialUserResponse> response = dialUserController.dialusersPost("operator", request);

        // Assert
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
        assertEquals("权限不足，仅ADMIN用户可操作", response.getBody().getMessage());
    }

    @Test
    public void testDialusersPost_EmptyUsername_ReturnsUnauthorized() {
        // Arrange
        CreateDialUserRequest request = new CreateDialUserRequest();
        request.setUsername("newuser");
        request.setPassword("password");

        // Act
        ResponseEntity<DialUserResponse> response = dialUserController.dialusersPost("", request);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
        assertEquals("未提供用户名", response.getBody().getMessage());
    }

    @Test
    public void testDialusersPost_UsernameExists_ReturnsConflict() {
        // Arrange
        CreateDialUserRequest request = new CreateDialUserRequest();
        request.setUsername("existinguser");
        request.setPassword("password");
        
        when(userRoleService.getUserRolesByUsername("admin")).thenReturn(Arrays.asList("ADMIN"));
        when(dialUserService.createUser("existinguser", "password", "admin"))
                .thenThrow(new IllegalArgumentException("用户名已存在: existinguser"));

        // Act
        ResponseEntity<DialUserResponse> response = dialUserController.dialusersPost("admin", request);

        // Assert
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
        assertEquals("用户名已存在: existinguser", response.getBody().getMessage());
    }

    @Test
    public void testDialusersPost_InvalidRequest_ReturnsBadRequest() {
        // Arrange
        CreateDialUserRequest request = new CreateDialUserRequest();
        request.setUsername("");
        request.setPassword("password");
        
        when(userRoleService.getUserRolesByUsername("admin")).thenReturn(Arrays.asList("ADMIN"));
        when(dialUserService.createUser("", "password", "admin"))
                .thenThrow(new IllegalArgumentException("用户名不能为空"));

        // Act
        ResponseEntity<DialUserResponse> response = dialUserController.dialusersPost("admin", request);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
        assertEquals("用户名不能为空", response.getBody().getMessage());
    }

    @Test
    public void testDialusersPost_ServiceException_ReturnsError() {
        // Arrange
        CreateDialUserRequest request = new CreateDialUserRequest();
        request.setUsername("newuser");
        request.setPassword("password");
        
        when(userRoleService.getUserRolesByUsername("admin")).thenReturn(Arrays.asList("ADMIN"));
        when(dialUserService.createUser("newuser", "password", "admin"))
                .thenThrow(new RuntimeException("Database error"));

        // Act
        ResponseEntity<DialUserResponse> response = dialUserController.dialusersPost("admin", request);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
        assertTrue(response.getBody().getMessage().contains("创建失败"));
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
