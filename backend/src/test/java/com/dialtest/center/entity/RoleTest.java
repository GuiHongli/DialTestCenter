/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.dialtest.center.entity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.huawei.dialtest.center.entity.Role;

/**
 * 角色枚举测试类，测试Role枚举的各种功能
 * 包括枚举值、权限检查方法、角色描述等功能
 * 验证角色权限逻辑的正确性
 * 
 * @author g00940940
 * @since 2025-09-06
 */
public class RoleTest {

    @Test
    public void testRoleValues() {
        // When
        Role[] roles = Role.values();

        // Then
        assertNotNull(roles);
        assertEquals(4, roles.length);
    }

    @Test
    public void testRoleValueOf() {
        // When & Then
        assertEquals(Role.ADMIN, Role.valueOf("ADMIN"));
        assertEquals(Role.OPERATOR, Role.valueOf("OPERATOR"));
        assertEquals(Role.BROWSER, Role.valueOf("BROWSER"));
        assertEquals(Role.EXECUTOR, Role.valueOf("EXECUTOR"));
    }

    @Test
    public void testAdminRole() {
        // When
        Role adminRole = Role.ADMIN;

        // Then
        assertNotNull(adminRole);
        assertEquals("管理员", adminRole.getDescription());
        assertEquals("拥有所有权限", adminRole.getPermission());
    }

    @Test
    public void testOperatorRole() {
        // When
        Role operatorRole = Role.OPERATOR;

        // Then
        assertNotNull(operatorRole);
        assertEquals("操作员", operatorRole.getDescription());
        assertEquals("可以执行拨测任务相关的所有操作", operatorRole.getPermission());
    }

    @Test
    public void testBrowserRole() {
        // When
        Role browserRole = Role.BROWSER;

        // Then
        assertNotNull(browserRole);
        assertEquals("浏览者", browserRole.getDescription());
        assertEquals("仅查看", browserRole.getPermission());
    }

    @Test
    public void testExecutorRole() {
        // When
        Role executorRole = Role.EXECUTOR;

        // Then
        assertNotNull(executorRole);
        assertEquals("执行机", executorRole.getDescription());
        assertEquals("执行机注册使用", executorRole.getPermission());
    }

    @Test
    public void testRoleToString() {
        // When & Then
        assertEquals("ADMIN", Role.ADMIN.toString());
        assertEquals("OPERATOR", Role.OPERATOR.toString());
        assertEquals("BROWSER", Role.BROWSER.toString());
        assertEquals("EXECUTOR", Role.EXECUTOR.toString());
    }

    @Test
    public void testRoleOrdinal() {
        // When & Then
        assertEquals(0, Role.ADMIN.ordinal());
        assertEquals(1, Role.OPERATOR.ordinal());
        assertEquals(2, Role.BROWSER.ordinal());
        assertEquals(3, Role.EXECUTOR.ordinal());
    }

    @Test
    public void testRoleName() {
        // When & Then
        assertEquals("ADMIN", Role.ADMIN.name());
        assertEquals("OPERATOR", Role.OPERATOR.name());
        assertEquals("BROWSER", Role.BROWSER.name());
        assertEquals("EXECUTOR", Role.EXECUTOR.name());
    }

    @Test
    public void testRoleCompareTo() {
        // When & Then
        assertTrue(Role.ADMIN.compareTo(Role.OPERATOR) < 0);
        assertTrue(Role.OPERATOR.compareTo(Role.BROWSER) < 0);
        assertTrue(Role.BROWSER.compareTo(Role.EXECUTOR) < 0);
        assertEquals(0, Role.ADMIN.compareTo(Role.ADMIN));
    }

    @Test
    public void testRoleGetDeclaringClass() {
        // When
        Class<?> declaringClass = Role.ADMIN.getDeclaringClass();

        // Then
        assertNotNull(declaringClass);
        assertEquals(Role.class, declaringClass);
    }

    @Test
    public void testRoleGetClass() {
        // When
        Class<?> roleClass = Role.ADMIN.getClass();

        // Then
        assertNotNull(roleClass);
        assertEquals(Role.class, roleClass);
    }

    @Test
    public void testRoleIsEnum() {
        // When & Then
        assertTrue(Role.ADMIN instanceof Enum);
        assertTrue(Role.OPERATOR instanceof Enum);
        assertTrue(Role.BROWSER instanceof Enum);
        assertTrue(Role.EXECUTOR instanceof Enum);
    }

    @Test
    public void testRoleEnumConstants() {
        // When
        Role[] enumConstants = Role.class.getEnumConstants();

        // Then
        assertNotNull(enumConstants);
        assertEquals(4, enumConstants.length);
        assertEquals(Role.ADMIN, enumConstants[0]);
        assertEquals(Role.OPERATOR, enumConstants[1]);
        assertEquals(Role.BROWSER, enumConstants[2]);
        assertEquals(Role.EXECUTOR, enumConstants[3]);
    }
}
