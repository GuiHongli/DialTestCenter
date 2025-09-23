import {
  DeleteOutlined,
  DownloadOutlined,
  EditOutlined,
  ExclamationCircleOutlined,
  FileTextOutlined,
  FileZipOutlined,
  PlusOutlined,
  ReloadOutlined,
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
import type { ColumnsType } from 'antd/es/table'
import React, { useEffect, useState } from 'react'
import { useTranslation } from '../hooks/useTranslation'
import { useI18n } from '../contexts/I18nContext'
import testCaseSetService from '../services/testCaseSetService'
import { TestCaseSet } from '../types/testCaseSet'
import TestCaseDetails from './TestCaseDetails'
import TestCaseSetUpload from './TestCaseSetUpload'
import TestCaseSetEdit from './TestCaseSetEdit'

const { Title } = Typography

const TestCaseSetManagement: React.FC = () => {
  const [testCaseSets, setTestCaseSets] = useState<TestCaseSet[]>([])
  const [loading, setLoading] = useState(false)
  const [uploadVisible, setUploadVisible] = useState(false)
  const [detailsVisible, setDetailsVisible] = useState(false)
  const [editVisible, setEditVisible] = useState(false)
  const [selectedTestCaseSet, setSelectedTestCaseSet] = useState<TestCaseSet | null>(null)
  const [pagination, setPagination] = useState({
    current: 1,
    pageSize: 10,
    total: 0,
  })

  const { translateTestCaseSet, translateCommon } = useTranslation()
  const { language } = useI18n()

  // 加载用例集列表
  const loadTestCaseSets = async (page: number = 1, pageSize: number = 10) => {
    try {
      setLoading(true)
      const response = await testCaseSetService.getTestCaseSets(page, pageSize)
      
      // 为每个用例集加载缺失脚本数量
      const testCaseSetsWithMissingCount = await Promise.all(
        response.data.map(async (testCaseSet) => {
          try {
            const missingScriptsResponse = await testCaseSetService.getMissingScripts(testCaseSet.id)
            // 只有当缺失脚本数量大于0时才设置字段
            if (missingScriptsResponse.data.count > 0) {
              return {
                ...testCaseSet,
                missingScriptsCount: missingScriptsResponse.data.count
              }
            } else {
              // 没有缺失脚本时，不设置missingScriptsCount字段
              return testCaseSet
            }
          } catch (error) {
            // 如果获取缺失脚本数量失败，不设置字段
            return testCaseSet
          }
        })
      )
      
      setTestCaseSets(testCaseSetsWithMissingCount)
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
  const handleTableChange = (pagination: any) => {
    loadTestCaseSets(pagination.current, pagination.pageSize)
  }

  // 下载用例集
  const handleDownload = async (record: TestCaseSet) => {
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
  const handleDelete = (record: TestCaseSet) => {
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
  const handleViewDetails = (record: TestCaseSet) => {
    setSelectedTestCaseSet(record)
    setDetailsVisible(true)
  }

  // 编辑用例集
  const handleEdit = (record: TestCaseSet) => {
    setSelectedTestCaseSet(record)
    setEditVisible(true)
  }

  // 格式化文件大小
  const formatFileSize = (bytes: number): string => {
    if (bytes === 0) return '0 B'
    const k = 1024
    const sizes = ['B', 'KB', 'MB', 'GB']
    const i = Math.floor(Math.log(bytes) / Math.log(k))
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
  }

  // 表格列定义
  const columns: ColumnsType<TestCaseSet> = [
    {
      title: translateTestCaseSet('table.name'),
      dataIndex: 'name',
      key: 'name',
      width: 200,
      render: (text: string, record: TestCaseSet) => (
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
          {record.missingScriptsCount && record.missingScriptsCount > 0 && (
            <Tooltip title={translateTestCaseSet('details.missingScriptsTooltip', { count: record.missingScriptsCount })}>
              <Tag 
                color="warning" 
                icon={<ExclamationCircleOutlined />}
                style={{ cursor: 'pointer' }}
                onClick={() => handleViewDetails(record)}
              >
                {translateTestCaseSet('details.missingScriptsTag', { count: record.missingScriptsCount })}
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
      render: (_, record: TestCaseSet) => {
        // 根据当前语言环境显示对应的业务类型
        if (language === 'en') {
          return record.businessEn || 'VPN_BLOCK'
        } else {
          return record.businessZh || 'VPN阻断'
        }
      },
    },
    {
      title: translateTestCaseSet('table.fileSize'),
      dataIndex: 'fileSize',
      key: 'fileSize',
      width: 100,
      render: (size: number) => formatFileSize(size),
    },
    {
      title: translateTestCaseSet('table.actions'),
      key: 'action',
      width: 200,
      render: (_, record: TestCaseSet) => (
        <Space size="small">
          <Tooltip title={translateTestCaseSet('table.viewTestCases')}>
            <Button
              type="text"
              icon={<FileTextOutlined />}
              onClick={() => handleViewDetails(record)}
            />
          </Tooltip>
          <Tooltip title={translateTestCaseSet('table.edit')}>
            <Button
              type="text"
              icon={<EditOutlined />}
              onClick={() => handleEdit(record)}
            />
          </Tooltip>
          <Tooltip title={translateTestCaseSet('table.download')}>
            <Button
              type="text"
              icon={<DownloadOutlined />}
              onClick={() => handleDownload(record)}
            />
          </Tooltip>
          <Tooltip title={translateTestCaseSet('table.delete')}>
            <Button
              type="text"
              danger
              icon={<DeleteOutlined />}
              onClick={() => handleDelete(record)}
            />
          </Tooltip>
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
          <Button
            type="primary"
            icon={<PlusOutlined />}
            onClick={() => setUploadVisible(true)}
          >
            {translateTestCaseSet('uploadTestCaseSet')}
          </Button>
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
    </div>
  )
}

export default TestCaseSetManagement
