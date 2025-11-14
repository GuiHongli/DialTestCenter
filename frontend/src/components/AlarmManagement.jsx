import React, { useState, useEffect, useCallback } from 'react'
import {
  Card,
  Table,
  Button,
  Space,
  Tag,
  message,
  Modal,
  Popconfirm,
  Tabs,
  Typography,
  Tooltip,
} from 'antd'
import {
  ReloadOutlined,
  DeleteOutlined,
  BellOutlined,
} from '@ant-design/icons'
import { AlarmService, AlarmUtils } from '../services/alarmService.js'
import { useTranslation } from '../hooks/useTranslation.js'
import { useI18n } from '../contexts/I18nContext.jsx'

const { Title, Text } = Typography
const { TabPane } = Tabs

/**
 * 告警管理组件
 */
const AlarmManagement = () => {
  const { translateAlarm, translateCommon } = useTranslation()
  const { language } = useI18n()
  
  // 状态管理
  const [alarms, setAlarms] = useState([])
  const [loading, setLoading] = useState(false)
  const [pagination, setPagination] = useState({
    current: 1,
    pageSize: 20,
    total: 0,
  })
  const [activeTab, setActiveTab] = useState('current')

  // 加载告警
  const loadAlarms = useCallback(async (page = 0, pageSize = 20, currentOnly = true) => {
    setLoading(true)
    try {
      const params = {
        page,
        size: pageSize,
        currentOnly,
      }
      
      const response = await AlarmService.getAlarms(params)
      
      if (response && response.content && Array.isArray(response.content)) {
        setAlarms(response.content)
        setPagination({
          current: response.number + 1,
          pageSize: response.size,
          total: response.totalElements,
        })
      } else {
        setAlarms([])
        setPagination({
          current: page + 1,
          pageSize: pageSize,
          total: 0,
        })
      }
    } catch (error) {
      message.error(translateAlarm('loadFailed') || 'Failed to load alarms')
    } finally {
      setLoading(false)
    }
  }, [translateAlarm])

  useEffect(() => {
    const currentOnly = activeTab === 'current'
    loadAlarms(0, pagination.pageSize, currentOnly)
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [activeTab])

  // 处理分页变化
  const handleTableChange = (page, pageSize) => {
    const currentOnly = activeTab === 'current'
    setPagination(prev => ({ ...prev, current: page, pageSize: pageSize || prev.pageSize }))
    loadAlarms(page - 1, pageSize || pagination.pageSize, currentOnly)
  }

  // 处理刷新
  const handleRefresh = () => {
    const currentOnly = activeTab === 'current'
    loadAlarms(pagination.current - 1, pagination.pageSize, currentOnly)
  }

  // 处理结束告警
  const handleEndAlarm = async (id) => {
    try {
      await AlarmService.endAlarm(id)
      message.success(translateAlarm('endSuccess') || 'Alarm ended successfully')
      handleRefresh()
    } catch (error) {
      message.error(translateAlarm('endFailed') || 'Failed to end alarm')
    }
  }

  // 处理标签页切换
  const handleTabChange = (key) => {
    setActiveTab(key)
    setPagination(prev => ({ ...prev, current: 1 }))
  }

  // 获取表格列定义
  const getColumns = (showEndTime = true) => {
    const baseColumns = [
      {
        title: translateAlarm('id') || 'ID',
        dataIndex: 'id',
        key: 'id',
        width: 80,
      },
      {
        title: translateAlarm('summary') || 'Summary',
        dataIndex: 'alarmSummary',
        key: 'alarmSummary',
        ellipsis: true,
      },
      {
        title: translateAlarm('description') || 'Description',
        dataIndex: 'alarmDescription',
        key: 'alarmDescription',
        ellipsis: true,
        render: (text) => text || '-',
      },
      {
        title: translateAlarm('level') || 'Level',
        dataIndex: 'alarmLevel',
        key: 'alarmLevel',
        width: 120,
        render: (level) => (
          <Tag color={AlarmUtils.getAlarmLevelColor(level)}>
            {AlarmUtils.getAlarmLevelText(level, language)}
          </Tag>
        ),
      },
      {
        title: translateAlarm('startTime') || 'Start Time',
        dataIndex: 'startTime',
        key: 'startTime',
        width: 180,
        render: (time) => AlarmUtils.formatAlarmTime(time),
      },
    ]

    // 如果显示结束时间，则添加结束时间列
    if (showEndTime) {
      baseColumns.push({
        title: translateAlarm('endTime') || 'End Time',
        dataIndex: 'endTime',
        key: 'endTime',
        width: 180,
        render: (time) => time ? AlarmUtils.formatAlarmTime(time) : '-',
      })
    }

    // 添加操作列
    baseColumns.push({
      title: translateCommon('actions') || 'Actions',
      key: 'actions',
      width: 100,
      render: (_, record) => (
        <Space size="small">
          {!record.endTime && (
            <Popconfirm
              title={translateAlarm('confirmEnd') || 'Are you sure to end this alarm?'}
              onConfirm={() => handleEndAlarm(record.id)}
              okText={translateCommon('yes') || 'Yes'}
              cancelText={translateCommon('no') || 'No'}
            >
              <Tooltip title={translateAlarm('end') || 'End Alarm'}>
                <Button
                  type="text"
                  danger
                  icon={<DeleteOutlined />}
                />
              </Tooltip>
            </Popconfirm>
          )}
        </Space>
      ),
    })

    return baseColumns
  }

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
            <BellOutlined style={{ marginRight: '8px' }} />
            {translateAlarm('title') || 'Alarm Management'}
          </Title>
          <Text type="secondary">{translateAlarm('description') || 'Manage system alarms and monitor alarm status'}</Text>
        </div>
        <Space>
          <Button
            icon={<ReloadOutlined />}
            onClick={handleRefresh}
          >
            {translateCommon('refresh') || 'Refresh'}
          </Button>
        </Space>
      </div>

      {/* 告警表格 */}
      <Card>
        <Tabs activeKey={activeTab} onChange={handleTabChange}>
          <TabPane
            tab={translateAlarm('currentAlarms') || 'Current Alarms'}
            key="current"
          >
            <Table
              columns={getColumns(false)}
              dataSource={alarms}
              rowKey="id"
              loading={loading}
              pagination={{
                ...pagination,
                showSizeChanger: true,
                showQuickJumper: true,
                showTotal: (total, range) => 
                  translateAlarm('table.pagination', { start: range[0], end: range[1], total }),
                onChange: handleTableChange,
                onShowSizeChange: (current, size) => {
                  setPagination(prev => ({ ...prev, current: 1, pageSize: size }))
                  const currentOnly = activeTab === 'current'
                  loadAlarms(0, size, currentOnly)
                },
              }}
              scroll={{ x: 1200 }}
              size="middle"
            />
          </TabPane>
          <TabPane
            tab={translateAlarm('allAlarms') || 'All Alarms'}
            key="all"
          >
            <Table
              columns={getColumns(true)}
              dataSource={alarms}
              rowKey="id"
              loading={loading}
              pagination={{
                ...pagination,
                showSizeChanger: true,
                showQuickJumper: true,
                showTotal: (total, range) => 
                  translateAlarm('table.pagination', { start: range[0], end: range[1], total }),
                onChange: handleTableChange,
                onShowSizeChange: (current, size) => {
                  setPagination(prev => ({ ...prev, current: 1, pageSize: size }))
                  const currentOnly = activeTab === 'current'
                  loadAlarms(0, size, currentOnly)
                },
              }}
              scroll={{ x: 1200 }}
              size="middle"
            />
          </TabPane>
        </Tabs>
      </Card>
    </div>
  )
}

export default AlarmManagement

