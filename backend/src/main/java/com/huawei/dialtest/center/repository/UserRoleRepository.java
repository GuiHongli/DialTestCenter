package com.huawei.dialtest.center.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.huawei.dialtest.center.entity.Role;
import com.huawei.dialtest.center.entity.UserRole;

/**
 * 用户角色关系数据访问层
 */
@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, Long> {

    /**
     * 根据用户名查询用户角色列表
     * @param username 用户名
     * @return 用户角色列表
     */
    List<UserRole> findByUsername(String username);

    /**
     * 根据用户名查询用户角色列表，按创建时间倒序排列
     * @param username 用户名
     * @return 用户角色列表
     */
    List<UserRole> findByUsernameOrderByCreatedTimeDesc(String username);

    /**
     * 根据用户名查询角色枚举列表
     * @param username 用户名
     * @return 角色枚举列表
     */
    @Query("SELECT ur.role FROM UserRole ur WHERE ur.username = :username")
    List<Role> findRolesByUsername(@Param("username") String username);

    /**
     * 根据用户名和角色查询用户角色关系
     * @param username 用户名
     * @param role 角色
     * @return 用户角色关系
     */
    Optional<UserRole> findByUsernameAndRole(String username, Role role);

    /**
     * 检查用户名和角色组合是否存在
     * @param username 用户名
     * @param role 角色
     * @return 是否存在
     */
    boolean existsByUsernameAndRole(String username, Role role);

    /**
     * 根据角色查询用户角色关系列表
     * @param role 角色
     * @return 用户角色关系列表
     */
    List<UserRole> findByRole(Role role);

    /**
     * 检查是否存在指定角色的用户
     * @param role 角色
     * @return 是否存在
     */
    boolean existsByRole(Role role);

    /**
     * 根据用户名删除所有角色
     * @param username 用户名
     */
    void deleteByUsername(String username);

    /**
     * 根据用户名和角色删除用户角色关系
     * @param username 用户名
     * @param role 角色
     */
    void deleteByUsernameAndRole(String username, Role role);

    /**
     * 统计指定角色的用户数量
     * @param role 角色
     * @return 用户数量
     */
    long countByRole(Role role);

    /**
     * 查询所有用户角色，按创建时间倒序排列
     * @return 用户角色列表
     */
    @Query("SELECT ur FROM UserRole ur ORDER BY ur.createdTime DESC")
    List<UserRole> findAllOrderByCreatedTimeDesc();
}
