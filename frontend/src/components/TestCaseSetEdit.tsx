import { Button, Form, Input, Modal, Select, message } from 'antd'
import React, { useEffect } from 'react'
import { useTranslation } from '../hooks/useTranslation'
import { useI18n } from '../contexts/I18nContext'
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
  const { language } = useI18n()

  useEffect(() => {
    if (visible && testCaseSet) {
      form.setFieldsValue({
        description: testCaseSet.description,
        businessZh: language === 'en' ? testCaseSet.businessEn : testCaseSet.businessZh
      })
    }
  }, [visible, testCaseSet, form, language])

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields()
      
      if (testCaseSet) {
        // 根据当前语言环境，将值设置到对应的字段
        const updateData: any = {
          description: values.description
        }
        
        if (language === 'en') {
          updateData.businessEn = values.businessZh
          updateData.businessZh = testCaseSet.businessZh // 保持中文值不变
        } else {
          updateData.businessZh = values.businessZh
          updateData.businessEn = testCaseSet.businessEn // 保持英文值不变
        }
        
        await testCaseSetService.updateTestCaseSet(testCaseSet.id, updateData)
        
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
        initialValues={{}}
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
          label={translateTestCaseSet('table.business')}
          rules={[
            { required: true, message: translateTestCaseSet('businessRequired') }
          ]}
        >
          <Select placeholder={translateTestCaseSet('table.business')}>
            <Option value="VPN阻断">VPN阻断</Option>
            <Option value="VPN_BLOCK">VPN_BLOCK</Option>
          </Select>
        </Form.Item>
      </Form>
    </Modal>
  )
}

export default TestCaseSetEdit
