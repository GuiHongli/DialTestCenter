package com.huawei.dialtest.center.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.huawei.dialtest.center.entity.TestCaseSet;

/**
 * 用例集Repository接口
 */
@Repository
public interface TestCaseSetRepository extends JpaRepository<TestCaseSet, Long> {

    /**
     * 根据名称和版本查找用例集
     */
    Optional<TestCaseSet> findByNameAndVersion(String name, String version);

    /**
     * 根据名称查找用例集列表
     */
    List<TestCaseSet> findByNameOrderByCreatedTimeDesc(String name);

    /**
     * 根据创建人查找用例集列表
     */
    List<TestCaseSet> findByCreatorOrderByCreatedTimeDesc(String creator);

    /**
     * 分页查询用例集列表
     */
    Page<TestCaseSet> findAllByOrderByCreatedTimeDesc(Pageable pageable);


    /**
     * 检查名称和版本是否存在
     */
    boolean existsByNameAndVersion(String name, String version);
}
