import {
    DashboardOutlined,
    FileZipOutlined,
    GlobalOutlined,
    HomeOutlined,
    MenuFoldOutlined,
    MenuUnfoldOutlined,
    SettingOutlined,
    UserOutlined,
    AppstoreOutlined,
} from '@ant-design/icons'
import { Layout as AntLayout, Button, Dropdown, Menu, Space, theme } from 'antd'
import React, { useState } from 'react'
import { useLocation, useNavigate } from 'react-router-dom'
import { useLanguage, useTranslation } from '../hooks/useTranslation'

const { Header, Sider, Content } = AntLayout

interface LayoutProps {
  children: React.ReactNode
}

const Layout: React.FC<LayoutProps> = ({ children }) => {
  const [collapsed, setCollapsed] = useState(false)
  const navigate = useNavigate()
  const location = useLocation()
  const { translateNavigation, translateApp, translateLanguage } = useTranslation()
  const { currentLanguage, setLanguage } = useLanguage()
  const {
    token: { colorBgContainer },
  } = theme.useToken()

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
      icon: <UserOutlined />,
      label: translateNavigation('userManagement'),
      children: [
        {
          key: '/users',
          icon: <UserOutlined />,
          label: translateNavigation('userList'),
        },
        {
          key: '/user-roles',
          icon: <UserOutlined />,
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
  ]

  // 语言切换菜单项
  const languageMenuItems = [
    {
      key: 'zh',
      label: translateLanguage('chinese'),
      onClick: () => setLanguage('zh'),
    },
    {
      key: 'en',
      label: translateLanguage('english'),
      onClick: () => setLanguage('en'),
    },
  ]

  const handleMenuClick = ({ key }: { key: string }) => {
    navigate(key)
  }

  return (
    <AntLayout style={{ minHeight: '100vh' }}>
      <Sider trigger={null} collapsible collapsed={collapsed}>
        <div style={{ height: 32, margin: 16, background: 'rgba(255, 255, 255, 0.2)' }} />
        <Menu
          theme="dark"
          mode="inline"
          selectedKeys={[location.pathname]}
          items={menuItems}
          onClick={handleMenuClick}
        />
      </Sider>
      <AntLayout>
        <Header style={{ padding: 0, background: colorBgContainer, display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
          <div style={{ display: 'flex', alignItems: 'center' }}>
            <Button
              type="text"
              icon={collapsed ? <MenuUnfoldOutlined /> : <MenuFoldOutlined />}
              onClick={() => setCollapsed(!collapsed)}
              style={{
                fontSize: '16px',
                width: 64,
                height: 64,
              }}
            />
            <span style={{ fontSize: '18px', fontWeight: 'bold', marginLeft: '16px' }}>
              {translateApp('title')}
            </span>
          </div>
          <div style={{ marginRight: '24px' }}>
            <Dropdown
              menu={{ items: languageMenuItems }}
              placement="bottomRight"
              trigger={['click']}
            >
              <Button type="text" icon={<GlobalOutlined />}>
                <Space>
                  {currentLanguage === 'zh' ? translateLanguage('chinese') : translateLanguage('english')}
                </Space>
              </Button>
            </Dropdown>
          </div>
        </Header>
        <Content
          style={{
            margin: '24px 16px',
            padding: 24,
            minHeight: 280,
          }}
        >
          {children}
        </Content>
      </AntLayout>
    </AntLayout>
  )
}

export default Layout
