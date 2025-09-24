import {
  DeleteOutlined,
  EditOutlined,
  PlusOutlined,
  ReloadOutlined,
  SearchOutlined,
  TeamOutlined,
  UserOutlined
} from '@ant-design/icons';
import {
  Button,
  Card,
  Col,
  Input,
  message,
  Modal,
  Popconfirm,
  Row,
  Space,
  Statistic,
  Table,
  Tag,
  Tooltip,
  Typography
} from 'antd';
import type { ColumnsType } from 'antd/es/table';
import React, { useEffect, useMemo, useState } from 'react';
import { usePermission, PagePermission } from '../hooks/usePermission';
import { useTranslation } from '../hooks/useTranslation';
import { UserRoleService } from '../services/userRoleService';
import { Role, UserRole, UserRoleFormData } from '../types/userRole';
import { UserRoleForm } from './UserRoleForm';

const { Title, Text } = Typography;

/**
 * 用户角色管理页面组件
 */
export const UserRoleManagement: React.FC = () => {
  const [userRoles, setUserRoles] = useState<UserRole[]>([]);
  const [loading, setLoading] = useState(false);
  const [modalVisible, setModalVisible] = useState(false);
  const [editingUserRole, setEditingUserRole] = useState<UserRole | null>(null);
  const [formLoading, setFormLoading] = useState(false);
  const [executorCount, setExecutorCount] = useState<number>(0);
  const [searchText, setSearchText] = useState('');
  const [pagination, setPagination] = useState({
    current: 1,
    pageSize: 10,
    total: 0,
  });

  const { hasPagePermission } = usePermission();
  const { translateUserRole, translateCommon } = useTranslation();

  const canManage = hasPagePermission('user-role-management', 'create') || 
                   hasPagePermission('user-role-management', 'edit') || 
                   hasPagePermission('user-role-management', 'delete');

  useEffect(() => {
    // 页面进入时加载用户角色列表和执行机数量
    loadUserRoles(0, 10);
    loadExecutorCount();
  }, []);

  const loadUserRoles = async (page: number = 0, pageSize: number = 10, search?: string) => {
    try {
      setLoading(true);
      const response = await UserRoleService.getUserRolesWithPagination(page, pageSize, search);
      setUserRoles(response.content);
      setPagination({
        current: response.number + 1, // 转换为1开始的页码
        pageSize: response.size,
        total: response.totalElements,
      });
    } catch (err) {
      message.error(err instanceof Error ? err.message : translateUserRole('loadFailed'));
    } finally {
      setLoading(false);
    }
  };

  const loadExecutorCount = async () => {
    try {
      const count = await UserRoleService.getExecutorCount();
      setExecutorCount(count);
    } catch (err) {
      console.error('获取执行机数量失败:', err);
      message.error(err instanceof Error ? err.message : '获取执行机数量失败');
      setExecutorCount(0);
    }
  };

  const handleCreateUserRole = async (formData: UserRoleFormData) => {
    try {
      setFormLoading(true);
      await UserRoleService.createUserRole(formData);
      setModalVisible(false);
      message.success(translateUserRole('createSuccess'));
      await loadUserRoles(pagination.current - 1, pagination.pageSize, searchText);
      await loadExecutorCount();
    } catch (err) {
      message.error(err instanceof Error ? err.message : translateUserRole('createFailed'));
    } finally {
      setFormLoading(false);
    }
  };

  const handleUpdateUserRole = async (formData: UserRoleFormData) => {
    if (!editingUserRole) return;

    try {
      setFormLoading(true);
      await UserRoleService.updateUserRole(editingUserRole.id, formData);
      setEditingUserRole(null);
      setModalVisible(false);
      message.success(translateUserRole('updateSuccess'));
      await loadUserRoles(pagination.current - 1, pagination.pageSize, searchText);
      await loadExecutorCount();
    } catch (err) {
      message.error(err instanceof Error ? err.message : translateUserRole('updateFailed'));
    } finally {
      setFormLoading(false);
    }
  };

  const handleDeleteUserRole = async (id: number) => {
    try {
      await UserRoleService.deleteUserRole(id);
      message.success(translateUserRole('deleteSuccess'));
      await loadUserRoles(pagination.current - 1, pagination.pageSize, searchText);
      await loadExecutorCount();
    } catch (err) {
      message.error(err instanceof Error ? err.message : translateUserRole('deleteFailed'));
    }
  };

  const handleEdit = (record: UserRole) => {
    setEditingUserRole(record);
    setModalVisible(true);
  };

  const handleAdd = () => {
    setEditingUserRole(null);
    setModalVisible(true);
  };

  const handleModalCancel = () => {
    setModalVisible(false);
    setEditingUserRole(null);
  };


  // 处理搜索
  const handleSearch = () => {
    loadUserRoles(0, pagination.pageSize, searchText);
  };

  // 处理分页变化
  const handleTableChange = (pagination: any) => {
    loadUserRoles(pagination.current - 1, pagination.pageSize, searchText); // 转换为0开始的页码
  };

  // 获取角色标签颜色
  const getRoleTagColor = (role: Role) => {
    const colorMap = {
      ADMIN: 'red',
      OPERATOR: 'blue',
      BROWSER: 'green',
      EXECUTOR: 'orange'
    };
    return colorMap[role] || 'default';
  };

  // 统计信息
  const getStatistics = () => {
    const totalUsers = new Set(userRoles.map(ur => ur.username)).size;
    const adminCount = userRoles.filter(ur => ur.role === 'ADMIN').length;
    const operatorCount = userRoles.filter(ur => ur.role === 'OPERATOR').length;
    // 使用API获取的执行机数量
    const executorCountValue = executorCount;
    
    return { totalUsers, adminCount, operatorCount, executorCount: executorCountValue };
  };

  const statistics = getStatistics();

  // 表格列配置
  const columns: ColumnsType<UserRole> = useMemo(() => [
    {
      title: translateUserRole('table.id'),
      dataIndex: 'id',
      key: 'id',
      width: 80,
      sorter: (a: UserRole, b: UserRole) => a.id - b.id,
    },
    {
      title: translateUserRole('table.username'),
      dataIndex: 'username',
      key: 'username',
      width: 150,
      sorter: (a: UserRole, b: UserRole) => a.username.localeCompare(b.username),
      render: (text: string) => (
        <Space>
          <UserOutlined />
          <Text strong>{text}</Text>
        </Space>
      ),
    },
    {
      title: translateUserRole('table.role'),
      dataIndex: 'role',
      key: 'role',
      width: 200,
      filters: [
        { text: translateUserRole('table.filters.admin'), value: 'ADMIN' },
        { text: translateUserRole('table.filters.operator'), value: 'OPERATOR' },
        { text: translateUserRole('table.filters.browser'), value: 'BROWSER' },
        { text: translateUserRole('table.filters.executor'), value: 'EXECUTOR' },
      ],
      onFilter: (value: any, record: UserRole) => record.role === value,
      render: (role: Role) => (
        <Tag color={getRoleTagColor(role)}>
          {translateUserRole(`form.roleDescriptions.${role}`)}
        </Tag>
      ),
    },
    {
      title: translateUserRole('table.actions'),
      key: 'action',
      width: 120,
      render: (_: any, record: UserRole) => (
        <Space size="small">
          <PagePermission pageId="user-role-management" operation="edit">
            <Tooltip title={translateUserRole('table.edit')}>
              <Button
                type="text"
                icon={<EditOutlined />}
                onClick={() => handleEdit(record)}
              />
            </Tooltip>
          </PagePermission>
          <PagePermission pageId="user-role-management" operation="delete">
            <Popconfirm
              title={translateUserRole('confirmDelete')}
              onConfirm={() => handleDeleteUserRole(record.id)}
              okText={translateCommon('confirm')}
              cancelText={translateCommon('cancel')}
            >
              <Tooltip title={translateUserRole('table.delete')}>
                <Button
                  type="text"
                  danger
                  icon={<DeleteOutlined />}
                />
              </Tooltip>
            </Popconfirm>
          </PagePermission>
        </Space>
      ),
    },
  ], [translateUserRole, translateCommon, canManage]);

  return (
    <div style={{ padding: '24px' }}>
      {/* 页面标题和操作按钮 */}
      <div style={{ 
        display: 'flex', 
        justifyContent: 'space-between', 
        alignItems: 'center', 
        marginBottom: '24px' 
      }}>
        <div style={{ textAlign: 'left' }}>
          <Title level={2} style={{ margin: 0, textAlign: 'left' }}>
            <TeamOutlined style={{ marginRight: '8px' }} />
            {translateUserRole('title')}
          </Title>
        </div>
        <Space>
          <PagePermission pageId="user-role-management" operation="create">
            <Button
              type="primary"
              icon={<PlusOutlined />}
              onClick={handleAdd}
            >
              {translateUserRole('addUserRole')}
            </Button>
          </PagePermission>
          <Button
            icon={<ReloadOutlined />}
            onClick={() => loadUserRoles(pagination.current - 1, pagination.pageSize, searchText)}
          >
            {translateCommon('refresh')}
          </Button>
        </Space>
      </div>

      {/* 统计信息卡片 - 只有管理员才能看到 */}
      {canManage && (
        <Row gutter={16} style={{ marginBottom: '24px' }}>
          <Col span={6}>
            <Card>
              <Statistic
                title={translateUserRole('statistics.totalUsers')}
                value={statistics.totalUsers}
                prefix={<UserOutlined />}
                valueStyle={{ color: '#1890ff' }}
              />
            </Card>
          </Col>
          <Col span={6}>
            <Card>
              <Statistic
                title={translateUserRole('statistics.totalExecutors')}
                value={statistics.executorCount}
                prefix={<TeamOutlined />}
                valueStyle={{ color: '#fa8c16' }}
              />
            </Card>
          </Col>
          <Col span={6}>
            <Card>
              <Statistic
                title={translateUserRole('statistics.adminCount')}
                value={statistics.adminCount}
                valueStyle={{ color: '#f5222d' }}
              />
            </Card>
          </Col>
          <Col span={6}>
            <Card>
              <Statistic
                title={translateUserRole('statistics.operatorCount')}
                value={statistics.operatorCount}
                valueStyle={{ color: '#722ed1' }}
              />
            </Card>
          </Col>
        </Row>
      )}

      {/* 搜索和过滤 */}
      <Card style={{ marginBottom: '16px' }}>
        <Row gutter={[16, 16]} align="middle">
          <Col xs={24} sm={8} md={6}>
            <div style={{ 
              display: 'flex', 
              alignItems: 'center', 
              gap: '8px',
              height: '40px'
            }}>
              <SearchOutlined style={{ color: '#1890ff', fontSize: '16px' }} />
              <span style={{ 
                fontSize: '14px', 
                color: '#262626',
                fontWeight: 500,
                whiteSpace: 'nowrap'
              }}>
                {translateUserRole('filters.searchUsername')}:
              </span>
            </div>
          </Col>
          <Col xs={24} sm={16} md={14}>
            <Input
              placeholder={translateUserRole('form.usernamePlaceholder')}
              value={searchText}
              onChange={(e) => setSearchText(e.target.value)}
              allowClear
              onPressEnter={handleSearch}
              size="middle"
              style={{ 
                borderRadius: '8px',
                boxShadow: '0 2px 4px rgba(0,0,0,0.02)'
              }}
              prefix={<SearchOutlined style={{ color: '#bfbfbf' }} />}
            />
          </Col>
          <Col xs={24} sm={24} md={4}>
            <Button
              type="primary"
              onClick={handleSearch}
              size="middle"
              style={{ 
                borderRadius: '8px',
                minWidth: '100px',
                boxShadow: '0 2px 4px rgba(24, 144, 255, 0.2)'
              }}
            >
              {translateCommon('search')}
            </Button>
          </Col>
        </Row>
      </Card>

      {/* 用户角色表格 */}
      <Card>
        <Table
          columns={columns}
          dataSource={userRoles}
          rowKey="id"
          loading={loading}
          pagination={{
            ...pagination,
            showSizeChanger: true,
            showQuickJumper: true,
            showTotal: (total, range) => 
              translateUserRole('table.pagination', { start: range[0], end: range[1], total }),
          }}
          onChange={handleTableChange}
          scroll={{ x: 800 }}
          size="middle"
        />
      </Card>

      {/* 新增/编辑表单对话框 */}
      <Modal
        title={editingUserRole ? translateUserRole('editUserRole') : translateUserRole('addUserRole')}
        open={modalVisible}
        onCancel={handleModalCancel}
        footer={null}
        width={600}
        destroyOnClose
      >
        <UserRoleForm
          initialData={editingUserRole ? {
            username: editingUserRole.username,
            role: editingUserRole.role as Role
          } : undefined}
          onSubmit={editingUserRole ? handleUpdateUserRole : handleCreateUserRole}
          onCancel={handleModalCancel}
          isLoading={formLoading}
        />
      </Modal>
    </div>
  );
};
