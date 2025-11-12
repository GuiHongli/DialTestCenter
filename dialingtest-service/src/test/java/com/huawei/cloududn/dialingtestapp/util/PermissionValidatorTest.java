/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.cloududn.dialingtestapp.util;

import com.huawei.cloududn.dialingtestapp.service.UserRoleService;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * 权限校验工具类测试
 *
 * @author g00940940
 * @since 2025-01-27
 */
@RunWith(MockitoJUnitRunner.class)
public class PermissionValidatorTest {

    @Mock
    private UserRoleService userRoleService;

    @InjectMocks
    private PermissionValidator permissionValidator;

    /**
     * 测试PermissionValidationResult.success - 创建成功结果
     */
    @Test
    public void testPermissionValidationResult_Success_CreatesValidResult() {
        // Act
        PermissionValidator.PermissionValidationResult result =
                PermissionValidator.PermissionValidationResult.success();

        // Assert
        assertTrue(result.isValid());
        assertNull(result.getErrorMessage());
    }

    /**
     * 测试PermissionValidationResult.failure - 创建失败结果
     */
    @Test
    public void testPermissionValidationResult_Failure_CreatesInvalidResultWithMessage() {
        // Arrange
        String errorMessage = "权限不足";

        // Act
        PermissionValidator.PermissionValidationResult result =
                PermissionValidator.PermissionValidationResult.failure(errorMessage);

        // Assert
        assertFalse(result.isValid());
        assertEquals(errorMessage, result.getErrorMessage());
    }

    /**
     * 测试validateUsername - 有效用户名
     */
    @Test
    public void testValidateUsername_ValidUsername_ReturnsSuccess() {
        // Arrange
        String username = "testuser";

        // Act
        PermissionValidator.PermissionValidationResult result = permissionValidator.validateUsername(username);

        // Assert
        assertTrue(result.isValid());
        assertNull(result.getErrorMessage());
    }

    /**
     * 测试validateUsername - null用户名
     */
    @Test
    public void testValidateUsername_NullUsername_ReturnsFailure() {
        // Arrange
        String username = null;

        // Act
        PermissionValidator.PermissionValidationResult result = permissionValidator.validateUsername(username);

        // Assert
        assertFalse(result.isValid());
        assertEquals("未提供用户名", result.getErrorMessage());
    }

    /**
     * 测试validateUsername - 空字符串用户名
     */
    @Test
    public void testValidateUsername_EmptyUsername_ReturnsFailure() {
        // Arrange
        String username = "";

        // Act
        PermissionValidator.PermissionValidationResult result = permissionValidator.validateUsername(username);

        // Assert
        assertFalse(result.isValid());
        assertEquals("未提供用户名", result.getErrorMessage());
    }

    /**
     * 测试validateUsername - 空白字符串用户名
     */
    @Test
    public void testValidateUsername_WhitespaceUsername_ReturnsFailure() {
        // Arrange
        String username = "   ";

        // Act
        PermissionValidator.PermissionValidationResult result = permissionValidator.validateUsername(username);

        // Assert
        assertFalse(result.isValid());
        assertEquals("未提供用户名", result.getErrorMessage());
    }

    /**
     * 测试checkRole - 用户有正确角色，无操作描述
     */
    @Test
    public void testCheckRole_UserHasRole_NoDescription_ReturnsSuccess() {
        // Arrange
        String username = "testuser";
        String requiredRole = "ADMIN";
        String operationDescription = null;
        List<String> userRoles = Arrays.asList("ADMIN", "OPERATOR");

        when(userRoleService.getUserRolesByUsername(username)).thenReturn(userRoles);

        // Act
        PermissionValidator.PermissionValidationResult result =
                permissionValidator.checkRole(username, requiredRole, operationDescription);

        // Assert
        assertTrue(result.isValid());
        assertNull(result.getErrorMessage());
        verify(userRoleService).getUserRolesByUsername(username);
    }

    /**
     * 测试checkRole - 用户有正确角色，有操作描述
     */
    @Test
    public void testCheckRole_UserHasRole_WithDescription_ReturnsSuccess() {
        // Arrange
        String username = "testuser";
        String requiredRole = "ADMIN";
        String operationDescription = "删除用户";
        List<String> userRoles = Arrays.asList("ADMIN");

        when(userRoleService.getUserRolesByUsername(username)).thenReturn(userRoles);

        // Act
        PermissionValidator.PermissionValidationResult result =
                permissionValidator.checkRole(username, requiredRole, operationDescription);

        // Assert
        assertTrue(result.isValid());
        assertNull(result.getErrorMessage());
        verify(userRoleService).getUserRolesByUsername(username);
    }

    /**
     * 测试checkRole - 用户没有所需角色，无操作描述
     */
    @Test
    public void testCheckRole_UserDoesNotHaveRole_NoDescription_ReturnsFailure() {
        // Arrange
        String username = "testuser";
        String requiredRole = "ADMIN";
        String operationDescription = null;
        List<String> userRoles = Arrays.asList("OPERATOR", "BROWSER");

        when(userRoleService.getUserRolesByUsername(username)).thenReturn(userRoles);

        // Act
        PermissionValidator.PermissionValidationResult result =
                permissionValidator.checkRole(username, requiredRole, operationDescription);

        // Assert
        assertFalse(result.isValid());
        assertEquals("权限不足，需要管理员权限", result.getErrorMessage());
        verify(userRoleService).getUserRolesByUsername(username);
    }

    /**
     * 测试checkRole - 用户没有所需角色，有操作描述
     */
    @Test
    public void testCheckRole_UserDoesNotHaveRole_WithDescription_ReturnsFailure() {
        // Arrange
        String username = "testuser";
        String requiredRole = "ADMIN";
        String operationDescription = "删除用户";
        List<String> userRoles = Arrays.asList("OPERATOR");

        when(userRoleService.getUserRolesByUsername(username)).thenReturn(userRoles);

        // Act
        PermissionValidator.PermissionValidationResult result =
                permissionValidator.checkRole(username, requiredRole, operationDescription);

        // Assert
        assertFalse(result.isValid());
        assertEquals("权限不足，删除用户需要管理员权限", result.getErrorMessage());
        verify(userRoleService).getUserRolesByUsername(username);
    }

    /**
     * 测试checkRole - 用户名为null
     */
    @Test
    public void testCheckRole_NullUsername_ReturnsFailure() {
        // Arrange
        String username = null;
        String requiredRole = "ADMIN";
        String operationDescription = "删除用户";

        // Act
        PermissionValidator.PermissionValidationResult result =
                permissionValidator.checkRole(username, requiredRole, operationDescription);

        // Assert
        assertFalse(result.isValid());
        assertEquals("未提供用户名", result.getErrorMessage());
        verify(userRoleService, never()).getUserRolesByUsername(anyString());
    }

    /**
     * 测试checkRole - 用户角色列表为空
     */
    @Test
    public void testCheckRole_EmptyUserRoles_ReturnsFailure() {
        // Arrange
        String username = "testuser";
        String requiredRole = "ADMIN";
        String operationDescription = null;
        List<String> userRoles = Collections.emptyList();

        when(userRoleService.getUserRolesByUsername(username)).thenReturn(userRoles);

        // Act
        PermissionValidator.PermissionValidationResult result =
                permissionValidator.checkRole(username, requiredRole, operationDescription);

        // Assert
        assertFalse(result.isValid());
        assertEquals("权限不足，需要管理员权限", result.getErrorMessage());
        verify(userRoleService).getUserRolesByUsername(username);
    }

    /**
     * 测试checkAnyRole - 用户有其中一个角色
     */
    @Test
    public void testCheckAnyRole_UserHasOneRole_ReturnsSuccess() {
        // Arrange
        String username = "testuser";
        List<String> requiredRoles = Arrays.asList("ADMIN", "OPERATOR");
        String operationDescription = null;
        List<String> userRoles = Arrays.asList("OPERATOR");

        when(userRoleService.getUserRolesByUsername(username)).thenReturn(userRoles);

        // Act
        PermissionValidator.PermissionValidationResult result =
                permissionValidator.checkAnyRole(username, requiredRoles, operationDescription);

        // Assert
        assertTrue(result.isValid());
        assertNull(result.getErrorMessage());
        verify(userRoleService).getUserRolesByUsername(username);
    }

    /**
     * 测试checkAnyRole - 用户有多个角色中的第一个
     */
    @Test
    public void testCheckAnyRole_UserHasFirstRole_ReturnsSuccess() {
        // Arrange
        String username = "testuser";
        List<String> requiredRoles = Arrays.asList("ADMIN", "OPERATOR");
        String operationDescription = "删除用户";
        List<String> userRoles = Arrays.asList("ADMIN");

        when(userRoleService.getUserRolesByUsername(username)).thenReturn(userRoles);

        // Act
        PermissionValidator.PermissionValidationResult result =
                permissionValidator.checkAnyRole(username, requiredRoles, operationDescription);

        // Assert
        assertTrue(result.isValid());
        assertNull(result.getErrorMessage());
        verify(userRoleService).getUserRolesByUsername(username);
    }

    /**
     * 测试checkAnyRole - 用户没有任何所需角色，无操作描述
     */
    @Test
    public void testCheckAnyRole_UserDoesNotHaveAnyRole_NoDescription_ReturnsFailure() {
        // Arrange
        String username = "testuser";
        List<String> requiredRoles = Arrays.asList("ADMIN", "OPERATOR");
        String operationDescription = null;
        List<String> userRoles = Arrays.asList("BROWSER");

        when(userRoleService.getUserRolesByUsername(username)).thenReturn(userRoles);

        // Act
        PermissionValidator.PermissionValidationResult result =
                permissionValidator.checkAnyRole(username, requiredRoles, operationDescription);

        // Assert
        assertFalse(result.isValid());
        assertTrue(result.getErrorMessage().contains("权限不足"));
        assertTrue(result.getErrorMessage().contains("管理员或操作员"));
        verify(userRoleService).getUserRolesByUsername(username);
    }

    /**
     * 测试checkAnyRole - 用户没有任何所需角色，有操作描述
     */
    @Test
    public void testCheckAnyRole_UserDoesNotHaveAnyRole_WithDescription_ReturnsFailure() {
        // Arrange
        String username = "testuser";
        List<String> requiredRoles = Arrays.asList("ADMIN", "OPERATOR");
        String operationDescription = "删除用户";
        List<String> userRoles = Arrays.asList("BROWSER");

        when(userRoleService.getUserRolesByUsername(username)).thenReturn(userRoles);

        // Act
        PermissionValidator.PermissionValidationResult result =
                permissionValidator.checkAnyRole(username, requiredRoles, operationDescription);

        // Assert
        assertFalse(result.isValid());
        assertTrue(result.getErrorMessage().contains("权限不足"));
        assertTrue(result.getErrorMessage().contains("删除用户"));
        assertTrue(result.getErrorMessage().contains("管理员或操作员"));
        verify(userRoleService).getUserRolesByUsername(username);
    }

    /**
     * 测试checkAnyRole - 用户名为null
     */
    @Test
    public void testCheckAnyRole_NullUsername_ReturnsFailure() {
        // Arrange
        String username = null;
        List<String> requiredRoles = Arrays.asList("ADMIN", "OPERATOR");
        String operationDescription = "删除用户";

        // Act
        PermissionValidator.PermissionValidationResult result =
                permissionValidator.checkAnyRole(username, requiredRoles, operationDescription);

        // Assert
        assertFalse(result.isValid());
        assertEquals("未提供用户名", result.getErrorMessage());
        verify(userRoleService, never()).getUserRolesByUsername(anyString());
    }

    /**
     * 测试checkAnyRole - 用户角色列表为空
     */
    @Test
    public void testCheckAnyRole_EmptyUserRoles_ReturnsFailure() {
        // Arrange
        String username = "testuser";
        List<String> requiredRoles = Arrays.asList("ADMIN", "OPERATOR");
        String operationDescription = null;
        List<String> userRoles = Collections.emptyList();

        when(userRoleService.getUserRolesByUsername(username)).thenReturn(userRoles);

        // Act
        PermissionValidator.PermissionValidationResult result =
                permissionValidator.checkAnyRole(username, requiredRoles, operationDescription);

        // Assert
        assertFalse(result.isValid());
        assertTrue(result.getErrorMessage().contains("管理员或操作员"));
        verify(userRoleService).getUserRolesByUsername(username);
    }

    /**
     * 测试checkAdmin - 用户是ADMIN
     */
    @Test
    public void testCheckAdmin_UserIsAdmin_ReturnsSuccess() {
        // Arrange
        String username = "adminuser";
        String operationDescription = "删除用户";
        List<String> userRoles = Arrays.asList("ADMIN");

        when(userRoleService.getUserRolesByUsername(username)).thenReturn(userRoles);

        // Act
        PermissionValidator.PermissionValidationResult result =
                permissionValidator.checkAdmin(username, operationDescription);

        // Assert
        assertTrue(result.isValid());
        assertNull(result.getErrorMessage());
        verify(userRoleService).getUserRolesByUsername(username);
    }

    /**
     * 测试checkAdmin - 用户不是ADMIN
     */
    @Test
    public void testCheckAdmin_UserIsNotAdmin_ReturnsFailure() {
        // Arrange
        String username = "operatoruser";
        String operationDescription = "删除用户";
        List<String> userRoles = Arrays.asList("OPERATOR");

        when(userRoleService.getUserRolesByUsername(username)).thenReturn(userRoles);

        // Act
        PermissionValidator.PermissionValidationResult result =
                permissionValidator.checkAdmin(username, operationDescription);

        // Assert
        assertFalse(result.isValid());
        assertEquals("权限不足，删除用户需要管理员权限", result.getErrorMessage());
        verify(userRoleService).getUserRolesByUsername(username);
    }

    /**
     * 测试checkAdminOrOperator - 用户是ADMIN
     */
    @Test
    public void testCheckAdminOrOperator_UserIsAdmin_ReturnsSuccess() {
        // Arrange
        String username = "adminuser";
        String operationDescription = "删除用户";
        List<String> userRoles = Arrays.asList("ADMIN");

        when(userRoleService.getUserRolesByUsername(username)).thenReturn(userRoles);

        // Act
        PermissionValidator.PermissionValidationResult result =
                permissionValidator.checkAdminOrOperator(username, operationDescription);

        // Assert
        assertTrue(result.isValid());
        assertNull(result.getErrorMessage());
        verify(userRoleService).getUserRolesByUsername(username);
    }

    /**
     * 测试checkAdminOrOperator - 用户是OPERATOR
     */
    @Test
    public void testCheckAdminOrOperator_UserIsOperator_ReturnsSuccess() {
        // Arrange
        String username = "operatoruser";
        String operationDescription = "删除用户";
        List<String> userRoles = Arrays.asList("OPERATOR");

        when(userRoleService.getUserRolesByUsername(username)).thenReturn(userRoles);

        // Act
        PermissionValidator.PermissionValidationResult result =
                permissionValidator.checkAdminOrOperator(username, operationDescription);

        // Assert
        assertTrue(result.isValid());
        assertNull(result.getErrorMessage());
        verify(userRoleService).getUserRolesByUsername(username);
    }

    /**
     * 测试checkAdminOrOperator - 用户既不是ADMIN也不是OPERATOR
     */
    @Test
    public void testCheckAdminOrOperator_UserIsNeither_ReturnsFailure() {
        // Arrange
        String username = "browseruser";
        String operationDescription = "删除用户";
        List<String> userRoles = Arrays.asList("BROWSER");

        when(userRoleService.getUserRolesByUsername(username)).thenReturn(userRoles);

        // Act
        PermissionValidator.PermissionValidationResult result =
                permissionValidator.checkAdminOrOperator(username, operationDescription);

        // Assert
        assertFalse(result.isValid());
        assertTrue(result.getErrorMessage().contains("权限不足"));
        assertTrue(result.getErrorMessage().contains("删除用户"));
        assertTrue(result.getErrorMessage().contains("管理员或操作员"));
        verify(userRoleService).getUserRolesByUsername(username);
    }

    /**
     * 测试checkRole - 角色显示名称映射（ADMIN）
     */
    @Test
    public void testCheckRole_AdminRole_ShowsCorrectDisplayName() {
        // Arrange
        String username = "testuser";
        String requiredRole = "ADMIN";
        String operationDescription = null;
        List<String> userRoles = Arrays.asList("BROWSER");

        when(userRoleService.getUserRolesByUsername(username)).thenReturn(userRoles);

        // Act
        PermissionValidator.PermissionValidationResult result =
                permissionValidator.checkRole(username, requiredRole, operationDescription);

        // Assert
        assertFalse(result.isValid());
        assertEquals("权限不足，需要管理员权限", result.getErrorMessage());
    }

    /**
     * 测试checkRole - 角色显示名称映射（OPERATOR）
     */
    @Test
    public void testCheckRole_OperatorRole_ShowsCorrectDisplayName() {
        // Arrange
        String username = "testuser";
        String requiredRole = "OPERATOR";
        String operationDescription = null;
        List<String> userRoles = Arrays.asList("BROWSER");

        when(userRoleService.getUserRolesByUsername(username)).thenReturn(userRoles);

        // Act
        PermissionValidator.PermissionValidationResult result =
                permissionValidator.checkRole(username, requiredRole, operationDescription);

        // Assert
        assertFalse(result.isValid());
        assertEquals("权限不足，需要操作员权限", result.getErrorMessage());
    }

    /**
     * 测试checkRole - 角色显示名称映射（BROWSER）
     */
    @Test
    public void testCheckRole_BrowserRole_ShowsCorrectDisplayName() {
        // Arrange
        String username = "testuser";
        String requiredRole = "BROWSER";
        String operationDescription = null;
        List<String> userRoles = Arrays.asList("ADMIN");

        when(userRoleService.getUserRolesByUsername(username)).thenReturn(userRoles);

        // Act
        PermissionValidator.PermissionValidationResult result =
                permissionValidator.checkRole(username, requiredRole, operationDescription);

        // Assert
        assertFalse(result.isValid());
        assertEquals("权限不足，需要浏览者权限", result.getErrorMessage());
    }

    /**
     * 测试checkRole - 角色显示名称映射（EXECUTOR）
     */
    @Test
    public void testCheckRole_ExecutorRole_ShowsCorrectDisplayName() {
        // Arrange
        String username = "testuser";
        String requiredRole = "EXECUTOR";
        String operationDescription = null;
        List<String> userRoles = Arrays.asList("ADMIN");

        when(userRoleService.getUserRolesByUsername(username)).thenReturn(userRoles);

        // Act
        PermissionValidator.PermissionValidationResult result =
                permissionValidator.checkRole(username, requiredRole, operationDescription);

        // Assert
        assertFalse(result.isValid());
        assertEquals("权限不足，需要执行机权限", result.getErrorMessage());
    }

    /**
     * 测试checkRole - 未知角色使用原值
     */
    @Test
    public void testCheckRole_UnknownRole_ShowsOriginalRole() {
        // Arrange
        String username = "testuser";
        String requiredRole = "UNKNOWN_ROLE";
        String operationDescription = null;
        List<String> userRoles = Arrays.asList("ADMIN");

        when(userRoleService.getUserRolesByUsername(username)).thenReturn(userRoles);

        // Act
        PermissionValidator.PermissionValidationResult result =
                permissionValidator.checkRole(username, requiredRole, operationDescription);

        // Assert
        assertFalse(result.isValid());
        assertEquals("权限不足，需要UNKNOWN_ROLE权限", result.getErrorMessage());
    }

    /**
     * 测试checkAnyRole - 多个角色显示名称映射
     */
    @Test
    public void testCheckAnyRole_MultipleRoles_ShowsCorrectDisplayNames() {
        // Arrange
        String username = "testuser";
        List<String> requiredRoles = Arrays.asList("ADMIN", "OPERATOR", "BROWSER");
        String operationDescription = null;
        List<String> userRoles = Arrays.asList("EXECUTOR");

        when(userRoleService.getUserRolesByUsername(username)).thenReturn(userRoles);

        // Act
        PermissionValidator.PermissionValidationResult result =
                permissionValidator.checkAnyRole(username, requiredRoles, operationDescription);

        // Assert
        assertFalse(result.isValid());
        assertTrue(result.getErrorMessage().contains("管理员或操作员或浏览者"));
    }

}

