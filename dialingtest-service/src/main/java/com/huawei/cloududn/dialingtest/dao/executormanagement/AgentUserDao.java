package com.huawei.cloududn.dialingtest.dao.executormanagement;

import com.huawei.cloududn.dialingtest.model.AgentUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

/**
 * DAO for agent_user table.
 */
@Repository
@Mapper
public interface AgentUserDao {

    /**
     * Find one by username.
     *
     * @param username username
     * @return entity or null
     */
    @Select("SELECT id, username, password, last_login_time AS lastLoginTime FROM agent_user WHERE username = #{username}")
    AgentUser findByUsername(@Param("username") String username);
}


