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
  Modal,
  Descriptions,
  Collapse,
} from 'antd'
import {
  ReloadOutlined,
  SearchOutlined,
  EyeOutlined,
  DownloadOutlined,
  ClearOutlined,
  UserOutlined,
  SettingOutlined,
} from '@ant-design/icons'
import { OperationLogService } from '../services/operationLogService.js'
import moment from 'moment'
import { useTranslation } from '../hooks/useTranslation.js'
import { useI18n } from '../contexts/I18nContext.jsx'

const { Option } = Select
const { RangePicker } = DatePicker
const { Title, Text } = Typography
const { Panel } = Collapse

/**
 * 操作记录管理组件
 */
const OperationLogManagement = () => {
  const { translateOperationLog, translateCommon } = useTranslation()
  const { language } = useI18n()
  
  // 状态管理
  const [logs, setLogs] = useState([])
  const [loading, setLoading] = useState(false)
  const [pagination, setPagination] = useState({
    current: 1,
    pageSize: 10,
    total: 0,
  })
  const [filters, setFilters] = useState({
    operationType: '',
    operationTarget: '',
    username: '',
    dateRange: null,
  })
  const [detailModalVisible, setDetailModalVisible] = useState(false)
  const [selectedLog, setSelectedLog] = useState(null)

  // 加载操作记录
  const loadLogs = useCallback(async (page = 1, pageSize = 10) => {
    setLoading(true)
    try {
      const params = {
        page,
        pageSize,
        ...filters,
        startTime: filters.dateRange && filters.dateRange[0] ? filters.dateRange[0].format('YYYY-MM-DD HH:mm:ss') : null,
        endTime: filters.dateRange && filters.dateRange[1] ? filters.dateRange[1].format('YYYY-MM-DD HH:mm:ss') : null,
      }
      
      const response = await OperationLogService.getOperationLogs(params)
      
      if (response && response.content && Array.isArray(response.content)) {
        setLogs(response.content)
        setPagination({
          current: response.number + 1, // 后端页码从0开始，前端从1开始
          pageSize: response.size,
          total: response.totalElements,
        })
      } else {
        // 如果API返回的数据格式不正确，设置默认值
        setLogs([])
        setPagination({
          current: page,
          pageSize: pageSize,
          total: 0,
        })
      }
    } catch (error) {
      message.error(translateOperationLog('loadFailed'))
    } finally {
      setLoading(false)
    }
  }, []) // 移除依赖项，避免无限循环

  useEffect(() => {
    loadLogs(0, 10) // 页面初次访问时查询第一页数据（后端页码0）
  }, []) // 只在组件挂载时执行一次

  // 处理搜索
  const handleSearch = () => {
    setPagination(prev => ({ ...prev, current: 1 }))
    loadLogs(0, pagination.pageSize) // 搜索时从第一页开始（后端页码0）
  }

  // 处理重置
  const handleReset = () => {
    setFilters({
      operationType: '',
      operationTarget: '',
      username: '',
      dateRange: null,
    })
    setPagination(prev => ({ ...prev, current: 1 }))
  }

  // 处理查看详情
  const handleViewDetail = (record) => {
    setSelectedLog(record)
    setDetailModalVisible(true)
  }

  // 处理导出
  const handleExport = async () => {
    try {
      await OperationLogService.exportLogs(filters)
      message.success(translateOperationLog('exportSuccess'))
    } catch (error) {
      message.error(translateOperationLog('exportFailed'))
    }
  }

  // 处理分页变化
  const handleTableChange = (newPagination) => {
    loadLogs(newPagination.current - 1, newPagination.pageSize) // 转换为后端页码（从0开始）
  }

  // 获取操作类型标签颜色
  const getOperationTypeColor = (type) => {
    const colorMap = {
      CREATE: 'green',
      UPDATE: 'blue',
      DELETE: 'red',
      DOWNLOAD: 'orange',
      UPLOAD: 'purple',
      LOGIN: 'cyan',
      LOGOUT: 'gray',
    }
    return colorMap[type] || 'default'
  }

  // 获取操作目标标签颜色
  const getOperationTargetColor = (target) => {
    const colorMap = {
      USER: 'blue',
      ROLE: 'green',
      TEST_CASE_SET: 'orange',
      SOFTWARE_PACKAGE: 'purple',
      PREPROCESS_RULE: 'cyan',
      SYSTEM: 'red',
    }
    return colorMap[target] || 'default'
  }

  // 表格列配置
  const columns = [
    {
      title: translateOperationLog('table.id'),
      dataIndex: 'id',
      key: 'id',
      width: 80,
      sorter: (a, b) => a.id - b.id,
    },
    {
      title: translateOperationLog('table.username'),
      dataIndex: 'username',
      key: 'username',
      width: 120,
      render: (text) => (
        <Space>
          <UserOutlined />
          <span>{text}</span>
        </Space>
      ),
    },
    {
      title: translateOperationLog('table.createdTime'),
      dataIndex: 'operationTime',
      key: 'operationTime',
      width: 180,
      render: (text) => text ? moment(text).format('YYYY-MM-DD HH:mm:ss') : '-',
    },
    {
      title: translateOperationLog('table.operationType'),
      dataIndex: 'operationType',
      key: 'operationType',
      width: 120,
      render: (text) => (
        <Tag color={getOperationTypeColor(text)}>
          {translateOperationLog(`operationTypes.${text}`)}
        </Tag>
      ),
    },
    {
      title: translateOperationLog('table.operationTarget'),
      dataIndex: 'operationTarget',
      key: 'operationTarget',
      width: 120,
      render: (text) => (
        <Tag color={getOperationTargetColor(text)}>
          {translateOperationLog(`operationTarget.${text}`)}
        </Tag>
      ),
    },
    {
      title: translateOperationLog('table.description'),
      dataIndex: 'description',
      key: 'description',
      width: 200,
      ellipsis: true,
      render: (text, record) => {
        // 根据当前语言显示对应的操作描述
        const description = language === 'zh' 
          ? record.operationDescriptionZh 
          : record.operationDescriptionEn;
        return description || '-';
      },
    },
    {
      title: translateOperationLog('table.actions'),
      key: 'action',
      width: 100,
      render: (_, record) => (
        <Space size="small">
          <Tooltip title={translateOperationLog('table.viewDetail')}>
            <Button
              type="text"
              icon={<EyeOutlined />}
              onClick={() => handleViewDetail(record)}
            />
          </Tooltip>
        </Space>
      ),
    },
  ]

  return (
    <div style={{ padding: '24px' }}>
      {/* 页面标题 */}
      <div style={{ marginBottom: '24px' }}>
        <Title level={2} style={{ margin: 0 }}>
          <SettingOutlined style={{ marginRight: '8px' }} />
          {translateOperationLog('title')}
        </Title>
        <Text type="secondary">{translateOperationLog('description')}</Text>
      </div>


      {/* 搜索表单 */}
      <Card style={{ marginBottom: '16px' }}>
        <Row gutter={[16, 16]}>
          {/* 操作用户 */}
          <Col xs={24} sm={12} md={6}>
            <div style={{ marginBottom: '8px' }}>
              <Text strong>{translateOperationLog('filters.username')}</Text>
            </div>
            <Input
              placeholder={translateOperationLog('filters.usernamePlaceholder')}
              value={filters.username}
              onChange={(e) => setFilters(prev => ({ ...prev, username: e.target.value }))}
            />
          </Col>
          
          {/* 操作类型 */}
          <Col xs={24} sm={12} md={6}>
            <div style={{ marginBottom: '8px' }}>
              <Text strong>{translateOperationLog('filters.operationType')}</Text>
            </div>
            <Select
              placeholder={translateOperationLog('filters.operationTypePlaceholder')}
              value={filters.operationType}
              onChange={(value) => setFilters(prev => ({ ...prev, operationType: value }))}
              allowClear
              style={{ width: '100%' }}
            >
              <Option value="CREATE">{translateOperationLog('operationType.CREATE')}</Option>
              <Option value="UPDATE">{translateOperationLog('operationType.UPDATE')}</Option>
              <Option value="DELETE">{translateOperationLog('operationType.DELETE')}</Option>
              <Option value="DOWNLOAD">{translateOperationLog('operationType.DOWNLOAD')}</Option>
              <Option value="UPLOAD">{translateOperationLog('operationType.UPLOAD')}</Option>
              <Option value="LOGIN">{translateOperationLog('operationType.LOGIN')}</Option>
              <Option value="LOGOUT">{translateOperationLog('operationType.LOGOUT')}</Option>
            </Select>
          </Col>
          
          {/* 操作对象 */}
          <Col xs={24} sm={12} md={6}>
            <div style={{ marginBottom: '8px' }}>
              <Text strong>{translateOperationLog('filters.operationTarget')}</Text>
            </div>
            <Select
              placeholder={translateOperationLog('filters.targetPlaceholder')}
              value={filters.operationTarget}
              onChange={(value) => setFilters(prev => ({ ...prev, operationTarget: value }))}
              allowClear
              style={{ width: '100%' }}
            >
              <Option value="USER">{translateOperationLog('operationTarget.USER')}</Option>
              <Option value="ROLE">{translateOperationLog('operationTarget.ROLE')}</Option>
              <Option value="TEST_CASE_SET">{translateOperationLog('operationTarget.TEST_CASE_SET')}</Option>
              <Option value="SOFTWARE_PACKAGE">{translateOperationLog('operationTarget.SOFTWARE_PACKAGE')}</Option>
              <Option value="PREPROCESS_RULE">{translateOperationLog('operationTarget.PREPROCESS_RULE')}</Option>
              <Option value="SYSTEM">{translateOperationLog('operationTarget.SYSTEM')}</Option>
            </Select>
          </Col>
          
          {/* 时间范围 */}
          <Col xs={24} sm={12} md={6}>
            <div style={{ marginBottom: '8px' }}>
              <Text strong>{translateOperationLog('filters.timeRange')}</Text>
            </div>
            <RangePicker
              placeholder={[translateOperationLog('filters.startTime'), translateOperationLog('filters.endTime')]}
              value={filters.dateRange}
              onChange={(dates) => setFilters(prev => ({ ...prev, dateRange: dates }))}
              showTime
              style={{ width: '100%' }}
            />
          </Col>
        </Row>
        <Row style={{ marginTop: '16px' }}>
          <Col span={24} style={{ textAlign: 'right' }}>
            <Space>
              <Button
                type="primary"
                icon={<SearchOutlined />}
                onClick={handleSearch}
              >
                {translateCommon('search')}
              </Button>
              <Button
                icon={<ClearOutlined />}
                onClick={handleReset}
              >
                {translateCommon('reset')}
              </Button>
              <Button
                icon={<ReloadOutlined />}
                onClick={() => loadLogs(pagination.current - 1, pagination.pageSize)}
              >
                {translateCommon('refresh')}
              </Button>
              <Button
                icon={<DownloadOutlined />}
                onClick={handleExport}
              >
                {translateOperationLog('export')}
              </Button>
            </Space>
          </Col>
        </Row>
      </Card>

      {/* 操作记录表格 */}
      <Table
        columns={columns}
        dataSource={logs}
        rowKey="id"
        loading={loading}
        pagination={{
          ...pagination,
          showSizeChanger: true,
          showQuickJumper: true,
          showTotal: (total, range) => 
            translateOperationLog('table.pagination', { start: range[0], end: range[1], total }),
        }}
        onChange={handleTableChange}
        scroll={{ x: 1200 }}
        size="middle"
      />

      {/* 详情对话框 */}
      <Modal
        title={translateOperationLog('modal.detailTitle')}
        open={detailModalVisible}
        onCancel={() => {
          setDetailModalVisible(false)
          setSelectedLog(null)
        }}
        footer={null}
        width={800}
      >
        {selectedLog && (
          <div>
            <Descriptions column={2} bordered>
              <Descriptions.Item label={translateOperationLog('table.id')}>
                {selectedLog.id}
              </Descriptions.Item>
              <Descriptions.Item label={translateOperationLog('table.username')}>
                {selectedLog.username}
              </Descriptions.Item>
              <Descriptions.Item label={translateOperationLog('table.operationType')}>
                <Tag color={getOperationTypeColor(selectedLog.operationType)}>
                  {translateOperationLog(`operationTypes.${selectedLog.operationType}`)}
                </Tag>
              </Descriptions.Item>
              <Descriptions.Item label={translateOperationLog('table.operationTarget')}>
                <Tag color={getOperationTargetColor(selectedLog.operationTarget)}>
                  {translateOperationLog(`operationTarget.${selectedLog.operationTarget}`)}
                </Tag>
              </Descriptions.Item>
              <Descriptions.Item label={translateOperationLog('table.description')} span={2}>
                {language === 'zh' 
                  ? selectedLog.operationDescriptionZh 
                  : selectedLog.operationDescriptionEn || '-'}
              </Descriptions.Item>
              <Descriptions.Item label={translateOperationLog('table.createdTime')}>
                {selectedLog.operationTime 
                  ? moment(selectedLog.operationTime).format('YYYY-MM-DD HH:mm:ss')
                  : '-'}
              </Descriptions.Item>
            </Descriptions>
            
            {/* 操作数据详情 */}
            {selectedLog.operationData && (
              <div style={{ marginTop: '16px' }}>
                <Collapse>
                  <Panel header={translateOperationLog('modal.operationDataDetails')} key="1">
                    <div style={{ backgroundColor: '#f5f5f5', padding: '12px', borderRadius: '4px' }}>
                      <pre style={{ 
                        margin: 0, 
                        fontSize: '12px', 
                        lineHeight: '1.4',
                        wordBreak: 'break-all',
                        whiteSpace: 'pre-wrap',
                        fontFamily: 'Monaco, Menlo, "Ubuntu Mono", monospace'
                      }}>
                        {JSON.stringify(selectedLog.operationData, null, 2)}
                      </pre>
                    </div>
                  </Panel>
                </Collapse>
              </div>
            )}
            
            {/* 用户代理信息 */}
            {selectedLog.userAgent && (
              <div style={{ marginTop: '16px' }}>
                <Collapse>
                  <Panel header={translateOperationLog('modal.userAgentDetails')} key="2">
                    <Text code style={{ wordBreak: 'break-all' }}>
                      {selectedLog.userAgent}
                    </Text>
                  </Panel>
                </Collapse>
              </div>
            )}
          </div>
        )}
      </Modal>
    </div>
  )
}

OperationLogManagement.displayName = 'OperationLogManagement'

export default OperationLogManagement
