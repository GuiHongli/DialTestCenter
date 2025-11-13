/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.cloududn.dialingtest.dao;

import com.huawei.cloududn.dialingtest.model.Alarm;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * AlarmLevelEnum类型处理器
 * 用于MyBatis将数据库中的字符串值（如"Urgent"）映射到Alarm.AlarmLevelEnum枚举
 *
 * @author g00940940
 * @since 2025-11-13
 */
@MappedTypes(Alarm.AlarmLevelEnum.class)
public class AlarmLevelEnumTypeHandler extends BaseTypeHandler<Alarm.AlarmLevelEnum> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Alarm.AlarmLevelEnum parameter, JdbcType jdbcType) throws SQLException {
        // 将枚举值转换为字符串存储到数据库
        ps.setString(i, parameter.toString());
    }

    @Override
    public Alarm.AlarmLevelEnum getNullableResult(ResultSet rs, String columnName) throws SQLException {
        // 从结果集中获取字符串值，使用fromValue方法转换为枚举
        String value = rs.getString(columnName);
        return value == null ? null : Alarm.AlarmLevelEnum.fromValue(value);
    }

    @Override
    public Alarm.AlarmLevelEnum getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        // 从结果集中获取字符串值，使用fromValue方法转换为枚举
        String value = rs.getString(columnIndex);
        return value == null ? null : Alarm.AlarmLevelEnum.fromValue(value);
    }

    @Override
    public Alarm.AlarmLevelEnum getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        // 从存储过程结果中获取字符串值，使用fromValue方法转换为枚举
        String value = cs.getString(columnIndex);
        return value == null ? null : Alarm.AlarmLevelEnum.fromValue(value);
    }
}

