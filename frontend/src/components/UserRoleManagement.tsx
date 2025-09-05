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
  Typography
} from 'antd';
import type { ColumnsType } from 'antd/es/table';
import React, { useEffect, useState } from 'react';
import { usePermission } from '../hooks/usePermission';
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
      message.error(err instanceof Error ? err.message : '获取用户角色失败');
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
      message.error(err instanceof Error ? err.message : '加载用户角色列表失败');
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
      message.success('用户角色创建成功');
      await loadUserRoles();
      await loadExecutorCount();
    } catch (err) {
      message.error(err instanceof Error ? err.message : '创建用户角色失败');
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
      message.success('用户角色更新成功');
      await loadUserRoles();
      await loadExecutorCount();
    } catch (err) {
      message.error(err instanceof Error ? err.message : '更新用户角色失败');
    } finally {
      setFormLoading(false);
    }
  };

  const handleDeleteUserRole = async (id: number) => {
    try {
      await UserRoleService.deleteUserRole(id);
      message.success('用户角色删除成功');
      await loadUserRoles();
      await loadExecutorCount();
    } catch (err) {
      message.error(err instanceof Error ? err.message : '删除用户角色失败');
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
      title: 'ID',
      dataIndex: 'id',
      key: 'id',
      width: 80,
      sorter: (a: UserRole, b: UserRole) => a.id - b.id,
    },
    {
      title: '用户名',
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
      title: '角色',
      dataIndex: 'role',
      key: 'role',
      width: 200,
      filters: [
        { text: '管理员', value: 'ADMIN' },
        { text: '操作员', value: 'OPERATOR' },
        { text: '浏览者', value: 'BROWSER' },
        { text: '执行机', value: 'EXECUTOR' },
      ],
      onFilter: (value: any, record: UserRole) => record.role === value,
      render: (role: Role) => (
        <Tag color={getRoleTagColor(role)}>
          {role} - {ROLE_DESCRIPTIONS[role]}
        </Tag>
      ),
    },
    {
      title: '创建时间',
      dataIndex: 'createdTime',
      key: 'createdTime',
      width: 180,
      sorter: (a: UserRole, b: UserRole) => 
        new Date(a.createdTime).getTime() - new Date(b.createdTime).getTime(),
      render: (text: string) => formatDateTime(text),
    },
    {
      title: '更新时间',
      dataIndex: 'updatedTime',
      key: 'updatedTime',
      width: 180,
      sorter: (a: UserRole, b: UserRole) => 
        new Date(a.updatedTime).getTime() - new Date(b.updatedTime).getTime(),
      render: (text: string) => formatDateTime(text),
    },
    ...(canManage ? [{
      title: '操作',
      key: 'action',
      width: 120,
      render: (_: any, record: UserRole) => (
        <Space size="small">
          <Button
            type="link"
            icon={<EditOutlined />}
            onClick={() => handleEdit(record)}
            size="small"
          >
            编辑
          </Button>
          <Popconfirm
            title="确定要删除这个用户角色吗？"
            description="此操作不可撤销"
            onConfirm={() => handleDeleteUserRole(record.id)}
            okText="确定"
            cancelText="取消"
          >
            <Button
              type="link"
              danger
              icon={<DeleteOutlined />}
              size="small"
            >
              删除
            </Button>
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
            正在获取用户权限信息...
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
            用户角色管理
          </Title>
          <Text type="secondary" style={{ fontSize: '14px' }}>
            当前用户角色: {currentUserRoles.length > 0 ? currentUserRoles.join(', ') : '无权限'}
          </Text>
          {/* 测试用角色切换按钮 */}
          <div style={{ marginTop: '8px' }}>
            <Space size="small">
              <Text type="secondary" style={{ fontSize: '12px' }}>测试切换用户:</Text>
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
              新增用户角色
            </Button>
            <Button
              icon={<ReloadOutlined />}
              onClick={loadUserRoles}
            >
              刷新
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
                title="总用户数"
                value={statistics.totalUsers}
                prefix={<UserOutlined />}
                valueStyle={{ color: '#1890ff' }}
              />
            </Card>
          </Col>
          <Col span={6}>
            <Card>
              <Statistic
                title="总执行机数"
                value={statistics.executorCount}
                prefix={<TeamOutlined />}
                valueStyle={{ color: '#fa8c16' }}
              />
            </Card>
          </Col>
          <Col span={6}>
            <Card>
              <Statistic
                title="管理员数"
                value={statistics.adminCount}
                valueStyle={{ color: '#f5222d' }}
              />
            </Card>
          </Col>
          <Col span={6}>
            <Card>
              <Statistic
                title="操作员数"
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
              `第 ${range[0]}-${range[1]} 条/共 ${total} 条`,
          }}
          scroll={{ x: 800 }}
          size="middle"
        />
      </Card>

      {/* 新增/编辑表单对话框 */}
      <Modal
        title={editingUserRole ? '编辑用户角色' : '新增用户角色'}
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
