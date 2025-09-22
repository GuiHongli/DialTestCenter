package com.huawei.cloududn.dialingtest.util;

import com.huawei.cloududn.dialingtest.model.CreateOperationLogRequest;
import com.huawei.cloududn.dialingtest.service.OperationLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 用户角色操作记录工具类
 */
@Component
public class UserRoleOperationLogUtil {
    
    private static final Logger logger = LoggerFactory.getLogger(UserRoleOperationLogUtil.class);
    
    @Autowired
    private OperationLogService operationLogService;
    
    /**
     * 记录用户角色创建操作
     */
    public void logUserRoleCreate(String operatorUsername, String targetUsername, String role) {
        try {
            CreateOperationLogRequest request = new CreateOperationLogRequest();
            request.setUsername(operatorUsername);
            request.setOperationType("CREATE");
            request.setOperationTarget("USER_ROLE");
            request.setOperationDescriptionZh("创建用户角色: " + targetUsername + " -> " + role);
            request.setOperationDescriptionEn("Create user role: " + targetUsername + " -> " + role);
            request.setOperationData("{\"targetUsername\": \"" + targetUsername + "\", \"role\": \"" + role + "\"}");
            
            operationLogService.createOperationLog(request);
            logger.debug("Logged user role create operation for user: {} with role: {}", targetUsername, role);
        } catch (Exception e) {
            logger.warn("Failed to log user role create operation: {}", e.getMessage());
        }
    }
    
    /**
     * 记录用户角色更新操作
     */
    public void logUserRoleUpdate(String operatorUsername, String targetUsername, String oldRole, String newRole) {
        try {
            CreateOperationLogRequest request = new CreateOperationLogRequest();
            request.setUsername(operatorUsername);
            request.setOperationType("UPDATE");
            request.setOperationTarget("USER_ROLE");
            request.setOperationDescriptionZh("更新用户角色: " + targetUsername + " 从 " + oldRole + " 改为 " + newRole);
            request.setOperationDescriptionEn("Update user role: " + targetUsername + " from " + oldRole + " to " + newRole);
            request.setOperationData("{\"targetUsername\": \"" + targetUsername + "\", \"oldRole\": \"" + oldRole + "\", \"newRole\": \"" + newRole + "\"}");
            
            operationLogService.createOperationLog(request);
            logger.debug("Logged user role update operation for user: {} from {} to {}", targetUsername, oldRole, newRole);
        } catch (Exception e) {
            logger.warn("Failed to log user role update operation: {}", e.getMessage());
        }
    }
    
    /**
     * 记录用户角色删除操作
     */
    public void logUserRoleDelete(String operatorUsername, String targetUsername, String role) {
        try {
            CreateOperationLogRequest request = new CreateOperationLogRequest();
            request.setUsername(operatorUsername);
            request.setOperationType("DELETE");
            request.setOperationTarget("USER_ROLE");
            request.setOperationDescriptionZh("删除用户角色: " + targetUsername + " -> " + role);
            request.setOperationDescriptionEn("Delete user role: " + targetUsername + " -> " + role);
            request.setOperationData("{\"targetUsername\": \"" + targetUsername + "\", \"role\": \"" + role + "\"}");
            
            operationLogService.createOperationLog(request);
            logger.debug("Logged user role delete operation for user: {} with role: {}", targetUsername, role);
        } catch (Exception e) {
            logger.warn("Failed to log user role delete operation: {}", e.getMessage());
        }
    }
}
