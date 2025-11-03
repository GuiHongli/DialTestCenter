import { Form, Input, Modal, Select, message } from 'antd'
import React, { useEffect } from 'react'
import { useTranslation } from '../hooks/useTranslation.js'
import { useI18n } from '../contexts/I18nContext.jsx'
import testCaseSetService from '../services/testCaseSetService.js'

const { Option } = Select

const TestCaseSetEdit = ({
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
      // Set business type value based on language environment
      // Chinese environment: display businessZh, English environment: display businessEn
      const businessValue = language === 'en' 
        ? (testCaseSet.businessEn || 'VPN_BLOCK')
        : (testCaseSet.businessZh || 'VPN阻断')
      
      form.setFieldsValue({
        description: testCaseSet.description || '',
        businessZh: businessValue
      })
    }
  }, [visible, testCaseSet, form, language])

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields()
      
      if (testCaseSet) {
        // Update data based on language environment
        // If editing in English, update businessEn, if in Chinese, update businessZh
        const updateData = {
          description: values.description || ''
        }
        
        if (language === 'en') {
          // English environment: update businessEn, keep businessZh unchanged
          updateData.businessEn = values.businessZh || testCaseSet.businessEn || 'VPN_BLOCK'
          updateData.businessZh = testCaseSet.businessZh || 'VPN阻断'
        } else {
          // Chinese environment: update businessZh, keep businessEn unchanged
          updateData.businessZh = values.businessZh || testCaseSet.businessZh || 'VPN阻断'
          updateData.businessEn = testCaseSet.businessEn || 'VPN_BLOCK'
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
            value={testCaseSet && testCaseSet.name}
            disabled
            placeholder={translateTestCaseSet('table.name')}
          />
        </Form.Item>

        <Form.Item
          label={translateTestCaseSet('table.version')}
        >
          <Input
            value={testCaseSet && testCaseSet.version}
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
            {language === 'en' ? (
              <Option value="VPN_BLOCK">VPN_BLOCK</Option>
            ) : (
              <Option value="VPN阻断">VPN阻断</Option>
            )}
          </Select>
        </Form.Item>
      </Form>
    </Modal>
  )
}

TestCaseSetEdit.displayName = 'TestCaseSetEdit'

export default TestCaseSetEdit