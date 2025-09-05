package com.dialtest.center.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.dialtest.center.entity.Role;
import com.dialtest.center.entity.UserRole;
import com.dialtest.center.service.UserRoleService;

/**
 * 管理员初始化器
 * 应用启动时自动创建默认管理员账号
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
    
    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (autoCreateAdmin) {
            initializeDefaultAdmin();
        } else {
            logger.info("自动创建管理员功能已禁用");
        }
    }
    
    /**
     * 初始化默认管理员
     */
    private void initializeDefaultAdmin() {
        try {
            // 检查是否存在管理员
            if (!userRoleService.hasAdminUser()) {
                // 创建默认管理员
                UserRole adminRole = new UserRole();
                adminRole.setUsername(defaultAdminUsername);
                adminRole.setRole(Role.ADMIN);
                
                userRoleService.save(adminRole);
                
                logger.info("默认管理员账号已创建: {}", defaultAdminUsername);
            } else {
                long adminCount = userRoleService.getAdminUserCount();
                logger.info("系统中已存在 {} 个管理员账号，跳过创建默认管理员", adminCount);
            }
            
        } catch (Exception e) {
            logger.error("初始化默认管理员失败: {}", e.getMessage(), e);
            // 不抛出异常，避免影响应用启动
        }
    }
}
