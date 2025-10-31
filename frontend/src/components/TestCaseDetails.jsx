import {
  CheckCircleOutlined,
  FileTextOutlined,
  InfoCircleOutlined,
  ReloadOutlined,
  DownloadOutlined,
} from '@ant-design/icons'
import {
  Alert,
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
import React, { useEffect, useState } from 'react'
import { useTranslation } from '../hooks/useTranslation.js'
import { useI18n } from '../contexts/I18nContext.jsx'
import testCaseSetService from '../services/testCaseSetService.js'
import { getValidationResultColumns, showTestCaseDetail } from '../utils/validationUtils.js'

const { Text } = Typography

const TestCaseDetails = ({
  visible,
  testCaseSet,
  onCancel,
}) => {
  const [validationResult, setValidationResult] = useState(null)
  const [loading, setLoading] = useState(false)
  const [exportLoading, setExportLoading] = useState(false)

  const { translateTestCaseSet } = useTranslation()
  const { language } = useI18n()

  // 加载校验结果
  const loadValidationResult = async (forceRefresh = false) => {
    if (!testCaseSet) {
      return
    }

    try {
      setLoading(true)
      const response = await testCaseSetService.getValidationResult(testCaseSet.id, forceRefresh)
      if (response && response.success && response.data && response.data.caseResults) {
        setValidationResult(response.data)
      } else {
        setValidationResult(null)
      }
    } catch (error) {
      console.error('Failed to load validation result:', error)
      setValidationResult(null)
    } finally {
      setLoading(false)
    }
  }

  // 刷新校验结果
  const handleRefresh = () => {
    loadValidationResult(true)
  }

  // 导出Excel
  const handleExport = async () => {
    if (!testCaseSet || !validationResult) {
      message.warning(translateTestCaseSet('validation.noResultToExport'))
      return
    }

    try {
      setExportLoading(true)
      await testCaseSetService.exportValidationResult(testCaseSet.id)
      message.success(translateTestCaseSet('validation.exportSuccess'))
    } catch (error) {
      console.error('Failed to export validation result:', error)
      message.error(translateTestCaseSet('validation.exportFailed'))
    } finally {
      setExportLoading(false)
    }
  }

  useEffect(() => {
    if (visible && testCaseSet) {
      loadValidationResult()
    } else {
      setValidationResult(null)
    }
  }, [visible, testCaseSet])

  // 格式化时间
  const formatDateTime = (dateTimeStr) => {
    if (!dateTimeStr) {
      return '-'
    }
    try {
      const date = new Date(dateTimeStr)
      const locale = language === 'en' ? 'en-US' : 'zh-CN'
      return date.toLocaleString(locale, {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit',
      })
    } catch (e) {
      return dateTimeStr
    }
  }

  // 获取状态标签
  const getStatusTag = (status) => {
    const statusKey = status || 'UNKNOWN'
    const statusText = translateTestCaseSet(`validation.status.${statusKey}`)
    if (status === 'COMPLETED') {
      return <Tag color="success">{statusText}</Tag>
    } else if (status === 'RUNNING') {
      return <Tag color="processing">{statusText}</Tag>
    } else if (status === 'PENDING') {
      return <Tag color="warning">{statusText}</Tag>
    } else if (status === 'FAILED') {
      return <Tag color="error">{statusText}</Tag>
    } else {
      return <Tag>{statusText}</Tag>
    }
  }

  // 用例详情表格列定义（使用共享工具函数）
  const columns = getValidationResultColumns(
    (record) => showTestCaseDetail(record, translateTestCaseSet),
    translateTestCaseSet
  )

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
      width={1400}
      footer={[
        <Button key="close" onClick={onCancel}>
          {translateTestCaseSet('details.close')}
        </Button>,
      ]}
    >
      {testCaseSet && (
        <div>
          {/* 第一部分：最近一次校验任务信息 */}
          {validationResult && validationResult.validationTaskStatus && (
            <Card size="small" style={{ marginBottom: '16px' }}>
              <Descriptions column={3} size="small">
                <Descriptions.Item label={translateTestCaseSet('validation.taskStatus')}>
                  {getStatusTag(validationResult.validationTaskStatus)}
                </Descriptions.Item>
                <Descriptions.Item label={translateTestCaseSet('validation.triggerTime')}>
                  {formatDateTime(validationResult.validationTaskCreatedTime)}
                </Descriptions.Item>
                <Descriptions.Item label={translateTestCaseSet('validation.completedTime')}>
                  {formatDateTime(validationResult.validationTaskCompletedTime)}
                </Descriptions.Item>
              </Descriptions>
            </Card>
          )}

          {/* 第二部分：统计卡片 */}
          {validationResult ? (
            <Row gutter={16} style={{ marginBottom: '16px' }}>
              <Col span={8}>
                <Card size="small">
                  <Space>
                    <InfoCircleOutlined style={{ color: '#1890ff', fontSize: '24px' }} />
                    <div>
                      <div style={{ fontSize: '20px', fontWeight: 'bold' }}>
                        {validationResult.totalCaseCount || 0}
                      </div>
                      <div style={{ color: '#666' }}>{translateTestCaseSet('statistics.totalCases')}</div>
                    </div>
                  </Space>
                </Card>
              </Col>
              <Col span={8}>
                <Card size="small">
                  <Space>
                    <CheckCircleOutlined style={{ color: '#52c41a', fontSize: '24px' }} />
                    <div>
                      <div style={{ fontSize: '20px', fontWeight: 'bold' }}>
                        {validationResult.passedCaseCount || 0}
                      </div>
                      <div style={{ color: '#666' }}>{translateTestCaseSet('statistics.passedCases')}</div>
                    </div>
                  </Space>
                </Card>
              </Col>
              <Col span={8}>
                <Card size="small">
                  <Space>
                    <InfoCircleOutlined style={{ color: '#722ed1', fontSize: '24px' }} />
                    <div>
                      <div style={{ fontSize: '20px', fontWeight: 'bold' }}>
                        {validationResult.matchRate ? Number(validationResult.matchRate).toFixed(2) : '0.00'}%
                      </div>
                      <div style={{ color: '#666' }}>{translateTestCaseSet('statistics.matchRate')}</div>
                    </div>
                  </Space>
                </Card>
              </Col>
            </Row>
          ) : (
            <Alert
              message={translateTestCaseSet('validation.noResult')}
              description={translateTestCaseSet('validation.noResultDescription')}
              type="info"
              showIcon
              style={{ marginBottom: '16px' }}
            />
          )}

          {/* 操作按钮 */}
          <div style={{ marginBottom: '16px', textAlign: 'right' }}>
            <Space>
              <Button
                icon={<ReloadOutlined />}
                onClick={handleRefresh}
                loading={loading}
              >
                {translateTestCaseSet('validation.refresh')}
              </Button>
              {validationResult && (
                <Button
                  type="primary"
                  icon={<DownloadOutlined />}
                  onClick={handleExport}
                  loading={exportLoading}
                >
                  {translateTestCaseSet('validation.export')}
                </Button>
              )}
            </Space>
          </div>

          {/* 第三部分：用例详情表格 */}
          {validationResult && validationResult.caseResults ? (
            <Table
              columns={columns}
              dataSource={validationResult.caseResults || []}
              rowKey={(record, index) => `${record.caseNumber || index}-${index}`}
              loading={loading}
              scroll={{ x: 1200 }}
              pagination={{
                pageSize: 10,
                showSizeChanger: true,
                showQuickJumper: true,
                showTotal: (total) => translateTestCaseSet('validation.table.paginationTotal', { total }),
              }}
            />
          ) : (
            <Alert
              message={translateTestCaseSet('validation.noCaseData')}
              description={translateTestCaseSet('validation.noCaseDataDescription')}
              type="warning"
              showIcon
            />
          )}
        </div>
      )}
    </Modal>
  )
}

TestCaseDetails.displayName = 'TestCaseDetails'

export default TestCaseDetails
