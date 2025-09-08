/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.dialtest.center;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * DialTestCenter主应用类，Spring Boot应用程序的入口点
 * 负责启动整个拨测控制中心应用程序
 *
 * @author g00940940
 * @since 2025-09-06
 */
@SpringBootApplication
public class DialTestCenterApplication {
    /**
     * 应用程序主入口方法
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(DialTestCenterApplication.class, args);
    }
}
