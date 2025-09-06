/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.dialtest.center.entity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.time.LocalDateTime;

import org.junit.Before;
import org.junit.Test;

/**
 * 用户角色实体测试类，测试UserRole实体的各种功能
 * 包括构造函数、getter/setter、equals/hashCode、toString等方法
 * 验证用户角色关系的数据完整性和业务逻辑正确性
 * 
 * @author g00940940
 * @since 2025-09-06
 */
public class UserRoleTest {

    private UserRole userRole1;
    private UserRole userRole2;
    private UserRole userRole3;

    @Before
    public void setUp() {
        userRole1 = new UserRole();
        userRole1.setId(1L);
        userRole1.setUsername("user1");
        userRole1.setRole(Role.ADMIN);
        userRole1.setCreatedTime(LocalDateTime.now());
        userRole1.setUpdatedTime(LocalDateTime.now());

        userRole2 = new UserRole();
        userRole2.setId(1L);
        userRole2.setUsername("user1");
        userRole2.setRole(Role.ADMIN);
        userRole2.setCreatedTime(LocalDateTime.now());
        userRole2.setUpdatedTime(LocalDateTime.now());

        userRole3 = new UserRole();
        userRole3.setId(2L);
        userRole3.setUsername("user2");
        userRole3.setRole(Role.OPERATOR);
        userRole3.setCreatedTime(LocalDateTime.now());
        userRole3.setUpdatedTime(LocalDateTime.now());
    }

    @Test
    public void testDefaultConstructor() {
        // When
        UserRole userRole = new UserRole();

        // Then
        assertNotNull(userRole);
    }

    @Test
    public void testParameterizedConstructor() {
        // When
        UserRole userRole = new UserRole("testuser", Role.ADMIN);

        // Then
        assertNotNull(userRole);
        assertEquals("testuser", userRole.getUsername());
        assertEquals(Role.ADMIN, userRole.getRole());
    }

    @Test
    public void testGettersAndSetters() {
        // Given
        UserRole userRole = new UserRole();
        Long id = 1L;
        String username = "testuser";
        Role role = Role.OPERATOR;
        LocalDateTime createdTime = LocalDateTime.now();
        LocalDateTime updatedTime = LocalDateTime.now();

        // When
        userRole.setId(id);
        userRole.setUsername(username);
        userRole.setRole(role);
        userRole.setCreatedTime(createdTime);
        userRole.setUpdatedTime(updatedTime);

        // Then
        assertEquals(id, userRole.getId());
        assertEquals(username, userRole.getUsername());
        assertEquals(role, userRole.getRole());
        assertEquals(createdTime, userRole.getCreatedTime());
        assertEquals(updatedTime, userRole.getUpdatedTime());
    }

    @Test
    public void testEquals_SameObject() {
        // When & Then
        assertTrue(userRole1.equals(userRole1));
    }

    @Test
    public void testEquals_EqualObjects() {
        // When & Then
        assertTrue(userRole1.equals(userRole2));
    }

    @Test
    public void testEquals_DifferentObjects() {
        // When & Then
        assertFalse(userRole1.equals(userRole3));
    }

    @Test
    public void testEquals_NullObject() {
        // When & Then
        assertFalse(userRole1.equals(null));
    }

    @Test
    public void testEquals_DifferentClass() {
        // When & Then
        assertFalse(userRole1.equals("not a UserRole"));
    }

    @Test
    public void testEquals_DifferentId() {
        // Given
        UserRole userRoleWithDifferentId = new UserRole();
        userRoleWithDifferentId.setId(999L);
        userRoleWithDifferentId.setUsername("user1");
        userRoleWithDifferentId.setRole(Role.ADMIN);

        // When & Then
        assertFalse(userRole1.equals(userRoleWithDifferentId));
    }

    @Test
    public void testEquals_DifferentUsername() {
        // Given
        UserRole userRoleWithDifferentUsername = new UserRole();
        userRoleWithDifferentUsername.setId(1L);
        userRoleWithDifferentUsername.setUsername("differentuser");
        userRoleWithDifferentUsername.setRole(Role.ADMIN);

        // When & Then
        assertFalse(userRole1.equals(userRoleWithDifferentUsername));
    }

    @Test
    public void testEquals_DifferentRole() {
        // Given
        UserRole userRoleWithDifferentRole = new UserRole();
        userRoleWithDifferentRole.setId(1L);
        userRoleWithDifferentRole.setUsername("user1");
        userRoleWithDifferentRole.setRole(Role.OPERATOR);

        // When & Then
        assertFalse(userRole1.equals(userRoleWithDifferentRole));
    }

    @Test
    public void testHashCode_EqualObjects() {
        // When & Then
        assertEquals(userRole1.hashCode(), userRole2.hashCode());
    }

    @Test
    public void testHashCode_DifferentObjects() {
        // When & Then
        assertNotEquals(userRole1.hashCode(), userRole3.hashCode());
    }

    @Test
    public void testHashCode_Consistency() {
        // When
        int hashCode1 = userRole1.hashCode();
        int hashCode2 = userRole1.hashCode();

        // Then
        assertEquals(hashCode1, hashCode2);
    }

    @Test
    public void testToString() {
        // When
        String toString = userRole1.toString();

        // Then
        assertNotNull(toString);
        assertTrue(toString.contains("UserRole"));
        assertTrue(toString.contains("id=1"));
        assertTrue(toString.contains("username='user1'"));
        assertTrue(toString.contains("role=ADMIN"));
    }

    @Test
    public void testToString_WithNullValues() {
        // Given
        UserRole userRole = new UserRole();
        userRole.setId(null);
        userRole.setUsername(null);
        userRole.setRole(null);

        // When
        String toString = userRole.toString();

        // Then
        assertNotNull(toString);
        assertTrue(toString.contains("UserRole"));
        assertTrue(toString.contains("id=null"));
        assertTrue(toString.contains("username='null'"));
        assertTrue(toString.contains("role=null"));
    }

    @Test
    public void testAllRoleTypes() {
        // Test all role types
        for (Role role : Role.values()) {
            UserRole userRole = new UserRole();
            userRole.setRole(role);
            assertEquals(role, userRole.getRole());
        }
    }

    @Test
    public void testUsernameWithSpecialCharacters() {
        // Given
        String specialUsername = "user@domain.com";
        UserRole userRole = new UserRole();

        // When
        userRole.setUsername(specialUsername);

        // Then
        assertEquals(specialUsername, userRole.getUsername());
    }

    @Test
    public void testUsernameWithSpaces() {
        // Given
        String usernameWithSpaces = "  user with spaces  ";
        UserRole userRole = new UserRole();

        // When
        userRole.setUsername(usernameWithSpaces);

        // Then
        assertEquals(usernameWithSpaces, userRole.getUsername());
    }

    @Test
    public void testEmptyUsername() {
        // Given
        String emptyUsername = "";
        UserRole userRole = new UserRole();

        // When
        userRole.setUsername(emptyUsername);

        // Then
        assertEquals(emptyUsername, userRole.getUsername());
    }

    @Test
    public void testNullUsername() {
        // Given
        UserRole userRole = new UserRole();

        // When
        userRole.setUsername(null);

        // Then
        assertEquals(null, userRole.getUsername());
    }

    @Test
    public void testNullRole() {
        // Given
        UserRole userRole = new UserRole();

        // When
        userRole.setRole(null);

        // Then
        assertEquals(null, userRole.getRole());
    }

    @Test
    public void testNullId() {
        // Given
        UserRole userRole = new UserRole();

        // When
        userRole.setId(null);

        // Then
        assertEquals(null, userRole.getId());
    }

    @Test
    public void testNullTimestamps() {
        // Given
        UserRole userRole = new UserRole();

        // When
        userRole.setCreatedTime(null);
        userRole.setUpdatedTime(null);

        // Then
        assertEquals(null, userRole.getCreatedTime());
        assertEquals(null, userRole.getUpdatedTime());
    }
}
