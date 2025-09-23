import { Button, Form, Input, Modal, Select, message } from 'antd'
import React, { useEffect } from 'react'
import { useTranslation } from '../hooks/useTranslation'
import testCaseSetService from '../services/testCaseSetService'
import { TestCaseSet } from '../types/testCaseSet'

const { Option } = Select

interface TestCaseSetEditProps {
  visible: boolean
  testCaseSet: TestCaseSet | null
  onCancel: () => void
  onSuccess: () => void
}

const TestCaseSetEdit: React.FC<TestCaseSetEditProps> = ({
  visible,
  testCaseSet,
  onCancel,
  onSuccess
}) => {
  const [form] = Form.useForm()
  const { translateTestCaseSet, translateCommon } = useTranslation()

  useEffect(() => {
    if (visible && testCaseSet) {
      form.setFieldsValue({
        description: testCaseSet.description,
        businessZh: testCaseSet.businessZh,
        businessEn: testCaseSet.businessEn
      })
    }
  }, [visible, testCaseSet, form])

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields()
      
      if (testCaseSet) {
        await testCaseSetService.updateTestCaseSet(testCaseSet.id, {
          description: values.description,
          businessZh: values.businessZh,
          businessEn: values.businessEn
        })
        
        message.success(translateTestCaseSet('updateSuccess'))
        onSuccess()
      }
    } catch (error) {
      message.error(translateTestCaseSet('updateFailed'))
    }
  }

  const handleCancel = () => {
    form.resetFields()
    onCancel()
  }

  return (
    <Modal
      title={translateTestCaseSet('editTestCaseSet')}
      open={visible}
      onOk={handleSubmit}
      onCancel={handleCancel}
      okText={translateCommon('confirm')}
      cancelText={translateCommon('cancel')}
      width={600}
    >
      <Form
        form={form}
        layout="vertical"
        initialValues={{
          businessZh: 'VPN阻断'
        }}
      >
        <Form.Item
          label={translateTestCaseSet('table.name')}
        >
          <Input
            value={testCaseSet?.name}
            disabled
            placeholder={translateTestCaseSet('table.name')}
          />
        </Form.Item>

        <Form.Item
          label={translateTestCaseSet('table.version')}
        >
          <Input
            value={testCaseSet?.version}
            disabled
            placeholder={translateTestCaseSet('table.version')}
          />
        </Form.Item>

        <Form.Item
          name="description"
          label={translateTestCaseSet('table.description')}
          rules={[
            { max: 1000, message: translateTestCaseSet('descriptionTooLong') }
          ]}
        >
          <Input.TextArea
            rows={4}
            placeholder={translateTestCaseSet('table.description')}
          />
        </Form.Item>

        <Form.Item
          name="businessZh"
          label={translateTestCaseSet('table.businessZh')}
          rules={[
            { required: true, message: translateTestCaseSet('businessZhRequired') }
          ]}
        >
          <Select placeholder={translateTestCaseSet('table.businessZh')}>
            <Option value="VPN阻断">VPN阻断</Option>
            <Option value="VPN_BLOCK">VPN_BLOCK</Option>
          </Select>
        </Form.Item>

        <Form.Item
          name="businessEn"
          label={translateTestCaseSet('table.businessEn')}
          rules={[
            { required: true, message: translateTestCaseSet('businessEnRequired') }
          ]}
        >
          <Select placeholder={translateTestCaseSet('table.businessEn')}>
            <Option value="VPN_BLOCK">VPN_BLOCK</Option>
            <Option value="VPN阻断">VPN阻断</Option>
          </Select>
        </Form.Item>
      </Form>
    </Modal>
  )
}

export default TestCaseSetEdit
