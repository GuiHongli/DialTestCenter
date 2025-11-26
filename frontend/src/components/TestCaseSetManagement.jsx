import {
  DeleteOutlined,
  DownloadOutlined,
  EditOutlined,
  FileTextOutlined,
  FileZipOutlined,
  PlusOutlined,
  ReloadOutlined,
  SafetyOutlined,
} from '@ant-design/icons'
import {
  Button,
  message,
  Modal,
  Space,
  Table,
  Tag,
  Tooltip,
  Typography,
} from 'antd'
import React, { useEffect, useState } from 'react'
import { useTranslation } from '../hooks/useTranslation.js'
import { usePermission, PagePermission } from '../hooks/usePermission.js'
import { useI18n } from '../contexts/I18nContext.jsx'
import testCaseSetService from '../services/testCaseSetService.js'
import TestCaseDetails from './TestCaseDetails.jsx'
import TestCaseSetUpload from './TestCaseSetUpload.jsx'
import TestCaseSetEdit from './TestCaseSetEdit.jsx'
import ValidationResultModal from './ValidationResultModal.jsx'

const { Title } = Typography

const TestCaseSetManagement = () => {
  const [testCaseSets, setTestCaseSets] = useState([])
  const [loading, setLoading] = useState(false)
  const [uploadVisible, setUploadVisible] = useState(false)
  const [detailsVisible, setDetailsVisible] = useState(false)
  const [editVisible, setEditVisible] = useState(false)
  const [validationVisible, setValidationVisible] = useState(false)
  const [selectedTestCaseSet, setSelectedTestCaseSet] = useState(null)
  const [pagination, setPagination] = useState({
    current: 1,
    pageSize: 10,
    total: 0,
  })

  const { translateTestCaseSet, translateCommon } = useTranslation()
  const { language } = useI18n()
  const { hasPagePermission } = usePermission()

  // 检查各种操作权限
  const canEdit = hasPagePermission('test-case-set', 'edit')
  const canDelete = hasPagePermission('test-case-set', 'delete')
  const canDownload = hasPagePermission('test-case-set', 'download')
  // 只有ADMIN和OPERATOR用户才能进行校验操作
  const canValidate = hasPagePermission('test-case-set', 'validate')

  // 加载用例集列表
  const loadTestCaseSets = async (page = 1, pageSize = 10) => {
    try {
      setLoading(true)
      const response = await testCaseSetService.getTestCaseSets(page, pageSize)
      
      // 为每个用例集加载校验结果获取匹配率
      const testCaseSetsWithMatchRate = await Promise.all(
        response.data.map(async (testCaseSet) => {
          try {
            const validationResponse = await testCaseSetService.getValidationResult(testCaseSet.id)
            // 如果存在校验结果，提取匹配率
            if (validationResponse && validationResponse.success && validationResponse.data && validationResponse.data.matchRate !== undefined) {
              return {
                ...testCaseSet,
                matchRate: validationResponse.data.matchRate
              }
            } else {
              // 如果没有校验结果，不设置matchRate字段
              return testCaseSet
            }
          } catch (error) {
            // 如果获取校验结果失败（可能还没有校验过），不设置字段
            return testCaseSet
          }
        })
      )
      
      setTestCaseSets(testCaseSetsWithMatchRate)
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

  useEffect(() => {
    loadTestCaseSets()
  }, [])

  // 处理分页变化
  const handleTableChange = (newPagination) => {
    loadTestCaseSets(newPagination.current, newPagination.pageSize)
  }

  // 下载用例集
  const handleDownload = async (record) => {
    if (!canDownload) {
      message.warning('权限不足，无法下载用例集')
      return
    }
    
    try {
      const blob = await testCaseSetService.downloadTestCaseSet(record.id)
      const url = window.URL.createObjectURL(blob)
      const a = document.createElement('a')
      a.href = url
      
      // 下载文件扩展名固定为 .zip
      const fileExtension = '.zip'
      
      a.download = `${record.name}_${record.version}${fileExtension}`
      document.body.appendChild(a)
      a.click()
      window.URL.revokeObjectURL(url)
      document.body.removeChild(a)
      message.success(translateTestCaseSet('downloadSuccess'))
    } catch (error) {
      message.error(translateTestCaseSet('downloadFailed'))
    }
  }

  // 删除用例集
  const handleDelete = (record) => {
    if (!canDelete) {
      message.warning('权限不足，无法删除用例集')
      return
    }
    
    Modal.confirm({
      title: translateTestCaseSet('confirmDelete'),
      content: translateTestCaseSet('deleteDescription', { name: record.name, version: record.version }),
      okText: translateCommon('confirm'),
      cancelText: translateCommon('cancel'),
      onOk: async () => {
        try {
          await testCaseSetService.deleteTestCaseSet(record.id)
          message.success(translateTestCaseSet('deleteSuccess'))
          loadTestCaseSets(pagination.current, pagination.pageSize)
        } catch (error) {
          message.error(translateTestCaseSet('deleteFailed'))
        }
      },
    })
  }

  // 查看测试用例详情
  const handleViewDetails = (record) => {
    setSelectedTestCaseSet(record)
    setDetailsVisible(true)
  }

  // 编辑用例集
  const handleEdit = (record) => {
    if (!canEdit) {
      message.warning('权限不足，无法编辑用例集')
      return
    }
    
    setSelectedTestCaseSet(record)
    setEditVisible(true)
  }

  // 格式化文件大小
  const formatFileSize = (bytes) => {
    if (bytes === 0) return '0 B'
    const k = 1024
    const sizes = ['B', 'KB', 'MB', 'GB']
    const i = Math.floor(Math.log(bytes) / Math.log(k))
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
  }

  // 表格列定义
  const columns = [
    {
      title: translateTestCaseSet('table.name'),
      dataIndex: 'name',
      key: 'name',
      width: 200,
      render: (text, record) => (
        <Space>
          <FileZipOutlined />
          <span 
            style={{ 
              cursor: 'pointer', 
              color: '#1890ff',
              textDecoration: 'underline'
            }}
            onClick={() => handleViewDetails(record)}
            title={translateTestCaseSet('details.clickToViewDetails')}
          >
            {text}
          </span>
          <Tag color="blue">{record.version}</Tag>
          {record.matchRate !== undefined && record.matchRate !== null && (
            <Tooltip 
              title={
                record.matchRate === 100 
                  ? translateTestCaseSet('table.statusAvailableTooltip', { matchRate: Number(record.matchRate).toFixed(2) })
                  : translateTestCaseSet('table.statusUnavailableTooltip', { matchRate: Number(record.matchRate).toFixed(2) })
              }
            >
              <Tag 
                color={record.matchRate === 100 ? 'success' : 'error'}
                style={{ cursor: 'pointer' }}
                onClick={() => handleViewDetails(record)}
              >
                {record.matchRate === 100 
                  ? translateTestCaseSet('table.statusAvailable')
                  : translateTestCaseSet('table.statusUnavailable')
                }
              </Tag>
            </Tooltip>
          )}
        </Space>
      ),
    },
    {
      title: translateTestCaseSet('table.business'),
      dataIndex: 'business',
      key: 'business',
      width: 120,
      render: (_, record) => {
        // 根据当前语言环境显示对应的业务类型
        if (language === 'en') {
          return record.businessEn || 'VPN_BLOCK'
        } else {
          return record.businessZh || 'VPN阻断'
        }
      },
    },
    {
      title: translateTestCaseSet('table.description'),
      dataIndex: 'description',
      key: 'description',
      width: 200,
      ellipsis: true,
      render: (text) => (
        <Tooltip title={text}>
          <span>{text || '-'}</span>
        </Tooltip>
      ),
    },
    {
      title: translateTestCaseSet('table.fileSize'),
      dataIndex: 'fileSize',
      key: 'fileSize',
      width: 100,
      render: (size) => formatFileSize(size),
    },
    {
      title: translateTestCaseSet('table.actions'),
      key: 'action',
      width: 200,
      render: (_, record) => (
        <Space size="small">
          <Tooltip title={translateTestCaseSet('table.viewTestCases')}>
            <Button
              type="text"
              icon={<FileTextOutlined />}
              onClick={() => handleViewDetails(record)}
            />
          </Tooltip>
          <PagePermission pageId="test-case-set" operation="edit">
            <Tooltip title={translateTestCaseSet('table.edit')}>
              <Button
                type="text"
                icon={<EditOutlined />}
                onClick={() => handleEdit(record)}
              />
            </Tooltip>
          </PagePermission>
          <PagePermission pageId="test-case-set" operation="download">
            <Tooltip title={translateTestCaseSet('table.download')}>
              <Button
                type="text"
                icon={<DownloadOutlined />}
                onClick={() => handleDownload(record)}
              />
            </Tooltip>
          </PagePermission>
          {canValidate && (
            <Tooltip title={translateTestCaseSet('validation.startValidation')}>
              <Button
                type="text"
                icon={<SafetyOutlined />}
                onClick={() => { setSelectedTestCaseSet(record); setValidationVisible(true) }}
              />
            </Tooltip>
          )}
          <PagePermission pageId="test-case-set" operation="delete">
            <Tooltip title={translateTestCaseSet('table.delete')}>
              <Button
                type="text"
                danger
                icon={<DeleteOutlined />}
                onClick={() => handleDelete(record)}
              />
            </Tooltip>
          </PagePermission>
        </Space>
      ),
    },
  ]

  return (
    <div style={{ padding: '24px' }}>
      {/* 页面标题和操作按钮 */}
      <div style={{ 
        display: 'flex', 
        justifyContent: 'space-between', 
        alignItems: 'center', 
        marginBottom: '24px' 
      }}>
        <div>
          <Title level={2} style={{ margin: 0 }}>
            <FileZipOutlined style={{ marginRight: '8px' }} />
            {translateTestCaseSet('title')}
          </Title>
        </div>
        <Space>
          <PagePermission pageId="test-case-set" operation="upload">
            <Button
              type="primary"
              icon={<PlusOutlined />}
              onClick={() => setUploadVisible(true)}
            >
              {translateTestCaseSet('uploadTestCaseSet')}
            </Button>
          </PagePermission>
          <Button
            icon={<ReloadOutlined />}
            onClick={() => {
              loadTestCaseSets(pagination.current, pagination.pageSize)
            }}
          >
            {translateCommon('refresh')}
          </Button>
        </Space>
      </div>

      {/* 用例集列表 */}
      <Table
        columns={columns}
        dataSource={testCaseSets}
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

      {/* 上传对话框 */}
      <TestCaseSetUpload
        visible={uploadVisible}
        onCancel={() => setUploadVisible(false)}
        onSuccess={() => {
          loadTestCaseSets(pagination.current, pagination.pageSize)
        }}
      />

      {/* 测试用例详情对话框 */}
      <TestCaseDetails
        visible={detailsVisible}
        testCaseSet={selectedTestCaseSet}
        onCancel={() => {
          setDetailsVisible(false)
          setSelectedTestCaseSet(null)
        }}
      />

      {/* 编辑用例集对话框 */}
      <TestCaseSetEdit
        visible={editVisible}
        testCaseSet={selectedTestCaseSet}
        onCancel={() => {
          setEditVisible(false)
          setSelectedTestCaseSet(null)
        }}
        onSuccess={() => {
          setEditVisible(false)
          setSelectedTestCaseSet(null)
          loadTestCaseSets(pagination.current, pagination.pageSize)
        }}
      />

      {/* 校验结果对话框 */}
      <ValidationResultModal
        open={validationVisible}
        testCaseSet={selectedTestCaseSet}
        onClose={() => { setValidationVisible(false); setSelectedTestCaseSet(null) }}
      />
    </div>
  )
}

TestCaseSetManagement.displayName = 'TestCaseSetManagement'

export default TestCaseSetManagement