/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.dialtest.center.repository;

import com.huawei.dialtest.center.entity.OperationLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 操作记录数据访问层接口
 * 提供操作记录的基本CRUD操作和查询功能
 *
 * @author g00940940
 * @since 2025-09-09
 */
@Repository
public class OperationLogRepository {
    
    @PersistenceContext
    private EntityManager entityManager;

    /**
     * 根据多个条件查询操作记录，按操作时间倒序排列
     * 使用Criteria API实现动态查询
     *
     * @param username 用户名（可选，支持模糊查询）
     * @param operationType 操作类型（可选）
     * @param target 操作对象（可选）
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @param pageable 分页参数
     * @return 操作记录分页结果
     */
    public Page<OperationLog> findByConditions(String username, String operationType, String target,
                                               LocalDateTime startTime, LocalDateTime endTime,
                                               Pageable pageable) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        
        // 创建查询
        CriteriaQuery<OperationLog> query = cb.createQuery(OperationLog.class);
        Root<OperationLog> root = query.from(OperationLog.class);
        
        // 构建动态条件
        List<Predicate> predicates = new ArrayList<>();
        
        if (username != null && !username.trim().isEmpty()) {
            predicates.add(cb.like(root.get("username"), "%" + username + "%"));
        }
        
        if (operationType != null && !operationType.trim().isEmpty()) {
            predicates.add(cb.equal(root.get("operationType"), operationType));
        }
        
        if (target != null && !target.trim().isEmpty()) {
            predicates.add(cb.equal(root.get("target"), target));
        }
        
        if (startTime != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("operationTime"), startTime));
        }
        
        if (endTime != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("operationTime"), endTime));
        }
        
        // 应用条件
        if (!predicates.isEmpty()) {
            query.where(cb.and(predicates.toArray(new Predicate[0])));
        }
        
        // 排序
        query.orderBy(cb.desc(root.get("operationTime")));
        
        // 执行查询
        List<OperationLog> results = entityManager.createQuery(query)
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize())
                .getResultList();
        
        // 获取总数
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<OperationLog> countRoot = countQuery.from(OperationLog.class);
        countQuery.select(cb.count(countRoot));
        
        if (!predicates.isEmpty()) {
            countQuery.where(cb.and(predicates.toArray(new Predicate[0])));
        }
        
        Long total = entityManager.createQuery(countQuery).getSingleResult();
        
        return new org.springframework.data.domain.PageImpl<>(results, pageable, total);
    }

    /**
     * 查询所有操作记录，按操作时间倒序排列
     *
     * @param pageable 分页参数
     * @return 操作记录分页结果
     */
    public Page<OperationLog> findAllOrderByOperationTimeDesc(Pageable pageable) {
        return findByConditions(null, null, null, null, null, pageable);
    }

    /**
     * 根据用户名查询操作记录，按操作时间倒序排列
     *
     * @param username 用户名
     * @param pageable 分页参数
     * @return 操作记录分页结果
     */
    public Page<OperationLog> findByUsernameOrderByOperationTimeDesc(String username, Pageable pageable) {
        return findByConditions(username, null, null, null, null, pageable);
    }

    /**
     * 查询最近的N条操作记录
     *
     * @param limit 限制数量
     * @return 操作记录列表
     */
    @SuppressWarnings("unchecked")
    public List<OperationLog> findRecentOperationLogs(int limit) {
        return entityManager.createNativeQuery(
                "SELECT * FROM operation_log ORDER BY operation_time DESC LIMIT ?1", 
                OperationLog.class)
                .setParameter(1, limit)
                .getResultList();
    }
}
