/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.cloududn.dialingtest.dao;

import com.huawei.cloududn.dialingtest.model.OperationType;
import com.huawei.cloududn.dialingtest.model.OperationTypeListResponse;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 操作类型数据访问接口，使用MyBatis进行数据库操作
 * 提供操作类型的查询功能
 *
 * @author g00940940
 * @since 2025-01-15
 */
@Mapper
public interface OperationTypeDao {
    
    /**
     * 查询所有操作类型
     *
     * @return 操作类型列表响应
     */
    default OperationTypeListResponse getAllOperationTypes() {
        List<OperationType> types = findAllOperationTypes();
        
        OperationTypeListResponse response = new OperationTypeListResponse();
        response.setSuccess(true);
        response.setMessage("查询成功");
        response.setData(types);
        
        return response;
    }
    
    /**
     * 查询所有操作类型
     *
     * @return 操作类型列表
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
        "FROM operation_types ",
        "WHERE is_active = true ",
        "ORDER BY id"
    })
    List<OperationType> findAllOperationTypes();
}
