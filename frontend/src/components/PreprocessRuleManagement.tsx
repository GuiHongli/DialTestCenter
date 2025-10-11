/**
 * 预处理规则管理组件
 */

import React, { useState, useEffect } from 'react';
import { Tabs, message } from 'antd';
import { useTranslation } from '../hooks/useTranslation';
import PreprocessRulePackageManagement from './PreprocessRulePackageManagement';
import PreprocessRuleListManagement from './PreprocessRuleListManagement';

const { TabPane } = Tabs;

const PreprocessRuleManagement: React.FC = () => {
  const { t } = useTranslation();
  const [activeTab, setActiveTab] = useState<string>('rules');
  const [refreshTrigger, setRefreshTrigger] = useState<number>(0);

  const handleTabChange = (key: string) => {
    setActiveTab(key);
    
    // 当切换到预处理规则列表Tab时，触发刷新
    if (key === 'rules') {
      setRefreshTrigger(prev => prev + 1);
    }
  };

  return (
    <div style={{ padding: '24px' }}>
      <Tabs activeKey={activeTab} onChange={handleTabChange}>
        <TabPane tab={t('preprocessRule.management.rulesTab')} key="rules">
          <PreprocessRuleListManagement refreshTrigger={refreshTrigger} />
        </TabPane>
        <TabPane tab={t('preprocessRule.management.packagesTab')} key="packages">
          <PreprocessRulePackageManagement />
        </TabPane>
      </Tabs>
    </div>
  );
};

export default PreprocessRuleManagement;
