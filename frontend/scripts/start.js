#!/usr/bin/env node

const { spawn } = require('child_process');
const path = require('path');

console.log('🚀 启动前端开发服务器...\n');

// 启动webpack dev server
const devServer = spawn('npx', ['webpack', 'serve', '--mode', 'development', '--port', '4396'], {
  stdio: 'inherit',
  shell: true,
  cwd: path.resolve(__dirname, '..')
});

devServer.on('error', (error) => {
  console.error('❌ 启动开发服务器失败:', error);
  process.exit(1);
});

devServer.on('close', (code) => {
  if (code !== 0) {
    console.error(`❌ 开发服务器退出，代码: ${code}`);
    process.exit(code);
  }
});

// 处理进程退出
process.on('SIGINT', () => {
  console.log('\n🛑 正在停止开发服务器...');
  devServer.kill('SIGINT');
});

process.on('SIGTERM', () => {
  console.log('\n🛑 正在停止开发服务器...');
  devServer.kill('SIGTERM');
});
