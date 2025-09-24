import {
  CheckCircleOutlined,
  CloseCircleOutlined,
  ExclamationCircleOutlined,
  FileTextOutlined,
  InfoCircleOutlined,
} from '@ant-design/icons'
import {
  Alert,
  Badge,
  Button,
  Card,
  Col,
  Descriptions,
  message,
  Modal,
  Row,
  Space,
  Table,
  Tag,
  Typography,
} from 'antd'
import type { ColumnsType } from 'antd/es/table'
import React, { useEffect, useState } from 'react'
import { useTranslation } from '../hooks/useTranslation'
import testCaseSetService from '../services/testCaseSetService'
import { TestCase, TestCaseSet } from '../types/testCaseSet'

const { Text } = Typography

interface TestCaseDetailsProps {
  visible: boolean
  testCaseSet: TestCaseSet | null
  onCancel: () => void
}

const TestCaseDetails: React.FC<TestCaseDetailsProps> = ({
  visible,
  testCaseSet,
  onCancel,
}) => {
  const [testCases, setTestCases] = useState<TestCase[]>([])
  const [missingScripts, setMissingScripts] = useState<TestCase[]>([])
  const [loading, setLoading] = useState(false)
  const [activeTab, setActiveTab] = useState<'all' | 'missing'>('all')
  const [pagination, setPagination] = useState({
    current: 1,
    pageSize: 10,
    total: 0,
  })

  const { translateTestCaseSet } = useTranslation()

  // 加载测试用例列表
  const loadTestCases = async (page: number = 1, pageSize: number = 10) => {
    if (!testCaseSet) return

    try {
      setLoading(true)
      const response = await testCaseSetService.getTestCases(testCaseSet.id, page, pageSize)
      setTestCases(response.data)
      setPagination({
        current: response.page,
        pageSize: response.pageSize,
        total: response.total,
      })
    } catch (error) {
      message.error(translateTestCaseSet('loadFailed'))
    } finally {
      setLoading(false)
    }
  }

  // 加载缺失脚本信息
  const loadMissingScripts = async () => {
    if (!testCaseSet) return

    try {
      const response = await testCaseSetService.getMissingScripts(testCaseSet.id)
      setMissingScripts(response.testCases)
    } catch (error) {
      message.error(translateTestCaseSet('loadFailed'))
    }
  }

  useEffect(() => {
    if (visible && testCaseSet) {
      loadTestCases()
      loadMissingScripts()
    }
  }, [visible, testCaseSet])

  // 处理分页变化
  const handleTableChange = (pagination: any) => {
    loadTestCases(pagination.current, pagination.pageSize)
  }

  // 统计信息
  const getStatistics = () => {
    const totalCases = pagination.total
    const missingCount = missingScripts.length
    const matchedCount = totalCases - missingCount
    const matchRate = totalCases > 0 ? ((matchedCount / totalCases) * 100).toFixed(1) : '0'

    return {
      totalCases,
      matchedCount,
      missingCount,
      matchRate,
    }
  }

  const stats = getStatistics()

  // 测试用例表格列定义
  const testCaseColumns: ColumnsType<TestCase> = [
    {
      title: translateTestCaseSet('details.caseNumber'),
      dataIndex: 'caseNumber',
      key: 'caseNumber',
      width: 120,
      render: (text: string, record: TestCase) => (
        <Space>
          <Text code>{text}</Text>
          {record.scriptExists ? (
            <CheckCircleOutlined style={{ color: '#52c41a' }} />
          ) : (
            <CloseCircleOutlined style={{ color: '#ff4d4f' }} />
          )}
        </Space>
      ),
    },
    {
      title: translateTestCaseSet('details.caseName'),
      dataIndex: 'caseName',
      key: 'caseName',
      width: 200,
      ellipsis: true,
    },
    {
      title: translateTestCaseSet('details.businessCategory'),
      dataIndex: 'businessCategory',
      key: 'businessCategory',
      width: 120,
      ellipsis: true,
    },
    {
      title: translateTestCaseSet('details.appName'),
      dataIndex: 'appName',
      key: 'appName',
      width: 120,
      ellipsis: true,
    },
    {
      title: translateTestCaseSet('details.scriptStatus'),
      dataIndex: 'scriptExists',
      key: 'scriptExists',
      width: 100,
      render: (exists: boolean) => (
        <Tag color={exists ? 'success' : 'error'}>
          {exists ? translateTestCaseSet('details.matched') : translateTestCaseSet('details.missing')}
        </Tag>
      ),
    },
    {
      title: translateTestCaseSet('details.action'),
      key: 'action',
      width: 80,
      render: (_, record: TestCase) => (
        <Button
          type="link"
          size="small"
          onClick={() => showTestCaseDetail(record)}
        >
          {translateTestCaseSet('details.details')}
        </Button>
      ),
    },
  ]

  // 显示测试用例详情
  const showTestCaseDetail = (testCase: TestCase) => {
    Modal.info({
      title: translateTestCaseSet('details.testCaseDetailTitle', { caseNumber: testCase.caseNumber }),
      width: 800,
      content: (
        <Descriptions column={1} bordered>
          <Descriptions.Item label={translateTestCaseSet('details.caseName')}>{testCase.caseName}</Descriptions.Item>
          <Descriptions.Item label={translateTestCaseSet('details.caseNumber')}>{testCase.caseNumber}</Descriptions.Item>
          <Descriptions.Item label={translateTestCaseSet('details.businessCategory')}>{testCase.businessCategory || '-'}</Descriptions.Item>
          <Descriptions.Item label={translateTestCaseSet('details.appName')}>{testCase.appName || '-'}</Descriptions.Item>
          <Descriptions.Item label={translateTestCaseSet('details.scriptStatus')}>
            <Tag color={testCase.scriptExists ? 'success' : 'error'}>
              {testCase.scriptExists ? translateTestCaseSet('details.matched') : translateTestCaseSet('details.missing')}
            </Tag>
          </Descriptions.Item>
          <Descriptions.Item label={translateTestCaseSet('details.dependenciesPackage')} span={2}>
            <div style={{ maxHeight: '200px', overflow: 'auto' }}>
              {testCase.dependenciesPackage || '-'}
            </div>
          </Descriptions.Item>
          <Descriptions.Item label={translateTestCaseSet('details.dependenciesRule')} span={2}>
            <div style={{ maxHeight: '200px', overflow: 'auto' }}>
              {testCase.dependenciesRule || '-'}
            </div>
          </Descriptions.Item>
          <Descriptions.Item label={translateTestCaseSet('details.environmentConfig')} span={2}>
            <div style={{ maxHeight: '200px', overflow: 'auto' }}>
              {testCase.environmentConfig || '-'}
            </div>
          </Descriptions.Item>
          <Descriptions.Item label={translateTestCaseSet('details.testSteps')} span={2}>
            <div style={{ maxHeight: '200px', overflow: 'auto' }}>
              {testCase.testSteps || '-'}
            </div>
          </Descriptions.Item>
          <Descriptions.Item label={translateTestCaseSet('details.expectedResult')} span={2}>
            <div style={{ maxHeight: '200px', overflow: 'auto' }}>
              {testCase.expectedResult || '-'}
            </div>
          </Descriptions.Item>
        </Descriptions>
      ),
    })
  }

  return (
    <Modal
      title={
        <Space>
          <FileTextOutlined />
          <span>{translateTestCaseSet('details.title')}</span>
          {testCaseSet && (
            <Tag color="blue">{testCaseSet.name} - {testCaseSet.version}</Tag>
          )}
        </Space>
      }
      open={visible}
      onCancel={onCancel}
      width={1200}
      footer={[
        <Button key="close" onClick={onCancel}>
          {translateTestCaseSet('details.close')}
        </Button>,
      ]}
    >
      {testCaseSet && (
        <div>
          {/* 统计信息 */}
          <Row gutter={16} style={{ marginBottom: '16px' }}>
            <Col span={6}>
              <Card size="small">
                <Space>
                  <InfoCircleOutlined style={{ color: '#1890ff' }} />
                  <div>
                    <div style={{ fontSize: '20px', fontWeight: 'bold' }}>{stats.totalCases}</div>
                    <div style={{ color: '#666' }}>{translateTestCaseSet('details.totalCases')}</div>
                  </div>
                </Space>
              </Card>
            </Col>
            <Col span={6}>
              <Card size="small">
                <Space>
                  <CheckCircleOutlined style={{ color: '#52c41a' }} />
                  <div>
                    <div style={{ fontSize: '20px', fontWeight: 'bold' }}>{stats.matchedCount}</div>
                    <div style={{ color: '#666' }}>{translateTestCaseSet('details.matched')}</div>
                  </div>
                </Space>
              </Card>
            </Col>
            <Col span={6}>
              <Card size="small">
                <Space>
                  <CloseCircleOutlined style={{ color: '#ff4d4f' }} />
                  <div>
                    <div style={{ fontSize: '20px', fontWeight: 'bold' }}>{stats.missingCount}</div>
                    <div style={{ color: '#666' }}>{translateTestCaseSet('details.missingScripts')}</div>
                  </div>
                </Space>
              </Card>
            </Col>
            <Col span={6}>
              <Card size="small">
                <Space>
                  <Badge count={`${stats.matchRate}%`} style={{ backgroundColor: '#52c41a' }} />
                  <div>
                    <div style={{ fontSize: '20px', fontWeight: 'bold' }}>{translateTestCaseSet('details.matchRate')}</div>
                    <div style={{ color: '#666' }}>{translateTestCaseSet('details.scriptIntegrity')}</div>
                  </div>
                </Space>
              </Card>
            </Col>
          </Row>

          {/* 缺失脚本警告 */}
          {stats.missingCount > 0 && (
            <Alert
              message={translateTestCaseSet('details.missingScriptsAlert')}
              description={translateTestCaseSet('details.missingScriptsDescription', { count: stats.missingCount })}
              type="warning"
              icon={<ExclamationCircleOutlined />}
              showIcon
              style={{ marginBottom: '16px' }}
              action={
                <Button
                  size="small"
                  type="primary"
                  onClick={() => setActiveTab('missing')}
                >
                  {translateTestCaseSet('details.viewDetails')}
                </Button>
              }
            />
          )}

          {/* 标签页切换 */}
          <div style={{ marginBottom: '16px' }}>
            <Space>
              <Button
                type={activeTab === 'all' ? 'primary' : 'default'}
                onClick={() => setActiveTab('all')}
              >
                {translateTestCaseSet('details.allCases')} ({stats.totalCases})
              </Button>
              <Button
                type={activeTab === 'missing' ? 'primary' : 'default'}
                danger={stats.missingCount > 0}
                onClick={() => setActiveTab('missing')}
              >
                {translateTestCaseSet('details.missingScriptsTab')} ({stats.missingCount})
              </Button>
            </Space>
          </div>

          {/* 表格内容 */}
          {activeTab === 'all' ? (
            <Table
              columns={testCaseColumns}
              dataSource={testCases}
              rowKey="id"
              loading={loading}
              pagination={{
                ...pagination,
                showSizeChanger: true,
                showQuickJumper: true,
                showTotal: (total, range) =>
                  translateTestCaseSet('table.pagination', { start: range[0], end: range[1], total }),
              }}
              onChange={handleTableChange}
            />
          ) : (
            <Table
              columns={testCaseColumns}
              dataSource={missingScripts}
              rowKey="id"
              loading={loading}
              pagination={false}
            />
          )}
        </div>
      )}
    </Modal>
  )
}

export default TestCaseDetails
