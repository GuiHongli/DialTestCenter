import {
  DeleteOutlined,
  DownloadOutlined,
  EditOutlined,
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
import testCaseSetService from '../services/testCaseSetService'
import { TestCaseSet } from '../types/testCaseSet'
import TestCaseSetUpload from './TestCaseSetUpload'

const { Title } = Typography

const TestCaseSetManagement: React.FC = () => {
  const [testCaseSets, setTestCaseSets] = useState<TestCaseSet[]>([])
  const [loading, setLoading] = useState(false)
  const [uploadVisible, setUploadVisible] = useState(false)
  const [pagination, setPagination] = useState({
    current: 1,
    pageSize: 10,
    total: 0,
  })

  const { translateTestCaseSet, translateCommon } = useTranslation()

  // 加载用例集列表
  const loadTestCaseSets = async (page: number = 1, pageSize: number = 10) => {
    try {
      setLoading(true)
      const response = await testCaseSetService.getTestCaseSets(page, pageSize)
      setTestCaseSets(response.data)
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
      
      // 根据文件格式确定下载文件扩展名
      let fileExtension = '.zip' // 默认扩展名
      
      if (record.fileFormat === 'tar.gz') {
        fileExtension = '.tar.gz'
      } else if (record.fileFormat === 'zip') {
        fileExtension = '.zip'
      } else {
        // 如果没有fileFormat字段，尝试从Content-Type判断
        const contentType = blob.type
        if (contentType.includes('gzip') || contentType.includes('tar')) {
          fileExtension = '.tar.gz'
        }
      }
      
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
          <span>{text}</span>
          <Tag color="blue">{record.version}</Tag>
          {record.fileFormat && (
            <Tag color={record.fileFormat === 'tar.gz' ? 'green' : 'blue'}>
              {record.fileFormat.toUpperCase()}
            </Tag>
          )}
        </Space>
      ),
    },
    {
      title: translateTestCaseSet('table.creator'),
      dataIndex: 'creator',
      key: 'creator',
      width: 120,
    },
    {
      title: translateTestCaseSet('table.fileSize'),
      dataIndex: 'fileSize',
      key: 'fileSize',
      width: 100,
      render: (size: number) => formatFileSize(size),
    },
    {
      title: translateTestCaseSet('table.createdTime'),
      dataIndex: 'createdTime',
      key: 'createdTime',
      width: 180,
      render: (time: string) => new Date(time).toLocaleString(),
    },
    {
      title: translateTestCaseSet('table.updatedTime'),
      dataIndex: 'updatedTime',
      key: 'updatedTime',
      width: 180,
      render: (time: string) => new Date(time).toLocaleString(),
    },
    {
      title: translateTestCaseSet('table.actions'),
      key: 'action',
      width: 150,
      render: (_, record: TestCaseSet) => (
        <Space size="small">
          <Tooltip title={translateTestCaseSet('table.download')}>
            <Button
              type="text"
              icon={<DownloadOutlined />}
              onClick={() => handleDownload(record)}
            />
          </Tooltip>
          <Tooltip title={translateTestCaseSet('table.edit')}>
            <Button
              type="text"
              icon={<EditOutlined />}
              onClick={() => message.info(translateTestCaseSet('editFeaturePending'))}
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
    </div>
  )
}

export default TestCaseSetManagement
