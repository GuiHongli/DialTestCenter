import { Button, Form, Input, Select, Space } from 'antd';
import React from 'react';
import { useTranslation } from '../hooks/useTranslation';
import { UserRoleFormData, Role } from '../types/userRole';

const { Option } = Select;

interface UserRoleFormProps {
  initialData?: UserRoleFormData;
  onSubmit: (data: UserRoleFormData) => void;
  onCancel: () => void;
  isLoading?: boolean;
}

/**
 * 用户角色表单组件
 */
export const UserRoleForm: React.FC<UserRoleFormProps> = ({
  initialData,
  onSubmit,
  onCancel,
  isLoading = false
}) => {
  const [form] = Form.useForm();
  const { translateUserRole, translateCommon } = useTranslation();

  const handleSubmit = (values: UserRoleFormData) => {
    onSubmit(values);
  };

  // 获取角色选项
  const getRoleOptions = () => {
    const roles: Role[] = ['ADMIN', 'OPERATOR', 'BROWSER', 'EXECUTOR'];
    return roles.map(role => (
      <Option key={role} value={role}>
        {translateUserRole(`form.roleDescriptions.${role}`)}
      </Option>
    ));
  };

  return (
    <Form
      form={form}
      layout="vertical"
      initialValues={initialData}
      onFinish={handleSubmit}
      autoComplete="off"
    >
      <Form.Item
        label={translateUserRole('form.username')}
        name="username"
        rules={[
          { required: true, message: translateUserRole('form.usernamePlaceholder') },
          { max: 100, message: '用户名长度不能超过100个字符' }
        ]}
      >
        <Input placeholder={translateUserRole('form.usernamePlaceholder')} disabled={isLoading} />
      </Form.Item>

      <Form.Item
        label={translateUserRole('form.role')}
        name="role"
        rules={[{ required: true, message: translateUserRole('form.rolePlaceholder') }]}
      >
        <Select placeholder={translateUserRole('form.rolePlaceholder')} disabled={isLoading}>
          {getRoleOptions()}
        </Select>
      </Form.Item>

      <Form.Item style={{ marginBottom: 0, textAlign: 'right' }}>
        <Space>
          <Button onClick={onCancel} disabled={isLoading}>
            {translateCommon('cancel')}
          </Button>
          <Button type="primary" htmlType="submit" loading={isLoading}>
            {translateCommon('save')}
          </Button>
        </Space>
      </Form.Item>
    </Form>
  );
};
