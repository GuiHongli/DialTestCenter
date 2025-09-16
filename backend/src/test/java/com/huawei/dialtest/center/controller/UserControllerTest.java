/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.dialtest.center.controller;

import com.huawei.dialtest.center.entity.DialUser;
import com.huawei.dialtest.center.service.DialUserService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * DialUserController控制器测试
 * 测试用户控制器的REST API接口，包括HTTP请求处理
 *
 * @author g00940940
 * @since 2025-09-09
 */
@RunWith(MockitoJUnitRunner.class)
public class DialUserControllerTest {
    @Mock
    private DialUserService userService;

    @InjectMocks
    private DialUserController userController;

    private DialUser testDialUser;
    private List<DialUser> testDialUsers;

    @Before
    public void setUp() {
        testDialUser = new DialUser();
        testDialUser.setId(1L);
        testDialUser.setDialUsername("testuser");
        testDialUser.setPassword("$2a$10$encodedpassword");
        testDialUser.setLastLoginTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        DialUser user2 = new DialUser();
        user2.setId(2L);
        user2.setDialUsername("testuser2");
        user2.setPassword("$2a$10$encodedpassword2");

        testDialUsers = Arrays.asList(testDialUser, user2);
    }

    @Test
    public void testGetAllDialUsers_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<DialUser> userPage = new PageImpl<>(testDialUsers, pageable, 2);
        when(userService.getAllDialUsers(1, 10, null)).thenReturn(userPage);

        ResponseEntity<?> response = userController.getAllDialUsers(1, 10, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof Map);
        @SuppressWarnings("unchecked")
        Map<String, Object> result = (Map<String, Object>) response.getBody();
        assertTrue(result.containsKey("data"));
        assertTrue(result.containsKey("total"));
        assertTrue(result.containsKey("page"));
        assertTrue(result.containsKey("pageSize"));
        assertTrue(result.containsKey("totalPages"));
        verify(userService).getAllDialUsers(1, 10, null);
    }

    @Test
    public void testGetAllDialUsers_Error() {
        when(userService.getAllDialUsers(1, 10, null)).thenThrow(new RuntimeException("Service error"));

        ResponseEntity<?> response = userController.getAllDialUsers(1, 10, null);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof Map);
        @SuppressWarnings("unchecked")
        Map<String, Object> errorResponse = (Map<String, Object>) response.getBody();
        assertEquals("INTERNAL_ERROR", errorResponse.get("error"));
        verify(userService).getAllDialUsers(1, 10, null);
    }

    @Test
    public void testGetDialUserById_Success() {
        when(userService.getDialUserById(1L)).thenReturn(Optional.of(testDialUser));

        ResponseEntity<?> response = userController.getDialUserById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof DialUser);
        DialUser result = (DialUser) response.getBody();
        assertEquals("testuser", result.getDialUsername());
        verify(userService).getDialUserById(1L);
    }

    @Test
    public void testGetDialUserById_NotFound() {
        when(userService.getDialUserById(999L)).thenReturn(Optional.empty());

        ResponseEntity<?> response = userController.getDialUserById(999L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(userService).getDialUserById(999L);
    }

    @Test
    public void testGetDialUserById_Error() {
        when(userService.getDialUserById(1L)).thenThrow(new RuntimeException("Service error"));

        ResponseEntity<?> response = userController.getDialUserById(1L);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(userService).getDialUserById(1L);
    }

    @Test
    public void testGetDialUserByDialUsername_Success() {
        when(userService.getDialUserByDialUsername("testuser")).thenReturn(Optional.of(testDialUser));

        ResponseEntity<?> response = userController.getDialUserByDialUsername("testuser");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof DialUser);
        DialUser result = (DialUser) response.getBody();
        assertEquals("testuser", result.getDialUsername());
        verify(userService).getDialUserByDialUsername("testuser");
    }

    @Test
    public void testGetDialUserByDialUsername_NotFound() {
        when(userService.getDialUserByDialUsername("nonexistent")).thenReturn(Optional.empty());

        ResponseEntity<?> response = userController.getDialUserByDialUsername("nonexistent");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(userService).getDialUserByDialUsername("nonexistent");
    }

    @Test
    public void testGetDialUserByDialUsername_Error() {
        when(userService.getDialUserByDialUsername("testuser")).thenThrow(new RuntimeException("Service error"));

        ResponseEntity<?> response = userController.getDialUserByDialUsername("testuser");

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(userService).getDialUserByDialUsername("testuser");
    }

    @Test
    public void testCreateDialUser_Success() {
        Map<String, String> request = new HashMap<>();
        request.put("username", "newuser");
        request.put("password", "password123");

        when(userService.createDialUser("newuser", "password123")).thenReturn(testDialUser);

        ResponseEntity<?> response = userController.createDialUser(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof DialUser);
        DialUser result = (DialUser) response.getBody();
        assertEquals("testuser", result.getDialUsername());
        verify(userService).createDialUser("newuser", "password123");
    }

    @Test
    public void testCreateDialUser_MissingDialUsername() {
        Map<String, String> request = new HashMap<>();
        request.put("password", "password123");

        ResponseEntity<?> response = userController.createDialUser(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof Map);
        @SuppressWarnings("unchecked")
        Map<String, Object> errorResponse = (Map<String, Object>) response.getBody();
        assertEquals("VALIDATION_ERROR", errorResponse.get("error"));
        verify(userService, never()).createDialUser(anyString(), anyString());
    }

    @Test
    public void testCreateDialUser_MissingPassword() {
        Map<String, String> request = new HashMap<>();
        request.put("username", "newuser");

        ResponseEntity<?> response = userController.createDialUser(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof Map);
        @SuppressWarnings("unchecked")
        Map<String, Object> errorResponse = (Map<String, Object>) response.getBody();
        assertEquals("VALIDATION_ERROR", errorResponse.get("error"));
        verify(userService, never()).createDialUser(anyString(), anyString());
    }

    @Test
    public void testCreateDialUser_ValidationError() {
        Map<String, String> request = new HashMap<>();
        request.put("username", "existinguser");
        request.put("password", "password123");

        when(userService.createDialUser("existinguser", "password123"))
            .thenThrow(new IllegalArgumentException("DialUsername already exists"));

        ResponseEntity<?> response = userController.createDialUser(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof Map);
        @SuppressWarnings("unchecked")
        Map<String, Object> errorResponse = (Map<String, Object>) response.getBody();
        assertEquals("VALIDATION_ERROR", errorResponse.get("error"));
        verify(userService).createDialUser("existinguser", "password123");
    }

    @Test
    public void testCreateDialUser_Error() {
        Map<String, String> request = new HashMap<>();
        request.put("username", "newuser");
        request.put("password", "password123");

        when(userService.createDialUser("newuser", "password123"))
            .thenThrow(new RuntimeException("Service error"));

        ResponseEntity<?> response = userController.createDialUser(request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(userService).createDialUser("newuser", "password123");
    }

    @Test
    public void testUpdateDialUser_Success() {
        Map<String, String> request = new HashMap<>();
        request.put("username", "updateduser");
        request.put("password", "newpassword");

        when(userService.updateDialUser(1L, "updateduser", "newpassword")).thenReturn(testDialUser);

        ResponseEntity<?> response = userController.updateDialUser(1L, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof DialUser);
        DialUser result = (DialUser) response.getBody();
        assertEquals("testuser", result.getDialUsername());
        verify(userService).updateDialUser(1L, "updateduser", "newpassword");
    }

    @Test
    public void testUpdateDialUser_EmptyDialUsername() {
        Map<String, String> request = new HashMap<>();
        request.put("username", "");
        request.put("password", "newpassword");

        ResponseEntity<?> response = userController.updateDialUser(1L, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(userService, never()).updateDialUser(anyLong(), anyString(), anyString());
    }

    @Test
    public void testUpdateDialUser_ValidationError() {
        Map<String, String> request = new HashMap<>();
        request.put("username", "existinguser");
        request.put("password", "newpassword");

        when(userService.updateDialUser(1L, "existinguser", "newpassword"))
            .thenThrow(new IllegalArgumentException("DialUsername already exists"));

        ResponseEntity<?> response = userController.updateDialUser(1L, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(userService).updateDialUser(1L, "existinguser", "newpassword");
    }

    @Test
    public void testUpdateDialUser_Error() {
        Map<String, String> request = new HashMap<>();
        request.put("username", "updateduser");
        request.put("password", "newpassword");

        when(userService.updateDialUser(1L, "updateduser", "newpassword"))
            .thenThrow(new RuntimeException("Service error"));

        ResponseEntity<?> response = userController.updateDialUser(1L, request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(userService).updateDialUser(1L, "updateduser", "newpassword");
    }

    @Test
    public void testDeleteDialUser_Success() {
        doNothing().when(userService).deleteDialUser(1L);

        ResponseEntity<?> response = userController.deleteDialUser(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof Map);
        @SuppressWarnings("unchecked")
        Map<String, Object> successResponse = (Map<String, Object>) response.getBody();
        assertEquals(true, successResponse.get("success"));
        verify(userService).deleteDialUser(1L);
    }

    @Test
    public void testDeleteDialUser_ValidationError() {
        doThrow(new IllegalArgumentException("DialUser not found"))
            .when(userService).deleteDialUser(999L);

        ResponseEntity<?> response = userController.deleteDialUser(999L);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(userService).deleteDialUser(999L);
    }

    @Test
    public void testDeleteDialUser_Error() {
        doThrow(new RuntimeException("Service error"))
            .when(userService).deleteDialUser(1L);

        ResponseEntity<?> response = userController.deleteDialUser(1L);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(userService).deleteDialUser(1L);
    }

    @Test
    public void testSearchDialUsers_Success() {
        when(userService.searchDialUsersByDialUsername("test")).thenReturn(testDialUsers);

        ResponseEntity<?> response = userController.searchDialUsers("test");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof List);
        @SuppressWarnings("unchecked")
        List<DialUser> result = (List<DialUser>) response.getBody();
        assertEquals(2, result.size());
        verify(userService).searchDialUsersByDialUsername("test");
    }

    @Test
    public void testSearchDialUsers_Error() {
        when(userService.searchDialUsersByDialUsername("test"))
            .thenThrow(new RuntimeException("Service error"));

        ResponseEntity<?> response = userController.searchDialUsers("test");

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(userService).searchDialUsersByDialUsername("test");
    }

    @Test
    public void testValidatePassword_Success() {
        Map<String, String> request = new HashMap<>();
        request.put("username", "testuser");
        request.put("password", "password123");

        when(userService.validatePassword("testuser", "password123")).thenReturn(true);

        ResponseEntity<?> response = userController.validatePassword(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof Map);
        @SuppressWarnings("unchecked")
        Map<String, Object> result = (Map<String, Object>) response.getBody();
        assertEquals(true, result.get("valid"));
        verify(userService).validatePassword("testuser", "password123");
    }

    @Test
    public void testValidatePassword_InvalidPassword() {
        Map<String, String> request = new HashMap<>();
        request.put("username", "testuser");
        request.put("password", "wrongpassword");

        when(userService.validatePassword("testuser", "wrongpassword")).thenReturn(false);

        ResponseEntity<?> response = userController.validatePassword(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof Map);
        @SuppressWarnings("unchecked")
        Map<String, Object> result = (Map<String, Object>) response.getBody();
        assertEquals(false, result.get("valid"));
        verify(userService).validatePassword("testuser", "wrongpassword");
    }

    @Test
    public void testValidatePassword_MissingDialUsername() {
        Map<String, String> request = new HashMap<>();
        request.put("password", "password123");

        ResponseEntity<?> response = userController.validatePassword(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(userService, never()).validatePassword(anyString(), anyString());
    }

    @Test
    public void testValidatePassword_MissingPassword() {
        Map<String, String> request = new HashMap<>();
        request.put("username", "testuser");

        ResponseEntity<?> response = userController.validatePassword(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(userService, never()).validatePassword(anyString(), anyString());
    }

    @Test
    public void testValidatePassword_Error() {
        Map<String, String> request = new HashMap<>();
        request.put("username", "testuser");
        request.put("password", "password123");

        when(userService.validatePassword("testuser", "password123"))
            .thenThrow(new RuntimeException("Service error"));

        ResponseEntity<?> response = userController.validatePassword(request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(userService).validatePassword("testuser", "password123");
    }

    @Test
    public void testUpdateLastLoginTime_Success() {
        Map<String, String> request = new HashMap<>();
        request.put("username", "testuser");

        doNothing().when(userService).updateLastLoginTime("testuser");

        ResponseEntity<?> response = userController.updateLastLoginTime(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof Map);
        @SuppressWarnings("unchecked")
        Map<String, Object> successResponse = (Map<String, Object>) response.getBody();
        assertEquals(true, successResponse.get("success"));
        verify(userService).updateLastLoginTime("testuser");
    }

    @Test
    public void testUpdateLastLoginTime_MissingDialUsername() {
        Map<String, String> request = new HashMap<>();

        ResponseEntity<?> response = userController.updateLastLoginTime(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(userService, never()).updateLastLoginTime(anyString());
    }

    @Test
    public void testUpdateLastLoginTime_Error() {
        Map<String, String> request = new HashMap<>();
        request.put("username", "testuser");

        doThrow(new RuntimeException("Service error"))
            .when(userService).updateLastLoginTime("testuser");

        ResponseEntity<?> response = userController.updateLastLoginTime(request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(userService).updateLastLoginTime("testuser");
    }
}
