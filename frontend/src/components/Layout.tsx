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
    TeamOutlined,
    DesktopOutlined,
    SafetyCertificateOutlined,
    ToolOutlined,
    HistoryOutlined,
} from '@ant-design/icons'
import { Layout as AntLayout, Button, Dropdown, Menu, Space } from 'antd'
import React, { useState } from 'react'
import { useLocation, useHistory } from 'react-router-dom'
import { useLanguage, useTranslation } from '../hooks/useTranslation'

const { Header, Sider, Content } = AntLayout

interface LayoutProps {
  children: React.ReactNode
}

const Layout: React.FC<LayoutProps> = ({ children }) => {
  const [collapsed, setCollapsed] = useState(false)
  const history = useHistory()
  const location = useLocation()
  const { translateNavigation, translateApp, translateLanguage } = useTranslation()
  const { currentLanguage, setLanguage } = useLanguage()
  const colorBgContainer = '#ffffff'

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
    history.push(key)
  }

  return (
    <AntLayout style={{ minHeight: '100vh', overflow: 'hidden' }}>
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
        <Header style={{ padding: 0, background: colorBgContainer as any, display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
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
            overflow: 'auto',
            height: 'calc(100vh - 64px - 48px)',
          }}
        >
          {children}
        </Content>
      </AntLayout>
    </AntLayout>
  )
}

export default Layout
