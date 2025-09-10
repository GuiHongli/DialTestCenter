/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.dialtest.center.entity;

import org.junit.Test;
import org.junit.Before;

import java.time.LocalDateTime;

import static org.junit.Assert.*;

/**
 * User实体类测试
 * 测试用户实体的基本功能，包括构造函数、getter/setter、equals/hashCode/toString方法
 *
 * @author g00940940
 * @since 2025-09-09
 */
public class UserTest {
    private User user;
    private LocalDateTime testTime;

    @Before
    public void setUp() {
        testTime = LocalDateTime.now();
        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setPassword("password123");
        user.setLastLoginTime(testTime);
        user.setCreatedTime(testTime);
        user.setUpdatedTime(testTime);
    }

    @Test
    public void testDefaultConstructor() {
        User newUser = new User();
        assertNotNull(newUser);
        assertNull(newUser.getId());
        assertNull(newUser.getUsername());
        assertNull(newUser.getPassword());
        assertNull(newUser.getLastLoginTime());
        assertNull(newUser.getCreatedTime());
        assertNull(newUser.getUpdatedTime());
    }

    @Test
    public void testParameterizedConstructor() {
        User newUser = new User("newuser", "newpassword");
        assertNotNull(newUser);
        assertNull(newUser.getId());
        assertEquals("newuser", newUser.getUsername());
        assertEquals("newpassword", newUser.getPassword());
        assertNull(newUser.getLastLoginTime());
        assertNotNull(newUser.getCreatedTime());
        assertNull(newUser.getUpdatedTime());
    }

    @Test
    public void testGettersAndSetters() {
        // Test ID
        assertEquals(Long.valueOf(1L), user.getId());
        user.setId(2L);
        assertEquals(Long.valueOf(2L), user.getId());

        // Test Username
        assertEquals("testuser", user.getUsername());
        user.setUsername("newuser");
        assertEquals("newuser", user.getUsername());

        // Test Password
        assertEquals("password123", user.getPassword());
        user.setPassword("newpassword");
        assertEquals("newpassword", user.getPassword());

        // Test Last Login Time
        assertEquals(testTime, user.getLastLoginTime());
        LocalDateTime newTime = LocalDateTime.now().plusHours(1);
        user.setLastLoginTime(newTime);
        assertEquals(newTime, user.getLastLoginTime());

        // Test Created Time
        assertEquals(testTime, user.getCreatedTime());
        LocalDateTime newCreatedTime = LocalDateTime.now().plusHours(2);
        user.setCreatedTime(newCreatedTime);
        assertEquals(newCreatedTime, user.getCreatedTime());

        // Test Updated Time
        assertEquals(testTime, user.getUpdatedTime());
        LocalDateTime newUpdatedTime = LocalDateTime.now().plusHours(3);
        user.setUpdatedTime(newUpdatedTime);
        assertEquals(newUpdatedTime, user.getUpdatedTime());
    }

    @Test
    public void testEquals() {
        User user1 = new User();
        user1.setId(1L);
        user1.setUsername("testuser");

        User user2 = new User();
        user2.setId(1L);
        user2.setUsername("testuser");

        User user3 = new User();
        user3.setId(2L);
        user3.setUsername("testuser");

        // Same ID should be equal
        assertTrue(user1.equals(user2));
        assertTrue(user2.equals(user1));

        // Different ID should not be equal
        assertFalse(user1.equals(user3));
        assertFalse(user3.equals(user1));

        // Same object should be equal
        assertTrue(user1.equals(user1));

        // Null should not be equal
        assertFalse(user1.equals(null));

        // Different class should not be equal
        assertFalse(user1.equals("string"));
    }

    @Test
    public void testHashCode() {
        User user1 = new User();
        user1.setId(1L);

        User user2 = new User();
        user2.setId(1L);

        User user3 = new User();
        user3.setId(2L);

        // Same ID should have same hashCode
        assertEquals(user1.hashCode(), user2.hashCode());

        // hashCode should be consistent
        assertEquals(user1.hashCode(), user1.hashCode());
        assertEquals(user3.hashCode(), user3.hashCode());
    }

    @Test
    public void testToString() {
        String toString = user.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("User{"));
        assertTrue(toString.contains("id=1"));
        assertTrue(toString.contains("username='testuser'"));
        assertTrue(toString.contains("lastLoginTime=" + testTime));
        assertTrue(toString.contains("createdTime=" + testTime));
        assertTrue(toString.contains("updatedTime=" + testTime));
    }

    @Test
    public void testNullValues() {
        User nullUser = new User();
        assertNull(nullUser.getId());
        assertNull(nullUser.getUsername());
        assertNull(nullUser.getPassword());
        assertNull(nullUser.getLastLoginTime());
        assertNull(nullUser.getCreatedTime());
        assertNull(nullUser.getUpdatedTime());
    }

    @Test
    public void testEmptyUsername() {
        user.setUsername("");
        assertEquals("", user.getUsername());
    }

    @Test
    public void testEmptyPassword() {
        user.setPassword("");
        assertEquals("", user.getPassword());
    }
}
