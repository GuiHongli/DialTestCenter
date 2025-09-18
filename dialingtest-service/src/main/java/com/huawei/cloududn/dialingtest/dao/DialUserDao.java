/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.cloududn.dialingtest.dao;

import com.huawei.cloududn.dialingtest.model.DialUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 拨测用户数据访问层
 *
 * @author g00940940
 * @since 2025-09-18
 */
@Repository
public class DialUserDao {
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    private static final RowMapper<DialUser> DIAL_USER_ROW_MAPPER = new RowMapper<DialUser>() {
        @Override
        public DialUser mapRow(ResultSet rs, int rowNum) throws SQLException {
            DialUser dialUser = new DialUser();
            dialUser.setId(rs.getInt("id"));
            dialUser.setUsername(rs.getString("username"));
            dialUser.setPassword(rs.getString("password"));
            if (rs.getTimestamp("last_login_time") != null) {
                dialUser.setLastLoginTime(rs.getTimestamp("last_login_time").toLocalDateTime());
            }
            return dialUser;
        }
    };
    
    /**
     * 插入拨测用户
     *
     * @param dialUser 拨测用户
     * @return 插入的用户ID
     */
    public Integer insert(DialUser dialUser) {
        String sql = "INSERT INTO dialuser (username, password, last_login_time) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, dialUser.getUsername(), dialUser.getPassword(), dialUser.getLastLoginTime());
        
        // 获取插入的ID
        String getIdSql = "SELECT LAST_INSERT_ID()";
        return jdbcTemplate.queryForObject(getIdSql, Integer.class);
    }
    
    /**
     * 根据ID查询拨测用户
     *
     * @param id 用户ID
     * @return 拨测用户
     */
    public DialUser findById(Integer id) {
        String sql = "SELECT * FROM dialuser WHERE id = ?";
        List<DialUser> users = jdbcTemplate.query(sql, DIAL_USER_ROW_MAPPER, id);
        return users.isEmpty() ? null : users.get(0);
    }
    
    /**
     * 根据用户名查询拨测用户
     *
     * @param username 用户名
     * @return 拨测用户
     */
    public DialUser findByUsername(String username) {
        String sql = "SELECT * FROM dialuser WHERE username = ?";
        List<DialUser> users = jdbcTemplate.query(sql, DIAL_USER_ROW_MAPPER, username);
        return users.isEmpty() ? null : users.get(0);
    }
    
    /**
     * 分页查询拨测用户
     *
     * @param username 用户名过滤条件
     * @param offset 偏移量
     * @param limit 限制数量
     * @return 拨测用户列表
     */
    public List<DialUser> findByPage(String username, int offset, int limit) {
        StringBuilder sql = new StringBuilder("SELECT * FROM dialuser");
        if (username != null && !username.trim().isEmpty()) {
            sql.append(" WHERE username LIKE ?");
            sql.append(" LIMIT ?, ?");
            return jdbcTemplate.query(sql.toString(), DIAL_USER_ROW_MAPPER, "%" + username + "%", offset, limit);
        } else {
            sql.append(" LIMIT ?, ?");
            return jdbcTemplate.query(sql.toString(), DIAL_USER_ROW_MAPPER, offset, limit);
        }
    }
    
    /**
     * 统计拨测用户总数
     *
     * @param username 用户名过滤条件
     * @return 总数
     */
    public Long count(String username) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM dialuser");
        if (username != null && !username.trim().isEmpty()) {
            sql.append(" WHERE username LIKE ?");
            return jdbcTemplate.queryForObject(sql.toString(), Long.class, "%" + username + "%");
        } else {
            return jdbcTemplate.queryForObject(sql.toString(), Long.class);
        }
    }
    
    /**
     * 更新拨测用户
     *
     * @param dialUser 拨测用户
     * @return 更新行数
     */
    public int update(DialUser dialUser) {
        String sql = "UPDATE dialuser SET username = ?, password = ?, last_login_time = ? WHERE id = ?";
        return jdbcTemplate.update(sql, dialUser.getUsername(), dialUser.getPassword(), 
                dialUser.getLastLoginTime(), dialUser.getId());
    }
    
    /**
     * 删除拨测用户
     *
     * @param id 用户ID
     * @return 删除行数
     */
    public int deleteById(Integer id) {
        String sql = "DELETE FROM dialuser WHERE id = ?";
        return jdbcTemplate.update(sql, id);
    }
    
    /**
     * 检查用户名是否存在
     *
     * @param username 用户名
     * @param excludeId 排除的用户ID（用于更新时检查）
     * @return 是否存在
     */
    public boolean existsByUsername(String username, Integer excludeId) {
        String sql;
        if (excludeId != null) {
            sql = "SELECT COUNT(*) FROM dialuser WHERE username = ? AND id != ?";
            Long count = jdbcTemplate.queryForObject(sql, Long.class, username, excludeId);
            return count > 0;
        } else {
            sql = "SELECT COUNT(*) FROM dialuser WHERE username = ?";
            Long count = jdbcTemplate.queryForObject(sql, Long.class, username);
            return count > 0;
        }
    }
}
