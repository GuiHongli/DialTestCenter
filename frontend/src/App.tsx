import { Route, Switch } from 'react-router-dom'
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
        <Switch>
          <Route path="/" exact component={Home} />
          <Route path="/users" component={UserManagementPage} />
          <Route path="/user-roles" component={UserRoleManagementPage} />
          <Route path="/test-case-sets" component={TestCaseSetManagementPage} />
          <Route path="/software-packages" component={SoftwarePackageManagementPage} />
          <Route path="/operation-logs" component={OperationLogManagementPage} />
        </Switch>
      </Layout>
    </I18nProvider>
  )
}

export default App
