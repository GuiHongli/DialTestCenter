package com.huawei.cloududn.dialingtest.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Hello控制器
 * 提供基础的Hello接口
 * 
 * @author DialTestCenter
 * @version 1.0.0
 * @since 2024-01-01
 */
@RestController
@RequestMapping("/api")
public class HelloController {

    /**
     * Hello接口
     * 
     * @return 欢迎信息
     */
    @GetMapping("/hello")
    public Map<String, Object> hello() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Hello, DialTest Center!");
        response.put("status", "success");
        return response;
    }

    /**
     * 健康检查接口
     * 
     * @return 服务状态
     */
    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "dialingtest-service");
        response.put("version", "1.0.0");
        return response;
    }
}

