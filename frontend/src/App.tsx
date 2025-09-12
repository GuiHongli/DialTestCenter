import { Route, Routes } from 'react-router-dom'
import './App.css'
import Layout from './components/Layout'
import { I18nProvider } from './contexts/I18nContext'
import Home from './pages/Home'
import TestCaseSetManagementPage from './pages/TestCaseSetManagement'
import UserRoleManagementPage from './pages/UserRoleManagement'
import UserManagementPage from './pages/UserManagement'
import SoftwarePackageManagementPage from './pages/SoftwarePackageManagement'
import OperationLogManagementPage from './pages/OperationLogManagement'

function App() {
  return (
    <I18nProvider>
      <Layout>
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/users" element={<UserManagementPage />} />
          <Route path="/user-roles" element={<UserRoleManagementPage />} />
          <Route path="/test-case-sets" element={<TestCaseSetManagementPage />} />
          <Route path="/software-packages" element={<SoftwarePackageManagementPage />} />
          <Route path="/operation-logs" element={<OperationLogManagementPage />} />
        </Routes>
      </Layout>
    </I18nProvider>
  )
}

export default App
