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
      message.error('获取用例集列表失败')
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
      a.download = `${record.name}_${record.version}.zip`
      document.body.appendChild(a)
      a.click()
      window.URL.revokeObjectURL(url)
      document.body.removeChild(a)
      message.success('下载成功')
    } catch (error) {
      message.error('下载失败')
    }
  }

  // 删除用例集
  const handleDelete = (record: TestCaseSet) => {
    Modal.confirm({
      title: '确认删除',
      content: `确定要删除用例集"${record.name}_${record.version}"吗？`,
      okText: '确定',
      cancelText: '取消',
      onOk: async () => {
        try {
          await testCaseSetService.deleteTestCaseSet(record.id)
          message.success('删除成功')
          loadTestCaseSets(pagination.current, pagination.pageSize)
        } catch (error) {
          message.error('删除失败')
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
      title: '用例集名称',
      dataIndex: 'name',
      key: 'name',
      width: 200,
      render: (text: string, record: TestCaseSet) => (
        <Space>
          <FileZipOutlined />
          <span>{text}</span>
          <Tag color="blue">{record.version}</Tag>
        </Space>
      ),
    },
    {
      title: '创建人',
      dataIndex: 'creator',
      key: 'creator',
      width: 120,
    },
    {
      title: '文件大小',
      dataIndex: 'fileSize',
      key: 'fileSize',
      width: 100,
      render: (size: number) => formatFileSize(size),
    },
    {
      title: '创建时间',
      dataIndex: 'createdTime',
      key: 'createdTime',
      width: 180,
      render: (time: string) => new Date(time).toLocaleString(),
    },
    {
      title: '更新时间',
      dataIndex: 'updatedTime',
      key: 'updatedTime',
      width: 180,
      render: (time: string) => new Date(time).toLocaleString(),
    },
    {
      title: '操作',
      key: 'action',
      width: 150,
      render: (_, record: TestCaseSet) => (
        <Space size="small">
          <Tooltip title="下载">
            <Button
              type="text"
              icon={<DownloadOutlined />}
              onClick={() => handleDownload(record)}
            />
          </Tooltip>
          <Tooltip title="编辑">
            <Button
              type="text"
              icon={<EditOutlined />}
              onClick={() => message.info('编辑功能待开发')}
            />
          </Tooltip>
          <Tooltip title="删除">
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
            用例集管理
          </Title>
        </div>
        <Space>
          <Button
            type="primary"
            icon={<PlusOutlined />}
            onClick={() => setUploadVisible(true)}
          >
            上传用例集
          </Button>
          <Button
            icon={<ReloadOutlined />}
            onClick={() => {
              loadTestCaseSets(pagination.current, pagination.pageSize)
            }}
          >
            刷新
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
            `第 ${range[0]}-${range[1]} 条/共 ${total} 条`,
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
