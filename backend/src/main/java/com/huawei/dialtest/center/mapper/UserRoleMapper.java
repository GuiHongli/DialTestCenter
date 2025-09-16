/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.dialtest.center.mapper;

import com.huawei.dialtest.center.entity.Role;
import com.huawei.dialtest.center.entity.DialUserRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户角色关系Mapper接口，提供用户角色相关的数据库操作
 * 包括用户角色的查询、创建、更新、删除等基本CRUD操作
 *
 * @author g00940940
 * @since 2025-09-09
 */
@Mapper
public interface UserRoleMapper {

    /**
     * 根据ID查找用户角色关系
     *
     * @param id 用户角色关系ID
     * @return 用户角色关系对象
     */
    DialUserRole findById(@Param("id") Long id);

    /**
     * 根据用户名查询用户角色列表
     *
     * @param username 用户名
     * @return 用户角色列表
     */
    List<DialUserRole> findByUsername(@Param("username") String username);

    /**
     * 根据用户名查询用户角色列表，按创建时间倒序排列
     *
     * @param username 用户名
     * @return 用户角色列表
     */
    List<DialUserRole> findByUsernameOrderByCreatedTimeDesc(@Param("username") String username);

    /**
     * 根据用户名查询角色枚举列表
     *
     * @param username 用户名
     * @return 角色枚举列表
     */
    List<Role> findRolesByUsername(@Param("username") String username);

    /**
     * 根据用户名和角色查询用户角色关系
     *
     * @param username 用户名
     * @param role 角色
     * @return 用户角色关系
     */
    DialUserRole findByUsernameAndRole(@Param("username") String username, @Param("role") String role);

    /**
     * 检查用户名和角色组合是否存在
     *
     * @param username 用户名
     * @param role 角色
     * @return 是否存在
     */
    boolean existsByUsernameAndRole(@Param("username") String username, @Param("role") String role);

    /**
     * 根据角色查询用户角色关系列表
     *
     * @param role 角色
     * @return 用户角色关系列表
     */
    List<DialUserRole> findByRole(@Param("role") String role);

    /**
     * 检查是否存在指定角色的用户
     *
     * @param role 角色
     * @return 是否存在
     */
    boolean existsByRole(@Param("role") String role);

    /**
     * 根据用户名删除所有角色
     *
     * @param username 用户名
     * @return 影响行数
     */
    int deleteByUsername(@Param("username") String username);

    /**
     * 根据用户名和角色删除用户角色关系
     *
     * @param username 用户名
     * @param role 角色
     * @return 影响行数
     */
    int deleteByUsernameAndRole(@Param("username") String username, @Param("role") String role);

    /**
     * 统计指定角色的用户数量
     *
     * @param role 角色
     * @return 用户数量
     */
    long countByRole(@Param("role") String role);

    /**
     * 查询所有用户角色，按创建时间倒序排列
     *
     * @return 用户角色列表
     */
    List<DialUserRole> findAllOrderByCreatedTimeDesc();

    /**
     * 统计用户角色关系总数
     *
     * @return 用户角色关系总数
     */
    long count();

    /**
     * 插入用户角色关系
     *
     * @param userRole 用户角色关系对象
     * @return 影响行数
     */
    int insert(DialUserRole userRole);

    /**
     * 更新用户角色关系
     *
     * @param userRole 用户角色关系对象
     * @return 影响行数
     */
    int update(DialUserRole userRole);

    /**
     * 根据ID删除用户角色关系
     *
     * @param id 用户角色关系ID
     * @return 影响行数
     */
    int deleteById(@Param("id") Long id);

    /**
     * 分页查询所有用户角色，按创建时间倒序排列
     *
     * @param pageNo 页码（从0开始）
     * @param pageSize 每页大小
     * @return 用户角色列表
     */
    List<DialUserRole> findAllByOrderByCreatedTimeDesc(@Param("pageNo") int pageNo, @Param("pageSize") int pageSize);

    /**
     * 根据用户名分页查询用户角色，按创建时间倒序排列
     *
     * @param username 用户名
     * @param pageNo 页码（从0开始）
     * @param pageSize 每页大小
     * @return 用户角色列表
     */
    List<DialUserRole> findByUsernameContainingWithPage(@Param("username") String username, @Param("pageNo") int pageNo, @Param("pageSize") int pageSize);

    /**
     * 统计包含指定用户名的用户角色数量
     *
     * @param username 用户名
     * @return 用户角色数量
     */
    long countByUsernameContaining(@Param("username") String username);
}
