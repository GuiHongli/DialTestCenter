package com.dialtest.center.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
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

import com.dialtest.center.entity.Role;
import com.dialtest.center.entity.UserRole;
import com.dialtest.center.repository.UserRoleRepository;

/**
 * UserRoleService测试类
 */
@RunWith(MockitoJUnitRunner.class)
public class UserRoleServiceTest {

    @Mock
    private UserRoleRepository userRoleRepository;

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
        when(userRoleRepository.findByUsernameOrderByCreatedTimeDesc(username)).thenReturn(testUserRoles);

        // When
        List<UserRole> result = userRoleService.getUserRoles(username);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(username, result.get(0).getUsername());
        assertEquals(Role.ADMIN, result.get(0).getRole());
        verify(userRoleRepository).findByUsernameOrderByCreatedTimeDesc(username);
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
        when(userRoleRepository.findByUsernameOrderByCreatedTimeDesc(username)).thenReturn(Collections.emptyList());

        // When
        List<UserRole> result = userRoleService.getUserRoles(username);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(userRoleRepository).findByUsernameOrderByCreatedTimeDesc(username);
    }

    @Test
    public void testSaveUserRole_Success_NewUserRole() {
        // Given
        UserRole newUserRole = new UserRole();
        newUserRole.setUsername("newuser");
        newUserRole.setRole(Role.OPERATOR);
        // ID为null表示新建
        
        when(userRoleRepository.existsByUsernameAndRole(anyString(), any(Role.class))).thenReturn(false);
        when(userRoleRepository.save(any(UserRole.class))).thenReturn(newUserRole);

        // When
        UserRole result = userRoleService.save(newUserRole);

        // Then
        assertNotNull(result);
        assertEquals("newuser", result.getUsername());
        assertEquals(Role.OPERATOR, result.getRole());
        verify(userRoleRepository).existsByUsernameAndRole("newuser", Role.OPERATOR);
        verify(userRoleRepository).save(newUserRole);
    }

    @Test
    public void testSaveUserRole_DuplicateRole() {
        // Given
        UserRole duplicateUserRole = new UserRole();
        duplicateUserRole.setUsername("testuser");
        duplicateUserRole.setRole(Role.ADMIN);
        
        when(userRoleRepository.existsByUsernameAndRole(anyString(), any(Role.class))).thenReturn(true);

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
        when(userRoleRepository.existsById(id)).thenReturn(true);

        // When
        userRoleService.deleteById(id);

        // Then
        verify(userRoleRepository).existsById(id);
        verify(userRoleRepository).deleteById(id);
    }

    @Test
    public void testDeleteById_IDNotExist() {
        // Given
        Long id = 999L;
        when(userRoleRepository.existsById(id)).thenReturn(false);

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
        when(userRoleRepository.existsByRole(Role.ADMIN)).thenReturn(true);

        // When
        boolean result = userRoleService.hasAdminUser();

        // Then
        assertTrue(result);
        verify(userRoleRepository).existsByRole(Role.ADMIN);
    }

    @Test
    public void testHasAdminUser_NoAdmin() {
        // Given
        when(userRoleRepository.existsByRole(Role.ADMIN)).thenReturn(false);

        // When
        boolean result = userRoleService.hasAdminUser();

        // Then
        assertFalse(result);
        verify(userRoleRepository).existsByRole(Role.ADMIN);
    }

    @Test
    public void testHasRole_UserHasRole() {
        // Given
        String username = "testuser";
        Role role = Role.ADMIN;
        when(userRoleRepository.existsByUsernameAndRole(username, role)).thenReturn(true);

        // When
        boolean result = userRoleService.hasRole(username, role);

        // Then
        assertTrue(result);
        verify(userRoleRepository).existsByUsernameAndRole(username, role);
    }

    @Test
    public void testHasRole_UserNotHasRole() {
        // Given
        String username = "testuser";
        Role role = Role.OPERATOR;
        when(userRoleRepository.existsByUsernameAndRole(username, role)).thenReturn(false);

        // When
        boolean result = userRoleService.hasRole(username, role);

        // Then
        assertFalse(result);
        verify(userRoleRepository).existsByUsernameAndRole(username, role);
    }

    @Test
    public void testHasRole_EmptyUsername() {
        // When
        boolean result = userRoleService.hasRole("", Role.ADMIN);

        // Then
        assertFalse(result);
        verify(userRoleRepository, never()).existsByUsernameAndRole(anyString(), any(Role.class));
    }

    @Test
    public void testHasRole_NullRole() {
        // When
        boolean result = userRoleService.hasRole("testuser", null);

        // Then
        assertFalse(result);
        verify(userRoleRepository, never()).existsByUsernameAndRole(anyString(), any(Role.class));
    }

    // ========== 新增测试用例以提高覆盖率 ==========

    @Test
    public void testGetUserRoleEnums_Success() {
        // Given
        String username = "testuser";
        List<Role> expectedRoles = Arrays.asList(Role.ADMIN, Role.OPERATOR);
        when(userRoleRepository.findRolesByUsername(username)).thenReturn(expectedRoles);

        // When
        List<Role> result = userRoleService.getUserRoleEnums(username);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(Role.ADMIN));
        assertTrue(result.contains(Role.OPERATOR));
        verify(userRoleRepository).findRolesByUsername(username);
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
        when(userRoleRepository.findAllOrderByCreatedTimeDesc()).thenReturn(expectedUserRoles);

        // When
        List<UserRole> result = userRoleService.getAllUserRoles();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(userRoleRepository).findAllOrderByCreatedTimeDesc();
    }

    @Test
    public void testSaveUserRole_Success_UpdateUserRole() {
        // Given
        UserRole existingUserRole = new UserRole();
        existingUserRole.setId(1L);
        existingUserRole.setUsername("updateduser");
        existingUserRole.setRole(Role.OPERATOR);
        
        // 模拟查找不到其他相同用户名和角色的记录
        when(userRoleRepository.findByUsernameAndRole("updateduser", Role.OPERATOR))
            .thenReturn(Optional.empty());
        when(userRoleRepository.save(any(UserRole.class))).thenReturn(existingUserRole);

        // When
        UserRole result = userRoleService.save(existingUserRole);

        // Then
        assertNotNull(result);
        assertEquals("updateduser", result.getUsername());
        assertEquals(Role.OPERATOR, result.getRole());
        verify(userRoleRepository).findByUsernameAndRole("updateduser", Role.OPERATOR);
        verify(userRoleRepository).save(existingUserRole);
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
        when(userRoleRepository.findByUsernameAndRole("updateduser", Role.OPERATOR))
            .thenReturn(Optional.of(duplicateUserRole));

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
        when(userRoleRepository.findById(id)).thenReturn(Optional.of(testUserRole));

        // When
        Optional<UserRole> result = userRoleService.findById(id);

        // Then
        assertTrue(result.isPresent());
        assertEquals(testUserRole.getId(), result.get().getId());
        assertEquals(testUserRole.getUsername(), result.get().getUsername());
        verify(userRoleRepository).findById(id);
    }

    @Test
    public void testFindById_NotFound() {
        // Given
        Long id = 999L;
        when(userRoleRepository.findById(id)).thenReturn(Optional.empty());

        // When
        Optional<UserRole> result = userRoleService.findById(id);

        // Then
        assertFalse(result.isPresent());
        verify(userRoleRepository).findById(id);
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
        when(userRoleRepository.findByRole(role)).thenReturn(testUserRoles);

        // When
        List<UserRole> result = userRoleService.getUserRolesByRole(role);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(Role.ADMIN, result.get(0).getRole());
        verify(userRoleRepository).findByRole(role);
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
        when(userRoleRepository.countByRole(Role.EXECUTOR)).thenReturn(expectedCount);

        // When
        long result = userRoleService.getExecutorUserCount();

        // Then
        assertEquals(expectedCount, result);
        verify(userRoleRepository).countByRole(Role.EXECUTOR);
    }

    @Test
    public void testHasRole_NullUsername() {
        // When
        boolean result = userRoleService.hasRole(null, Role.ADMIN);

        // Then
        assertFalse(result);
        verify(userRoleRepository, never()).existsByUsernameAndRole(anyString(), any(Role.class));
    }

    @Test
    public void testGetUserRoles_UsernameWithSpaces() {
        // Given
        String usernameWithSpaces = "  testuser  ";
        String trimmedUsername = "testuser";
        when(userRoleRepository.findByUsernameOrderByCreatedTimeDesc(trimmedUsername)).thenReturn(testUserRoles);

        // When
        List<UserRole> result = userRoleService.getUserRoles(usernameWithSpaces);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(userRoleRepository).findByUsernameOrderByCreatedTimeDesc(trimmedUsername);
    }

    @Test
    public void testGetUserRoleEnums_UsernameWithSpaces() {
        // Given
        String usernameWithSpaces = "  testuser  ";
        String trimmedUsername = "testuser";
        List<Role> expectedRoles = Arrays.asList(Role.ADMIN);
        when(userRoleRepository.findRolesByUsername(trimmedUsername)).thenReturn(expectedRoles);

        // When
        List<Role> result = userRoleService.getUserRoleEnums(usernameWithSpaces);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(userRoleRepository).findRolesByUsername(trimmedUsername);
    }

    @Test
    public void testSaveUserRole_UsernameWithSpaces() {
        // Given
        UserRole userRole = new UserRole();
        userRole.setUsername("  testuser  ");
        userRole.setRole(Role.ADMIN);
        
        when(userRoleRepository.existsByUsernameAndRole("testuser", Role.ADMIN)).thenReturn(false);
        when(userRoleRepository.save(any(UserRole.class))).thenReturn(userRole);

        // When
        UserRole result = userRoleService.save(userRole);

        // Then
        assertNotNull(result);
        assertEquals("testuser", result.getUsername()); // 应该被trim
        verify(userRoleRepository).existsByUsernameAndRole("testuser", Role.ADMIN);
    }
}
