import React, { useState } from 'react'
import { Modal, Form, Input, Select, message } from 'antd'
import { AlarmUtils } from '../services/alarmService.js'
import { useTranslation } from '../hooks/useTranslation.js'
import { useI18n } from '../contexts/I18nContext.jsx'

const { TextArea } = Input
const { Option } = Select

/**
 * 告警表单组件
 */
const AlarmForm = ({ visible, onCancel, onSubmit }) => {
  const { translateAlarm, translateCommon } = useTranslation()
  const { language } = useI18n()
  const [form] = Form.useForm()
  const [loading, setLoading] = useState(false)

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields()
      setLoading(true)
      await onSubmit(values)
      form.resetFields()
    } catch (error) {
      if (error.errorFields) {
        // 表单验证错误
        return
      }
      // 其他错误已在父组件处理
    } finally {
      setLoading(false)
    }
  }

  const handleCancel = () => {
    form.resetFields()
    onCancel()
  }

  return (
    <Modal
      title={translateAlarm('create') || 'Create Alarm'}
      open={visible}
      onOk={handleSubmit}
      onCancel={handleCancel}
      confirmLoading={loading}
      okText={translateCommon('submit') || 'Submit'}
      cancelText={translateCommon('cancel') || 'Cancel'}
      width={600}
    >
      <Form
        form={form}
        layout="vertical"
        initialValues={{
          alarmLevel: 'Minor',
        }}
      >
        <Form.Item
          name="alarmSummary"
          label={translateAlarm('summary') || 'Summary'}
          rules={[
            { required: true, message: translateAlarm('summaryRequired') || 'Summary is required' },
            { max: 200, message: translateAlarm('summaryMaxLength') || 'Summary cannot exceed 200 characters' },
          ]}
        >
          <Input placeholder={translateAlarm('summaryPlaceholder') || 'Enter alarm summary'} />
        </Form.Item>

        <Form.Item
          name="alarmDescription"
          label={translateAlarm('description') || 'Description'}
        >
          <TextArea
            rows={4}
            placeholder={translateAlarm('descriptionPlaceholder') || 'Enter alarm description (optional)'}
          />
        </Form.Item>

        <Form.Item
          name="alarmLevel"
          label={translateAlarm('level') || 'Level'}
          rules={[
            { required: true, message: translateAlarm('levelRequired') || 'Level is required' },
          ]}
        >
          <Select placeholder={translateAlarm('levelPlaceholder') || 'Select alarm level'}>
            <Option value="Urgent">
              {AlarmUtils.getAlarmLevelText('Urgent', language)}
            </Option>
            <Option value="Important">
              {AlarmUtils.getAlarmLevelText('Important', language)}
            </Option>
            <Option value="Minor">
              {AlarmUtils.getAlarmLevelText('Minor', language)}
            </Option>
          </Select>
        </Form.Item>
      </Form>
    </Modal>
  )
}

export default AlarmForm

