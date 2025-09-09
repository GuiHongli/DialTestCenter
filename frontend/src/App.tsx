import { Route, Routes } from 'react-router-dom'
import './App.css'
import Layout from './components/Layout'
import { I18nProvider } from './contexts/I18nContext'
import './i18n'; // 初始化国际化
import Home from './pages/Home'
import TestCaseSetManagementPage from './pages/TestCaseSetManagement'
import UserRoleManagementPage from './pages/UserRoleManagement'
import SoftwarePackageManagementPage from './pages/SoftwarePackageManagement'

function App() {
  return (
    <I18nProvider>
      <Layout>
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/dashboard" element={<Home />} />
          <Route path="/user-roles" element={<UserRoleManagementPage />} />
          <Route path="/test-case-sets" element={<TestCaseSetManagementPage />} />
          <Route path="/software-packages" element={<SoftwarePackageManagementPage />} />
        </Routes>
      </Layout>
    </I18nProvider>
  )
}

export default App
