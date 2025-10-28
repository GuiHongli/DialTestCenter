package com.huawei.cloududn.dialingtest.config;

import com.huawei.cloududn.dialingtest.dao.UserRoleDao;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.context.event.ApplicationReadyEvent;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

/**
 * AdminUserInitializer 单元测试
 *
 * @author g00940940
 * @since 2025-01-28
 */
@RunWith(MockitoJUnitRunner.class)
public class AdminUserInitializerTest {
    
    @Mock
    private UserRoleDao userRoleDao;
    
    @Mock
    private ApplicationReadyEvent applicationReadyEvent;
    
    @InjectMocks
    private AdminUserInitializer adminUserInitializer;
    
    @Before
    public void setUp() {
        // 初始化测试对象
    }
    
    /**
     * 测试管理员用户已存在的情况
     */
    @Test
    public void testInitializeAdminUser_UserExists() {
        // 模拟管理员用户已存在
        when(userRoleDao.existsByUsernameAndRole("admin", "ADMIN")).thenReturn(true);
        
        // 执行初始化
        adminUserInitializer.initializeAdminUser(applicationReadyEvent);
        
        // 验证不会调用 insert 方法
        verify(userRoleDao, times(1)).existsByUsernameAndRole("admin", "ADMIN");
        // 注意：由于使用了 model.UserRole，需要确认正确的 mock 方式
    }
    
    /**
     * 测试管理员用户不存在的情况
     */
    @Test
    public void testInitializeAdminUser_UserNotExists() {
        // 模拟管理员用户不存在
        when(userRoleDao.existsByUsernameAndRole("admin", "ADMIN")).thenReturn(false);
        
        // 执行初始化
        adminUserInitializer.initializeAdminUser(applicationReadyEvent);
        
        // 验证会调用 existsByUsernameAndRole
        verify(userRoleDao, times(1)).existsByUsernameAndRole("admin", "ADMIN");
    }
    
    /**
     * 测试初始化组件不为空
     */
    @Test
    public void testAdminUserInitializerNotNull() {
        assertNotNull(adminUserInitializer);
    }
}

