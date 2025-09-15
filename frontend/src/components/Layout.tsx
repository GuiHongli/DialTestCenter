import {
    DashboardOutlined,
    FileZipOutlined,
    HomeOutlined,
    MenuFoldOutlined,
    MenuUnfoldOutlined,
    SettingOutlined,
    UserOutlined,
    AppstoreOutlined,
    TeamOutlined,
    DesktopOutlined,
    SafetyCertificateOutlined,
    ToolOutlined,
    HistoryOutlined,
} from '@ant-design/icons'
import { Layout as AntLayout, Button, Menu, Typography } from 'antd'
import React, { useState } from 'react'
import { useLocation, useHistory } from 'react-router-dom'
import { useLanguage, useTranslation } from '../hooks/useTranslation'

const { Header, Sider, Content, Footer } = AntLayout
const { Text } = Typography

interface LayoutProps {
  children: React.ReactNode
}

const Layout: React.FC<LayoutProps> = ({ children }) => {
  const [collapsed, setCollapsed] = useState(false)
  const history = useHistory()
  const location = useLocation()
  const { translateNavigation, translateApp, translateFooter } = useTranslation()
  const { currentLanguage, setLanguage } = useLanguage()

  const menuItems = [
    {
      key: '/',
      icon: <HomeOutlined />,
      label: translateNavigation('home'),
    },
    {
      key: '/dashboard',
      icon: <DashboardOutlined />,
      label: translateNavigation('dashboard'),
    },
    {
      key: 'user',
      icon: <TeamOutlined />,
      label: translateNavigation('userManagement'),
      children: [
        {
          key: '/users',
          icon: <DesktopOutlined />,
          label: translateNavigation('userList'),
        },
        {
          key: '/user-roles',
          icon: <SafetyCertificateOutlined />,
          label: translateNavigation('roleManagement'),
        },
      ],
    },
    {
      key: 'config',
      icon: <SettingOutlined />,
      label: translateNavigation('configManagement'),
      children: [
        {
          key: '/test-case-sets',
          icon: <FileZipOutlined />,
          label: translateNavigation('testCaseSetManagement'),
        },
        {
          key: '/software-packages',
          icon: <AppstoreOutlined />,
          label: translateNavigation('softwarePackageManagement'),
        },
      ],
    },
    {
      key: 'operation',
      icon: <ToolOutlined />,
      label: translateNavigation('operationManagement'),
      children: [
        {
          key: '/operation-logs',
          icon: <HistoryOutlined />,
          label: translateNavigation('operationLogManagement'),
        },
      ],
    },
  ]

  // 语言切换菜单项
  const handleMenuClick = ({ key }: { key: string }) => {
    history.push(key)
  }

  return (
    <AntLayout style={{ minHeight: '100vh' }}>
      <Sider 
        trigger={null} 
        collapsible 
        collapsed={collapsed}
        style={{ position: 'relative' }}
      >
        <div style={{ height: 32, margin: 16, background: 'rgba(255, 255, 255, 0.2)' }} />
        <Menu
          theme="dark"
          mode="inline"
          selectedKeys={[location.pathname]}
          items={menuItems}
          onClick={handleMenuClick}
        />
        {/* 收起按钮 - 放在左下角 */}
        <div style={{
          position: 'absolute',
          bottom: 16,
          left: 16,
          right: 16,
          display: 'flex',
          justifyContent: 'center'
        }}>
          <Button
            type="text"
            icon={collapsed ? <MenuUnfoldOutlined /> : <MenuFoldOutlined />}
            onClick={() => setCollapsed(!collapsed)}
            style={{
              color: 'white',
              fontSize: '16px',
              width: collapsed ? '100%' : '100%',
              height: 40,
              border: '1px solid rgba(255, 255, 255, 0.2)',
              borderRadius: '4px',
              backgroundColor: 'rgba(255, 255, 255, 0.1)',
              transition: 'all 0.3s ease'
            }}
          />
        </div>
      </Sider>
      <AntLayout>
        <Header style={{ padding: 0, background: '#001529', display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
          <div style={{ display: 'flex', alignItems: 'center' }}>
            <span style={{ fontSize: '18px', fontWeight: 'bold', marginLeft: '16px', color: 'white' }}>
              {translateApp('title')}
            </span>
          </div>
        <div style={{ marginRight: '24px', display: 'flex', alignItems: 'center' }}>
          <span 
            onClick={() => setLanguage('zh')}
            style={{ 
              color: currentLanguage === 'zh' ? 'white' : 'rgba(255, 255, 255, 0.6)',
              fontSize: '14px',
              fontWeight: 'bold',
              padding: '8px 12px',
              cursor: 'pointer',
              transition: 'all 0.3s ease'
            }}
          >
            中
          </span>
          <span style={{ color: 'rgba(255, 255, 255, 0.4)', margin: '0 2px' }}>|</span>
          <span 
            onClick={() => setLanguage('en')}
            style={{ 
              color: currentLanguage === 'en' ? 'white' : 'rgba(255, 255, 255, 0.6)',
              fontSize: '14px',
              fontWeight: 'bold',
              padding: '8px 12px',
              cursor: 'pointer',
              transition: 'all 0.3s ease'
            }}
          >
            EN
          </span>
        </div>
        </Header>
        <Content
          style={{
            margin: '24px 16px',
            padding: 24,
            minHeight: 280,
            overflow: 'auto',
            height: 'calc(100vh - 64px - 48px - 60px)',
          }}
        >
          {children}
        </Content>
        <Footer
          style={{
            textAlign: 'center',
            backgroundColor: '#f0f2f5',
            borderTop: '1px solid #d9d9d9',
            padding: '12px 24px',
            height: '60px',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
          }}
        >
          <Text type="secondary" style={{ fontSize: '14px' }}>
            {translateFooter('copyright') || '© 2024 Dial Test Center. 版权所有.'}
          </Text>
        </Footer>
      </AntLayout>
    </AntLayout>
  )
}

export default Layout
