/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.dialtest.center.mapper;

import com.huawei.dialtest.center.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户Mapper接口
 * 提供用户数据的增删改查操作
 *
 * @author g00940940
 * @since 2025-09-09
 */
@Mapper
public interface UserMapper {

    /**
     * 根据ID查找用户
     *
     * @param id 用户ID
     * @return 用户信息
     */
    User findById(@Param("id") Long id);

    /**
     * 根据用户名查找用户
     *
     * @param username 用户名
     * @return 用户信息
     */
    User findByUsername(@Param("username") String username);

    /**
     * 检查用户名是否存在
     *
     * @param username 用户名
     * @return 是否存在
     */
    boolean existsByUsername(@Param("username") String username);

    /**
     * 根据用户名模糊查询用户列表
     *
     * @param username 用户名关键字
     * @return 用户列表
     */
    List<User> findByUsernameContaining(@Param("username") String username);

    /**
     * 获取所有用户，按创建时间倒序排列
     *
     * @return 用户列表
     */
    List<User> findAllOrderByCreatedTimeDesc();

    /**
     * 插入用户
     *
     * @param user 用户对象
     * @return 影响行数
     */
    int insert(User user);

    /**
     * 更新用户
     *
     * @param user 用户对象
     * @return 影响行数
     */
    int update(User user);

    /**
     * 根据ID删除用户
     *
     * @param id 用户ID
     * @return 影响行数
     */
    int deleteById(@Param("id") Long id);

    /**
     * 统计用户总数
     *
     * @return 用户总数
     */
    long count();
}
