/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.dialtest.center.controller;

import com.huawei.dialtest.center.dto.BaseApiResponse;
import com.huawei.dialtest.center.dto.PagedResponse;
import com.huawei.dialtest.center.entity.Role;
import com.huawei.dialtest.center.entity.DialUserRole;
import com.huawei.dialtest.center.service.UserRoleService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * UserRoleController控制器测试
 * 测试用户角色控制器的REST API接口，包括HTTP请求处理
 *
 * @author g00940940
 * @since 2025-09-16
 */
@RunWith(MockitoJUnitRunner.class)
public class UserRoleControllerTest {
    @Mock
    private UserRoleService userRoleService;

    @InjectMocks
    private UserRoleController userRoleController;

    private DialUserRole testUserRole;
    private List<DialUserRole> testUserRoles;

    @Before
    public void setUp() {
        testUserRole = new DialUserRole();
        testUserRole.setId(1L);
        testUserRole.setUsername("testuser");
        testUserRole.setRole(Role.ADMIN);

        DialUserRole userRole2 = new DialUserRole();
        userRole2.setId(2L);
        userRole2.setUsername("testuser2");
        userRole2.setRole(Role.EXECUTOR);

        testUserRoles = Arrays.asList(testUserRole, userRole2);
    }

    @Test
    public void testGetUserRoles_Success() {
        Page<DialUserRole> userRolePage = new PageImpl<>(testUserRoles, PageRequest.of(0, 10), 2);
        when(userRoleService.getAllUserRoles(1, 10, null)).thenReturn(userRolePage);

        ResponseEntity<BaseApiResponse<PagedResponse<DialUserRole>>> response = userRoleController.getUserRoles(1, 10, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertNotNull(response.getBody().getData());
        assertEquals(testUserRoles, response.getBody().getData().getData());
        assertEquals(2L, response.getBody().getData().getTotal());
        assertEquals(1, response.getBody().getData().getPage());
        assertEquals(10, response.getBody().getData().getPageSize());
        verify(userRoleService).getAllUserRoles(1, 10, null);
    }

    @Test
    public void testGetUserRoles_ValidationError() {
        when(userRoleService.getAllUserRoles(1, 10, null))
                .thenThrow(new IllegalArgumentException("Invalid parameters"));

        ResponseEntity<BaseApiResponse<PagedResponse<DialUserRole>>> response = userRoleController.getUserRoles(1, 10, null);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("VALIDATION_ERROR", response.getBody().getErrorCode());
        assertEquals("Invalid parameters", response.getBody().getMessage());
        verify(userRoleService).getAllUserRoles(1, 10, null);
    }

    @Test
    public void testGetUserRoles_DatabaseError() {
        when(userRoleService.getAllUserRoles(1, 10, null))
                .thenThrow(new DataAccessException("Database error") {});

        ResponseEntity<BaseApiResponse<PagedResponse<DialUserRole>>> response = userRoleController.getUserRoles(1, 10, null);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("DATABASE_ERROR", response.getBody().getErrorCode());
        verify(userRoleService).getAllUserRoles(1, 10, null);
    }

    @Test
    public void testCreateUserRole_Success() {
        when(userRoleService.save(any(DialUserRole.class))).thenReturn(testUserRole);

        UserRoleController.UserRoleRequest request = new UserRoleController.UserRoleRequest();
        request.setUsername("testuser");
        request.setRole(Role.ADMIN);

        ResponseEntity<BaseApiResponse<DialUserRole>> response = userRoleController.createUserRole(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals(testUserRole, response.getBody().getData());
        assertEquals("User role created successfully", response.getBody().getMessage());
        verify(userRoleService).save(any(DialUserRole.class));
    }

    @Test
    public void testCreateUserRole_ValidationError() {
        UserRoleController.UserRoleRequest request = new UserRoleController.UserRoleRequest();
        request.setUsername("");
        request.setRole(Role.ADMIN);

        ResponseEntity<BaseApiResponse<DialUserRole>> response = userRoleController.createUserRole(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("VALIDATION_ERROR", response.getBody().getErrorCode());
    }

    @Test
    public void testCreateUserRole_DatabaseError() {
        when(userRoleService.save(any(DialUserRole.class)))
                .thenThrow(new DataAccessException("Database error") {});

        UserRoleController.UserRoleRequest request = new UserRoleController.UserRoleRequest();
        request.setUsername("testuser");
        request.setRole(Role.ADMIN);

        ResponseEntity<BaseApiResponse<DialUserRole>> response = userRoleController.createUserRole(request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("DATABASE_ERROR", response.getBody().getErrorCode());
        verify(userRoleService).save(any(DialUserRole.class));
    }

    @Test
    public void testUpdateUserRole_Success() {
        when(userRoleService.findById(1L)).thenReturn(Optional.of(testUserRole));
        when(userRoleService.save(any(DialUserRole.class))).thenReturn(testUserRole);

        UserRoleController.UserRoleRequest request = new UserRoleController.UserRoleRequest();
        request.setUsername("updateduser");
        request.setRole(Role.EXECUTOR);

        ResponseEntity<BaseApiResponse<DialUserRole>> response = userRoleController.updateUserRole(1L, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals(testUserRole, response.getBody().getData());
        assertEquals("User role updated successfully", response.getBody().getMessage());
        verify(userRoleService).findById(1L);
        verify(userRoleService).save(any(DialUserRole.class));
    }

    @Test
    public void testUpdateUserRole_NotFound() {
        when(userRoleService.findById(1L)).thenReturn(Optional.empty());

        UserRoleController.UserRoleRequest request = new UserRoleController.UserRoleRequest();
        request.setUsername("updateduser");
        request.setRole(Role.EXECUTOR);

        ResponseEntity<BaseApiResponse<DialUserRole>> response = userRoleController.updateUserRole(1L, request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("NOT_FOUND", response.getBody().getErrorCode());
        verify(userRoleService).findById(1L);
    }

    @Test
    public void testUpdateUserRole_ValidationError() {
        when(userRoleService.findById(1L)).thenReturn(Optional.of(testUserRole));
        when(userRoleService.save(any(DialUserRole.class)))
                .thenThrow(new IllegalArgumentException("Invalid parameters"));

        UserRoleController.UserRoleRequest request = new UserRoleController.UserRoleRequest();
        request.setUsername("updateduser");
        request.setRole(Role.EXECUTOR);

        ResponseEntity<BaseApiResponse<DialUserRole>> response = userRoleController.updateUserRole(1L, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("VALIDATION_ERROR", response.getBody().getErrorCode());
        verify(userRoleService).findById(1L);
        verify(userRoleService).save(any(DialUserRole.class));
    }

    @Test
    public void testDeleteUserRole_Success() {
        doNothing().when(userRoleService).deleteById(1L);

        ResponseEntity<BaseApiResponse<String>> response = userRoleController.deleteUserRole(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("User role deleted successfully", response.getBody().getData());
        verify(userRoleService).deleteById(1L);
    }

    @Test
    public void testDeleteUserRole_NotFound() {
        doThrow(new IllegalArgumentException("User role not found")).when(userRoleService).deleteById(1L);

        ResponseEntity<BaseApiResponse<String>> response = userRoleController.deleteUserRole(1L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("NOT_FOUND", response.getBody().getErrorCode());
        verify(userRoleService).deleteById(1L);
    }

    @Test
    public void testDeleteUserRole_DatabaseError() {
        doThrow(new DataAccessException("Database error") {}).when(userRoleService).deleteById(1L);

        ResponseEntity<BaseApiResponse<String>> response = userRoleController.deleteUserRole(1L);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("DATABASE_ERROR", response.getBody().getErrorCode());
        verify(userRoleService).deleteById(1L);
    }

    @Test
    public void testGetExecutorCount_Success() {
        when(userRoleService.getExecutorUserCount()).thenReturn(5L);

        ResponseEntity<BaseApiResponse<Long>> response = userRoleController.getExecutorCount();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals(Long.valueOf(5L), response.getBody().getData());
        verify(userRoleService).getExecutorUserCount();
    }

    @Test
    public void testGetExecutorCount_DatabaseError() {
        when(userRoleService.getExecutorUserCount())
                .thenThrow(new DataAccessException("Database error") {});

        ResponseEntity<BaseApiResponse<Long>> response = userRoleController.getExecutorCount();

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("DATABASE_ERROR", response.getBody().getErrorCode());
        verify(userRoleService).getExecutorUserCount();
    }
}