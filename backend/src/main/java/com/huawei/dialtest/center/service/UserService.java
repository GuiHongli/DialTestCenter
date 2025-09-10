/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.dialtest.center.service;

import com.huawei.dialtest.center.entity.User;
import com.huawei.dialtest.center.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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
    private UserRepository userRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * 获取所有用户列表
     *
     * @return 用户列表
     */
    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        try {
            logger.info("Getting all users");
            List<User> users = userRepository.findAllOrderByCreatedTimeDesc();
            logger.info("Successfully retrieved {} users", users.size());
            return users;
        } catch (DataAccessException e) {
            logger.error("Failed to get all users", e);
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
    public Optional<User> getUserById(Long id) {
        try {
            logger.info("Getting user by ID: {}", id);
            Optional<User> user = userRepository.findById(id);
            if (user.isPresent()) {
                logger.info("Successfully retrieved user: {}", user.get().getUsername());
            } else {
                logger.warn("User not found with ID: {}", id);
            }
            return user;
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
    public Optional<User> getUserByUsername(String username) {
        try {
            logger.info("Getting user by username: {}", username);
            Optional<User> user = userRepository.findByUsername(username);
            if (user.isPresent()) {
                logger.info("Successfully retrieved user: {}", username);
            } else {
                logger.warn("User not found with username: {}", username);
            }
            return user;
        } catch (DataAccessException e) {
            logger.error("Failed to get user by username: {}", username, e);
            throw new RuntimeException("Failed to retrieve user", e);
        }
    }

    /**
     * 创建新用户
     *
     * @param username 用户名
     * @param password 密码
     * @return 创建的用户
     */
    public User createUser(String username, String password) {
        try {
            logger.info("Creating new user: {}", username);
            
            if (userRepository.existsByUsername(username)) {
                logger.warn("Username already exists: {}", username);
                throw new IllegalArgumentException("Username already exists: " + username);
            }

            String encodedPassword = passwordEncoder.encode(password);
            User user = new User(username, encodedPassword);
            User savedUser = userRepository.save(user);
            
            logger.info("Successfully created user: {}", username);
            return savedUser;
        } catch (DataAccessException e) {
            logger.error("Failed to create user: {}", username, e);
            throw new RuntimeException("Failed to create user", e);
        }
    }

    /**
     * 更新用户信息
     *
     * @param id 用户ID
     * @param username 新用户名
     * @param password 新密码
     * @return 更新后的用户
     */
    public User updateUser(Long id, String username, String password) {
        try {
            logger.info("Updating user with ID: {}", id);
            
            User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + id));

            if (username != null && !username.equals(user.getUsername())) {
                if (userRepository.existsByUsername(username)) {
                    logger.warn("Username already exists: {}", username);
                    throw new IllegalArgumentException("Username already exists: " + username);
                }
                user.setUsername(username);
            }

            if (password != null && !password.isEmpty()) {
                String encodedPassword = passwordEncoder.encode(password);
                user.setPassword(encodedPassword);
            }

            user.setUpdatedTime(LocalDateTime.now());
            User updatedUser = userRepository.save(user);
            
            logger.info("Successfully updated user: {}", updatedUser.getUsername());
            return updatedUser;
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
            
            if (!userRepository.existsById(id)) {
                logger.warn("User not found with ID: {}", id);
                throw new IllegalArgumentException("User not found with ID: " + id);
            }

            userRepository.deleteById(id);
            logger.info("Successfully deleted user with ID: {}", id);
        } catch (DataAccessException e) {
            logger.error("Failed to delete user with ID: {}", id, e);
            throw new RuntimeException("Failed to delete user", e);
        }
    }

    /**
     * 更新用户最后登录时间
     *
     * @param username 用户名
     */
    public void updateLastLoginTime(String username) {
        try {
            logger.info("Updating last login time for user: {}", username);
            
            Optional<User> userOpt = userRepository.findByUsername(username);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                user.setLastLoginTime(LocalDateTime.now());
                userRepository.save(user);
                logger.info("Successfully updated last login time for user: {}", username);
            } else {
                logger.warn("User not found for updating last login time: {}", username);
            }
        } catch (DataAccessException e) {
            logger.error("Failed to update last login time for user: {}", username, e);
            throw new RuntimeException("Failed to update last login time", e);
        }
    }

    /**
     * 验证用户密码
     *
     * @param username 用户名
     * @param password 密码
     * @return 验证结果
     */
    @Transactional(readOnly = true)
    public boolean validatePassword(String username, String password) {
        try {
            logger.info("Validating password for user: {}", username);
            
            Optional<User> userOpt = userRepository.findByUsername(username);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                boolean isValid = passwordEncoder.matches(password, user.getPassword());
                logger.info("Password validation result for user {}: {}", username, isValid);
                return isValid;
            } else {
                logger.warn("User not found for password validation: {}", username);
                return false;
            }
        } catch (DataAccessException e) {
            logger.error("Failed to validate password for user: {}", username, e);
            throw new RuntimeException("Failed to validate password", e);
        }
    }

    /**
     * 根据用户名搜索用户
     *
     * @param username 用户名关键字
     * @return 用户列表
     */
    @Transactional(readOnly = true)
    public List<User> searchUsersByUsername(String username) {
        try {
            logger.info("Searching users by username: {}", username);
            List<User> users = userRepository.findByUsernameContaining(username);
            logger.info("Found {} users matching username: {}", users.size(), username);
            return users;
        } catch (DataAccessException e) {
            logger.error("Failed to search users by username: {}", username, e);
            throw new RuntimeException("Failed to search users", e);
        }
    }
}
