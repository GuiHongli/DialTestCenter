/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.cloududn.dialingtest.model;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * DialUser模型测试类
 *
 * @author Generated
 * @since 2025-09-18
 */
public class DialUserTest {

    @Test
    public void testDefaultConstructor_CreatesEmptyUser() {
        // Act
        DialUser user = new DialUser();

        // Assert
        assertNotNull(user);
        assertNull(user.getId());
        assertNull(user.getUsername());
        assertNull(user.getPassword());
        assertNull(user.getLastLoginTime());
    }

    @Test
    public void testSettersAndGetters_Id_SetsAndGetsCorrectly() {
        // Arrange
        DialUser user = new DialUser();
        Integer expectedId = 1;

        // Act
        user.setId(expectedId);

        // Assert
        assertEquals(expectedId, user.getId());
    }

    @Test
    public void testSettersAndGetters_Username_SetsAndGetsCorrectly() {
        // Arrange
        DialUser user = new DialUser();
        String expectedUsername = "testuser";

        // Act
        user.setUsername(expectedUsername);

        // Assert
        assertEquals(expectedUsername, user.getUsername());
    }

    @Test
    public void testSettersAndGetters_Password_SetsAndGetsCorrectly() {
        // Arrange
        DialUser user = new DialUser();
        String expectedPassword = "password123";

        // Act
        user.setPassword(expectedPassword);

        // Assert
        assertEquals(expectedPassword, user.getPassword());
    }

    @Test
    public void testSettersAndGetters_LastLoginTime_SetsAndGetsCorrectly() {
        // Arrange
        DialUser user = new DialUser();
        String expectedTime = "2025-09-18T10:30:00Z";

        // Act
        user.setLastLoginTime(expectedTime);

        // Assert
        assertEquals(expectedTime, user.getLastLoginTime());
    }

    @Test
    public void testFluentSetters_Id_ReturnsThis() {
        // Arrange
        DialUser user = new DialUser();
        Integer expectedId = 1;

        // Act
        DialUser result = user.id(expectedId);

        // Assert
        assertSame(user, result);
        assertEquals(expectedId, user.getId());
    }

    @Test
    public void testFluentSetters_Username_ReturnsThis() {
        // Arrange
        DialUser user = new DialUser();
        String expectedUsername = "testuser";

        // Act
        DialUser result = user.username(expectedUsername);

        // Assert
        assertSame(user, result);
        assertEquals(expectedUsername, user.getUsername());
    }

    @Test
    public void testFluentSetters_Password_ReturnsThis() {
        // Arrange
        DialUser user = new DialUser();
        String expectedPassword = "password123";

        // Act
        DialUser result = user.password(expectedPassword);

        // Assert
        assertSame(user, result);
        assertEquals(expectedPassword, user.getPassword());
    }

    @Test
    public void testFluentSetters_LastLoginTime_ReturnsThis() {
        // Arrange
        DialUser user = new DialUser();
        String expectedTime = "2025-09-18T10:30:00Z";

        // Act
        DialUser result = user.lastLoginTime(expectedTime);

        // Assert
        assertSame(user, result);
        assertEquals(expectedTime, user.getLastLoginTime());
    }

    @Test
    public void testEquals_SameObject_ReturnsTrue() {
        // Arrange
        DialUser user = createTestUser(1, "testuser", "password", "2025-09-18T10:30:00Z");

        // Act & Assert
        assertTrue(user.equals(user));
    }

    @Test
    public void testEquals_SameValues_ReturnsTrue() {
        // Arrange
        DialUser user1 = createTestUser(1, "testuser", "password", "2025-09-18T10:30:00Z");
        DialUser user2 = createTestUser(1, "testuser", "password", "2025-09-18T10:30:00Z");

        // Act & Assert
        assertTrue(user1.equals(user2));
        assertTrue(user2.equals(user1));
    }

    @Test
    public void testEquals_DifferentId_ReturnsFalse() {
        // Arrange
        DialUser user1 = createTestUser(1, "testuser", "password", "2025-09-18T10:30:00Z");
        DialUser user2 = createTestUser(2, "testuser", "password", "2025-09-18T10:30:00Z");

        // Act & Assert
        assertFalse(user1.equals(user2));
        assertFalse(user2.equals(user1));
    }

    @Test
    public void testEquals_DifferentUsername_ReturnsFalse() {
        // Arrange
        DialUser user1 = createTestUser(1, "testuser1", "password", "2025-09-18T10:30:00Z");
        DialUser user2 = createTestUser(1, "testuser2", "password", "2025-09-18T10:30:00Z");

        // Act & Assert
        assertFalse(user1.equals(user2));
        assertFalse(user2.equals(user1));
    }

    @Test
    public void testEquals_DifferentPassword_ReturnsFalse() {
        // Arrange
        DialUser user1 = createTestUser(1, "testuser", "password1", "2025-09-18T10:30:00Z");
        DialUser user2 = createTestUser(1, "testuser", "password2", "2025-09-18T10:30:00Z");

        // Act & Assert
        assertFalse(user1.equals(user2));
        assertFalse(user2.equals(user1));
    }

    @Test
    public void testEquals_DifferentLastLoginTime_ReturnsFalse() {
        // Arrange
        DialUser user1 = createTestUser(1, "testuser", "password", "2025-09-18T10:30:00Z");
        DialUser user2 = createTestUser(1, "testuser", "password", "2025-09-18T11:30:00Z");

        // Act & Assert
        assertFalse(user1.equals(user2));
        assertFalse(user2.equals(user1));
    }

    @Test
    public void testEquals_NullObject_ReturnsFalse() {
        // Arrange
        DialUser user = createTestUser(1, "testuser", "password", "2025-09-18T10:30:00Z");

        // Act & Assert
        assertFalse(user.equals(null));
    }

    @Test
    public void testEquals_DifferentClass_ReturnsFalse() {
        // Arrange
        DialUser user = createTestUser(1, "testuser", "password", "2025-09-18T10:30:00Z");
        String otherObject = "not a user";

        // Act & Assert
        assertFalse(user.equals(otherObject));
    }

    @Test
    public void testHashCode_SameValues_ReturnsSameHashCode() {
        // Arrange
        DialUser user1 = createTestUser(1, "testuser", "password", "2025-09-18T10:30:00Z");
        DialUser user2 = createTestUser(1, "testuser", "password", "2025-09-18T10:30:00Z");

        // Act
        int hashCode1 = user1.hashCode();
        int hashCode2 = user2.hashCode();

        // Assert
        assertEquals(hashCode1, hashCode2);
    }

    @Test
    public void testHashCode_DifferentValues_ReturnsDifferentHashCode() {
        // Arrange
        DialUser user1 = createTestUser(1, "testuser1", "password", "2025-09-18T10:30:00Z");
        DialUser user2 = createTestUser(2, "testuser2", "password", "2025-09-18T10:30:00Z");

        // Act
        int hashCode1 = user1.hashCode();
        int hashCode2 = user2.hashCode();

        // Assert
        assertNotEquals(hashCode1, hashCode2);
    }

    @Test
    public void testToString_ContainsAllFields() {
        // Arrange
        DialUser user = createTestUser(1, "testuser", "password", "2025-09-18T10:30:00Z");

        // Act
        String result = user.toString();

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("1"));
        assertTrue(result.contains("testuser"));
        assertTrue(result.contains("password"));
        assertTrue(result.contains("2025-09-18T10:30:00Z"));
    }

    @Test
    public void testToString_NullFields_HandlesGracefully() {
        // Arrange
        DialUser user = new DialUser();

        // Act
        String result = user.toString();

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("null"));
    }

    private DialUser createTestUser(Integer id, String username, String password, String lastLoginTime) {
        DialUser user = new DialUser();
        user.setId(id);
        user.setUsername(username);
        user.setPassword(password);
        user.setLastLoginTime(lastLoginTime);
        return user;
    }
}
