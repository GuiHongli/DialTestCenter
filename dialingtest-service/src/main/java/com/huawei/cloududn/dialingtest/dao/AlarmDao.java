/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.cloududn.dialingtest.dao;

import com.huawei.cloududn.dialingtest.model.Alarm;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 告警数据访问对象
 * 提供告警的数据库操作功能
 *
 * @author g00940940
 * @since 2025-01-15
 */
@Mapper
public interface AlarmDao {

    /**
     * 分页查询告警
     *
     * @param page 页码
     * @param size 每页大小
     * @param currentOnly 是否只查询当前告警（未结束的告警）
     * @return 告警列表
     */
    @Select({
        "<script>",
        "SELECT ",
        "    a.id, ",
        "    a.alarm_summary as alarmSummary, ",
        "    a.alarm_description as alarmDescription, ",
        "    a.alarm_level as alarmLevel, ",
        "    TO_CHAR(a.start_time, 'YYYY-MM-DD\"T\"HH24:MI:SS.MS') as startTime, ",
        "    CASE WHEN a.end_time IS NULL THEN NULL ELSE TO_CHAR(a.end_time, 'YYYY-MM-DD\"T\"HH24:MI:SS.MS') END as endTime ",
        "FROM alarms a ",
        "WHERE 1=1 ",
        "<if test='currentOnly != null and currentOnly == true'>",
        "    AND a.end_time IS NULL ",
        "</if>",
        "ORDER BY a.start_time DESC ",
        "LIMIT #{size} OFFSET #{page} * #{size}",
        "</script>"
    })
    List<Alarm> findAlarmsWithPagination(
            @Param("page") Integer page,
            @Param("size") Integer size,
            @Param("currentOnly") Boolean currentOnly);

    /**
     * 统计告警总数
     *
     * @param currentOnly 是否只统计当前告警（未结束的告警）
     * @return 总数
     */
    @Select({
        "<script>",
        "SELECT COUNT(*) ",
        "FROM alarms a ",
        "WHERE 1=1 ",
        "<if test='currentOnly != null and currentOnly == true'>",
        "    AND a.end_time IS NULL ",
        "</if>",
        "</script>"
    })
    Long countAlarms(@Param("currentOnly") Boolean currentOnly);

    /**
     * 根据ID查询告警
     *
     * @param id 告警ID
     * @return 告警对象
     */
    @Select("SELECT a.id, a.alarm_summary as alarmSummary, a.alarm_description as alarmDescription, a.alarm_level as alarmLevel, TO_CHAR(a.start_time, 'YYYY-MM-DD\"T\"HH24:MI:SS.MS') as startTime, CASE WHEN a.end_time IS NULL THEN NULL ELSE TO_CHAR(a.end_time, 'YYYY-MM-DD\"T\"HH24:MI:SS.MS') END as endTime FROM alarms a WHERE a.id = #{id}")
    Alarm findById(@Param("id") Long id);

    /**
     * 保存告警
     *
     * @param alarm 告警对象
     * @return 影响行数
     */
    @Insert("INSERT INTO alarms (alarm_summary, alarm_description, alarm_level) VALUES (#{alarmSummary}, #{alarmDescription}, #{alarmLevel})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int save(Alarm alarm);

    /**
     * 更新告警（设置结束时间）
     *
     * @param id 告警ID
     * @return 影响行数
     */
    @Update("UPDATE alarms SET end_time = CURRENT_TIMESTAMP WHERE id = #{id} AND end_time IS NULL")
    int updateEndTime(@Param("id") Long id);
}

