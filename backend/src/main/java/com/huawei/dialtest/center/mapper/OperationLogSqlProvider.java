/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.dialtest.center.mapper;

import org.apache.ibatis.jdbc.SQL;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * OperationLog动态SQL提供者
 *
 * @author g00940940
 * @since 2025-09-17
 */
public class OperationLogSqlProvider {

    /**
     * 根据条件查询操作记录
     *
     * @param params 查询参数
     * @return SQL语句
     */
    public String findByConditions(Map<String, Object> params) {
        return new SQL() {{
            SELECT("*");
            FROM("operation_log");
            
            String username = (String) params.get("username");
            if (username != null && !username.isEmpty()) {
                WHERE("username LIKE CONCAT('%', #{username}, '%')");
            }
            
            String operationType = (String) params.get("operationType");
            if (operationType != null && !operationType.isEmpty()) {
                WHERE("operation_type = #{operationType}");
            }
            
            String target = (String) params.get("target");
            if (target != null && !target.isEmpty()) {
                WHERE("target LIKE CONCAT('%', #{target}, '%')");
            }
            
            LocalDateTime startTime = (LocalDateTime) params.get("startTime");
            if (startTime != null) {
                WHERE("operation_time >= #{startTime}");
            }
            
            LocalDateTime endTime = (LocalDateTime) params.get("endTime");
            if (endTime != null) {
                WHERE("operation_time <= #{endTime}");
            }
            
            ORDER_BY("operation_time DESC");
        }}.toString() + " LIMIT #{pageSize} OFFSET #{pageNo}";
    }

    /**
     * 根据条件统计操作记录总数
     *
     * @param params 查询参数
     * @return SQL语句
     */
    public String countByConditions(Map<String, Object> params) {
        return new SQL() {{
            SELECT("COUNT(*)");
            FROM("operation_log");
            
            String username = (String) params.get("username");
            if (username != null && !username.isEmpty()) {
                WHERE("username LIKE CONCAT('%', #{username}, '%')");
            }
            
            String operationType = (String) params.get("operationType");
            if (operationType != null && !operationType.isEmpty()) {
                WHERE("operation_type = #{operationType}");
            }
            
            String target = (String) params.get("target");
            if (target != null && !target.isEmpty()) {
                WHERE("target LIKE CONCAT('%', #{target}, '%')");
            }
            
            LocalDateTime startTime = (LocalDateTime) params.get("startTime");
            if (startTime != null) {
                WHERE("operation_time >= #{startTime}");
            }
            
            LocalDateTime endTime = (LocalDateTime) params.get("endTime");
            if (endTime != null) {
                WHERE("operation_time <= #{endTime}");
            }
        }}.toString();
    }
}
