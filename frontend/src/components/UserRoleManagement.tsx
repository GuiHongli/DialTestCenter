import {
  DeleteOutlined,
  EditOutlined,
  PlusOutlined,
  ReloadOutlined,
  TeamOutlined,
  UserOutlined
} from '@ant-design/icons';
import {
  Button,
  Card,
  Col,
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
import React, { useEffect, useState } from 'react';
import { usePermission } from '../hooks/usePermission';
import { useTranslation } from '../hooks/useTranslation';
import { UserRoleService } from '../services/userRoleService';
import { Role, ROLE_DESCRIPTIONS, UserRole, UserRoleFormData } from '../types/userRole';
import { UserRoleForm } from './UserRoleForm';

const { Title, Text } = Typography;

/**
 * 用户角色管理页面组件
 */
export const UserRoleManagement: React.FC = () => {
  const [userRoles, setUserRoles] = useState<UserRole[]>([]);
  const [loading, setLoading] = useState(true);
  const [modalVisible, setModalVisible] = useState(false);
  const [editingUserRole, setEditingUserRole] = useState<UserRole | null>(null);
  const [formLoading, setFormLoading] = useState(false);
  const [currentUserRoles, setCurrentUserRoles] = useState<string[]>([]);
  const [userRoleLoading, setUserRoleLoading] = useState(true);
  const [executorCount, setExecutorCount] = useState<number>(0);

  const { canManageUsers } = usePermission();
  const { translateUserRole, translateCommon } = useTranslation();

  const canManage = canManageUsers(currentUserRoles);

  useEffect(() => {
    // 页面进入时先获取当前用户角色
    loadCurrentUserRoles();
  }, []);

  // 获取当前用户角色
  const loadCurrentUserRoles = async () => {
    try {
      setUserRoleLoading(true);
      // 模拟调用外部接口获取userName，暂时使用admin
      // 可以通过URL参数或localStorage来模拟不同用户
      const urlParams = new URLSearchParams(window.location.search);
      const mockUserName = urlParams.get('user') || 'admin';
      
      // 调用后台角色查询接口
      const userRoles = await UserRoleService.getUserRoles(mockUserName);
      const roleNames = userRoles.map(ur => ur.role);
      setCurrentUserRoles(roleNames);
      
      // 获取当前用户角色后，再加载所有用户角色列表
      await loadUserRoles();
      // 加载执行机数量
      await loadExecutorCount();
    } catch (err) {
      message.error(err instanceof Error ? err.message : translateUserRole('getUserRolesFailed'));
      // 如果获取用户角色失败，默认设置为非管理员权限
      setCurrentUserRoles([]);
    } finally {
      setUserRoleLoading(false);
    }
  };

  const loadUserRoles = async () => {
    try {
      setLoading(true);
      const data = await UserRoleService.getUserRoles();
      setUserRoles(data);
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
      // 如果API调用失败，使用本地计算作为备选
      const localCount = userRoles.filter(ur => ur.role === 'EXECUTOR').length;
      setExecutorCount(localCount);
    }
  };

  const handleCreateUserRole = async (formData: UserRoleFormData) => {
    try {
      setFormLoading(true);
      await UserRoleService.createUserRole(formData);
      setModalVisible(false);
      message.success(translateUserRole('createSuccess'));
      await loadUserRoles();
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
      await loadUserRoles();
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
      await loadUserRoles();
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

  const formatDateTime = (dateTime: string) => {
    return new Date(dateTime).toLocaleString('zh-CN');
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
  const columns: ColumnsType<UserRole> = [
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
          {role} - {ROLE_DESCRIPTIONS[role]}
        </Tag>
      ),
    },
    {
      title: translateUserRole('table.createdTime'),
      dataIndex: 'createdTime',
      key: 'createdTime',
      width: 180,
      sorter: (a: UserRole, b: UserRole) => 
        new Date(a.createdTime).getTime() - new Date(b.createdTime).getTime(),
      render: (text: string) => formatDateTime(text),
    },
    {
      title: translateUserRole('table.updatedTime'),
      dataIndex: 'updatedTime',
      key: 'updatedTime',
      width: 180,
      sorter: (a: UserRole, b: UserRole) => 
        new Date(a.updatedTime).getTime() - new Date(b.updatedTime).getTime(),
      render: (text: string) => formatDateTime(text),
    },
    ...(canManage ? [{
      title: translateUserRole('table.actions'),
      key: 'action',
      width: 120,
      render: (_: any, record: UserRole) => (
        <Space size="small">
          <Tooltip title={translateUserRole('table.edit')}>
            <Button
              type="text"
              icon={<EditOutlined />}
              onClick={() => handleEdit(record)}
            />
          </Tooltip>
          <Popconfirm
            title={translateUserRole('confirmDelete')}
            description={translateUserRole('deleteDescription')}
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
        </Space>
      ),
    }] : []),
  ];

  // 如果正在加载用户角色，显示加载状态
  if (userRoleLoading) {
    return (
      <div style={{ 
        padding: '24px', 
        display: 'flex', 
        justifyContent: 'center', 
        alignItems: 'center', 
        height: '400px' 
      }}>
        <div style={{ textAlign: 'center' }}>
          <div style={{ fontSize: '16px', color: '#666', marginBottom: '16px' }}>
            {translateUserRole('gettingUserPermission')}
          </div>
        </div>
      </div>
    );
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
            <TeamOutlined style={{ marginRight: '8px' }} />
            {translateUserRole('title')}
          </Title>
          <Text type="secondary" style={{ fontSize: '14px' }}>
            {translateUserRole('currentUserRoles')}: {currentUserRoles.length > 0 ? currentUserRoles.join(', ') : translateUserRole('noPermission')}
          </Text>
          {/* 测试用角色切换按钮 */}
          <div style={{ marginTop: '8px' }}>
            <Space size="small">
              <Text type="secondary" style={{ fontSize: '12px' }}>{translateUserRole('testSwitchUser')}:</Text>
              <Button 
                size="small" 
                type={currentUserRoles.includes('ADMIN') ? 'primary' : 'default'}
                onClick={() => window.location.href = '/user-roles?user=admin'}
              >
                admin
              </Button>
              <Button 
                size="small" 
                type={currentUserRoles.includes('OPERATOR') && !currentUserRoles.includes('ADMIN') ? 'primary' : 'default'}
                onClick={() => window.location.href = '/user-roles?user=operator1'}
              >
                operator1
              </Button>
              <Button 
                size="small" 
                type={currentUserRoles.includes('BROWSER') && !currentUserRoles.includes('ADMIN') ? 'primary' : 'default'}
                onClick={() => window.location.href = '/user-roles?user=browser1'}
              >
                browser1
              </Button>
            </Space>
          </div>
        </div>
        {canManage && (
          <Space>
            <Button
              type="primary"
              icon={<PlusOutlined />}
              onClick={handleAdd}
            >
              {translateUserRole('addUserRole')}
            </Button>
            <Button
              icon={<ReloadOutlined />}
              onClick={loadUserRoles}
            >
              {translateCommon('refresh')}
            </Button>
          </Space>
        )}
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

      {/* 用户角色表格 */}
      <Card>
        <Table
          columns={columns}
          dataSource={userRoles}
          rowKey="id"
          loading={loading}
          pagination={{
            total: userRoles.length,
            pageSize: 10,
            showSizeChanger: true,
            showQuickJumper: true,
            showTotal: (total, range) => 
              translateUserRole('table.pagination', { start: range[0], end: range[1], total }),
          }}
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
