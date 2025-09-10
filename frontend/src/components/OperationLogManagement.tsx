import React, { useState, useEffect, useCallback } from 'react'
import {
  Card,
  Table,
  Button,
  Input,
  Select,
  DatePicker,
  Space,
  Row,
  Col,
  Tag,
  message,
  Tooltip,
  Typography,
} from 'antd'
import {
  ReloadOutlined,
  SearchOutlined,
  EyeOutlined,
} from '@ant-design/icons'
import { OperationLogService, OperationLogUtils } from '../services/operationLogService'
import { 
  OperationLog, 
  OperationLogQueryParams, 
  OperationLogFilter,
  OperationType,
  OperationTarget 
} from '../types/operationLog'
import dayjs from 'dayjs'

const { Option } = Select
const { RangePicker } = DatePicker
const { Title } = Typography

/**
 * 操作记录管理组件
 */
const OperationLogManagement: React.FC = () => {
  
  // 状态管理
  const [operationLogs, setOperationLogs] = useState<OperationLog[]>([])
  const [loading, setLoading] = useState(false)
  const [pagination, setPagination] = useState({
    current: 1,
    pageSize: 20,
    total: 0,
  })
  const [filters, setFilters] = useState<OperationLogFilter>({})

  // 加载操作记录
  const loadOperationLogs = useCallback(async (params: OperationLogQueryParams = {}) => {
    setLoading(true)
    try {
      const queryParams = {
        page: (pagination.current - 1),
        size: pagination.pageSize,
        ...filters,
        ...params,
      }
      
      const response = await OperationLogService.getOperationLogs(queryParams)
      setOperationLogs(response.content)
      setPagination(prev => ({
        ...prev,
        total: response.totalElements,
      }))
    } catch (error) {
      console.error('Failed to load operation logs:', error)
      message.error('加载操作记录失败')
    } finally {
      setLoading(false)
    }
  }, [pagination.current, pagination.pageSize, filters])


  // 初始化加载
  useEffect(() => {
    loadOperationLogs()
  }, [loadOperationLogs])

  // 处理搜索
  const handleSearch = () => {
    setPagination(prev => ({ ...prev, current: 1 }))
    loadOperationLogs()
  }

  // 处理重置
  const handleReset = () => {
    setFilters({})
    setPagination(prev => ({ ...prev, current: 1 }))
    loadOperationLogs()
  }

  // 处理筛选条件变化
  const handleFilterChange = (key: keyof OperationLogFilter, value: any) => {
    setFilters(prev => ({ ...prev, [key]: value }))
  }

  // 处理分页变化
  const handleTableChange = (paginationInfo: any) => {
    setPagination(prev => ({
      ...prev,
      current: paginationInfo.current,
      pageSize: paginationInfo.pageSize,
    }))
  }


  // 表格列配置
  const columns = [
    {
      title: 'ID',
      dataIndex: 'id',
      key: 'id',
      width: 80,
      sorter: true,
    },
    {
      title: '用户名',
      dataIndex: 'username',
      key: 'username',
      width: 120,
      sorter: true,
      filters: [
        { text: 'admin', value: 'admin' },
        { text: 'operator', value: 'operator' },
        { text: 'browser', value: 'browser' },
      ],
    },
    {
      title: '操作时间',
      dataIndex: 'operationTime',
      key: 'operationTime',
      width: 180,
      sorter: true,
      render: (time: string) => OperationLogUtils.formatOperationTime(time),
    },
    {
      title: '操作类型',
      dataIndex: 'operationType',
      key: 'operationType',
      width: 120,
      sorter: true,
      filters: Object.values(OperationType).map(type => ({
        text: OperationLogUtils.getOperationTypeText(type),
        value: type,
      })),
      render: (type: string) => (
        <Tag color={OperationLogUtils.getOperationTypeColor(type)}>
          {OperationLogUtils.getOperationTypeText(type)}
        </Tag>
      ),
    },
    {
      title: '操作对象',
      dataIndex: 'target',
      key: 'target',
      width: 150,
      sorter: true,
      filters: Object.values(OperationTarget).map(target => ({
        text: target,
        value: target,
      })),
    },
    {
      title: '操作描述',
      dataIndex: 'description',
      key: 'description',
      ellipsis: true,
      render: (description: string) => (
        <Tooltip title={description}>
          {OperationLogUtils.truncateText(description, 50)}
        </Tooltip>
      ),
    },
    {
      title: '操作',
      key: 'action',
      width: 120,
      render: () => (
        <Space size="small">
          <Tooltip title="查看详情">
            <Button
              type="text"
              icon={<EyeOutlined />}
              size="small"
              onClick={() => {
                // TODO: 实现查看详情功能
                message.info('查看详情功能待实现')
              }}
            />
          </Tooltip>
        </Space>
      ),
    },
  ]

  return (
    <div>
      <Title level={2} style={{ textAlign: 'left' }}>操作记录管理</Title>

      {/* 筛选器 */}
      <Card style={{ marginBottom: '16px' }}>
        <Row gutter={16}>
          <Col span={6}>
            <Space.Compact style={{ width: '100%' }}>
              <span style={{
                padding: '4px 8px',
                backgroundColor: '#f5f5f5',
                border: '1px solid #d9d9d9',
                borderRight: 'none',
                borderRadius: '6px 0 0 6px',
                fontSize: '14px',
                color: '#666',
                display: 'flex',
                alignItems: 'center',
                minWidth: '100px',
                justifyContent: 'center'
              }}>
                用户名
              </span>
              <Input
                placeholder="请输入用户名"
                value={filters.username || ''}
                onChange={(e) => handleFilterChange('username', e.target.value)}
                allowClear
                style={{ borderRadius: '0 6px 6px 0' }}
              />
            </Space.Compact>
          </Col>
          <Col span={6}>
            <Space.Compact style={{ width: '100%' }}>
              <span style={{
                padding: '4px 8px',
                backgroundColor: '#f5f5f5',
                border: '1px solid #d9d9d9',
                borderRight: 'none',
                borderRadius: '6px 0 0 6px',
                fontSize: '14px',
                color: '#666',
                display: 'flex',
                alignItems: 'center',
                minWidth: '100px',
                justifyContent: 'center'
              }}>
                操作类型
              </span>
              <Select
                placeholder="请选择操作类型"
                value={filters.operationType}
                onChange={(value) => handleFilterChange('operationType', value)}
                allowClear
                style={{ width: '100%', borderRadius: '0 6px 6px 0' }}
              >
                {Object.values(OperationType).map(type => (
                  <Option key={type} value={type}>
                    {OperationLogUtils.getOperationTypeText(type)}
                  </Option>
                ))}
              </Select>
            </Space.Compact>
          </Col>
          <Col span={6}>
            <Space.Compact style={{ width: '100%' }}>
              <span style={{
                padding: '4px 8px',
                backgroundColor: '#f5f5f5',
                border: '1px solid #d9d9d9',
                borderRight: 'none',
                borderRadius: '6px 0 0 6px',
                fontSize: '14px',
                color: '#666',
                display: 'flex',
                alignItems: 'center',
                minWidth: '100px',
                justifyContent: 'center'
              }}>
                操作对象
              </span>
              <Select
                placeholder="请选择操作对象"
                value={filters.target}
                onChange={(value) => handleFilterChange('target', value)}
                allowClear
                style={{ width: '100%', borderRadius: '0 6px 6px 0' }}
              >
                {Object.values(OperationTarget).map(target => (
                  <Option key={target} value={target}>
                    {target}
                  </Option>
                ))}
              </Select>
            </Space.Compact>
          </Col>
          <Col span={6}>
            <Space.Compact style={{ width: '100%' }}>
              <span style={{
                padding: '4px 8px',
                backgroundColor: '#f5f5f5',
                border: '1px solid #d9d9d9',
                borderRight: 'none',
                borderRadius: '6px 0 0 6px',
                fontSize: '14px',
                color: '#666',
                display: 'flex',
                alignItems: 'center',
                minWidth: '100px',
                justifyContent: 'center'
              }}>
                时间范围
              </span>
              <RangePicker
                style={{ width: '100%', borderRadius: '0 6px 6px 0' }}
                showTime={{ format: 'HH:mm:ss' }}
                format="YYYY-MM-DD HH:mm:ss"
                placeholder={['开始时间', '结束时间']}
                value={filters.startTime && filters.endTime ? [
                  dayjs(filters.startTime),
                  dayjs(filters.endTime)
                ] : null}
                onChange={(dates) => {
                  if (dates && dates[0] && dates[1]) {
                    handleFilterChange('startTime', dates[0].format('YYYY-MM-DD HH:mm:ss'))
                    handleFilterChange('endTime', dates[1].format('YYYY-MM-DD HH:mm:ss'))
                  } else {
                    handleFilterChange('startTime', undefined)
                    handleFilterChange('endTime', undefined)
                  }
                }}
              />
            </Space.Compact>
          </Col>
        </Row>
        <Row style={{ marginTop: '16px' }}>
          <Col span={24} style={{ textAlign: 'right' }}>
            <Space>
              <Button
                type="primary"
                icon={<SearchOutlined />}
                onClick={handleSearch}
                loading={loading}
              >
                搜索
              </Button>
              <Button
                icon={<ReloadOutlined />}
                onClick={handleReset}
              >
                重置
              </Button>
            </Space>
          </Col>
        </Row>
      </Card>

      {/* 操作记录表格 */}
      <Card>
        <Table
          columns={columns}
          dataSource={operationLogs}
          rowKey="id"
          loading={loading}
          pagination={{
            ...pagination,
            showSizeChanger: true,
            showQuickJumper: true,
            showTotal: (total, range) => `第 ${range[0]}-${range[1]} 条，共 ${total} 条`,
          }}
          onChange={handleTableChange}
          scroll={{ x: 1200 }}
        />
      </Card>
    </div>
  )
}

export default OperationLogManagement
