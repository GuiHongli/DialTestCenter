import {
  DeleteOutlined,
  DownloadOutlined,
  FileZipOutlined,
  PlusOutlined,
  ReloadOutlined,
  UploadOutlined,
} from '@ant-design/icons'
import {
  Button,
  message,
  Modal,
  Space,
  Table,
  Tooltip,
  Typography,
  Upload,
} from 'antd'
import React, { useEffect, useState } from 'react'
import { useTranslation } from '../hooks/useTranslation.js'
import { usePermission, PagePermission } from '../hooks/usePermission.js'

const { Title } = Typography
const { Dragger } = Upload

const SampleImportManagement = () => {
  const [samples, setSamples] = useState([])
  const [loading, setLoading] = useState(false)
  const [uploadVisible, setUploadVisible] = useState(false)
  const [pagination, setPagination] = useState({
    current: 1,
    pageSize: 10,
    total: 0,
  })

  const { translateSampleImport, translateCommon } = useTranslation()
  const { hasPagePermission } = usePermission()

  // 检查各种操作权限
  const canDelete = hasPagePermission('sample-import', 'delete')
  const canDownload = hasPagePermission('sample-import', 'download')
  const canUpload = hasPagePermission('sample-import', 'upload')

  // 加载样本列表（mock数据）
  const loadSamples = async (page = 1, pageSize = 10) => {
    try {
      setLoading(true)
      // Mock数据
      const mockSamples = [
        {
          id: 1,
          name: '样本包_001',
          version: 'v1.0',
          fileSize: 1024000,
          description: '测试样本包1',
          createdTime: '2024-01-01 10:00:00',
        },
        {
          id: 2,
          name: '样本包_002',
          version: 'v1.1',
          fileSize: 2048000,
          description: '测试样本包2',
          createdTime: '2024-01-02 11:00:00',
        },
        {
          id: 3,
          name: '样本包_003',
          version: 'v2.0',
          fileSize: 3072000,
          description: '测试样本包3',
          createdTime: '2024-01-03 12:00:00',
        },
      ]
      
      setSamples(mockSamples)
      setPagination({
        current: page,
        pageSize: pageSize,
        total: mockSamples.length,
      })
    } catch (error) {
      message.error(translateSampleImport('loadFailed'))
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadSamples()
  }, [])

  // 处理分页变化
  const handleTableChange = (newPagination) => {
    loadSamples(newPagination.current, newPagination.pageSize)
  }

  // 下载样本
  const handleDownload = async (record) => {
    if (!canDownload) {
      message.warning('权限不足，无法下载样本')
      return
    }
    
    try {
      // Mock下载功能
      message.success(translateSampleImport('downloadSuccess'))
    } catch (error) {
      message.error(translateSampleImport('downloadFailed'))
    }
  }

  // 删除样本
  const handleDelete = (record) => {
    if (!canDelete) {
      message.warning('权限不足，无法删除样本')
      return
    }
    
    Modal.confirm({
      title: translateSampleImport('confirmDelete'),
      content: translateSampleImport('deleteDescription', { name: record.name, version: record.version }),
      okText: translateCommon('confirm'),
      cancelText: translateCommon('cancel'),
      onOk: async () => {
        try {
          // Mock删除功能
          message.success(translateSampleImport('deleteSuccess'))
          loadSamples(pagination.current, pagination.pageSize)
        } catch (error) {
          message.error(translateSampleImport('deleteFailed'))
        }
      },
    })
  }

  // 格式化文件大小
  const formatFileSize = (bytes) => {
    if (bytes === 0) return '0 B'
    const k = 1024
    const sizes = ['B', 'KB', 'MB', 'GB']
    const i = Math.floor(Math.log(bytes) / Math.log(k))
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
  }

  // 处理上传
  const handleUpload = async (file) => {
    if (!canUpload) {
      message.warning('权限不足，无法上传样本')
      return false
    }

    // Mock上传功能
    message.success(translateSampleImport('uploadSuccess'))
    loadSamples(pagination.current, pagination.pageSize)
    setUploadVisible(false)
    return false
  }

  // 表格列定义
  const columns = [
    {
      title: translateSampleImport('table.name'),
      dataIndex: 'name',
      key: 'name',
      width: 200,
      render: (text, record) => (
        <Space>
          <FileZipOutlined />
          <span>{text}</span>
          <span style={{ color: '#999' }}>{record.version}</span>
        </Space>
      ),
    },
    {
      title: translateSampleImport('table.description'),
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
      title: translateSampleImport('table.fileSize'),
      dataIndex: 'fileSize',
      key: 'fileSize',
      width: 100,
      render: (size) => formatFileSize(size),
    },
    {
      title: translateSampleImport('table.createdTime'),
      dataIndex: 'createdTime',
      key: 'createdTime',
      width: 180,
    },
    {
      title: translateSampleImport('table.actions'),
      key: 'action',
      width: 150,
      render: (_, record) => (
        <Space size="small">
          <PagePermission pageId="sample-import" operation="download">
            <Tooltip title={translateSampleImport('table.download')}>
              <Button
                type="text"
                icon={<DownloadOutlined />}
                onClick={() => handleDownload(record)}
              />
            </Tooltip>
          </PagePermission>
          <PagePermission pageId="sample-import" operation="delete">
            <Tooltip title={translateSampleImport('table.delete')}>
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
            {translateSampleImport('title')}
          </Title>
        </div>
        <Space>
          <Button
            type="primary"
            icon={<UploadOutlined />}
            onClick={() => {
              message.info(translateSampleImport('featureDeveloping'))
            }}
          >
            {translateSampleImport('uploadSample')}
          </Button>
          <PagePermission pageId="sample-import" operation="upload">
            <Button
              icon={<PlusOutlined />}
              onClick={() => setUploadVisible(true)}
            >
              {translateSampleImport('importSample')}
            </Button>
          </PagePermission>
          <Button
            icon={<ReloadOutlined />}
            onClick={() => {
              loadSamples(pagination.current, pagination.pageSize)
            }}
          >
            {translateCommon('refresh')}
          </Button>
        </Space>
      </div>

      {/* 样本列表 */}
      <Table
        columns={columns}
        dataSource={samples}
        rowKey="id"
        loading={loading}
        pagination={{
          ...pagination,
          showSizeChanger: true,
          showQuickJumper: true,
          showTotal: (total, range) =>
            translateSampleImport('table.pagination', { start: range[0], end: range[1], total }),
        }}
        onChange={handleTableChange}
      />

      {/* 上传对话框 */}
      <Modal
        title={translateSampleImport('upload.title')}
        open={uploadVisible}
        onCancel={() => setUploadVisible(false)}
        footer={null}
        width={600}
      >
        <Dragger
          name="file"
          multiple={false}
          accept=".zip"
          beforeUpload={handleUpload}
          onRemove={() => {}}
        >
          <p className="ant-upload-drag-icon">
            <FileZipOutlined />
          </p>
          <p className="ant-upload-text">{translateSampleImport('upload.dragTip')}</p>
          <p className="ant-upload-hint">
            {translateSampleImport('upload.supportFormat')}
          </p>
        </Dragger>
      </Modal>
    </div>
  )
}

SampleImportManagement.displayName = 'SampleImportManagement'

export default SampleImportManagement

