package com.huawei.cloududn.dialingtest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 拨测服务主应用程序类
 * 
 * @author DialTestCenter
 * @version 1.0.0
 * @since 2024-01-01
 */
@SpringBootApplication
public class Application {

    /**
     * 应用程序主入口
     * 
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}