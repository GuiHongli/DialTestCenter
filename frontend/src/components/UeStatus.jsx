import React, { useState, useEffect } from 'react';
import { 
  Table, 
  Card, 
  Button, 
  Input, 
  Space, 
  Tag, 
  Typography,
  Badge,
  Modal,
  Descriptions,
  Empty
} from 'antd';
import { 
  SearchOutlined, 
  ReloadOutlined, 
  MonitorOutlined,
  AppstoreOutlined,
  InfoCircleOutlined,
  MobileOutlined
} from '@ant-design/icons';
import { useTranslation } from '../hooks/useTranslation';

const { Title } = Typography;

// 模拟数据生成器 - 与 ExecutorStatus 保持一致但扁平化
const generateMockData = () => {
  const ues = [];
  const executorsCount = 12;
  const prefixes = ['138', '139', '135', '136', '137', '150', '151', '152', '157', '158', '159', '182', '183', '187', '188', '130', '131', '132', '155', '156', '185', '186', '133', '153', '180', '181', '189'];
  
  // 常见的应用包名
  const commonApps = [
    { name: 'WeChat', package: 'com.tencent.mm' },
    { name: 'QQ', package: 'com.tencent.mobileqq' },
    { name: 'TikTok', package: 'com.ss.android.ugc.aweme' },
    { name: 'Alipay', package: 'com.eg.android.AlipayGphone' },
    { name: 'Taobao', package: 'com.taobao.taobao' },
    { name: 'Chrome', package: 'com.android.chrome' },
    { name: 'Maps', package: 'com.google.android.apps.maps' },
    { name: 'YouTube', package: 'com.google.android.youtube' },
    { name: 'Facebook', package: 'com.facebook.katana' },
    { name: 'Instagram', package: 'com.instagram.android' }
  ];

  for (let i = 1; i <= executorsCount; i++) {
    const totalUe = Math.floor(Math.random() * 15) + 5; // 5-20个UE
    const onlineUe = Math.floor(Math.random() * (totalUe + 1)); // 0-total个在线
    const executorName = `Executor-Node-${i.toString().padStart(2, '0')}`;
    
    // 为每个执行机分配一个固定的号段
    const prefix = prefixes[Math.floor(Math.random() * prefixes.length)];
    const regionCode = Math.floor(Math.random() * 9000 + 1000);
    
    for (let j = 1; j <= totalUe; j++) {
        const isRunning = j <= onlineUe && Math.random() > 0.7;
        
        // 生成随机安装应用列表
        const installedApps = [];
        const appCount = Math.floor(Math.random() * 8) + 3; // 3-10个应用
        const shuffledApps = [...commonApps].sort(() => 0.5 - Math.random());
        
        for (let k = 0; k < appCount; k++) {
          installedApps.push({
            appName: shuffledApps[k].name,
            packageName: shuffledApps[k].package,
            version: `${Math.floor(Math.random() * 10)}.${Math.floor(Math.random() * 10)}.${Math.floor(Math.random() * 100)}`,
            installTime: new Date(Date.now() - Math.floor(Math.random() * 10000000000)).toLocaleString()
          });
        }

        ues.push({
            msisdn: `86${prefix}${regionCode}${j.toString().padStart(4, '0')}`,
            brand: ['Huawei', 'Xiaomi', 'Oppo', 'Vivo', 'Samsung'][Math.floor(Math.random() * 5)],
            model: `Model-${Math.floor(Math.random() * 100)}`,
            os: `Android ${10 + Math.floor(Math.random() * 4)}`,
            status: j <= onlineUe ? 1 : 0,
            isRunning: isRunning,
            taskId: isRunning ? `TASK-${Date.now()}-${j}` : '-',
            battery: Math.floor(Math.random() * 100),
            executorName: executorName,
            installedApps: installedApps
        });
    }
  }
  return ues;
};

const UeStatus = () => {
  const { t } = useTranslation();
  const [loading, setLoading] = useState(false);
  const [data, setData] = useState([]);
  const [searchText, setSearchText] = useState('');
  const [modalVisible, setModalVisible] = useState(false);
  const [currentUe, setCurrentUe] = useState(null);

  // 加载数据
  const loadData = () => {
    setLoading(true);
    // 模拟API延迟
    setTimeout(() => {
      setData(generateMockData());
      setLoading(false);
    }, 800);
  };

  useEffect(() => {
    loadData();
  }, []);

  // 过滤数据
  const filteredData = data.filter(item => 
    item.msisdn.toLowerCase().includes(searchText.toLowerCase()) || 
    item.executorName.toLowerCase().includes(searchText.toLowerCase())
  );

  const handleViewDetails = (record) => {
    setCurrentUe(record);
    setModalVisible(true);
  };

  const columns = [
    {
      title: t('ueStatus.table.msisdn'),
      dataIndex: 'msisdn',
      key: 'msisdn',
      width: 180,
      fixed: 'left',
      render: (text, record) => (
        <Space>
          <MobileOutlined />
          <a onClick={() => handleViewDetails(record)} style={{ fontWeight: 'bold' }}>{text}</a>
        </Space>
      ),
    },
    {
      title: t('ueStatus.table.executor'),
      dataIndex: 'executorName',
      key: 'executorName',
      width: 180,
    },
    {
      title: t('ueStatus.table.status'),
      dataIndex: 'status',
      key: 'status',
      width: 100,
      render: (status) => (
        <Badge status={status === 1 ? 'success' : 'default'} text={status === 1 ? t('executorStatus.detail.ueTable.online') : t('executorStatus.detail.ueTable.offline')} />
      ),
    },
    {
      title: t('ueStatus.table.runningStatus'),
      key: 'runningStatus',
      width: 100,
      render: (_, record) => (
        <Badge status={record.isRunning ? 'processing' : 'default'} text={record.isRunning ? t('executorStatus.detail.ueTable.running') : t('executorStatus.detail.ueTable.idle')} />
      ),
    },
    {
      title: t('ueStatus.table.taskId'),
      dataIndex: 'taskId',
      key: 'taskId',
      width: 200,
    },
    {
      title: t('ueStatus.table.brandModel'),
      key: 'brandModel',
      render: (_, record) => `${record.brand} ${record.model}`,
    },
    {
      title: t('ueStatus.table.os'),
      dataIndex: 'os',
      key: 'os',
    },
    {
      title: t('ueStatus.table.battery'),
      dataIndex: 'battery',
      key: 'battery',
      render: (val) => (
        <span style={{ color: val < 20 ? 'red' : 'inherit' }}>
          {val}%
        </span>
      )
    },
    {
      title: t('ueStatus.table.actions'),
      key: 'actions',
      fixed: 'right',
      width: 120,
      render: (_, record) => (
        <Button type="link" size="small" onClick={() => handleViewDetails(record)}>
          {t('ueStatus.table.viewDetails')}
        </Button>
      )
    }
  ];

  const appColumns = [
    {
      title: t('ueStatus.detail.appName'),
      dataIndex: 'appName',
      key: 'appName',
      width: 150,
    },
    {
      title: t('ueStatus.detail.packageName'),
      dataIndex: 'packageName',
      key: 'packageName',
      width: 200,
    },
    {
      title: t('ueStatus.detail.version'),
      dataIndex: 'version',
      key: 'version',
      width: 100,
    },
    {
      title: t('ueStatus.detail.installTime'),
      dataIndex: 'installTime',
      key: 'installTime',
      width: 180,
    }
  ];

  return (
    <div className="ue-status-page" style={{ padding: 24 }}>
      {/* 页面标题 */}
      <div style={{ marginBottom: 24 }}>
        <Title level={2} style={{ margin: 0, textAlign: 'left' }}>
          <MonitorOutlined style={{ marginRight: '8px' }} />
          {t('ueStatus.title')}
        </Title>
        <p style={{ color: 'rgba(0, 0, 0, 0.45)', marginTop: 8, textAlign: 'left' }}>
          {t('ueStatus.description')}
        </p>
      </div>

      <Card 
        title={t('ueStatus.title')} 
        extra={
          <Space>
             <Input 
               placeholder={t('ueStatus.searchPlaceholder')} 
               prefix={<SearchOutlined />} 
               value={searchText}
               onChange={e => setSearchText(e.target.value)}
               style={{ width: 300 }}
             />
             <Button icon={<ReloadOutlined />} onClick={loadData}>
               {t('ueStatus.refresh')}
             </Button>
          </Space>
        }
      >
        <Table
          columns={columns}
          dataSource={filteredData}
          rowKey="msisdn"
          loading={loading}
          scroll={{ x: 1300 }}
          pagination={{
              defaultPageSize: 10,
              showSizeChanger: true,
              showTotal: (total, range) => t('ueStatus.table.pagination', { start: range[0], end: range[1], total })
          }}
        />
      </Card>

      <Modal
        title={t('ueStatus.detail.title')}
        visible={modalVisible}
        onCancel={() => setModalVisible(false)}
        footer={[
          <Button key="close" onClick={() => setModalVisible(false)}>
            {t('common.close')}
          </Button>
        ]}
        width={800}
      >
        {currentUe && (
          <div style={{ display: 'flex', flexDirection: 'column', gap: '24px' }}>
            {/* 基本信息 */}
            <div>
              <Title level={5} style={{ marginBottom: 16 }}>
                <InfoCircleOutlined style={{ marginRight: 8 }} />
                {t('ueStatus.detail.basicInfo')}
              </Title>
              <Descriptions bordered column={2}>
                <Descriptions.Item label={t('ueStatus.table.msisdn')}>{currentUe.msisdn}</Descriptions.Item>
                <Descriptions.Item label={t('ueStatus.table.executor')}>{currentUe.executorName}</Descriptions.Item>
                <Descriptions.Item label={t('ueStatus.table.brandModel')}>{`${currentUe.brand} ${currentUe.model}`}</Descriptions.Item>
                <Descriptions.Item label={t('ueStatus.table.os')}>{currentUe.os}</Descriptions.Item>
                <Descriptions.Item label={t('ueStatus.table.status')}>
                  <Badge status={currentUe.status === 1 ? 'success' : 'default'} text={currentUe.status === 1 ? t('executorStatus.detail.ueTable.online') : t('executorStatus.detail.ueTable.offline')} />
                </Descriptions.Item>
                <Descriptions.Item label={t('ueStatus.table.battery')}>
                  <span style={{ color: currentUe.battery < 20 ? 'red' : 'inherit' }}>{currentUe.battery}%</span>
                </Descriptions.Item>
              </Descriptions>
            </div>

            {/* 应用列表 */}
            <div>
              <Title level={5} style={{ marginBottom: 16 }}>
                <AppstoreOutlined style={{ marginRight: 8 }} />
                {t('ueStatus.detail.installedApps')}
              </Title>
              <Table 
                columns={appColumns} 
                dataSource={currentUe.installedApps} 
                rowKey="packageName"
                pagination={{ pageSize: 5 }}
                size="small"
              />
            </div>
          </div>
        )}
      </Modal>
    </div>
  );
};

export default UeStatus;
