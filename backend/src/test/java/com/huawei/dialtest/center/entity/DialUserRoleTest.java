/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.dialtest.center.entity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.time.LocalDateTime;

import org.junit.Before;
import org.junit.Test;

import com.huawei.dialtest.center.entity.Role;
import com.huawei.dialtest.center.entity.DialUserRole;

/**
 * 用户角色实体测试类，测试UserRole实体的各种功能
 * 包括构造函数、getter/setter、equals/hashCode、toString等方法
 * 验证用户角色关系的数据完整性和业务逻辑正确性
 * 
 * @author g00940940
 * @since 2025-09-06
 */
public class DialUserRoleTest {

    private DialUserRole userRole1;
    private DialUserRole userRole2;
    private DialUserRole userRole3;

    @Before
    public void setUp() {
        userRole1 = new DialUserRole();
        userRole1.setId(1L);
        userRole1.setUsername("user1");
        userRole1.setRole(Role.ADMIN);

        userRole2 = new DialUserRole();
        userRole2.setId(1L);
        userRole2.setUsername("user1");
        userRole2.setRole(Role.ADMIN);

        userRole3 = new DialUserRole();
        userRole3.setId(2L);
        userRole3.setUsername("user2");
        userRole3.setRole(Role.OPERATOR);
    }

    @Test
    public void testDefaultConstructor() {
        // When
        DialUserRole userRole = new DialUserRole();

        // Then
        assertNotNull(userRole);
    }

    @Test
    public void testParameterizedConstructor() {
        // When
        DialUserRole userRole = new DialUserRole("testuser", Role.ADMIN);

        // Then
        assertNotNull(userRole);
        assertEquals("testuser", userRole.getUsername());
        assertEquals(Role.ADMIN, userRole.getRole());
    }

    @Test
    public void testGettersAndSetters() {
        // Given
        DialUserRole userRole = new DialUserRole();
        Long id = 1L;
        String username = "testuser";
        Role role = Role.OPERATOR;

        // When
        userRole.setId(id);
        userRole.setUsername(username);
        userRole.setRole(role);

        // Then
        assertEquals(id, userRole.getId());
        assertEquals(username, userRole.getUsername());
        assertEquals(role, userRole.getRole());
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
        assertFalse(userRole1.equals("not a DialUserRole"));
    }

    @Test
    public void testEquals_DifferentId() {
        // Given
        DialUserRole userRoleWithDifferentId = new DialUserRole();
        userRoleWithDifferentId.setId(999L);
        userRoleWithDifferentId.setUsername("user1");
        userRoleWithDifferentId.setRole(Role.ADMIN);

        // When & Then
        assertFalse(userRole1.equals(userRoleWithDifferentId));
    }

    @Test
    public void testEquals_DifferentUsername() {
        // Given
        DialUserRole userRoleWithDifferentUsername = new DialUserRole();
        userRoleWithDifferentUsername.setId(1L);
        userRoleWithDifferentUsername.setUsername("differentuser");
        userRoleWithDifferentUsername.setRole(Role.ADMIN);

        // When & Then
        assertFalse(userRole1.equals(userRoleWithDifferentUsername));
    }

    @Test
    public void testEquals_DifferentRole() {
        // Given
        DialUserRole userRoleWithDifferentRole = new DialUserRole();
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
        assertTrue(toString.contains("DialUserRole"));
        assertTrue(toString.contains("id=1"));
        assertTrue(toString.contains("username='user1'"));
        assertTrue(toString.contains("role=ADMIN"));
    }

    @Test
    public void testToString_WithNullValues() {
        // Given
        DialUserRole userRole = new DialUserRole();
        userRole.setId(null);
        userRole.setUsername(null);
        userRole.setRole(null);

        // When
        String toString = userRole.toString();

        // Then
        assertNotNull(toString);
        assertTrue(toString.contains("DialUserRole"));
        assertTrue(toString.contains("id=null"));
        assertTrue(toString.contains("username='null'"));
        assertTrue(toString.contains("role=null"));
    }

    @Test
    public void testAllRoleTypes() {
        // Test all role types
        for (Role role : Role.values()) {
            DialUserRole userRole = new DialUserRole();
            userRole.setRole(role);
            assertEquals(role, userRole.getRole());
        }
    }

    @Test
    public void testUsernameWithSpecialCharacters() {
        // Given
        String specialUsername = "user@domain.com";
        DialUserRole userRole = new DialUserRole();

        // When
        userRole.setUsername(specialUsername);

        // Then
        assertEquals(specialUsername, userRole.getUsername());
    }

    @Test
    public void testUsernameWithSpaces() {
        // Given
        String usernameWithSpaces = "  user with spaces  ";
        DialUserRole userRole = new DialUserRole();

        // When
        userRole.setUsername(usernameWithSpaces);

        // Then
        assertEquals(usernameWithSpaces, userRole.getUsername());
    }

    @Test
    public void testEmptyUsername() {
        // Given
        String emptyUsername = "";
        DialUserRole userRole = new DialUserRole();

        // When
        userRole.setUsername(emptyUsername);

        // Then
        assertEquals(emptyUsername, userRole.getUsername());
    }

    @Test
    public void testNullUsername() {
        // Given
        DialUserRole userRole = new DialUserRole();

        // When
        userRole.setUsername(null);

        // Then
        assertEquals(null, userRole.getUsername());
    }

    @Test
    public void testNullRole() {
        // Given
        DialUserRole userRole = new DialUserRole();

        // When
        userRole.setRole(null);

        // Then
        assertEquals(null, userRole.getRole());
    }

    @Test
    public void testNullId() {
        // Given
        DialUserRole userRole = new DialUserRole();

        // When
        userRole.setId(null);

        // Then
        assertEquals(null, userRole.getId());
    }

    @Test
    public void testNullValues() {
        // Given
        DialUserRole userRole = new DialUserRole();

        // When
        userRole.setId(null);
        userRole.setUsername(null);
        userRole.setRole(null);

        // Then
        assertEquals(null, userRole.getId());
        assertEquals(null, userRole.getUsername());
        assertEquals(null, userRole.getRole());
    }
}
