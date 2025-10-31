import React from 'react'
import {
  CheckCircleOutlined,
  CloseCircleOutlined,
} from '@ant-design/icons'
import {
  Button,
  Descriptions,
  Modal,
  Tag,
  Tooltip,
  Typography,
} from 'antd'

const { Text } = Typography

/**
 * 显示测试用例详情弹窗
 * @param {Object} testCase - 测试用例对象
 * @param {Function} translate - 翻译函数
 */
export const showTestCaseDetail = (testCase, translate) => {
  const t = translate || ((key, params) => key)
  Modal.info({
    title: t('validation.detailModal.title', { caseNumber: testCase.caseNumber }),
    width: 800,
    content: (
      <Descriptions column={1} bordered>
        <Descriptions.Item label={t('validation.detailModal.caseNumber')}>{testCase.caseNumber || '-'}</Descriptions.Item>
        <Descriptions.Item label={t('validation.detailModal.caseName')}>{testCase.caseName || '-'}</Descriptions.Item>
        <Descriptions.Item label={t('validation.detailModal.businessCategory')}>{testCase.businessCategory || '-'}</Descriptions.Item>
        <Descriptions.Item label={t('validation.detailModal.appName')}>{testCase.appName || '-'}</Descriptions.Item>
        <Descriptions.Item label={t('validation.detailModal.scriptValidation')}>
          <Tag color={testCase.scriptMatchValid ? 'success' : 'error'}>
            {testCase.scriptMatchValid ? t('validation.detailModal.passed') : t('validation.detailModal.failed')}
          </Tag>
        </Descriptions.Item>
        <Descriptions.Item label={t('validation.detailModal.ruleValidation')}>
          <Tag color={testCase.preprocessRuleValid ? 'success' : 'error'}>
            {testCase.preprocessRuleValid ? t('validation.detailModal.passed') : t('validation.detailModal.failed')}
          </Tag>
        </Descriptions.Item>
        <Descriptions.Item label={t('validation.detailModal.packageValidation')}>
          <Tag color={testCase.softwarePackageValid ? 'success' : 'error'}>
            {testCase.softwarePackageValid ? t('validation.detailModal.passed') : t('validation.detailModal.failed')}
          </Tag>
        </Descriptions.Item>
        <Descriptions.Item label={t('validation.detailModal.overallResult')}>
          <Tag color={testCase.overallValid ? 'success' : 'error'}>
            {testCase.overallValid ? t('validation.detailModal.passed') : t('validation.detailModal.failed')}
          </Tag>
        </Descriptions.Item>
        {testCase.validReasons && testCase.validReasons.length > 0 && (
          <Descriptions.Item label={t('validation.detailModal.passedItems')} span={2}>
            <ul style={{ margin: 0, paddingLeft: '20px' }}>
              {testCase.validReasons.map((reason, idx) => (
                <li key={idx} style={{ color: '#52c41a', marginBottom: '4px' }}>
                  <CheckCircleOutlined style={{ marginRight: '4px' }} />
                  {reason}
                </li>
              ))}
            </ul>
          </Descriptions.Item>
        )}
        {testCase.invalidReasons && testCase.invalidReasons.length > 0 && (
          <Descriptions.Item label={t('validation.detailModal.failedItems')} span={2}>
            <ul style={{ margin: 0, paddingLeft: '20px' }}>
              {testCase.invalidReasons.map((reason, idx) => (
                <li key={idx} style={{ color: '#ff4d4f', marginBottom: '4px' }}>
                  <CloseCircleOutlined style={{ marginRight: '4px' }} />
                  {reason}
                </li>
              ))}
            </ul>
          </Descriptions.Item>
        )}
      </Descriptions>
    ),
  })
}

/**
 * 获取用例校验结果表格列定义
 * @param {Function} onDetailClick - 点击详情按钮的回调函数，可选
 * @param {Function} translate - 翻译函数
 * @returns {Array} 表格列配置数组
 */
export const getValidationResultColumns = (onDetailClick, translate) => {
  const t = translate || ((key, params) => key)
  const handleDetailClick = (record) => {
    if (onDetailClick) {
      onDetailClick(record, translate)
    }
  }
  
  return [
    {
      title: t('validation.table.caseNumber'),
      dataIndex: 'caseNumber',
      key: 'caseNumber',
      width: 120,
      render: (text) => <Text code>{text}</Text>,
    },
    {
      title: t('validation.table.caseName'),
      dataIndex: 'caseName',
      key: 'caseName',
      width: 200,
      ellipsis: true,
    },
    {
      title: t('validation.table.businessCategory'),
      dataIndex: 'businessCategory',
      key: 'businessCategory',
      width: 120,
      ellipsis: true,
      render: (text) => text || '-',
    },
    {
      title: t('validation.table.appName'),
      dataIndex: 'appName',
      key: 'appName',
      width: 120,
      ellipsis: true,
      render: (text) => text || '-',
    },
    {
      title: t('validation.table.scriptExists'),
      dataIndex: 'scriptMatchValid',
      key: 'scriptMatchValid',
      width: 100,
      render: (valid, record) => {
        const tooltipTitle = valid 
          ? t('validation.tooltip.scriptPassed')
          : (record.invalidReasons && record.invalidReasons.length > 0
              ? record.invalidReasons.filter(r => r.includes('script') || r.includes('脚本')).join('; ')
              : t('validation.tooltip.scriptFailed'))
        return (
          <Tooltip title={tooltipTitle}>
            <Tag color={valid ? 'success' : 'error'}>
              {valid ? <CheckCircleOutlined /> : <CloseCircleOutlined />}
              {valid ? t('validation.detailModal.passed') : t('validation.detailModal.failed')}
            </Tag>
          </Tooltip>
        )
      },
    },
    {
      title: t('validation.table.ruleExists'),
      dataIndex: 'preprocessRuleValid',
      key: 'preprocessRuleValid',
      width: 120,
      render: (valid, record) => {
        const tooltipTitle = valid 
          ? t('validation.tooltip.rulePassed')
          : (record.invalidReasons && record.invalidReasons.length > 0
              ? record.invalidReasons.filter(r => r.includes('rule') || r.includes('规则')).join('; ')
              : t('validation.tooltip.ruleFailed'))
        return (
          <Tooltip title={tooltipTitle}>
            <Tag color={valid ? 'success' : 'error'}>
              {valid ? <CheckCircleOutlined /> : <CloseCircleOutlined />}
              {valid ? t('validation.detailModal.passed') : t('validation.detailModal.failed')}
            </Tag>
          </Tooltip>
        )
      },
    },
    {
      title: t('validation.table.packageExists'),
      dataIndex: 'softwarePackageValid',
      key: 'softwarePackageValid',
      width: 140,
      render: (valid, record) => {
        const tooltipTitle = valid 
          ? t('validation.tooltip.packagePassed')
          : (record.invalidReasons && record.invalidReasons.length > 0
              ? record.invalidReasons.filter(r => r.includes('package') || r.includes('软件包')).join('; ')
              : t('validation.tooltip.packageFailed'))
        return (
          <Tooltip title={tooltipTitle}>
            <Tag color={valid ? 'success' : 'error'}>
              {valid ? <CheckCircleOutlined /> : <CloseCircleOutlined />}
              {valid ? t('validation.detailModal.passed') : t('validation.detailModal.failed')}
            </Tag>
          </Tooltip>
        )
      },
    },
    {
      title: t('validation.table.overallResult'),
      dataIndex: 'overallValid',
      key: 'overallValid',
      width: 120,
      render: (valid, record) => {
        const tooltipTitle = valid 
          ? t('validation.tooltip.overallPassed')
          : (record.invalidReasons && record.invalidReasons.length > 0
              ? record.invalidReasons.join('; ')
              : t('validation.tooltip.overallFailed'))
        return (
          <Tooltip title={tooltipTitle}>
            <Tag color={valid ? 'success' : 'error'}>
              {valid ? <CheckCircleOutlined /> : <CloseCircleOutlined />}
              {valid ? t('validation.detailModal.passed') : t('validation.detailModal.failed')}
            </Tag>
          </Tooltip>
        )
      },
    },
    ...(onDetailClick ? [{
      title: t('validation.table.action'),
      key: 'action',
      width: 100,
      fixed: 'right',
      render: (_, record) => (
        <Button
          type="link"
          size="small"
          onClick={() => handleDetailClick(record)}
        >
          {t('validation.table.details')}
        </Button>
      ),
    }] : []),
  ]
}

