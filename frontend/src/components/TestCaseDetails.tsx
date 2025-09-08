import {
  BugOutlined,
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

const { Title, Text } = Typography

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

  const { translateTestCaseSet, translateCommon } = useTranslation()

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
      message.error('获取测试用例列表失败')
    } finally {
      setLoading(false)
    }
  }

  // 加载缺失脚本信息
  const loadMissingScripts = async () => {
    if (!testCaseSet) return

    try {
      const response = await testCaseSetService.getMissingScripts(testCaseSet.id)
      setMissingScripts(response.data)
    } catch (error) {
      message.error('获取缺失脚本信息失败')
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
      title: '用例编号',
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
      title: '用例名称',
      dataIndex: 'caseName',
      key: 'caseName',
      width: 200,
      ellipsis: true,
    },
    {
      title: '业务大类',
      dataIndex: 'businessCategory',
      key: 'businessCategory',
      width: 120,
      ellipsis: true,
    },
    {
      title: 'App名称',
      dataIndex: 'appName',
      key: 'appName',
      width: 120,
      ellipsis: true,
    },
    {
      title: '脚本状态',
      dataIndex: 'scriptExists',
      key: 'scriptExists',
      width: 100,
      render: (exists: boolean) => (
        <Tag color={exists ? 'success' : 'error'}>
          {exists ? '已匹配' : '缺失'}
        </Tag>
      ),
    },
    {
      title: '操作',
      key: 'action',
      width: 80,
      render: (_, record: TestCase) => (
        <Button
          type="link"
          size="small"
          onClick={() => showTestCaseDetail(record)}
        >
          详情
        </Button>
      ),
    },
  ]

  // 显示测试用例详情
  const showTestCaseDetail = (testCase: TestCase) => {
    Modal.info({
      title: `测试用例详情 - ${testCase.caseNumber}`,
      width: 800,
      content: (
        <Descriptions column={1} bordered>
          <Descriptions.Item label="用例名称">{testCase.caseName}</Descriptions.Item>
          <Descriptions.Item label="用例编号">{testCase.caseNumber}</Descriptions.Item>
          <Descriptions.Item label="逻辑组网">{testCase.networkTopology || '-'}</Descriptions.Item>
          <Descriptions.Item label="业务大类">{testCase.businessCategory || '-'}</Descriptions.Item>
          <Descriptions.Item label="App名称">{testCase.appName || '-'}</Descriptions.Item>
          <Descriptions.Item label="脚本状态">
            <Tag color={testCase.scriptExists ? 'success' : 'error'}>
              {testCase.scriptExists ? '已匹配' : '缺失'}
            </Tag>
          </Descriptions.Item>
          <Descriptions.Item label="测试步骤" span={2}>
            <div style={{ maxHeight: '200px', overflow: 'auto' }}>
              {testCase.testSteps || '-'}
            </div>
          </Descriptions.Item>
          <Descriptions.Item label="预期结果" span={2}>
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
          <span>测试用例详情</span>
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
          关闭
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
                    <div style={{ color: '#666' }}>总用例数</div>
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
                    <div style={{ color: '#666' }}>已匹配</div>
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
                    <div style={{ color: '#666' }}>缺失脚本</div>
                  </div>
                </Space>
              </Card>
            </Col>
            <Col span={6}>
              <Card size="small">
                <Space>
                  <Badge count={`${stats.matchRate}%`} style={{ backgroundColor: '#52c41a' }} />
                  <div>
                    <div style={{ fontSize: '20px', fontWeight: 'bold' }}>匹配率</div>
                    <div style={{ color: '#666' }}>脚本完整性</div>
                  </div>
                </Space>
              </Card>
            </Col>
          </Row>

          {/* 缺失脚本警告 */}
          {stats.missingCount > 0 && (
            <Alert
              message="发现缺失脚本"
              description={`有 ${stats.missingCount} 个测试用例缺少对应的Python脚本文件。请检查scripts目录下是否存在对应的脚本文件。`}
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
                  查看详情
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
                所有用例 ({stats.totalCases})
              </Button>
              <Button
                type={activeTab === 'missing' ? 'primary' : 'default'}
                danger={stats.missingCount > 0}
                onClick={() => setActiveTab('missing')}
              >
                缺失脚本 ({stats.missingCount})
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
                  `第 ${range[0]}-${range[1]} 条，共 ${total} 条`,
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
