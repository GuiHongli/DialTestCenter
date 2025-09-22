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
    public void logUserRoleUpdate(String operatorUsername, String oldUsername, String newUsername, String oldRole, String newRole) {
        try {
            CreateOperationLogRequest request = new CreateOperationLogRequest();
            request.setUsername(operatorUsername);
            request.setOperationType("UPDATE");
            request.setOperationTarget("USER_ROLE");
            
            // 构建操作描述
            StringBuilder descriptionZh = new StringBuilder();
            StringBuilder descriptionEn = new StringBuilder();
            StringBuilder operationData = new StringBuilder();
            
            operationData.append("{");
            boolean hasChanges = false;
            
            // 检查用户名变化
            if (!oldUsername.equals(newUsername)) {
                descriptionZh.append("更新用户名: ").append(oldUsername).append(" -> ").append(newUsername);
                descriptionEn.append("Update username: ").append(oldUsername).append(" -> ").append(newUsername);
                operationData.append("\"oldUsername\": \"").append(oldUsername).append("\", \"newUsername\": \"").append(newUsername).append("\"");
                hasChanges = true;
            }
            
            // 检查角色变化
            if (!oldRole.equals(newRole)) {
                if (hasChanges) {
                    descriptionZh.append("; ");
                    descriptionEn.append("; ");
                    operationData.append(", ");
                }
                descriptionZh.append("更新角色: ").append(oldRole).append(" -> ").append(newRole);
                descriptionEn.append("Update role: ").append(oldRole).append(" -> ").append(newRole);
                operationData.append("\"oldRole\": \"").append(oldRole).append("\", \"newRole\": \"").append(newRole).append("\"");
                hasChanges = true;
            }
            
            // 如果没有变化，记录为无变化
            if (!hasChanges) {
                descriptionZh.append("更新用户角色: ").append(newUsername).append(" (无变化)");
                descriptionEn.append("Update user role: ").append(newUsername).append(" (no changes)");
                operationData.append("\"username\": \"").append(newUsername).append("\", \"role\": \"").append(newRole).append("\", \"changes\": \"none\"");
            }
            
            operationData.append("}");
            
            request.setOperationDescriptionZh(descriptionZh.toString());
            request.setOperationDescriptionEn(descriptionEn.toString());
            request.setOperationData(operationData.toString());
            
            operationLogService.createOperationLog(request);
            logger.debug("Logged user role update operation for user: {} -> {} with role: {} -> {}", oldUsername, newUsername, oldRole, newRole);
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
