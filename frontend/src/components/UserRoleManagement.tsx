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
  const [loading, setLoading] = useState(false);
  const [modalVisible, setModalVisible] = useState(false);
  const [editingUserRole, setEditingUserRole] = useState<UserRole | null>(null);
  const [formLoading, setFormLoading] = useState(false);
  const [currentUserRoles, setCurrentUserRoles] = useState<string[]>([]);
  const [userRoleLoading, setUserRoleLoading] = useState(true);
  const [executorCount, setExecutorCount] = useState<number>(0);
  const [searchText, setSearchText] = useState('');
  const [pagination, setPagination] = useState({
    current: 1,
    pageSize: 10,
    total: 0,
  });

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
      
      // 调用权限检查接口获取用户角色
      const permissionResult = await UserRoleService.checkPermission({
        username: mockUserName,
        roles: ['ADMIN', 'OPERATOR', 'BROWSER', 'EXECUTOR']
      });
      setCurrentUserRoles(permissionResult.userRoles);
      
      // 获取当前用户角色后，再加载所有用户角色列表
      await loadUserRoles(0, 10);
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
      // 使用本地计算执行机数量
      const localCount = userRoles.filter(ur => ur.role === 'EXECUTOR').length;
      setExecutorCount(localCount);
    } catch (err) {
      console.error('获取执行机数量失败:', err);
      // 如果计算失败，设置为0
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
          {role} - {ROLE_DESCRIPTIONS[role]}
        </Tag>
      ),
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
  ], [translateUserRole, translateCommon, canManage]);

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
        <div style={{ textAlign: 'left' }}>
          <Title level={2} style={{ margin: 0, textAlign: 'left' }}>
            <TeamOutlined style={{ marginRight: '8px' }} />
            {translateUserRole('title')}
          </Title>
          <Text type="secondary" style={{ fontSize: '14px', textAlign: 'left' }}>
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
              onClick={() => loadUserRoles(pagination.current - 1, pagination.pageSize, searchText)}
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
