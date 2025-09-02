import React from 'react'
import { Card, Row, Col, Statistic, Typography, Space } from 'antd'
import { UserOutlined, DashboardOutlined, SettingOutlined, CheckCircleOutlined } from '@ant-design/icons'

const { Title, Paragraph } = Typography

const Home: React.FC = () => {
  return (
    <div>
      <Title level={2}>欢迎使用 Dial Test Center</Title>
      <Paragraph>
        这是一个集成了前台页面和后台实现的全栈服务系统，提供用户管理、数据统计等功能。
      </Paragraph>
      
      <Row gutter={[16, 16]} style={{ marginTop: '24px' }}>
        <Col xs={24} sm={12} lg={6}>
          <Card>
            <Statistic
              title="总用户数"
              value={1128}
              prefix={<UserOutlined />}
              valueStyle={{ color: '#3f8600' }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card>
            <Statistic
              title="活跃用户"
              value={93}
              prefix={<CheckCircleOutlined />}
              valueStyle={{ color: '#1890ff' }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card>
            <Statistic
              title="系统状态"
              value="正常"
              prefix={<DashboardOutlined />}
              valueStyle={{ color: '#52c41a' }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card>
            <Statistic
              title="配置项"
              value={25}
              prefix={<SettingOutlined />}
              valueStyle={{ color: '#722ed1' }}
            />
          </Card>
        </Col>
      </Row>

      <Row gutter={[16, 16]} style={{ marginTop: '24px' }}>
        <Col xs={24} lg={12}>
          <Card title="系统信息" bordered={false}>
            <Space direction="vertical" size="large" style={{ width: '100%' }}>
              <div>
                <strong>前端技术栈：</strong> React 18 + TypeScript + Ant Design + Vite
              </div>
              <div>
                <strong>后端技术栈：</strong> Spring Boot 3.4.5 + JDK 21 + PostgreSQL + JUnit 4 + Mockito
              </div>
              <div>
                <strong>开发环境：</strong> 支持热重载、TypeScript 类型检查、ESLint 代码规范
              </div>
            </Space>
          </Card>
        </Col>
        <Col xs={24} lg={12}>
          <Card title="快速开始" bordered={false}>
            <Space direction="vertical" size="middle" style={{ width: '100%' }}>
              <div>1. 启动后端服务：<code>./mvnw spring-boot:run</code></div>
              <div>2. 启动前端服务：<code>npm run dev</code></div>
              <div>3. 访问系统：<code>http://localhost:3000</code></div>
              <div>4. 查看API文档：<code>http://localhost:8080/api</code></div>
            </Space>
          </Card>
        </Col>
      </Row>
    </div>
  )
}

export default Home
