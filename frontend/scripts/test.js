#!/usr/bin/env node

const { spawn } = require('child_process');
const path = require('path');

console.log('🧪 运行前端测试...\n');

// 运行ESLint检查
const lintProcess = spawn('npx', ['eslint', '.', '--ext', 'js,jsx', '--report-unused-disable-directives', '--max-warnings', '0'], {
  stdio: 'inherit',
  shell: true,
  cwd: path.resolve(__dirname, '..')
});

lintProcess.on('error', (error) => {
  console.error('❌ 运行ESLint失败:', error);
  process.exit(1);
});

lintProcess.on('close', (code) => {
  if (code === 0) {
    console.log('\n✅ 代码检查通过!');
  } else {
    console.log(`\n❌ 代码检查失败，退出代码: ${code}`);
    process.exit(code);
  }
});
