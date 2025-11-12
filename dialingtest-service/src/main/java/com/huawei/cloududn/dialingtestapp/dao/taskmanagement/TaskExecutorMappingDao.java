/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2025. All rights reserved.
 */

package com.huawei.cloududn.dialingtestapp.dao.taskmanagement;

import com.huawei.cloududn.dialingtestapp.entity.TaskExecutorMapping;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

/**
 * 任务执行机映射表DAO
 *
 * @author g00940940
 * @since 2025-11-09
 */
@Repository
@Mapper
public interface TaskExecutorMappingDao {

    /**
     * 插入任务执行机映射记录
     *
     * @param mapping 映射实体
     * @return 影响行数
     */
    @Insert("INSERT INTO task_executor_mapping(task_id, executor_name, ue_serial, assign_time) " +
            "VALUES(#{taskId}, #{executorName}, #{ueSerial}, #{assignTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(TaskExecutorMapping mapping);

    /**
     * 根据taskId查询映射记录
     *
     * @param taskId 任务ID
     * @return 映射实体
     */
    @Select("SELECT id, task_id AS taskId, executor_name AS executorName, ue_serial AS ueSerial, " +
            "assign_time AS assignTime FROM task_executor_mapping WHERE task_id = #{taskId}")
    TaskExecutorMapping findByTaskId(@Param("taskId") String taskId);
}

