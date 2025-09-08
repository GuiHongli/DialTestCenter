/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.dialtest.center.config;

import com.huawei.dialtest.center.entity.Role;
import com.huawei.dialtest.center.entity.UserRole;
import com.huawei.dialtest.center.service.UserRoleService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

/**
 * 管理员初始化器，应用启动时自动创建默认管理员账号
 * 负责在系统启动时检查并创建必要的管理员用户角色
 *
 * @author g00940940
 * @since 2025-09-06
 */
@Component
public class AdminInitializer implements ApplicationRunner {
    private static final Logger logger = LoggerFactory.getLogger(AdminInitializer.class);

    @Autowired
    private UserRoleService userRoleService;

    @Value("${app.admin.default-username:admin}")
    private String defaultAdminUsername;

    @Value("${app.admin.auto-create:true}")
    private boolean autoCreateAdmin;

    /**
     * 应用程序启动时执行初始化逻辑
     *
     * @param args 应用程序启动参数
     * @throws Exception 初始化过程中的异常
     */
    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (autoCreateAdmin) {
            initializeDefaultAdmin();
        } else {
            logger.info("Auto-create admin feature is disabled");
        }
    }

    /**
     * 初始化默认管理员账号
     */
    private void initializeDefaultAdmin() {
        try {
            if (!userRoleService.hasAdminUser()) {
                UserRole adminRole = new UserRole();
                adminRole.setUsername(defaultAdminUsername);
                adminRole.setRole(Role.ADMIN);

                userRoleService.save(adminRole);

                logger.info("Default admin account created: {}", defaultAdminUsername);
            } else {
                long adminCount = userRoleService.getAdminUserCount();
                logger.info("System already has {} admin accounts, skipping default admin creation", adminCount);
            }
        } catch (IllegalArgumentException e) {
            logger.error("Invalid parameters for admin initialization: {}", e.getMessage(), e);
        } catch (DataAccessException e) {
            logger.error("Database error during admin initialization: {}", e.getMessage(), e);
        } catch (IllegalStateException e) {
            logger.error("Service state error during admin initialization: {}", e.getMessage(), e);
        }
    }
}
