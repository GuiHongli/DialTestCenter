/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.dialtest.center.mapper;

import org.apache.ibatis.jdbc.SQL;

import java.util.Map;

/**
 * SoftwarePackage动态SQL提供者
 *
 * @author g00940940
 * @since 2025-09-17
 */
public class SoftwarePackageSqlProvider {

    /**
     * 根据条件查询软件包
     *
     * @param params 查询参数
     * @return SQL语句
     */
    public String findByConditions(Map<String, Object> params) {
        return new SQL() {{
            SELECT("*");
            FROM("software_package");
            
            String platform = (String) params.get("platform");
            if (platform != null && !platform.isEmpty()) {
                WHERE("platform = #{platform}");
            }
            
            String creator = (String) params.get("creator");
            if (creator != null && !creator.isEmpty()) {
                WHERE("creator = #{creator}");
            }
            
            String softwareName = (String) params.get("softwareName");
            if (softwareName != null && !softwareName.isEmpty()) {
                WHERE("LOWER(software_name) LIKE LOWER(CONCAT('%', #{softwareName}, '%'))");
            }
            
            ORDER_BY("created_time DESC");
        }}.toString() + " LIMIT #{pageSize} OFFSET #{pageNo}";
    }

    /**
     * 根据条件统计软件包数量
     *
     * @param params 查询参数
     * @return SQL语句
     */
    public String countByConditions(Map<String, Object> params) {
        return new SQL() {{
            SELECT("COUNT(*)");
            FROM("software_package");
            
            String platform = (String) params.get("platform");
            if (platform != null && !platform.isEmpty()) {
                WHERE("platform = #{platform}");
            }
            
            String creator = (String) params.get("creator");
            if (creator != null && !creator.isEmpty()) {
                WHERE("creator = #{creator}");
            }
            
            String softwareName = (String) params.get("softwareName");
            if (softwareName != null && !softwareName.isEmpty()) {
                WHERE("LOWER(software_name) LIKE LOWER(CONCAT('%', #{softwareName}, '%'))");
            }
        }}.toString();
    }
}
