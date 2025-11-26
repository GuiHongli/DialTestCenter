import {
  ReloadOutlined,
  DatabaseOutlined,
} from '@ant-design/icons'
import {
  Button,
  Space,
  Table,
  Typography,
} from 'antd'
import React, { useEffect, useState } from 'react'
import { useTranslation } from '../hooks/useTranslation.js'

const { Title } = Typography

const NetworkElementModelManagement = () => {
  const [networkElements, setNetworkElements] = useState([])
  const [loading, setLoading] = useState(false)
  const [pagination, setPagination] = useState({
    current: 1,
    pageSize: 10,
    total: 0,
  })

  const { translateNetworkElementModel, translateCommon } = useTranslation()

  // 加载网元模型列表（mock数据）
  const loadNetworkElements = async (page = 1, pageSize = 10) => {
    try {
      setLoading(true)
      // Mock 5条数据
      const mockData = [
        {
          id: 1,
          name: '网元设备_001',
          formalModel: '正式模型_v1.0',
          testModel: '测试模型_v1.0',
        },
        {
          id: 2,
          name: '网元设备_002',
          formalModel: '正式模型_v1.1',
          testModel: '测试模型_v1.1',
        },
        {
          id: 3,
          name: '网元设备_003',
          formalModel: '正式模型_v2.0',
          testModel: '测试模型_v2.0',
        },
        {
          id: 4,
          name: '网元设备_004',
          formalModel: '正式模型_v2.1',
          testModel: '测试模型_v2.1',
        },
        {
          id: 5,
          name: '网元设备_005',
          formalModel: '正式模型_v3.0',
          testModel: '测试模型_v3.0',
        },
      ]
      
      setNetworkElements(mockData)
      setPagination({
        current: page,
        pageSize: pageSize,
        total: mockData.length,
      })
    } catch (error) {
      // Mock错误处理
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadNetworkElements()
  }, [])

  // 处理分页变化
  const handleTableChange = (newPagination) => {
    loadNetworkElements(newPagination.current, newPagination.pageSize)
  }

  // 表格列定义
  const columns = [
    {
      title: translateNetworkElementModel('table.name'),
      dataIndex: 'name',
      key: 'name',
      width: 200,
    },
    {
      title: translateNetworkElementModel('table.formalModel'),
      dataIndex: 'formalModel',
      key: 'formalModel',
      width: 200,
    },
    {
      title: translateNetworkElementModel('table.testModel'),
      dataIndex: 'testModel',
      key: 'testModel',
      width: 200,
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
            <DatabaseOutlined style={{ marginRight: '8px' }} />
            {translateNetworkElementModel('title')}
          </Title>
        </div>
        <Space>
          <Button
            icon={<ReloadOutlined />}
            onClick={() => {
              loadNetworkElements(pagination.current, pagination.pageSize)
            }}
          >
            {translateCommon('refresh')}
          </Button>
        </Space>
      </div>

      {/* 网元模型列表 */}
      <Table
        columns={columns}
        dataSource={networkElements}
        rowKey="id"
        loading={loading}
        pagination={{
          ...pagination,
          showSizeChanger: true,
          showQuickJumper: true,
          showTotal: (total, range) =>
            translateNetworkElementModel('table.pagination', { start: range[0], end: range[1], total }),
        }}
        onChange={handleTableChange}
      />
    </div>
  )
}

NetworkElementModelManagement.displayName = 'NetworkElementModelManagement'

export default NetworkElementModelManagement

