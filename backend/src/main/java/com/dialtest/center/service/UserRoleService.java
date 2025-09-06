/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.dialtest.center.service;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dialtest.center.entity.Role;
import com.dialtest.center.entity.UserRole;
import com.dialtest.center.repository.UserRoleRepository;

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
    private UserRoleRepository userRoleRepository;
    
    /**
     * 根据用户名获取用户角色列表
     * @param username 用户名
     * @return 用户角色列表
     */
    @Transactional(readOnly = true)
    public List<UserRole> getUserRoles(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("用户名不能为空");
        }
        
        logger.debug("查询用户角色: {}", username);
        return userRoleRepository.findByUsernameOrderByCreatedTimeDesc(username.trim());
    }
    
    /**
     * 根据用户名获取角色枚举列表
     * @param username 用户名
     * @return 角色枚举列表
     */
    @Transactional(readOnly = true)
    public List<Role> getUserRoleEnums(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("用户名不能为空");
        }
        
        logger.debug("查询用户角色枚举: {}", username);
        return userRoleRepository.findRolesByUsername(username.trim());
    }
    
    /**
     * 获取所有用户角色关系
     * @return 所有用户角色关系列表
     */
    @Transactional(readOnly = true)
    public List<UserRole> getAllUserRoles() {
        logger.debug("查询所有用户角色关系");
        return userRoleRepository.findAllOrderByCreatedTimeDesc();
    }
    
    /**
     * 保存用户角色关系
     * @param userRole 用户角色关系
     * @return 保存后的用户角色关系
     */
    public UserRole save(UserRole userRole) {
        if (userRole == null) {
            throw new IllegalArgumentException("用户角色关系不能为空");
        }
        
        if (userRole.getUsername() == null || userRole.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("用户名不能为空");
        }
        
        if (userRole.getRole() == null) {
            throw new IllegalArgumentException("角色不能为空");
        }
        
        userRole.setUsername(userRole.getUsername().trim());
        
        // 如果是更新操作（有ID），需要检查是否存在其他记录有相同的用户名和角色组合
        if (userRole.getId() != null) {
            Optional<UserRole> existingUserRole = userRoleRepository.findByUsernameAndRole(
                userRole.getUsername(), userRole.getRole());
            if (existingUserRole.isPresent() && !existingUserRole.get().getId().equals(userRole.getId())) {
                throw new IllegalArgumentException("用户角色关系已存在: " + userRole.getUsername() + " - " + userRole.getRole());
            }
        } else {
            // 如果是新建操作（无ID），检查是否已存在相同的用户名和角色组合
            if (userRoleRepository.existsByUsernameAndRole(userRole.getUsername(), userRole.getRole())) {
                throw new IllegalArgumentException("用户角色关系已存在: " + userRole.getUsername() + " - " + userRole.getRole());
            }
        }
        
        logger.info("保存用户角色关系: {} - {}", userRole.getUsername(), userRole.getRole());
        return userRoleRepository.save(userRole);
    }
    
    /**
     * 根据ID查找用户角色关系
     * @param id ID
     * @return 用户角色关系
     */
    @Transactional(readOnly = true)
    public Optional<UserRole> findById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID不能为空");
        }
        
        logger.debug("根据ID查询用户角色关系: {}", id);
        return userRoleRepository.findById(id);
    }
    
    /**
     * 根据ID删除用户角色关系
     * @param id ID
     */
    public void deleteById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID不能为空");
        }
        
        if (!userRoleRepository.existsById(id)) {
            throw new IllegalArgumentException("用户角色关系不存在: " + id);
        }
        
        logger.info("删除用户角色关系: {}", id);
        userRoleRepository.deleteById(id);
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
            throw new IllegalArgumentException("角色不能为空");
        }
        
        logger.info("删除用户角色关系: {} - {}", username, role);
        userRoleRepository.deleteByUsernameAndRole(username.trim(), role);
    }
    
    /**
     * 检查是否存在管理员用户
     * @return 是否存在管理员
     */
    @Transactional(readOnly = true)
    public boolean hasAdminUser() {
        boolean hasAdmin = userRoleRepository.existsByRole(Role.ADMIN);
        logger.debug("检查是否存在管理员: {}", hasAdmin);
        return hasAdmin;
    }
    
    /**
     * 获取管理员用户数量
     * @return 管理员用户数量
     */
    @Transactional(readOnly = true)
    public long getAdminUserCount() {
        long count = userRoleRepository.countByRole(Role.ADMIN);
        logger.debug("管理员用户数量: {}", count);
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
            throw new IllegalArgumentException("角色不能为空");
        }
        
        logger.debug("根据角色查询用户角色关系: {}", role);
        return userRoleRepository.findByRole(role);
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
        
        boolean hasRole = userRoleRepository.existsByUsernameAndRole(username.trim(), role);
        logger.debug("检查用户角色: {} - {} = {}", username, role, hasRole);
        return hasRole;
    }
    
    /**
     * 获取执行机用户数量
     * @return 执行机用户数量
     */
    @Transactional(readOnly = true)
    public long getExecutorUserCount() {
        long count = userRoleRepository.countByRole(Role.EXECUTOR);
        logger.debug("执行机用户数量: {}", count);
        return count;
    }
}
