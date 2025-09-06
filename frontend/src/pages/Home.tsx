import { CheckCircleOutlined, DashboardOutlined, SettingOutlined } from '@ant-design/icons'
import { Card, Col, Row, Space, Statistic, Typography } from 'antd'
import React from 'react'
import { useTranslation } from '../hooks/useTranslation'

const { Title, Paragraph } = Typography

const Home: React.FC = () => {
  const { translateHome } = useTranslation()

  return (
    <div>
      <Title level={2}>{translateHome('title')}</Title>
      <Paragraph>
        {translateHome('description')}
      </Paragraph>
      
      <Row gutter={[16, 16]} style={{ marginTop: '24px' }}>
        <Col xs={24} sm={12} lg={6}>
          <Card>
            <Statistic
              title={translateHome('statistics.dialTestTasks')}
              value={25}
              prefix={<CheckCircleOutlined />}
              valueStyle={{ color: '#1890ff' }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card>
            <Statistic
              title={translateHome('statistics.systemStatus')}
              value={translateHome('statistics.normal')}
              prefix={<DashboardOutlined />}
              valueStyle={{ color: '#52c41a' }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card>
            <Statistic
              title={translateHome('statistics.configItems')}
              value={25}
              prefix={<SettingOutlined />}
              valueStyle={{ color: '#722ed1' }}
            />
          </Card>
        </Col>
      </Row>

      <Row gutter={[16, 16]} style={{ marginTop: '24px' }}>
        <Col xs={24} lg={12}>
          <Card title={translateHome('systemInfo.title')} bordered={false}>
            <Space direction="vertical" size="large" style={{ width: '100%' }}>
              <div>
                <strong>{translateHome('systemInfo.frontendStack')}</strong> {translateHome('systemInfo.frontendStackValue')}
              </div>
              <div>
                <strong>{translateHome('systemInfo.backendStack')}</strong> {translateHome('systemInfo.backendStackValue')}
              </div>
              <div>
                <strong>{translateHome('systemInfo.devEnvironment')}</strong> {translateHome('systemInfo.devEnvironmentValue')}
              </div>
            </Space>
          </Card>
        </Col>
        <Col xs={24} lg={12}>
          <Card title={translateHome('quickStart.title')} bordered={false}>
            <Space direction="vertical" size="middle" style={{ width: '100%' }}>
              <div>1. {translateHome('quickStart.step1')}<code>./mvnw spring-boot:run</code></div>
              <div>2. {translateHome('quickStart.step2')}<code>npm run dev</code></div>
              <div>3. {translateHome('quickStart.step3')}<code>http://localhost:3000</code></div>
              <div>4. {translateHome('quickStart.step4')}<code>http://localhost:8080/api</code></div>
            </Space>
          </Card>
        </Col>
      </Row>
    </div>
  )
}

export default Home