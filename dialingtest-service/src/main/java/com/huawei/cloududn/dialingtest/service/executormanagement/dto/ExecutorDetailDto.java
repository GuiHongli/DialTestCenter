/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2025. All rights reserved.
 */

package com.huawei.cloududn.dialingtest.service.executormanagement.dto;

import com.huawei.cloududn.dialingtest.controller.executormanagement.websocket.dto.UeItemDto;

import java.util.Date;
import java.time.Instant;
import java.util.List;

/**
 * 执行机详情DTO
 * 包含执行机基本信息和关联的UE列表
 *
 * @author g00940940
 * @since 2025-11-11
 */
public class ExecutorDetailDto {

    private String name;
    private String ip;
    private Integer status;
    private String lastOnlineTime;
    private List<UeItemDto> ueList;

    public ExecutorDetailDto() {
    }

    public ExecutorDetailDto(String name, String ip, Integer status, String lastOnlineTime, List<UeItemDto> ueList) {
        this.name = name;
        this.ip = ip;
        this.status = status;
        this.lastOnlineTime = lastOnlineTime;
        this.ueList = ueList;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getLastOnlineTime() {
        return lastOnlineTime;
    }

    public void setLastOnlineTime(String lastOnlineTime) {
        this.lastOnlineTime = lastOnlineTime;
    }

    public List<UeItemDto> getUeList() {
        return ueList;
    }

    public void setUeList(List<UeItemDto> ueList) {
        this.ueList = ueList;
    }

    @Override
    public String toString() {
        return "ExecutorDetailDto{" +
                "name='" + name + '\'' +
                ", ip='" + ip + '\'' +
                ", status=" + status +
                ", lastOnlineTime=" + lastOnlineTime +
                ", ueList=" + (ueList != null ? ueList.size() : 0) + " items" +
                '}';
    }
}
