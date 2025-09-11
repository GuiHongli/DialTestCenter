/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.dialtest.center.mapper;

import com.huawei.dialtest.center.entity.OperationLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 操作记录Mapper接口
 * 提供操作记录的数据访问方法
 *
 * @author g00940940
 * @since 2025-09-09
 */
@Mapper
public interface OperationLogMapper {

    /**
     * 根据多个条件查询操作记录
     *
     * @param username 用户名（可选，支持模糊查询）
     * @param operationType 操作类型（可选）
     * @param target 操作对象（可选）
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @param pageNo 页码（从1开始）
     * @param pageSize 每页大小
     * @return 操作记录列表
     */
    List<OperationLog> findByConditions(@Param("username") String username,
                                       @Param("operationType") String operationType,
                                       @Param("target") String target,
                                       @Param("startTime") LocalDateTime startTime,
                                       @Param("endTime") LocalDateTime endTime,
                                       @Param("pageNo") int pageNo,
                                       @Param("pageSize") int pageSize);

    /**
     * 查询所有操作记录，按操作时间倒序排列
     *
     * @param pageNo 页码（从1开始）
     * @param pageSize 每页大小
     * @return 操作记录列表
     */
    List<OperationLog> findAllOrderByOperationTimeDesc(@Param("pageNo") int pageNo, @Param("pageSize") int pageSize);

    /**
     * 根据用户名查询操作记录，按操作时间倒序排列
     *
     * @param username 用户名
     * @param pageNo 页码（从1开始）
     * @param pageSize 每页大小
     * @return 操作记录列表
     */
    List<OperationLog> findByUsernameOrderByOperationTimeDesc(@Param("username") String username, 
                                                             @Param("pageNo") int pageNo, 
                                                             @Param("pageSize") int pageSize);

    /**
     * 根据ID获取操作记录
     *
     * @param id 操作记录ID
     * @return 操作记录
     */
    OperationLog findById(@Param("id") Long id);

    /**
     * 插入操作记录
     *
     * @param operationLog 操作记录
     * @return 影响行数
     */
    int insert(OperationLog operationLog);

    /**
     * 查询最近的N条操作记录
     *
     * @param limit 限制数量
     * @return 操作记录列表
     */
    List<OperationLog> findRecentOperationLogs(@Param("limit") int limit);

    /**
     * 根据条件统计操作记录总数
     *
     * @param username 用户名（可选，支持模糊查询）
     * @param operationType 操作类型（可选）
     * @param target 操作对象（可选）
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @return 记录总数
     */
    long countByConditions(@Param("username") String username,
                          @Param("operationType") String operationType,
                          @Param("target") String target,
                          @Param("startTime") LocalDateTime startTime,
                          @Param("endTime") LocalDateTime endTime);
}
