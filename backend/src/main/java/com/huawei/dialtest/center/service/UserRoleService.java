/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.dialtest.center.service;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.huawei.dialtest.center.entity.Role;
import com.huawei.dialtest.center.dto.PagedResponse;
import com.huawei.dialtest.center.mapper.UserRoleMapper;

/**
 * 用户角色服务类，提供用户角色管理的业务逻辑处理
 * 包括用户角色分配、权限检查、角色查询等功能
 * 支持事务管理，确保数据一致性
 *
 * @author g00940940
 * @since 2025-09-06
 */
@Service
@Transactional
public class UserRoleService {

    private static final Logger logger = LoggerFactory.getLogger(UserRoleService.class);

    @Autowired
    private UserRoleMapper userRoleMapper;

    @Autowired
    private OperationLogService operationLogService;

    /**
     * 根据用户名获取用户角色列表
     *
     * @param username 用户名，不能为空
     * @return 用户角色列表
     * @throws IllegalArgumentException 当用户名为空时抛出
     */
    @Transactional(readOnly = true)
    public List<UserRole> getUserRoles(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be empty");
        }

        logger.debug("Querying user roles for: {}", username);
        return userRoleMapper.findByUsernameOrderByCreatedTimeDesc(username.trim());
    }

    /**
     * 根据用户名获取角色枚举列表
     *
     * @param username 用户名，不能为空
     * @return 角色枚举列表
     * @throws IllegalArgumentException 当用户名为空时抛出
     */
    @Transactional(readOnly = true)
    public List<Role> getUserRoleEnums(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be empty");
        }

        logger.debug("Querying user role enums for: {}", username);
        return userRoleMapper.findRolesByUsername(username.trim());
    }

    /**
     * 获取所有用户角色关系
     *
     * @return 所有用户角色关系列表
     */
    @Transactional(readOnly = true)
    public List<UserRole> getAllUserRoles() {
        logger.debug("Querying all user role relationships");
        return userRoleMapper.findAllOrderByCreatedTimeDesc();
    }

    /**
     * 保存用户角色关系
     *
     * @param userRole 用户角色关系，不能为空
     * @return 保存后的用户角色关系
     * @throws IllegalArgumentException 当用户角色为空时抛出
     */
    public UserRole save(UserRole userRole) {
        if (userRole == null) {
            throw new IllegalArgumentException("User role cannot be null");
        }

        if (userRole.getUsername() == null || userRole.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be empty");
        }

        if (userRole.getRole() == null) {
            throw new IllegalArgumentException("Role cannot be null");
        }

        userRole.setUsername(userRole.getUsername().trim());

        // 如果是更新操作（有ID），需要检查是否存在其他记录有相同的用户名和角色组合
        if (userRole.getId() != null) {
            UserRole existingUserRole = userRoleMapper.findByUsernameAndRole(
                userRole.getUsername(), userRole.getRole().toString());
            if (existingUserRole != null && !existingUserRole.getId().equals(userRole.getId())) {
                throw new IllegalArgumentException("User role relationship already exists: " + userRole.getUsername() + " - " + userRole.getRole());
            }
        } else {
            // 如果是新建操作（无ID），检查是否已存在相同的用户名和角色组合
            if (userRoleMapper.existsByUsernameAndRole(userRole.getUsername(), userRole.getRole().toString())) {
                throw new IllegalArgumentException("User role relationship already exists: " + userRole.getUsername() + " - " + userRole.getRole());
            }
        }

        logger.info("Saving user role relationship: {} - {}", userRole.getUsername(), userRole.getRole());
        int result = userRoleMapper.insert(userRole);
        if (result > 0) {
            // 记录操作日志
            String operationType = userRole.getId() != null ? "UPDATE" : "CREATE";
            String description = operationType.equals("CREATE") ? 
                "创建用户角色: " + userRole.getUsername() + " - " + userRole.getRole() :
                "更新用户角色: " + userRole.getUsername() + " - " + userRole.getRole();
            operationLogService.logOperation(userRole.getUsername(), operationType, "USER_ROLE", description);
            return userRole;
        } else {
            throw new RuntimeException("Failed to save user role relationship");
        }
    }

    /**
     * 根据ID查找用户角色关系
     *
     * @param id ID，不能为空
     * @return 用户角色关系
     * @throws IllegalArgumentException 当ID为空时抛出
     */
    @Transactional(readOnly = true)
    public Optional<UserRole> findById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null");
        }

        logger.debug("Finding user role by ID: {}", id);
        UserRole userRole = userRoleMapper.findById(id);
        return Optional.ofNullable(userRole);
    }

    /**
     * 根据ID删除用户角色关系
     *
     * @param id ID，不能为空
     * @throws IllegalArgumentException 当ID为空或用户角色关系不存在时抛出
     */
    public void deleteById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null");
        }

        UserRole userRole = userRoleMapper.findById(id);
        if (userRole == null) {
            throw new IllegalArgumentException("User role relationship does not exist: " + id);
        }

        logger.info("Deleting user role relationship: {}", id);
        int result = userRoleMapper.deleteById(id);
        if (result == 0) {
            throw new RuntimeException("Failed to delete user role relationship");
        }
        // 记录操作日志
        operationLogService.logOperation(userRole.getUsername(), "DELETE", "USER_ROLE", 
            "删除用户角色: " + userRole.getUsername() + " - " + userRole.getRole());
    }

    /**
     * 根据用户名和角色删除用户角色关系
     * @param username 用户名
     * @param role 角色
     */
    public void deleteByUsernameAndRole(String username, Role role) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("用户名不能为空");
        }

        if (role == null) {
            throw new IllegalArgumentException("Role cannot be null");
        }

        logger.info("Deleting user role relationship: {} - {}", username, role);
        int result = userRoleMapper.deleteByUsernameAndRole(username.trim(), role.toString());
        if (result == 0) {
            throw new RuntimeException("Failed to delete user role relationship");
        }
    }

    /**
     * 检查是否存在管理员用户
     * @return 是否存在管理员
     */
    @Transactional(readOnly = true)
    public boolean hasAdminUser() {
        boolean hasAdmin = userRoleMapper.existsByRole(Role.ADMIN.toString());
        logger.debug("Checking if admin exists: {}", hasAdmin);
        return hasAdmin;
    }

    /**
     * 获取管理员用户数量
     * @return 管理员用户数量
     */
    @Transactional(readOnly = true)
    public long getAdminUserCount() {
        long count = userRoleMapper.countByRole(Role.ADMIN.toString());
        logger.debug("Admin user count: {}", count);
        return count;
    }

    /**
     * 根据角色获取用户角色关系列表
     * @param role 角色
     * @return 用户角色关系列表
     */
    @Transactional(readOnly = true)
    public List<UserRole> getUserRolesByRole(Role role) {
        if (role == null) {
            throw new IllegalArgumentException("Role cannot be null");
        }

        logger.debug("Querying user roles by role: {}", role);
        return userRoleMapper.findByRole(role.toString());
    }

    /**
     * 检查用户是否拥有指定角色
     * @param username 用户名
     * @param role 角色
     * @return 是否拥有该角色
     */
    @Transactional(readOnly = true)
    public boolean hasRole(String username, Role role) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }

        if (role == null) {
            return false;
        }

        boolean hasRole = userRoleMapper.existsByUsernameAndRole(username.trim(), role.toString());
        logger.debug("Checking user role: {} - {} = {}", username, role, hasRole);
        return hasRole;
    }

    /**
     * 获取执行机用户数量
     * @return 执行机用户数量
     */
    @Transactional(readOnly = true)
    public long getExecutorUserCount() {
        long count = userRoleMapper.countByRole(Role.EXECUTOR.toString());
        logger.debug("Executor user count: {}", count);
        return count;
    }

    /**
     * 分页获取所有用户角色
     *
     * @param page 页码（从1开始）
     * @param pageSize 每页大小
     * @param search 搜索关键词（可选）
     * @return 分页的用户角色列表
     */
    @Transactional(readOnly = true)
    public PagedResponse<UserRole> getAllUserRoles(int page, int pageSize, String search) {
        logger.debug("Getting user roles with pagination: page={}, pageSize={}, search={}", page, pageSize, search);
        
        // 转换为从0开始的页码
        int pageNo = page - 1;
        
        List<UserRole> userRoles;
        long total;
        
        if (search != null && !search.trim().isEmpty()) {
            // 有搜索条件时，使用搜索查询
            userRoles = userRoleMapper.findByUsernameContainingWithPage(search.trim(), pageNo * pageSize, pageSize);
            total = userRoleMapper.countByUsernameContaining(search.trim());
        } else {
            // 无搜索条件时，查询所有
            userRoles = userRoleMapper.findAllByOrderByCreatedTimeDesc(pageNo * pageSize, pageSize);
            total = userRoleMapper.count();
        }
        
        return new PagedResponse<>(userRoles, total, page, pageSize);
    }
}
