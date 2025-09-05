import { Button, Form, Input, Select, Space } from 'antd';
import React from 'react';
import { ROLE_DESCRIPTIONS, UserRoleFormData } from '../types/userRole';

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

  const handleSubmit = (values: UserRoleFormData) => {
    onSubmit(values);
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
        label="用户名"
        name="username"
        rules={[
          { required: true, message: '请输入用户名' },
          { max: 100, message: '用户名长度不能超过100个字符' }
        ]}
      >
        <Input placeholder="请输入用户名" disabled={isLoading} />
      </Form.Item>

      <Form.Item
        label="角色"
        name="role"
        rules={[{ required: true, message: '请选择角色' }]}
      >
        <Select placeholder="请选择角色" disabled={isLoading}>
          {Object.entries(ROLE_DESCRIPTIONS).map(([role, description]) => (
            <Option key={role} value={role}>
              {role} - {description}
            </Option>
          ))}
        </Select>
      </Form.Item>

      <Form.Item style={{ marginBottom: 0, textAlign: 'right' }}>
        <Space>
          <Button onClick={onCancel} disabled={isLoading}>
            取消
          </Button>
          <Button type="primary" htmlType="submit" loading={isLoading}>
            保存
          </Button>
        </Space>
      </Form.Item>
    </Form>
  );
};
