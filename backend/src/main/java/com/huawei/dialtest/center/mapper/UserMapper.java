/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.dialtest.center.mapper;

import com.huawei.dialtest.center.entity.DialDialUser;
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
public interface DialUserMapper {

    /**
     * 根据ID查找用户
     *
     * @param id 用户ID
     * @return 用户信息
     */
    DialUser findById(@Param("id") Long id);

    /**
     * 根据用户名查找用户
     *
     * @param username 用户名
     * @return 用户信息
     */
    DialUser findByDialUsername(@Param("username") String username);

    /**
     * 检查用户名是否存在
     *
     * @param username 用户名
     * @return 是否存在
     */
    boolean existsByDialUsername(@Param("username") String username);

    /**
     * 根据用户名模糊查询用户列表
     *
     * @param username 用户名关键字
     * @return 用户列表
     */
    List<DialUser> findByDialUsernameContaining(@Param("username") String username);

    /**
     * 分页查询用户列表，按创建时间倒序排列
     *
     * @param pageNo 页码（从0开始）
     * @param pageSize 每页大小
     * @return 用户列表
     */
    List<DialUser> findAllByOrderByCreatedTimeDesc(@Param("pageNo") int pageNo, @Param("pageSize") int pageSize);

    /**
     * 根据用户名模糊查询用户列表（分页）
     *
     * @param username 用户名关键字
     * @param pageNo 页码（从0开始）
     * @param pageSize 每页大小
     * @return 用户列表
     */
    List<DialUser> findByDialUsernameContainingWithPage(@Param("username") String username, @Param("pageNo") int pageNo, @Param("pageSize") int pageSize);

    /**
     * 统计用户名模糊查询的总数
     *
     * @param username 用户名关键字
     * @return 总数
     */
    long countByDialUsernameContaining(@Param("username") String username);

    /**
     * 插入用户
     *
     * @param user 用户对象
     * @return 影响行数
     */
    int insert(DialUser user);

    /**
     * 更新用户
     *
     * @param user 用户对象
     * @return 影响行数
     */
    int update(DialUser user);

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
