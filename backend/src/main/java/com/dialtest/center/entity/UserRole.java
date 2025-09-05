package com.dialtest.center.entity;

import java.time.LocalDateTime;
import java.util.Objects;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * 用户角色关系实体
 */
@Entity
@Table(name = "user_role", 
       uniqueConstraints = @UniqueConstraint(name = "uk_username_role", columnNames = {"username", "role"}),
       indexes = {
           @Index(name = "idx_username", columnList = "username"),
           @Index(name = "idx_role", columnList = "role")
       })
public class UserRole {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "username", nullable = false, length = 100)
    private String username;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 50)
    private Role role;
    
    @CreationTimestamp
    @Column(name = "created_time")
    private LocalDateTime createdTime;
    
    @UpdateTimestamp
    @Column(name = "updated_time")
    private LocalDateTime updatedTime;
    
    // 默认构造函数
    public UserRole() {}
    
    // 带参数构造函数
    public UserRole(String username, Role role) {
        this.username = username;
        this.role = role;
    }
    
    // Getter和Setter方法
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public Role getRole() {
        return role;
    }
    
    public void setRole(Role role) {
        this.role = role;
    }
    
    public LocalDateTime getCreatedTime() {
        return createdTime;
    }
    
    public void setCreatedTime(LocalDateTime createdTime) {
        this.createdTime = createdTime;
    }
    
    public LocalDateTime getUpdatedTime() {
        return updatedTime;
    }
    
    public void setUpdatedTime(LocalDateTime updatedTime) {
        this.updatedTime = updatedTime;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserRole userRole = (UserRole) o;
        return Objects.equals(id, userRole.id) &&
               Objects.equals(username, userRole.username) &&
               role == userRole.role;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id, username, role);
    }
    
    @Override
    public String toString() {
        return "UserRole{" +
               "id=" + id +
               ", username='" + username + '\'' +
               ", role=" + role +
               ", createdTime=" + createdTime +
               ", updatedTime=" + updatedTime +
               '}';
    }
}
