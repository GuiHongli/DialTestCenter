/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.cloududn.dialingtest.service;

import com.huawei.cloududn.dialingtest.dao.AlarmDao;
import com.huawei.cloududn.dialingtest.model.Alarm;
import com.huawei.cloududn.dialingtest.model.AlarmPageResponse;
import com.huawei.cloududn.dialingtest.model.AlarmPageResponseData;
import com.huawei.cloududn.dialingtest.model.AlarmResponse;
import com.huawei.cloududn.dialingtest.model.CreateAlarmRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 告警服务类，提供告警的业务逻辑处理
 * 包括告警的创建、查询、结束等功能
 *
 * @author g00940940
 * @since 2025-01-15
 */
@Service
public class AlarmService {
    private static final Logger logger = LoggerFactory.getLogger(AlarmService.class);

    @Autowired
    private AlarmDao alarmDao;

    /**
     * 分页查询告警
     *
     * @param page 页码
     * @param size 每页大小
     * @param currentOnly 是否只查询当前告警（未结束的告警）
     * @return 告警分页响应
     */
    public AlarmPageResponse getAlarms(Integer page, Integer size, Boolean currentOnly) {
        logger.debug("Querying alarms with parameters: page={}, size={}, currentOnly={}", 
                    page, size, currentOnly);

        // 参数验证和默认值设置
        if (page == null || page < 0) {
            page = 0;
        }
        if (size == null || size <= 0) {
            size = 20;
        }

        // 查询告警列表
        List<Alarm> alarms = alarmDao.findAlarmsWithPagination(page, size, currentOnly);

        // 查询总数
        Long totalElements = alarmDao.countAlarms(currentOnly);

        // 构建分页响应
        AlarmPageResponse response = new AlarmPageResponse();
        response.setSuccess(true);
        response.setMessage("Query successful");

        AlarmPageResponseData data = new AlarmPageResponseData();
        data.setContent(alarms);
        data.setTotalElements(totalElements.intValue());
        data.setTotalPages((int) Math.ceil((double) totalElements / size));
        data.setSize(size);
        data.setNumber(page);

        response.setData(data);

        logger.debug("Successfully queried {} alarms", alarms.size());
        return response;
    }

    /**
     * 创建告警
     *
     * @param request 创建请求
     * @return 告警响应
     */
    public AlarmResponse createAlarm(CreateAlarmRequest request) {
        logger.debug("Creating alarm: summary={}, level={}", 
                    request.getAlarmSummary(), request.getAlarmLevel());

        // 参数验证
        validateCreateRequest(request);

        // 构建告警对象
        Alarm alarm = new Alarm();
        alarm.setAlarmSummary(request.getAlarmSummary());
        alarm.setAlarmDescription(request.getAlarmDescription());
        // 将枚举类型转换为Alarm的枚举类型
        // OpenAPI生成的枚举类使用toString()方法获取字符串值
        String levelValue = request.getAlarmLevel().toString();
        alarm.setAlarmLevel(Alarm.AlarmLevelEnum.fromValue(levelValue));
        
        // start_time由数据库默认值自动设置
        // end_time默认为null，表示告警未结束

        // 保存到数据库
        int result = alarmDao.save(alarm);

        if (result <= 0) {
            throw new IllegalStateException("Failed to create alarm, database operation did not take effect");
        }

        // 查询保存后的告警（获取完整信息）
        Long alarmId = alarm.getId() != null ? alarm.getId().longValue() : null;
        if (alarmId == null) {
            throw new IllegalStateException("Failed to get alarm ID after creation");
        }
        Alarm savedAlarm = alarmDao.findById(alarmId);

        // 构建响应
        AlarmResponse response = new AlarmResponse();
        response.setSuccess(true);
        response.setMessage("Alarm created successfully");
        response.setData(savedAlarm);

        logger.info("Successfully created alarm with ID: {}", savedAlarm.getId());
        return response;
    }

    /**
     * 结束告警（删除告警）
     *
     * @param id 告警ID
     * @return 告警响应
     */
    public AlarmResponse endAlarm(Integer id) {
        logger.debug("Ending alarm: id={}", id);

        // 参数验证
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid alarm ID");
        }

        // 查询告警记录
        Alarm alarm = alarmDao.findById(id.longValue());
        if (alarm == null) {
            throw new IllegalArgumentException("Alarm not found");
        }

        // 检查告警是否已结束
        if (alarm.getEndTime() != null) {
            throw new IllegalStateException("Alarm already ended, cannot end again");
        }

        // 更新结束时间
        int result = alarmDao.updateEndTime(id.longValue());
        if (result <= 0) {
            throw new IllegalStateException("Failed to end alarm, database operation did not take effect");
        }

        // 查询更新后的告警
        Alarm updatedAlarm = alarmDao.findById(id.longValue());

        // 构建响应
        AlarmResponse response = new AlarmResponse();
        response.setSuccess(true);
        response.setMessage("Alarm ended successfully");
        response.setData(updatedAlarm);

        logger.info("Successfully ended alarm with ID: {}", id);
        return response;
    }

    /**
     * 验证创建请求参数
     *
     * @param request 创建请求
     */
    private void validateCreateRequest(CreateAlarmRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request cannot be null");
        }
        if (request.getAlarmSummary() == null || request.getAlarmSummary().trim().isEmpty()) {
            throw new IllegalArgumentException("Alarm summary cannot be empty");
        }
        if (request.getAlarmSummary().length() > 200) {
            throw new IllegalArgumentException("Alarm summary length cannot exceed 200 characters");
        }
        if (request.getAlarmLevel() == null) {
            throw new IllegalArgumentException("Alarm level cannot be empty");
        }
        // 枚举类型已经由OpenAPI验证，这里只需要检查是否为null
    }
}

