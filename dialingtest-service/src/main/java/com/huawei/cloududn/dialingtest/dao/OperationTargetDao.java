/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.cloududn.dialingtest.dao;

import com.huawei.cloududn.dialingtest.model.OperationTarget;
import com.huawei.cloududn.dialingtest.model.OperationTargetListResponse;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 操作目标数据访问接口，使用MyBatis进行数据库操作
 * 提供操作目标的查询功能
 *
 * @author g00940940
 * @since 2025-01-15
 */
@Mapper
public interface OperationTargetDao {
    
    /**
     * 查询所有操作目标
     *
     * @return 操作目标列表响应
     */
    default OperationTargetListResponse getAllOperationTargets() {
        List<OperationTarget> targets = findAllOperationTargets();
        
        OperationTargetListResponse response = new OperationTargetListResponse();
        response.setSuccess(true);
        response.setMessage("查询成功");
        response.setData(targets);
        
        return response;
    }
    
    /**
     * 查询所有操作目标
     *
     * @return 操作目标列表
     */
    @Select({
        "SELECT ",
        "    id, ",
        "    code, ",
        "    name_zh as nameZh, ",
        "    name_en as nameEn, ",
        "    description_zh as descriptionZh, ",
        "    description_en as descriptionEn, ",
        "    is_active as isActive, ",
        "    TO_CHAR(created_at, 'YYYY-MM-DD\"T\"HH24:MI:SS.MS\"Z\"') as createdAt ",
        "FROM operation_targets ",
        "WHERE is_active = true ",
        "ORDER BY id"
    })
    List<OperationTarget> findAllOperationTargets();
}
