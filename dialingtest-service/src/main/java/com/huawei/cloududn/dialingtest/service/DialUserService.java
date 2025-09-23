package com.huawei.cloududn.dialingtest.service;

import com.huawei.cloududn.dialingtest.dao.DialUserDao;
import com.huawei.cloududn.dialingtest.model.DialUser;
import com.huawei.cloududn.dialingtest.util.OperationLogUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 拨测用户服务类
 * 
 * @author Generated
 */
@Service
@Transactional
public class DialUserService {
    
    @Autowired
    private DialUserDao dialUserDao;
    
    @Autowired
    private OperationLogUtil operationLogUtil;
    
    @Autowired
    private UserRoleService userRoleService;
    
    /**
     * 分页查询用户
     * 
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @param username 用户名过滤条件
     * @return 用户列表
     */
    @Transactional(readOnly = true)
    public List<DialUser> findUsersWithPagination(int page, int size, String username) {
        int offset = page * size;
        return dialUserDao.findUsersWithPagination(offset, size, username);
    }
    
    /**
     * 统计用户总数
     * 
     * @param username 用户名过滤条件
     * @return 用户总数
     */
    @Transactional(readOnly = true)
    public long countUsers(String username) {
        return dialUserDao.countUsers(username);
    }
    
    /**
     * 根据ID查询用户
     * 
     * @param id 用户ID
     * @return 用户信息
     */
    @Transactional(readOnly = true)
    public DialUser findById(Integer id) {
        return dialUserDao.findById(id);
    }
    
    /**
     * 根据用户名查询用户
     * 
     * @param username 用户名
     * @return 用户信息
     */
    @Transactional(readOnly = true)
    public DialUser findByUsername(String username) {
        return dialUserDao.findByUsername(username);
    }
    
    /**
     * 创建用户
     * 
     * @param username 用户名
     * @param password 密码
     * @param operatorUsername 操作用户名
     * @return 创建的用户
     * @throws IllegalArgumentException 如果用户名已存在
     * @throws IllegalStateException 如果数据库操作失败
     */
    public DialUser createUser(String username, String password, String operatorUsername) {
        // 检查用户名是否已存在
        if (dialUserDao.findByUsername(username) != null) {
            throw new IllegalArgumentException("用户名已存在: " + username);
        }
        
        // 创建新用户
        DialUser user = new DialUser();
        user.setUsername(username);
        user.setPassword(password);
        user.setLastLoginTime(LocalDateTime.now().toString());
        
        int result = dialUserDao.create(user);
        if (result == 0) {
            throw new IllegalStateException("创建用户失败，数据库操作未生效");
        }
        
        // 自动为执行机账号分配EXECUTOR角色
        try {
            userRoleService.createUserRole(username, "EXECUTOR", "SYSTEM");
        } catch (Exception e) {
            // 如果角色分配失败，记录警告但不影响用户创建
            // 这里可以选择抛出异常或记录警告日志
            throw new IllegalStateException("创建用户成功，但角色分配失败: " + e.getMessage());
        }
        
        // 记录操作日志
        String userDetails = "用户名:" + username + ", 密码:已设置, 角色:EXECUTOR";
        operationLogUtil.logUserCreate(operatorUsername, username, userDetails);
        
        return user;
    }
    
    /**
     * 更新用户
     * 
     * @param id 用户ID
     * @param username 用户名
     * @param password 密码
     * @param operatorUsername 操作用户名
     * @return 更新后的用户
     * @throws IllegalArgumentException 如果用户不存在或用户名已存在
     * @throws IllegalStateException 如果数据库操作失败
     */
    public DialUser updateUser(Integer id, String username, String password, String operatorUsername) {
        // 检查用户是否存在
        DialUser existingUser = dialUserDao.findById(id);
        if (existingUser == null) {
            throw new IllegalArgumentException("用户不存在: " + id);
        }
        
        // 保存原始用户名用于日志记录
        String originalUsername = existingUser.getUsername();
        
        // 如果用户名发生变化，检查新用户名是否已存在
        if (!originalUsername.equals(username)) {
            DialUser userWithSameName = dialUserDao.findByUsername(username);
            if (userWithSameName != null && !userWithSameName.getId().equals(id)) {
                throw new IllegalArgumentException("用户名已存在: " + username);
            }
        }
        
        // 更新用户信息
        existingUser.setUsername(username);
        if (password != null && !password.trim().isEmpty()) {
            existingUser.setPassword(password);
        }
        
        int updatedRows = dialUserDao.update(existingUser);
        if (updatedRows == 0) {
            throw new IllegalStateException("更新用户失败，数据库操作未生效");
        }
        
        // 构建变更信息
        StringBuilder oldValues = new StringBuilder();
        StringBuilder newValues = new StringBuilder();
        
        // 记录用户名变更
        if (!originalUsername.equals(username)) {
            oldValues.append("用户名:").append(originalUsername);
            newValues.append("用户名:").append(username);
        }
        
        // 记录密码变更
        if (password != null && !password.trim().isEmpty()) {
            if (oldValues.length() > 0) {
                oldValues.append(", ");
                newValues.append(", ");
            }
            oldValues.append("密码:已设置");
            newValues.append("密码:已更新");
        }
        
        String oldValuesStr = oldValues.length() > 0 ? oldValues.toString() : "无变更";
        String newValuesStr = newValues.length() > 0 ? newValues.toString() : "无变更";
        
        // 记录操作日志
        operationLogUtil.logUserUpdate(operatorUsername, username, oldValuesStr, newValuesStr);
        
        return existingUser;
    }
    
    /**
     * 删除用户
     * 
     * @param id 用户ID
     * @param operatorUsername 操作用户名
     * @throws IllegalArgumentException 如果用户不存在
     * @throws IllegalStateException 如果数据库操作失败
     */
    public void deleteUser(Integer id, String operatorUsername) {
        // 检查用户是否存在
        DialUser user = dialUserDao.findById(id);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在: " + id);
        }
        
        String username = user.getUsername();
        
        int deletedRows = dialUserDao.deleteById(id);
        if (deletedRows == 0) {
            throw new IllegalStateException("删除用户失败，数据库操作未生效");
        }
        
        // 记录操作日志
        String userInfo = "用户名:" + username + ", 最后登录:" + user.getLastLoginTime();
        operationLogUtil.logUserDelete(operatorUsername, username, userInfo);
    }
    
    /**
     * 删除用户（兼容旧接口）
     * 
     * @param id 用户ID
     * @throws IllegalArgumentException 如果用户不存在
     * @throws IllegalStateException 如果数据库操作失败
     */
    public void deleteUser(Integer id) {
        deleteUser(id, "system");
    }
    
    /**
     * 更新最后登录时间
     * 
     * @param id 用户ID
     */
    public void updateLastLoginTime(Integer id) {
        DialUser user = dialUserDao.findById(id);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在: " + id);
        }
        
        int updatedRows = dialUserDao.updateLastLoginTime(id, LocalDateTime.now());
        if (updatedRows == 0) {
            throw new IllegalArgumentException("用户不存在: " + id);
        }
        
        // 记录登录操作日志
        operationLogUtil.logUserLogin(user.getUsername());
    }
}
