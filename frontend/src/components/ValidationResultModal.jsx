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
import testCaseSetService from '../services/testCaseSetService.js'
import { getValidationResultColumns, showTestCaseDetail } from '../utils/validationUtils.js'

const { Text } = Typography

const POLL_INTERVAL_MS = 2000

const ValidationResultModal = ({ open, testCaseSet, onClose }) => {
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
      message.error('用例集信息不完整，无法触发校验')
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
        message.success('校验任务已提交')
        pollTimer.current = setTimeout(poll, POLL_INTERVAL_MS)
      } else {
        message.error(res?.message || '触发校验失败')
      }
    } catch (e) {
      console.error('触发校验失败:', e)
      message.error('触发校验失败')
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

  // 用例详情表格列定义（使用共享工具函数）
  const columns = getValidationResultColumns(showTestCaseDetail)

  return (
    <Modal
      title={`用例集校验 - ${testCaseSet?.name || ''} ${testCaseSet?.version ? 'v' + testCaseSet.version : ''}`}
      open={open}
      width={1400}
      onCancel={() => { clearTimer(); onClose && onClose() }}
      footer={[
        <Button key="close" onClick={() => { clearTimer(); onClose && onClose() }}>关闭</Button>,
      ]}
    >
      {!result && (
        <Space direction="vertical" style={{ width: '100%' }}>
          <Alert
            type="info"
            message="校验任务已提交"
            description={
              <Space>
                <Text>状态：</Text>
                <Tag color={status === 'RUNNING' ? 'processing' : 'default'}>
                  {status || 'PENDING'}
                </Tag>
                {typeof estimatedTime === 'number' && (
                  <Text>预计耗时：{estimatedTime}s</Text>
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
          <Alert
            type="success"
            message="校验完成"
            description={
              <Space>
                <Text>总用例数：{result.totalCaseCount}</Text>
                <Text>通过：{result.passedCaseCount}</Text>
                <Text>不通过：{result.failedCaseCount}</Text>
                <Text>匹配率：{Number(result.matchRate).toFixed(2)}%</Text>
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
              showTotal: (total) => `共 ${total} 条记录`,
            }}
          />
        </Space>
      )}
    </Modal>
  )
}

ValidationResultModal.displayName = 'ValidationResultModal'

export default ValidationResultModal



