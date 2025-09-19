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
  DownloadOutlined,
  ClearOutlined,
  UserOutlined,
  SettingOutlined,
  AimOutlined,
} from '@ant-design/icons'
import { OperationLogService, OperationLogUtils } from '../services/operationLogService'
import { 
  OperationLog, 
  OperationLogQueryParams, 
  OperationLogFilter,
  OperationType,
  OperationTarget 
} from '../types/operationLog'
import moment, { Moment } from 'moment'

const { Option } = Select
const { RangePicker } = DatePicker
const { Title, Text } = Typography

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
      setOperationLogs(response.data.content)
      setPagination(prev => ({
        ...prev,
        total: response.data.totalElements,
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

  // 处理导出
  const handleExport = async () => {
    try {
      setLoading(true)
      const blob = await OperationLogService.exportOperationLogs(filters)
      
      // 创建下载链接
      const url = window.URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url
      link.download = `operation-logs-${new Date().toISOString().split('T')[0]}.xlsx`
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      window.URL.revokeObjectURL(url)
      
      message.success('导出成功')
    } catch (error) {
      console.error('Failed to export operation logs:', error)
      message.error('导出失败')
    } finally {
      setLoading(false)
    }
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
      dataIndex: 'operationTarget',
      key: 'operationTarget',
      width: 150,
      sorter: true,
      filters: Object.values(OperationTarget).map(target => ({
        text: target,
        value: target,
      })),
    },
    {
      title: '操作描述',
      dataIndex: 'operationDescriptionZh',
      key: 'operationDescriptionZh',
      ellipsis: true,
      render: (description: string, record: OperationLog) => {
        const desc = description || record.operationDescriptionEn || ''
        return (
          <Tooltip title={desc}>
            {OperationLogUtils.truncateText(desc, 50)}
          </Tooltip>
        )
      },
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
      <Card 
        style={{ 
          marginBottom: '16px',
          borderRadius: '8px',
          boxShadow: '0 2px 8px rgba(0,0,0,0.06)'
        }}
        bodyStyle={{ padding: '20px' }}
      >
        <Row gutter={[16, 16]}>
          <Col xs={24} sm={12} md={8} lg={6}>
            <div style={{ marginBottom: '8px' }}>
              <Text strong style={{ color: '#262626' }}>用户名</Text>
            </div>
            <Input
              placeholder="请输入用户名"
              value={filters.username || ''}
              onChange={(e) => handleFilterChange('username', e.target.value)}
              allowClear
              prefix={<UserOutlined style={{ color: '#bfbfbf' }} />}
              style={{ borderRadius: '6px' }}
            />
          </Col>
          
          <Col xs={24} sm={12} md={8} lg={6}>
            <div style={{ marginBottom: '8px' }}>
              <Text strong style={{ color: '#262626' }}>操作类型</Text>
            </div>
            <Select
              placeholder="请选择操作类型"
              value={filters.operationType}
              onChange={(value) => handleFilterChange('operationType', value)}
              allowClear
              style={{ width: '100%' }}
              suffixIcon={<SettingOutlined style={{ color: '#bfbfbf' }} />}
            >
              {Object.values(OperationType).map(type => (
                <Option key={type} value={type}>
                  {OperationLogUtils.getOperationTypeText(type)}
                </Option>
              ))}
            </Select>
          </Col>
          
          <Col xs={24} sm={12} md={8} lg={6}>
            <div style={{ marginBottom: '8px' }}>
              <Text strong style={{ color: '#262626' }}>操作对象</Text>
            </div>
            <Select
              placeholder="请选择操作对象"
              value={filters.operationTarget}
              onChange={(value) => handleFilterChange('operationTarget', value)}
              allowClear
              style={{ width: '100%' }}
              suffixIcon={<AimOutlined style={{ color: '#bfbfbf' }} />}
            >
              {Object.values(OperationTarget).map(target => (
                <Option key={target} value={target}>
                  {target}
                </Option>
              ))}
            </Select>
          </Col>
          
          <Col xs={24} sm={12} md={8} lg={6}>
            <div style={{ marginBottom: '8px' }}>
              <Text strong style={{ color: '#262626' }}>操作时间</Text>
            </div>
            <RangePicker
              style={{ width: '100%' }}
              showTime={{ format: 'HH:mm:ss' }}
              format="YYYY-MM-DD HH:mm:ss"
              placeholder={['开始时间', '结束时间']}
              value={filters.startTime && filters.endTime ? [
                moment(filters.startTime),
                moment(filters.endTime)
              ] as [Moment, Moment] : null}
              onChange={(dates: null | [Moment, Moment]) => {
                if (dates && dates[0] && dates[1]) {
                  handleFilterChange('startTime', dates[0].format('YYYY-MM-DD HH:mm:ss'))
                  handleFilterChange('endTime', dates[1].format('YYYY-MM-DD HH:mm:ss'))
                } else {
                  handleFilterChange('startTime', undefined)
                  handleFilterChange('endTime', undefined)
                }
              }}
            />
          </Col>
        </Row>
        
        <Row justify="end" style={{ marginTop: '20px' }}>
          <Space size="middle">
            <Button 
              onClick={() => {
                setFilters({})
                setPagination(prev => ({ ...prev, current: 1 }))
                loadOperationLogs()
              }}
              icon={<ClearOutlined />}
            >
              重置
            </Button>
            <Button 
              type="primary" 
              onClick={handleSearch}
              icon={<SearchOutlined />}
              style={{ borderRadius: '6px' }}
            >
              搜索
            </Button>
            <Button 
              type="default" 
              onClick={handleExport}
              icon={<DownloadOutlined />}
              style={{ borderRadius: '6px' }}
            >
              导出
            </Button>
          </Space>
        </Row>
      </Card>

      {/* 操作记录表格 */}
      <Card bodyStyle={{ padding: 0 }}>
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
          scroll={{ x: 1200, y: 520 }}
        />
      </Card>
    </div>
  )
}

export default OperationLogManagement
