/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.dialtest.center.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.dialtest.center.entity.Role;
import com.dialtest.center.entity.UserRole;
import com.dialtest.center.service.UserRoleService;

/**
 * 用户角色控制器测试类，测试UserRoleController的REST API接口
 * 包括用户角色创建、更新、删除、查询等HTTP端点的测试
 * 使用Mockito模拟服务层依赖，验证控制器层的正确性
 * 
 * @author g00940940
 * @since 2025-09-06
 */
@RunWith(MockitoJUnitRunner.class)
public class UserRoleControllerTest {

    @Mock
    private UserRoleService userRoleService;

    @InjectMocks
    private UserRoleController userRoleController;

    private UserRole testUserRole;
    private UserRoleController.UserRoleRequest testRequest;

    @Before
    public void setUp() {
        testUserRole = new UserRole();
        testUserRole.setId(1L);
        testUserRole.setUsername("testuser");
        testUserRole.setRole(Role.ADMIN);

        testRequest = new UserRoleController.UserRoleRequest();
        testRequest.setUsername("testuser");
        testRequest.setRole(Role.ADMIN);
    }

    @Test
    public void testGetUserRoles_WithUsername() {
        // Given
        String username = "testuser";
        List<UserRole> expectedUserRoles = Arrays.asList(testUserRole);
        when(userRoleService.getUserRoles(username)).thenReturn(expectedUserRoles);

        // When
        ResponseEntity<List<UserRole>> response = userRoleController.getUserRoles(username);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals(username, response.getBody().get(0).getUsername());
        verify(userRoleService).getUserRoles(username);
    }

    @Test
    public void testGetUserRoles_WithoutUsername() {
        // Given
        List<UserRole> expectedUserRoles = Arrays.asList(testUserRole);
        when(userRoleService.getAllUserRoles()).thenReturn(expectedUserRoles);

        // When
        ResponseEntity<List<UserRole>> response = userRoleController.getUserRoles(null);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        verify(userRoleService).getAllUserRoles();
        verify(userRoleService, never()).getUserRoles(anyString());
    }

    @Test
    public void testGetUserRoles_EmptyUsername() {
        // Given
        List<UserRole> expectedUserRoles = Arrays.asList(testUserRole);
        when(userRoleService.getAllUserRoles()).thenReturn(expectedUserRoles);

        // When
        ResponseEntity<List<UserRole>> response = userRoleController.getUserRoles("");

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        verify(userRoleService).getAllUserRoles();
        verify(userRoleService, never()).getUserRoles(anyString());
    }

    @Test
    public void testGetUserRoles_ServiceException() {
        // Given
        String username = "testuser";
        when(userRoleService.getUserRoles(username)).thenThrow(new RuntimeException("Service error"));

        // When
        ResponseEntity<List<UserRole>> response = userRoleController.getUserRoles(username);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(userRoleService).getUserRoles(username);
    }

    @Test
    public void testCreateUserRole_Success() {
        // Given
        when(userRoleService.save(any(UserRole.class))).thenReturn(testUserRole);

        // When
        ResponseEntity<UserRole> response = userRoleController.createUserRole(testRequest);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(testUserRole.getId(), response.getBody().getId());
        assertEquals(testUserRole.getUsername(), response.getBody().getUsername());
        assertEquals(testUserRole.getRole(), response.getBody().getRole());
        verify(userRoleService).save(any(UserRole.class));
    }

    @Test
    public void testCreateUserRole_ServiceException() {
        // Given
        when(userRoleService.save(any(UserRole.class))).thenThrow(new RuntimeException("Service error"));

        // When
        ResponseEntity<UserRole> response = userRoleController.createUserRole(testRequest);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(userRoleService).save(any(UserRole.class));
    }

    @Test
    public void testUpdateUserRole_Success() {
        // Given
        Long id = 1L;
        when(userRoleService.findById(id)).thenReturn(Optional.of(testUserRole));
        when(userRoleService.save(any(UserRole.class))).thenReturn(testUserRole);

        // When
        ResponseEntity<UserRole> response = userRoleController.updateUserRole(id, testRequest);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(testUserRole.getId(), response.getBody().getId());
        verify(userRoleService).findById(id);
        verify(userRoleService).save(any(UserRole.class));
    }

    @Test
    public void testUpdateUserRole_NotFound() {
        // Given
        Long id = 999L;
        when(userRoleService.findById(id)).thenReturn(Optional.empty());

        // When
        ResponseEntity<UserRole> response = userRoleController.updateUserRole(id, testRequest);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(userRoleService).findById(id);
        verify(userRoleService, never()).save(any(UserRole.class));
    }

    @Test
    public void testUpdateUserRole_ServiceException() {
        // Given
        Long id = 1L;
        when(userRoleService.findById(id)).thenThrow(new RuntimeException("Service error"));

        // When
        ResponseEntity<UserRole> response = userRoleController.updateUserRole(id, testRequest);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(userRoleService).findById(id);
    }

    @Test
    public void testDeleteUserRole_Success() {
        // Given
        Long id = 1L;

        // When
        ResponseEntity<Void> response = userRoleController.deleteUserRole(id);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(userRoleService).deleteById(id);
    }

    @Test
    public void testDeleteUserRole_ServiceException() {
        // Given
        Long id = 1L;
        // 使用doThrow来模拟void方法的异常
        org.mockito.Mockito.doThrow(new RuntimeException("Service error"))
            .when(userRoleService).deleteById(id);

        // When
        ResponseEntity<Void> response = userRoleController.deleteUserRole(id);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(userRoleService).deleteById(id);
    }

    @Test
    public void testGetExecutorCount_Success() {
        // Given
        long expectedCount = 5L;
        when(userRoleService.getExecutorUserCount()).thenReturn(expectedCount);

        // When
        ResponseEntity<Long> response = userRoleController.getExecutorCount();

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(Long.valueOf(expectedCount), response.getBody());
        verify(userRoleService).getExecutorUserCount();
    }

    @Test
    public void testGetExecutorCount_ServiceException() {
        // Given
        when(userRoleService.getExecutorUserCount()).thenThrow(new RuntimeException("Service error"));

        // When
        ResponseEntity<Long> response = userRoleController.getExecutorCount();

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(userRoleService).getExecutorUserCount();
    }

    @Test
    public void testUserRoleRequest_SettersAndGetters() {
        // Given
        UserRoleController.UserRoleRequest request = new UserRoleController.UserRoleRequest();
        String username = "testuser";
        Role role = Role.OPERATOR;

        // When
        request.setUsername(username);
        request.setRole(role);

        // Then
        assertEquals(username, request.getUsername());
        assertEquals(role, request.getRole());
    }

    @Test
    public void testGetUserRoles_WithWhitespaceUsername() {
        // Given
        String username = "  testuser  ";
        List<UserRole> expectedUserRoles = Arrays.asList(testUserRole);
        when(userRoleService.getUserRoles("testuser")).thenReturn(expectedUserRoles);

        // When
        ResponseEntity<List<UserRole>> response = userRoleController.getUserRoles(username);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        verify(userRoleService).getUserRoles("testuser");
    }

    @Test
    public void testCreateUserRole_WithDifferentRole() {
        // Given
        UserRoleController.UserRoleRequest request = new UserRoleController.UserRoleRequest();
        request.setUsername("newuser");
        request.setRole(Role.OPERATOR);

        UserRole savedUserRole = new UserRole();
        savedUserRole.setId(2L);
        savedUserRole.setUsername("newuser");
        savedUserRole.setRole(Role.OPERATOR);

        when(userRoleService.save(any(UserRole.class))).thenReturn(savedUserRole);

        // When
        ResponseEntity<UserRole> response = userRoleController.createUserRole(request);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("newuser", response.getBody().getUsername());
        assertEquals(Role.OPERATOR, response.getBody().getRole());
        verify(userRoleService).save(any(UserRole.class));
    }

    @Test
    public void testUpdateUserRole_WithDifferentData() {
        // Given
        Long id = 1L;
        UserRoleController.UserRoleRequest request = new UserRoleController.UserRoleRequest();
        request.setUsername("updateduser");
        request.setRole(Role.OPERATOR);

        UserRole existingUserRole = new UserRole();
        existingUserRole.setId(id);
        existingUserRole.setUsername("olduser");
        existingUserRole.setRole(Role.ADMIN);

        UserRole updatedUserRole = new UserRole();
        updatedUserRole.setId(id);
        updatedUserRole.setUsername("updateduser");
        updatedUserRole.setRole(Role.OPERATOR);

        when(userRoleService.findById(id)).thenReturn(Optional.of(existingUserRole));
        when(userRoleService.save(any(UserRole.class))).thenReturn(updatedUserRole);

        // When
        ResponseEntity<UserRole> response = userRoleController.updateUserRole(id, request);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("updateduser", response.getBody().getUsername());
        assertEquals(Role.OPERATOR, response.getBody().getRole());
        verify(userRoleService).findById(id);
        verify(userRoleService).save(any(UserRole.class));
    }
}
