/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.dialtest.center.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.huawei.dialtest.center.entity.Role;
import com.huawei.dialtest.center.entity.UserRole;
import com.huawei.dialtest.center.mapper.UserRoleMapper;
import com.huawei.dialtest.center.service.UserRoleService;

/**
 * 用户角色服务测试类，测试UserRoleService的业务逻辑
 * 包括用户角色分配、权限检查、角色查询等功能的测试
 * 使用Mockito模拟依赖，验证服务层的正确性
 * 
 * @author g00940940
 * @since 2025-09-06
 */
@RunWith(MockitoJUnitRunner.class)
public class UserRoleServiceTest {

    @Mock
    private UserRoleMapper userRoleMapper;

    @InjectMocks
    private UserRoleService userRoleService;

    private UserRole testUserRole;
    private List<UserRole> testUserRoles;

    @Before
    public void setUp() {
        testUserRole = new UserRole();
        testUserRole.setId(1L);
        testUserRole.setUsername("testuser");
        testUserRole.setRole(Role.ADMIN);

        testUserRoles = Arrays.asList(testUserRole);
    }

    @Test
    public void testGetUserRoles_Success() {
        // Given
        String username = "testuser";
        when(userRoleMapper.findByUsernameOrderByCreatedTimeDesc(username)).thenReturn(testUserRoles);

        // When
        List<UserRole> result = userRoleService.getUserRoles(username);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(username, result.get(0).getUsername());
        assertEquals(Role.ADMIN, result.get(0).getRole());
        verify(userRoleMapper).findByUsernameOrderByCreatedTimeDesc(username);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetUserRoles_EmptyUsername() {
        // When & Then - 应该抛出异常
        userRoleService.getUserRoles("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetUserRoles_NullUsername() {
        // When & Then - 应该抛出异常
        userRoleService.getUserRoles(null);
    }

    @Test
    public void testGetUserRoles_UserNotExist() {
        // Given
        String username = "nonexistent";
        when(userRoleMapper.findByUsernameOrderByCreatedTimeDesc(username)).thenReturn(Collections.emptyList());

        // When
        List<UserRole> result = userRoleService.getUserRoles(username);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(userRoleMapper).findByUsernameOrderByCreatedTimeDesc(username);
    }

    @Test
    public void testSaveUserRole_Success_NewUserRole() {
        // Given
        UserRole newUserRole = new UserRole();
        newUserRole.setUsername("newuser");
        newUserRole.setRole(Role.OPERATOR);
        // ID为null表示新建

        when(userRoleMapper.existsByUsernameAndRole(anyString(), anyString())).thenReturn(false);
        when(userRoleMapper.insert(any(UserRole.class))).thenReturn(1);

        // When
        UserRole result = userRoleService.save(newUserRole);

        // Then
        assertNotNull(result);
        assertEquals("newuser", result.getUsername());
        assertEquals(Role.OPERATOR, result.getRole());
        verify(userRoleMapper).existsByUsernameAndRole("newuser", Role.OPERATOR.toString());
        verify(userRoleMapper).insert(newUserRole);
    }

    @Test
    public void testSaveUserRole_DuplicateRole() {
        // Given
        UserRole duplicateUserRole = new UserRole();
        duplicateUserRole.setUsername("testuser");
        duplicateUserRole.setRole(Role.ADMIN);

        when(userRoleMapper.existsByUsernameAndRole(anyString(), anyString())).thenReturn(true);

        // When & Then - 应该抛出异常
        try {
            userRoleService.save(duplicateUserRole);
            assertTrue("Expected IllegalArgumentException", false);
        } catch (IllegalArgumentException e) {
            // Expected exception
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSaveUserRole_NullUserRole() {
        // When & Then - 应该抛出异常
        userRoleService.save(null);
    }

    @Test
    public void testDeleteById_Success() {
        // Given
        Long id = 1L;
        when(userRoleMapper.findById(id)).thenReturn(testUserRole);
        when(userRoleMapper.deleteById(id)).thenReturn(1);

        // When
        userRoleService.deleteById(id);

        // Then
        verify(userRoleMapper).findById(id);
        verify(userRoleMapper).deleteById(id);
    }

    @Test
    public void testDeleteById_IDNotExist() {
        // Given
        Long id = 999L;
        when(userRoleMapper.findById(id)).thenReturn(null);

        // When & Then - 应该抛出异常
        try {
            userRoleService.deleteById(id);
            assertTrue("Expected IllegalArgumentException", false);
        } catch (IllegalArgumentException e) {
            // Expected exception
        }
    }

    @Test
    public void testHasAdminUser_AdminExists() {
        // Given
        when(userRoleMapper.existsByRole(Role.ADMIN.toString())).thenReturn(true);

        // When
        boolean result = userRoleService.hasAdminUser();

        // Then
        assertTrue(result);
        verify(userRoleMapper).existsByRole(Role.ADMIN.toString());
    }

    @Test
    public void testHasAdminUser_NoAdmin() {
        // Given
        when(userRoleMapper.existsByRole(Role.ADMIN.toString())).thenReturn(false);

        // When
        boolean result = userRoleService.hasAdminUser();

        // Then
        assertFalse(result);
        verify(userRoleMapper).existsByRole(Role.ADMIN.toString());
    }

    @Test
    public void testHasRole_UserHasRole() {
        // Given
        String username = "testuser";
        Role role = Role.ADMIN;
        when(userRoleMapper.existsByUsernameAndRole(username, role.toString())).thenReturn(true);

        // When
        boolean result = userRoleService.hasRole(username, role);

        // Then
        assertTrue(result);
        verify(userRoleMapper).existsByUsernameAndRole(username, role.toString());
    }

    @Test
    public void testHasRole_UserNotHasRole() {
        // Given
        String username = "testuser";
        Role role = Role.OPERATOR;
        when(userRoleMapper.existsByUsernameAndRole(username, role.toString())).thenReturn(false);

        // When
        boolean result = userRoleService.hasRole(username, role);

        // Then
        assertFalse(result);
        verify(userRoleMapper).existsByUsernameAndRole(username, role.toString());
    }

    @Test
    public void testHasRole_EmptyUsername() {
        // When
        boolean result = userRoleService.hasRole("", Role.ADMIN);

        // Then
        assertFalse(result);
        verify(userRoleMapper, never()).existsByUsernameAndRole(anyString(), anyString());
    }

    @Test
    public void testHasRole_NullRole() {
        // When
        boolean result = userRoleService.hasRole("testuser", null);

        // Then
        assertFalse(result);
        verify(userRoleMapper, never()).existsByUsernameAndRole(anyString(), anyString());
    }

    // ========== 新增测试用例以提高覆盖率 ==========

    @Test
    public void testGetUserRoleEnums_Success() {
        // Given
        String username = "testuser";
        List<Role> expectedRoles = Arrays.asList(Role.ADMIN, Role.OPERATOR);
        when(userRoleMapper.findRolesByUsername(username)).thenReturn(expectedRoles);

        // When
        List<Role> result = userRoleService.getUserRoleEnums(username);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(Role.ADMIN));
        assertTrue(result.contains(Role.OPERATOR));
        verify(userRoleMapper).findRolesByUsername(username);
    }

    @Test
    public void testGetUserRoleEnums_EmptyUsername() {
        // When & Then - 应该抛出异常
        try {
            userRoleService.getUserRoleEnums("");
            assertTrue("Expected IllegalArgumentException", false);
        } catch (IllegalArgumentException e) {
            // Expected exception
        }
    }

    @Test
    public void testGetUserRoleEnums_NullUsername() {
        // When & Then - 应该抛出异常
        try {
            userRoleService.getUserRoleEnums(null);
            assertTrue("Expected IllegalArgumentException", false);
        } catch (IllegalArgumentException e) {
            // Expected exception
        }
    }

    @Test
    public void testGetAllUserRoles_Success() {
        // Given
        UserRole userRole1 = new UserRole();
        userRole1.setId(1L);
        userRole1.setUsername("user1");
        userRole1.setRole(Role.ADMIN);

        UserRole userRole2 = new UserRole();
        userRole2.setId(2L);
        userRole2.setUsername("user2");
        userRole2.setRole(Role.OPERATOR);

        List<UserRole> expectedUserRoles = Arrays.asList(userRole1, userRole2);
        when(userRoleMapper.findAllOrderByCreatedTimeDesc()).thenReturn(expectedUserRoles);

        // When
        List<UserRole> result = userRoleService.getAllUserRoles();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(userRoleMapper).findAllOrderByCreatedTimeDesc();
    }

    @Test
    public void testGetAllUserRoles_WithPagination_Success() {
        // Given
        int page = 1;
        int pageSize = 10;
        String search = null;
        
        UserRole userRole1 = new UserRole();
        userRole1.setId(1L);
        userRole1.setUsername("user1");
        userRole1.setRole(Role.ADMIN);

        UserRole userRole2 = new UserRole();
        userRole2.setId(2L);
        userRole2.setUsername("user2");
        userRole2.setRole(Role.OPERATOR);

        List<UserRole> expectedUserRoles = Arrays.asList(userRole1, userRole2);
        long totalCount = 2L;
        
        when(userRoleMapper.findAllByOrderByCreatedTimeDesc(0, pageSize)).thenReturn(expectedUserRoles);
        when(userRoleMapper.count()).thenReturn(totalCount);

        // When
        Page<UserRole> result = userRoleService.getAllUserRoles(page, pageSize, search);

        // Then
        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertEquals(totalCount, result.getTotalElements());
        assertEquals(page, result.getNumber() + 1);
        assertEquals(pageSize, result.getSize());
        verify(userRoleMapper).findAllByOrderByCreatedTimeDesc(0, pageSize);
        verify(userRoleMapper).count();
    }

    @Test
    public void testGetAllUserRoles_WithSearch_Success() {
        // Given
        int page = 1;
        int pageSize = 10;
        String search = "test";
        
        UserRole userRole1 = new UserRole();
        userRole1.setId(1L);
        userRole1.setUsername("testuser1");
        userRole1.setRole(Role.ADMIN);

        List<UserRole> expectedUserRoles = Arrays.asList(userRole1);
        long totalCount = 1L;
        
        when(userRoleMapper.findByUsernameContainingWithPage(search, 0, pageSize)).thenReturn(expectedUserRoles);
        when(userRoleMapper.countByUsernameContaining(search)).thenReturn(totalCount);

        // When
        Page<UserRole> result = userRoleService.getAllUserRoles(page, pageSize, search);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(totalCount, result.getTotalElements());
        assertEquals(page, result.getNumber() + 1);
        assertEquals(pageSize, result.getSize());
        verify(userRoleMapper).findByUsernameContainingWithPage(search, 0, pageSize);
        verify(userRoleMapper).countByUsernameContaining(search);
    }

    @Test
    public void testGetAllUserRoles_WithEmptySearch_Success() {
        // Given
        int page = 1;
        int pageSize = 10;
        String search = "";
        
        UserRole userRole1 = new UserRole();
        userRole1.setId(1L);
        userRole1.setUsername("user1");
        userRole1.setRole(Role.ADMIN);

        List<UserRole> expectedUserRoles = Arrays.asList(userRole1);
        long totalCount = 1L;
        
        when(userRoleMapper.findAllByOrderByCreatedTimeDesc(0, pageSize)).thenReturn(expectedUserRoles);
        when(userRoleMapper.count()).thenReturn(totalCount);

        // When
        Page<UserRole> result = userRoleService.getAllUserRoles(page, pageSize, search);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(totalCount, result.getTotalElements());
        assertEquals(page, result.getNumber() + 1);
        assertEquals(pageSize, result.getSize());
        verify(userRoleMapper).findAllByOrderByCreatedTimeDesc(0, pageSize);
        verify(userRoleMapper).count();
    }

    @Test
    public void testSaveUserRole_Success_UpdateUserRole() {
        // Given
        UserRole existingUserRole = new UserRole();
        existingUserRole.setId(1L);
        existingUserRole.setUsername("updateduser");
        existingUserRole.setRole(Role.OPERATOR);

        // 模拟查找不到其他相同用户名和角色的记录
        when(userRoleMapper.findByUsernameAndRole("updateduser", Role.OPERATOR.toString()))
            .thenReturn(null);
        when(userRoleMapper.insert(any(UserRole.class))).thenReturn(1);

        // When
        UserRole result = userRoleService.save(existingUserRole);

        // Then
        assertNotNull(result);
        assertEquals("updateduser", result.getUsername());
        assertEquals(Role.OPERATOR, result.getRole());
        verify(userRoleMapper).findByUsernameAndRole("updateduser", Role.OPERATOR.toString());
        verify(userRoleMapper).insert(existingUserRole);
    }

    @Test
    public void testSaveUserRole_UpdateWithDuplicateRole() {
        // Given
        UserRole existingUserRole = new UserRole();
        existingUserRole.setId(1L);
        existingUserRole.setUsername("updateduser");
        existingUserRole.setRole(Role.OPERATOR);

        UserRole duplicateUserRole = new UserRole();
        duplicateUserRole.setId(2L); // 不同的ID
        duplicateUserRole.setUsername("updateduser");
        duplicateUserRole.setRole(Role.OPERATOR);

        // 模拟找到了其他相同用户名和角色的记录
        when(userRoleMapper.findByUsernameAndRole("updateduser", Role.OPERATOR.toString()))
            .thenReturn(duplicateUserRole);

        // When & Then - 应该抛出异常
        try {
            userRoleService.save(existingUserRole);
            assertTrue("Expected IllegalArgumentException", false);
        } catch (IllegalArgumentException e) {
            // Expected exception
        }
    }

    @Test
    public void testSaveUserRole_EmptyUsername() {
        // Given
        UserRole userRole = new UserRole();
        userRole.setUsername("");
        userRole.setRole(Role.ADMIN);

        // When & Then - 应该抛出异常
        try {
            userRoleService.save(userRole);
            assertTrue("Expected IllegalArgumentException", false);
        } catch (IllegalArgumentException e) {
            // Expected exception
        }
    }

    @Test
    public void testSaveUserRole_NullRole() {
        // Given
        UserRole userRole = new UserRole();
        userRole.setUsername("testuser");
        userRole.setRole(null);

        // When & Then - 应该抛出异常
        try {
            userRoleService.save(userRole);
            assertTrue("Expected IllegalArgumentException", false);
        } catch (IllegalArgumentException e) {
            // Expected exception
        }
    }

    @Test
    public void testFindById_Success() {
        // Given
        Long id = 1L;
        when(userRoleMapper.findById(id)).thenReturn(testUserRole);

        // When
        Optional<UserRole> result = userRoleService.findById(id);

        // Then
        assertTrue(result.isPresent());
        assertEquals(testUserRole.getId(), result.get().getId());
        assertEquals(testUserRole.getUsername(), result.get().getUsername());
        verify(userRoleMapper).findById(id);
    }

    @Test
    public void testFindById_NotFound() {
        // Given
        Long id = 999L;
        when(userRoleMapper.findById(id)).thenReturn(null);

        // When
        Optional<UserRole> result = userRoleService.findById(id);

        // Then
        assertFalse(result.isPresent());
        verify(userRoleMapper).findById(id);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFindById_NullId() {
        // When & Then - 应该抛出异常
        userRoleService.findById(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDeleteById_NullId() {
        // When & Then - 应该抛出异常
        userRoleService.deleteById(null);
    }

    @Test
    public void testGetUserRolesByRole_Success() {
        // Given
        Role role = Role.ADMIN;
        when(userRoleMapper.findByRole(role.toString())).thenReturn(testUserRoles);

        // When
        List<UserRole> result = userRoleService.getUserRolesByRole(role);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(Role.ADMIN, result.get(0).getRole());
        verify(userRoleMapper).findByRole(role.toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetUserRolesByRole_NullRole() {
        // When & Then - 应该抛出异常
        userRoleService.getUserRolesByRole(null);
    }

    @Test
    public void testGetExecutorUserCount_Success() {
        // Given
        long expectedCount = 5L;
        when(userRoleMapper.countByRole(Role.EXECUTOR.toString())).thenReturn(expectedCount);

        // When
        long result = userRoleService.getExecutorUserCount();

        // Then
        assertEquals(expectedCount, result);
        verify(userRoleMapper).countByRole(Role.EXECUTOR.toString());
    }

    @Test
    public void testHasRole_NullUsername() {
        // When
        boolean result = userRoleService.hasRole(null, Role.ADMIN);

        // Then
        assertFalse(result);
        verify(userRoleMapper, never()).existsByUsernameAndRole(anyString(), anyString());
    }

    @Test
    public void testGetUserRoles_UsernameWithSpaces() {
        // Given
        String usernameWithSpaces = "  testuser  ";
        String trimmedUsername = "testuser";
        when(userRoleMapper.findByUsernameOrderByCreatedTimeDesc(trimmedUsername)).thenReturn(testUserRoles);

        // When
        List<UserRole> result = userRoleService.getUserRoles(usernameWithSpaces);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(userRoleMapper).findByUsernameOrderByCreatedTimeDesc(trimmedUsername);
    }

    @Test
    public void testGetUserRoleEnums_UsernameWithSpaces() {
        // Given
        String usernameWithSpaces = "  testuser  ";
        String trimmedUsername = "testuser";
        List<Role> expectedRoles = Arrays.asList(Role.ADMIN);
        when(userRoleMapper.findRolesByUsername(trimmedUsername)).thenReturn(expectedRoles);

        // When
        List<Role> result = userRoleService.getUserRoleEnums(usernameWithSpaces);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(userRoleMapper).findRolesByUsername(trimmedUsername);
    }

    @Test
    public void testSaveUserRole_UsernameWithSpaces() {
        // Given
        UserRole userRole = new UserRole();
        userRole.setUsername("  testuser  ");
        userRole.setRole(Role.ADMIN);

        when(userRoleMapper.existsByUsernameAndRole("testuser", Role.ADMIN.toString())).thenReturn(false);
        when(userRoleMapper.insert(any(UserRole.class))).thenReturn(1);

        // When
        UserRole result = userRoleService.save(userRole);

        // Then
        assertNotNull(result);
        assertEquals("testuser", result.getUsername()); // 应该被trim
        verify(userRoleMapper).existsByUsernameAndRole("testuser", Role.ADMIN.toString());
    }
}
