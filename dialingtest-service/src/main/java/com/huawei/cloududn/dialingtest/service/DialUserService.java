package com.huawei.cloududn.dialingtest.service;

import com.huawei.cloududn.dialingtest.dao.DialUserDao;
import com.huawei.cloududn.dialingtest.model.DialUser;
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
     * @return 创建的用户
     * @throws IllegalArgumentException 如果用户名已存在
     * @throws IllegalStateException 如果数据库操作失败
     */
    public DialUser createUser(String username, String password) {
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
        
        return user;
    }
    
    /**
     * 更新用户
     * 
     * @param id 用户ID
     * @param username 用户名
     * @param password 密码
     * @return 更新后的用户
     * @throws IllegalArgumentException 如果用户不存在或用户名已存在
     * @throws IllegalStateException 如果数据库操作失败
     */
    public DialUser updateUser(Integer id, String username, String password) {
        // 检查用户是否存在
        DialUser existingUser = dialUserDao.findById(id);
        if (existingUser == null) {
            throw new IllegalArgumentException("用户不存在: " + id);
        }
        
        // 如果用户名发生变化，检查新用户名是否已存在
        if (!existingUser.getUsername().equals(username)) {
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
        
        return existingUser;
    }
    
    /**
     * 删除用户
     * 
     * @param id 用户ID
     * @throws IllegalArgumentException 如果用户不存在
     * @throws IllegalStateException 如果数据库操作失败
     */
    public void deleteUser(Integer id) {
        // 检查用户是否存在
        DialUser user = dialUserDao.findById(id);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在: " + id);
        }
        
        int deletedRows = dialUserDao.deleteById(id);
        if (deletedRows == 0) {
            throw new IllegalStateException("删除用户失败，数据库操作未生效");
        }
    }
    
    /**
     * 更新最后登录时间
     * 
     * @param id 用户ID
     */
    public void updateLastLoginTime(Integer id) {
        int updatedRows = dialUserDao.updateLastLoginTime(id, LocalDateTime.now());
        if (updatedRows == 0) {
            throw new IllegalArgumentException("用户不存在: " + id);
        }
    }
}
