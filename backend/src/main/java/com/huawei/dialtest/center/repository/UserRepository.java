/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.dialtest.center.repository;

import com.huawei.dialtest.center.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 用户数据访问层接口
 * 提供用户数据的增删改查操作
 *
 * @author g00940940
 * @since 2025-09-09
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * 根据用户名查找用户
     *
     * @param username 用户名
     * @return 用户信息
     */
    Optional<User> findByUsername(String username);

    /**
     * 检查用户名是否存在
     *
     * @param username 用户名
     * @return 是否存在
     */
    boolean existsByUsername(String username);

    /**
     * 根据用户名模糊查询用户列表
     *
     * @param username 用户名关键字
     * @return 用户列表
     */
    @Query("SELECT u FROM User u WHERE u.username LIKE %:username%")
    List<User> findByUsernameContaining(@Param("username") String username);

    /**
     * 获取所有用户，按创建时间倒序排列
     *
     * @return 用户列表
     */
    @Query("SELECT u FROM User u ORDER BY u.createdTime DESC")
    List<User> findAllOrderByCreatedTimeDesc();
}
