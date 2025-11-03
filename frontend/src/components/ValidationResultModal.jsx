import { LoadingOutlined } from '@ant-design/icons'
import { 
  Alert, 
  Button, 
  Modal, 
  Progress, 
  Space, 
  Table, 
  Tag,
  Typography, 
  message 
} from 'antd'
import React, { useEffect, useRef, useState } from 'react'
import { useTranslation } from '../hooks/useTranslation.js'
import { useI18n } from '../contexts/I18nContext.jsx'
import testCaseSetService from '../services/testCaseSetService.js'
import { getValidationResultColumns, showTestCaseDetail } from '../utils/validationUtils.js'

const { Text } = Typography

const POLL_INTERVAL_MS = 2000

const ValidationResultModal = ({ open, testCaseSet, onClose }) => {
  const { translateTestCaseSet, translateCommon } = useTranslation()
  const { language } = useI18n()
  const [submitting, setSubmitting] = useState(false)
  const [status, setStatus] = useState(null) // PENDING/RUNNING/COMPLETED/FAILED
  const [estimatedTime, setEstimatedTime] = useState(null)
  const [progress, setProgress] = useState(null)
  const [result, setResult] = useState(null)
  const pollTimer = useRef(null)

  const clearTimer = () => {
    if (pollTimer.current) {
      clearTimeout(pollTimer.current)
      pollTimer.current = null
    }
  }

  const poll = async () => {
    if (!testCaseSet || !testCaseSet.id) {
      clearTimer()
      return
    }
    
    try {
      const res = await testCaseSetService.getValidationResult(testCaseSet.id)
      // 若返回完整结果
      if (res && res.data && res.data.caseResults) {
        setResult(res.data)
        setStatus('COMPLETED')
        clearTimer()
        return
      }
      // 若返回任务状态
      if (res && res.data && res.data.status) {
        setStatus(res.data.status)
        if (typeof res.data.progress === 'number') {
          setProgress(res.data.progress)
        }
        // 如果返回了时间信息，更新 result（即使没有 caseResults）
        if (res.data.validationTaskCreatedTime || res.data.validationTaskStartedTime || res.data.validationTaskCompletedTime) {
          setResult(prevResult => ({
            ...prevResult,
            ...res.data
          }))
        }
        pollTimer.current = setTimeout(poll, POLL_INTERVAL_MS)
        return
      }
      // 未找到
      pollTimer.current = setTimeout(poll, POLL_INTERVAL_MS)
    } catch (e) {
      console.error('获取校验结果失败:', e)
      pollTimer.current = setTimeout(poll, POLL_INTERVAL_MS)
    }
  }

  const startValidation = async () => {
    if (!testCaseSet || !testCaseSet.id) {
      message.error(translateTestCaseSet('validation.incompleteInfo'))
      return
    }
    
    setSubmitting(true)
    setStatus('PENDING')
    setResult(null)
    setProgress(null)
    try {
      const res = await testCaseSetService.triggerValidation(testCaseSet.id)
      if (res && res.success) {
        // 期望 202 + task 信息
        if (res.data) {
          setEstimatedTime(res.data.estimatedTime || null)
          setStatus(res.data.status || 'PENDING')
        }
        message.success(translateTestCaseSet('validation.taskSubmitted'))
        pollTimer.current = setTimeout(poll, POLL_INTERVAL_MS)
      } else {
        message.error(res?.message || translateTestCaseSet('validation.triggerFailed'))
      }
    } catch (e) {
      console.error('触发校验失败:', e)
      message.error(translateTestCaseSet('validation.triggerFailed'))
    } finally {
      setSubmitting(false)
    }
  }

  useEffect(() => {
    if (open && testCaseSet) {
      startValidation()
    }
    return () => clearTimer()
  }, [open, testCaseSet])

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

  // 计算耗时（秒）
  const calculateDuration = (startTime, endTime) => {
    if (!startTime || !endTime) {
      return null
    }
    try {
      const start = new Date(startTime)
      const end = new Date(endTime)
      const durationMs = end.getTime() - start.getTime()
      const durationSeconds = Math.round(durationMs / 1000)
      return durationSeconds
    } catch (e) {
      return null
    }
  }

  // 用例详情表格列定义（使用共享工具函数）
  const handleShowDetail = (record) => {
    showTestCaseDetail(record, translateTestCaseSet)
  }
  const columns = getValidationResultColumns(handleShowDetail, translateTestCaseSet)

  return (
    <Modal
      title={translateTestCaseSet('validation.modalTitle', { 
        name: testCaseSet?.name || '', 
        version: testCaseSet?.version ? 'v' + testCaseSet.version : '' 
      })}
      open={open}
      width={1400}
      onCancel={() => { clearTimer(); onClose && onClose() }}
      footer={[
        <Button key="close" onClick={() => { clearTimer(); onClose && onClose() }}>
          {translateCommon('close')}
        </Button>,
      ]}
    >
      {!result && (
        <Space direction="vertical" style={{ width: '100%' }}>
          <Alert
            type="info"
            message={translateTestCaseSet('validation.taskSubmitted')}
            description={
              <Space>
                <Text>{translateTestCaseSet('validation.statusLabel')}:</Text>
                <Tag color={status === 'RUNNING' ? 'processing' : 'default'}>
                  {status ? translateTestCaseSet(`validation.status.${status}`) : translateTestCaseSet('validation.status.PENDING')}
                </Tag>
                {typeof estimatedTime === 'number' && (
                  <Text>{translateTestCaseSet('validation.estimatedTime')}: {estimatedTime}s</Text>
                )}
              </Space>
            }
            showIcon
          />
          <div style={{ padding: 8 }}>
            <Progress
              percent={typeof progress === 'number' ? progress : undefined}
              status={status === 'FAILED' ? 'exception' : (status === 'COMPLETED' ? 'success' : 'active')}
              strokeColor={{ from: '#108ee9', to: '#87d068' }}
              icon={<LoadingOutlined />}
            />
          </div>
        </Space>
      )}

      {result && (
        <Space direction="vertical" style={{ width: '100%' }}>
          {/* 任务时间信息 */}
          {(result.validationTaskCreatedTime || result.validationTaskStartedTime || result.validationTaskCompletedTime) && (
            <div style={{ marginBottom: '16px', padding: '12px', background: '#f5f5f5', borderRadius: '4px' }}>
              <Space direction="vertical" size="small" style={{ width: '100%' }}>
                {result.validationTaskCreatedTime && (
                  <div>
                    <Text strong>{translateTestCaseSet('validation.triggerTime')}: </Text>
                    <Text>{formatDateTime(result.validationTaskCreatedTime)}</Text>
                  </div>
                )}
                {result.validationTaskStartedTime && (
                  <div>
                    <Text strong>{translateTestCaseSet('validation.startTime')}: </Text>
                    <Text>{formatDateTime(result.validationTaskStartedTime)}</Text>
                  </div>
                )}
                {result.validationTaskCompletedTime && (
                  <div>
                    <Text strong>{translateTestCaseSet('validation.completedTime')}: </Text>
                    <Text>{formatDateTime(result.validationTaskCompletedTime)}</Text>
                  </div>
                )}
                {(() => {
                  const startTime = result.validationTaskStartedTime || result.validationTaskCreatedTime
                  const endTime = result.validationTaskCompletedTime
                  const duration = calculateDuration(startTime, endTime)
                  if (duration !== null) {
                    return (
                      <div>
                        <Text strong>{translateTestCaseSet('validation.duration')}: </Text>
                        <Text>{duration}s</Text>
                      </div>
                    )
                  }
                  return null
                })()}
              </Space>
            </div>
          )}

          <Alert
            type="success"
            message={translateTestCaseSet('validation.completed')}
            description={
              <Space>
                <Text>{translateTestCaseSet('validation.totalCases')}: {result.totalCaseCount}</Text>
                <Text>{translateTestCaseSet('validation.passedCases')}: {result.passedCaseCount}</Text>
                <Text>{translateTestCaseSet('validation.failedCases')}: {result.failedCaseCount}</Text>
                <Text>{translateTestCaseSet('validation.matchRateLabel')}: {Number(result.matchRate).toFixed(2)}%</Text>
              </Space>
            }
            showIcon
          />
          <Table
            columns={columns}
            dataSource={result.caseResults || []}
            rowKey={(record, index) => `${record.caseNumber || index}-${index}`}
            scroll={{ x: 1200 }}
            pagination={{
              pageSize: 10,
              showSizeChanger: true,
              showQuickJumper: true,
              showTotal: (total) => translateTestCaseSet('validation.table.paginationTotal', { total }),
            }}
          />
        </Space>
      )}
    </Modal>
  )
}

ValidationResultModal.displayName = 'ValidationResultModal'

export default ValidationResultModal



