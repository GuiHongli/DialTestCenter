import React from 'react'
import ReactDOM from 'react-dom'
import { HashRouter } from 'react-router-dom'
import App from './App'
import './index.css'
import 'antd/dist/antd.css'
import { initErrorHandler } from './utils/errorHandler'
import { initUsername } from './utils/apiUtils'

// 初始化错误处理，抑制 ResizeObserver 相关错误
initErrorHandler()

// 初始化用户名
initUsername()

ReactDOM.render(
  <HashRouter basename="/dialingtest">
    <App />
  </HashRouter>,
  document.getElementById('root')
)
