package com.huawei.cloududn.dialingtest.service;

import com.huawei.cloududn.dialingtest.dao.RoleDao;
import com.huawei.cloududn.dialingtest.dao.UserRoleDao;
import com.huawei.cloududn.dialingtest.model.*;
import com.huawei.cloududn.dialingtest.util.UserRoleOperationLogUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户角色管理服务
 */
@Service
@Transactional
public class UserRoleService {
    
    @Autowired
    private UserRoleDao userRoleDao;
    
    @Autowired
    private RoleDao roleDao;
    
    @Autowired
    private UserRoleOperationLogUtil operationLogUtil;
    
    /**
     * 创建用户角色关系
     */
    public UserRole createUserRole(String username, String role, String operatorUsername) {
        // 验证角色有效性
        if (!isValidRole(role)) {
            throw new IllegalArgumentException("无效的角色: " + role);
        }
        
        // 检查是否已存在
        if (userRoleDao.existsByUsernameAndRole(username, role)) {
            throw new IllegalArgumentException("用户角色关系已存在");
        }
        
        // 创建用户角色关系
        UserRole userRole = new UserRole();
        userRole.setUsername(username);
        userRole.setRole(UserRole.RoleEnum.fromValue(role));
        
        userRoleDao.insert(userRole);
        
        // 记录操作日志到操作记录模块
        operationLogUtil.logUserRoleCreate(operatorUsername, username, role);
        
        return userRole;
    }
    
    /**
     * 分页查询用户角色关系
     */
    public UserRolePageResponseData getUserRolesWithPagination(int page, int size, String search) {
        int offset = page * size;
        List<UserRole> content = userRoleDao.findByUsernameContainingIgnoreCase(search, offset, size);
        int totalElements = userRoleDao.countByUsernameContainingIgnoreCase(search);
        int totalPages = (int) Math.ceil((double) totalElements / size);
        
        UserRolePageResponseData data = new UserRolePageResponseData();
        data.setContent(content);
        data.setTotalElements(totalElements);
        data.setTotalPages(totalPages);
        data.setSize(size);
        data.setNumber(page);
        data.setFirst(page == 0);
        data.setLast(page >= totalPages - 1);
        
        return data;
    }
    
    /**
     * 根据用户名获取用户角色
     */
    public List<String> getUserRolesByUsername(String username) {
        List<UserRole> userRoles = userRoleDao.findByUsername(username);
        return userRoles.stream()
                .map(userRole -> userRole.getRole().toString())
                .collect(Collectors.toList());
    }
    
    /**
     * 检查用户是否拥有指定角色
     */
    public boolean hasRole(String username, String role) {
        return userRoleDao.existsByUsernameAndRole(username, role);
    }
    
    /**
     * 检查用户是否拥有指定角色中的任意一个
     */
    public boolean hasAnyRole(String username, List<String> roles) {
        return userRoleDao.existsByUsernameAndRoleIn(username, roles);
    }
    
    /**
     * 更新用户角色关系
     */
    public UserRole updateUserRole(Integer id, String username, String role, String operatorUsername) {
        // 验证角色有效性
        if (!isValidRole(role)) {
            throw new IllegalArgumentException("无效的角色: " + role);
        }
        
        // 查找现有记录
        UserRole existingUserRole = userRoleDao.findById(id);
        if (existingUserRole == null) {
            throw new IllegalArgumentException("用户角色关系不存在");
        }
        
        String oldUsername = existingUserRole.getUsername();
        String oldRole = existingUserRole.getRole().toString();
        
        // 更新用户角色关系
        existingUserRole.setUsername(username);
        existingUserRole.setRole(UserRole.RoleEnum.fromValue(role));
        
        userRoleDao.update(existingUserRole);
        
        // 记录操作日志
        operationLogUtil.logUserRoleUpdate(operatorUsername, oldUsername, username, oldRole, role);
        
        return existingUserRole;
    }
    
    /**
     * 删除用户角色关系
     */
    public void deleteUserRole(Integer id, String operatorUsername) {
        // 查找现有记录
        UserRole existingUserRole = userRoleDao.findById(id);
        if (existingUserRole == null) {
            throw new IllegalArgumentException("用户角色关系不存在");
        }
        
        String username = existingUserRole.getUsername();
        String role = existingUserRole.getRole().toString();
        
        // 删除用户角色关系
        userRoleDao.deleteById(id);
        
        // 记录操作日志
        operationLogUtil.logUserRoleDelete(operatorUsername, username, role);
    }
    
    /**
     * 获取所有角色定义
     */
    public List<Role> getAllRoles() {
        return roleDao.findAll();
    }
    
    /**
     * 验证角色是否有效
     */
    private boolean isValidRole(String role) {
        return Arrays.asList("ADMIN", "OPERATOR", "BROWSER", "EXECUTOR").contains(role);
    }
}
