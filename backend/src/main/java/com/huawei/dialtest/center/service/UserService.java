/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.dialtest.center.service;

import com.huawei.dialtest.center.dto.PagedResponse;
import com.huawei.dialtest.center.dto.UserCreateRequest;
import com.huawei.dialtest.center.dto.UserUpdateRequest;
import com.huawei.dialtest.center.entity.DialUser;
import com.huawei.dialtest.center.mapper.UserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 用户服务类，提供用户管理的业务逻辑
 * 支持用户的增删改查操作，包括密码加密和权限控制
 *
 * @author g00940940
 * @since 2025-09-09
 */
@Service
@Transactional
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private OperationLogService operationLogService;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * 获取用户列表（分页）
     *
     * @param page 页码，从1开始
     * @param pageSize 每页大小
     * @param search 搜索关键字（可选）
     * @return 用户分页数据
     */
    @Transactional(readOnly = true)
    public PagedResponse<DialUser> getAllUsers(int page, int pageSize, String search) {
        try {
            logger.debug("Getting users - page: {}, size: {}, search: {}", page, pageSize, search);
            
            List<DialUser> content;
            long total;
            
            if (search != null && !search.trim().isEmpty()) {
                // 带搜索条件的分页查询
                content = userMapper.findByUsernameContainingWithPage(search.trim(), page - 1, pageSize);
                total = userMapper.countByUsernameContaining(search.trim());
            } else {
                // 无搜索条件的分页查询
                content = userMapper.findAllByOrderByCreatedTimeDesc(page - 1, pageSize);
                total = userMapper.count();
            }
            
            PagedResponse<DialUser> result = new PagedResponse<>(content, total, page, pageSize);
            logger.info("Successfully retrieved {} users (page {}/{})", content.size(), page, (int) Math.ceil((double) total / pageSize));
            return result;
        } catch (DataAccessException e) {
            logger.error("Failed to get users", e);
            throw new RuntimeException("Failed to retrieve users", e);
        }
    }

    /**
     * 根据ID获取用户
     *
     * @param id 用户ID
     * @return 用户信息
     */
    @Transactional(readOnly = true)
    public Optional<DialUser> getUserById(Long id) {
        try {
            logger.info("Getting user by ID: {}", id);
            DialUser user = userMapper.findById(id);
            Optional<DialUser> userOptional = Optional.ofNullable(user);
            if (userOptional.isPresent()) {
                logger.info("Successfully retrieved user: {}", userOptional.get().getUsername());
            } else {
                logger.warn("User not found with ID: {}", id);
            }
            return userOptional;
        } catch (DataAccessException e) {
            logger.error("Failed to get user by ID: {}", id, e);
            throw new RuntimeException("Failed to retrieve user", e);
        }
    }

    /**
     * 根据用户名获取用户
     *
     * @param username 用户名
     * @return 用户信息
     */
    @Transactional(readOnly = true)
    public Optional<DialUser> getUserByUsername(String username) {
        try {
            logger.info("Getting user by username: {}", username);
            DialUser user = userMapper.findByUsername(username);
            Optional<DialUser> userOptional = Optional.ofNullable(user);
            if (userOptional.isPresent()) {
                logger.info("Successfully retrieved user: {}", username);
            } else {
                logger.warn("User not found with username: {}", username);
            }
            return userOptional;
        } catch (DataAccessException e) {
            logger.error("Failed to get user by username: {}", username, e);
            throw new RuntimeException("Failed to retrieve user", e);
        }
    }

    /**
     * 创建新用户
     *
     * @param request 用户创建请求
     * @return 创建的用户
     */
    public DialUser createUser(UserCreateRequest request) {
        try {
            logger.info("Creating new user: {}", request.getUsername());
            
            if (userMapper.existsByUsername(request.getUsername())) {
                logger.warn("Username already exists: {}", request.getUsername());
                throw new IllegalArgumentException("Username already exists: " + request.getUsername());
            }

            String encodedPassword = passwordEncoder.encode(request.getPassword());
            DialUser user = new DialUser(request.getUsername(), encodedPassword);
            // 创建用户时不设置最后登录时间，因为用户还没有登录过
            int result = userMapper.insert(user);
            if (result > 0) {
                logger.info("Successfully created user: {}", request.getUsername());
                // 记录操作日志
                operationLogService.logOperation(request.getUsername(), "CREATE", "USER", "创建用户: " + request.getUsername());
                return user;
            } else {
                throw new RuntimeException("Failed to create user");
            }
        } catch (DataAccessException e) {
            logger.error("Failed to create user: {}", request.getUsername(), e);
            throw new RuntimeException("Failed to create user", e);
        }
    }

    /**
     * 更新用户信息
     *
     * @param id 用户ID
     * @param request 用户更新请求
     * @return 更新后的用户
     */
    public DialUser updateUser(Long id, UserUpdateRequest request) {
        try {
            logger.info("Updating user with ID: {}", id);
            
            DialUser user = userMapper.findById(id);
            if (user == null) {
                throw new IllegalArgumentException("User not found with ID: " + id);
            }

            if (request.getUsername() != null && !request.getUsername().equals(user.getUsername())) {
                if (userMapper.existsByUsername(request.getUsername())) {
                    logger.warn("Username already exists: {}", request.getUsername());
                    throw new IllegalArgumentException("Username already exists: " + request.getUsername());
                }
                user.setUsername(request.getUsername());
            }

            if (request.getPassword() != null && !request.getPassword().isEmpty()) {
                String encodedPassword = passwordEncoder.encode(request.getPassword());
                user.setPassword(encodedPassword);
            }

            int result = userMapper.update(user);
            if (result > 0) {
                logger.info("Successfully updated user: {}", user.getUsername());
                // 记录操作日志
                operationLogService.logOperation(user.getUsername(), "UPDATE", "USER", "更新用户信息: " + user.getUsername());
                return user;
            } else {
                throw new RuntimeException("Failed to update user");
            }
        } catch (DataAccessException e) {
            logger.error("Failed to update user with ID: {}", id, e);
            throw new RuntimeException("Failed to update user", e);
        }
    }

    /**
     * 删除用户
     *
     * @param id 用户ID
     */
    public void deleteUser(Long id) {
        try {
            logger.info("Deleting user with ID: {}", id);
            
            DialUser user = userMapper.findById(id);
            if (user == null) {
                logger.warn("User not found with ID: {}", id);
                throw new IllegalArgumentException("User not found with ID: " + id);
            }
            
            int result = userMapper.deleteById(id);
            if (result == 0) {
                throw new RuntimeException("Failed to delete user");
            }
            logger.info("Successfully deleted user with ID: {}", id);
            // 记录操作日志
            operationLogService.logOperation(user.getUsername(), "DELETE", "USER", "删除用户: " + user.getUsername());
        } catch (DataAccessException e) {
            logger.error("Failed to delete user with ID: {}", id, e);
            throw new RuntimeException("Failed to delete user", e);
        }
    }


    /**
     * 根据用户名搜索用户
     *
     * @param username 用户名关键字
     * @return 用户列表
     */
    @Transactional(readOnly = true)
    public List<DialUser> searchUsersByUsername(String username) {
        try {
            logger.info("Searching users by username: {}", username);
            List<DialUser> users = userMapper.findByUsernameContaining(username);
            logger.info("Found {} users matching username: {}", users.size(), username);
            return users;
        } catch (DataAccessException e) {
            logger.error("Failed to search users by username: {}", username, e);
            throw new RuntimeException("Failed to search users", e);
        }
    }
}
