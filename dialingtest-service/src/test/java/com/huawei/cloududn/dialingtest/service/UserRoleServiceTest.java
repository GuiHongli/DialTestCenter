/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.cloududn.dialingtest.service;

import com.huawei.cloududn.dialingtest.dao.RoleDao;
import com.huawei.cloududn.dialingtest.dao.UserRoleDao;
import com.huawei.cloududn.dialingtest.model.*;
import com.huawei.cloududn.dialingtest.util.UserRoleOperationLogUtil;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 用户角色管理服务测试类
 *
 * @author g00940940
 * @since 2025-01-27
 */
@RunWith(MockitoJUnitRunner.class)
public class UserRoleServiceTest {

    @Mock
    private UserRoleDao userRoleDao;

    @Mock
    private RoleDao roleDao;

    @Mock
    private UserRoleOperationLogUtil operationLogUtil;

    @InjectMocks
    private UserRoleService userRoleService;

    private UserRole testUserRole;
    private Role testRole;
    private CreateUserRoleRequest testRequest;

    @Before
    public void setUp() {
        // 初始化测试数据
        testUserRole = new UserRole();
        testUserRole.setId(1);
        testUserRole.setUsername("testuser");
        testUserRole.setRole(UserRole.RoleEnum.ADMIN);

        testRole = new Role();
        testRole.setId(1);
        testRole.setCode("ADMIN");
        testRole.setNameZh("管理员");
        testRole.setNameEn("Administrator");

        testRequest = new CreateUserRoleRequest();
        testRequest.setUsername("testuser");
        testRequest.setRole(CreateUserRoleRequest.RoleEnum.ADMIN);
    }

    @Test
    public void testCreateUserRole_Success_ReturnsUserRole() {
        // Arrange
        when(userRoleDao.existsByUsernameAndRole("testuser", "ADMIN")).thenReturn(false);
        when(userRoleDao.insert(any(UserRole.class))).thenReturn(1);

        // Act
        UserRole result = userRoleService.createUserRole("testuser", "ADMIN", "admin");

        // Assert
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        assertEquals(UserRole.RoleEnum.ADMIN, result.getRole());
        verify(userRoleDao).existsByUsernameAndRole("testuser", "ADMIN");
        verify(userRoleDao).insert(any(UserRole.class));
        verify(operationLogUtil).logUserRoleCreate("admin", "testuser", "ADMIN");
    }

    @Test
    public void testCreateUserRole_InvalidRole_ThrowsException() {
        // Act & Assert
        try {
            userRoleService.createUserRole("testuser", "INVALID", "admin");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("无效的角色: INVALID", e.getMessage());
        }

        verify(userRoleDao, never()).existsByUsernameAndRole(anyString(), anyString());
        verify(userRoleDao, never()).insert(any(UserRole.class));
        verify(operationLogUtil, never()).logUserRoleCreate(anyString(), anyString(), anyString());
    }

    @Test
    public void testCreateUserRole_AlreadyExists_ThrowsException() {
        // Arrange
        when(userRoleDao.existsByUsernameAndRole("testuser", "ADMIN")).thenReturn(true);

        // Act & Assert
        try {
            userRoleService.createUserRole("testuser", "ADMIN", "admin");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("用户角色关系已存在", e.getMessage());
        }

        verify(userRoleDao).existsByUsernameAndRole("testuser", "ADMIN");
        verify(userRoleDao, never()).insert(any(UserRole.class));
        verify(operationLogUtil, never()).logUserRoleCreate(anyString(), anyString(), anyString());
    }

    @Test
    public void testGetUserRolesWithPagination_Success_ReturnsPageData() {
        // Arrange
        List<UserRole> userRoles = Arrays.asList(testUserRole);
        when(userRoleDao.findByUsernameContainingIgnoreCase("test", 0, 10)).thenReturn(userRoles);
        when(userRoleDao.countByUsernameContainingIgnoreCase("test")).thenReturn(1);

        // Act
        UserRolePageResponseData result = userRoleService.getUserRolesWithPagination(0, 10, "test");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(Integer.valueOf(1), result.getTotalElements());
        assertEquals(Integer.valueOf(1), result.getTotalPages());
        assertEquals(Integer.valueOf(0), result.getNumber());
        assertEquals(Integer.valueOf(10), result.getSize());
        assertTrue(result.isFirst());
        assertTrue(result.isLast());
        verify(userRoleDao).findByUsernameContainingIgnoreCase("test", 0, 10);
        verify(userRoleDao).countByUsernameContainingIgnoreCase("test");
    }

    @Test
    public void testGetUserRolesWithPagination_NoSearch_ReturnsAllData() {
        // Arrange
        List<UserRole> userRoles = Arrays.asList(testUserRole);
        when(userRoleDao.findByUsernameContainingIgnoreCase(null, 0, 10)).thenReturn(userRoles);
        when(userRoleDao.countByUsernameContainingIgnoreCase(null)).thenReturn(1);

        // Act
        UserRolePageResponseData result = userRoleService.getUserRolesWithPagination(0, 10, null);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(userRoleDao).findByUsernameContainingIgnoreCase(null, 0, 10);
        verify(userRoleDao).countByUsernameContainingIgnoreCase(null);
    }

    @Test
    public void testGetUserRolesByUsername_Success_ReturnsRoleList() {
        // Arrange
        List<UserRole> userRoles = Arrays.asList(testUserRole);
        when(userRoleDao.findByUsername("testuser")).thenReturn(userRoles);

        // Act
        List<String> result = userRoleService.getUserRolesByUsername("testuser");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("ADMIN", result.get(0));
        verify(userRoleDao).findByUsername("testuser");
    }

    @Test
    public void testGetUserRolesByUsername_NoRoles_ReturnsEmptyList() {
        // Arrange
        when(userRoleDao.findByUsername("testuser")).thenReturn(Arrays.asList());

        // Act
        List<String> result = userRoleService.getUserRolesByUsername("testuser");

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(userRoleDao).findByUsername("testuser");
    }

    @Test
    public void testHasAnyRole_UserHasRole_ReturnsTrue() {
        // Arrange
        List<String> roles = Arrays.asList("ADMIN", "OPERATOR");
        when(userRoleDao.existsByUsernameAndRoleIn("testuser", roles)).thenReturn(true);

        // Act
        boolean result = userRoleService.hasAnyRole("testuser", roles);

        // Assert
        assertTrue(result);
        verify(userRoleDao).existsByUsernameAndRoleIn("testuser", roles);
    }

    @Test
    public void testHasAnyRole_UserHasNoRole_ReturnsFalse() {
        // Arrange
        List<String> roles = Arrays.asList("ADMIN", "OPERATOR");
        when(userRoleDao.existsByUsernameAndRoleIn("testuser", roles)).thenReturn(false);

        // Act
        boolean result = userRoleService.hasAnyRole("testuser", roles);

        // Assert
        assertFalse(result);
        verify(userRoleDao).existsByUsernameAndRoleIn("testuser", roles);
    }

    @Test
    public void testHasRole_UserHasRole_ReturnsTrue() {
        // Arrange
        when(userRoleDao.existsByUsernameAndRole("testuser", "ADMIN")).thenReturn(true);

        // Act
        boolean result = userRoleService.hasRole("testuser", "ADMIN");

        // Assert
        assertTrue(result);
        verify(userRoleDao).existsByUsernameAndRole("testuser", "ADMIN");
    }

    @Test
    public void testHasRole_UserHasNoRole_ReturnsFalse() {
        // Arrange
        when(userRoleDao.existsByUsernameAndRole("testuser", "ADMIN")).thenReturn(false);

        // Act
        boolean result = userRoleService.hasRole("testuser", "ADMIN");

        // Assert
        assertFalse(result);
        verify(userRoleDao).existsByUsernameAndRole("testuser", "ADMIN");
    }

    @Test
    public void testUpdateUserRole_Success_ReturnsUpdatedUserRole() {
        // Arrange
        UserRole existingUserRole = new UserRole();
        existingUserRole.setId(1);
        existingUserRole.setUsername("testuser");
        existingUserRole.setRole(UserRole.RoleEnum.OPERATOR);

        when(userRoleDao.findById(1)).thenReturn(existingUserRole);
        when(userRoleDao.update(any(UserRole.class))).thenReturn(1);

        // Act
        UserRole result = userRoleService.updateUserRole(1, "testuser", "ADMIN", "admin");

        // Assert
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        assertEquals(UserRole.RoleEnum.ADMIN, result.getRole());
        verify(userRoleDao).findById(1);
        verify(userRoleDao).update(any(UserRole.class));
        verify(operationLogUtil).logUserRoleUpdate("admin", "testuser", "testuser", "OPERATOR", "ADMIN");
    }

    @Test
    public void testUpdateUserRole_InvalidRole_ThrowsException() {
        // Arrange
        UserRole existingUserRole = new UserRole();
        existingUserRole.setId(1);
        existingUserRole.setUsername("testuser");
        existingUserRole.setRole(UserRole.RoleEnum.OPERATOR);

        when(userRoleDao.findById(1)).thenReturn(existingUserRole);

        // Act & Assert
        try {
            userRoleService.updateUserRole(1, "testuser", "INVALID", "admin");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("无效的角色: INVALID", e.getMessage());
        }

        verify(userRoleDao).findById(1);
        verify(userRoleDao, never()).update(any(UserRole.class));
        verify(operationLogUtil, never()).logUserRoleUpdate(anyString(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    public void testUpdateUserRole_NotFound_ThrowsException() {
        // Arrange
        when(userRoleDao.findById(1)).thenReturn(null);

        // Act & Assert
        try {
            userRoleService.updateUserRole(1, "testuser", "ADMIN", "admin");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("用户角色关系不存在", e.getMessage());
        }

        verify(userRoleDao).findById(1);
        verify(userRoleDao, never()).update(any(UserRole.class));
        verify(operationLogUtil, never()).logUserRoleUpdate(anyString(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    public void testDeleteUserRole_Success_DeletesUserRole() {
        // Arrange
        UserRole existingUserRole = new UserRole();
        existingUserRole.setId(1);
        existingUserRole.setUsername("testuser");
        existingUserRole.setRole(UserRole.RoleEnum.ADMIN);

        when(userRoleDao.findById(1)).thenReturn(existingUserRole);
        when(userRoleDao.deleteById(1)).thenReturn(1);

        // Act
        userRoleService.deleteUserRole(1, "admin");

        // Assert
        verify(userRoleDao).findById(1);
        verify(userRoleDao).deleteById(1);
        verify(operationLogUtil).logUserRoleDelete("admin", "testuser", "ADMIN");
    }

    @Test
    public void testDeleteUserRole_NotFound_ThrowsException() {
        // Arrange
        when(userRoleDao.findById(1)).thenReturn(null);

        // Act & Assert
        try {
            userRoleService.deleteUserRole(1, "admin");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("用户角色关系不存在", e.getMessage());
        }

        verify(userRoleDao).findById(1);
        verify(userRoleDao, never()).deleteById(anyInt());
        verify(operationLogUtil, never()).logUserRoleDelete(anyString(), anyString(), anyString());
    }

    @Test
    public void testGetAllRoles_Success_ReturnsRoleList() {
        // Arrange
        List<Role> roles = Arrays.asList(testRole);
        when(roleDao.findAll()).thenReturn(roles);

        // Act
        List<Role> result = userRoleService.getAllRoles();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("ADMIN", result.get(0).getCode());
        verify(roleDao).findAll();
    }
    
    @Test
    public void testGetExecutorCount_Success_ReturnsCount() {
        // Arrange
        when(userRoleDao.countByRole("EXECUTOR")).thenReturn(5);

        // Act
        int result = userRoleService.getExecutorCount();

        // Assert
        assertEquals(5, result);
        verify(userRoleDao).countByRole("EXECUTOR");
    }
    
    @Test
    public void testGetExecutorCount_NoExecutors_ReturnsZero() {
        // Arrange
        when(userRoleDao.countByRole("EXECUTOR")).thenReturn(0);

        // Act
        int result = userRoleService.getExecutorCount();

        // Assert
        assertEquals(0, result);
        verify(userRoleDao).countByRole("EXECUTOR");
    }
}
