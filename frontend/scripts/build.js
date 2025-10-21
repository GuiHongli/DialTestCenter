#!/usr/bin/env node

const webpack = require('webpack');
const path = require('path');
const fs = require('fs');

// 导入webpack配置
const config = require('../webpack.config.js');

console.log('🚀 开始构建前端项目...\n');

// 创建webpack编译器
const compiler = webpack(config);

// 运行构建
compiler.run((err, stats) => {
  if (err) {
    console.error('❌ 构建失败:', err);
    process.exit(1);
  }

  if (stats.hasErrors()) {
    console.error('❌ 构建过程中出现错误:');
    console.error(stats.toString({
      colors: true,
      chunks: false,
      children: false,
      modules: false,
      assets: false,
      entrypoints: false,
      warnings: false
    }));
    process.exit(1);
  }

  if (stats.hasWarnings()) {
    console.warn('⚠️  构建过程中出现警告:');
    console.warn(stats.toString({
      colors: true,
      chunks: false,
      children: false,
      modules: false,
      assets: false,
      entrypoints: false,
      errors: false
    }));
  }

  // 显示构建信息
  const info = stats.toJson({
    all: false,
    assets: true,
    chunks: true,
    modules: false,
    timings: true,
    builtAt: true,
    hash: true
  });

  console.log('\n✅ 构建成功!');
  console.log(`📦 构建时间: ${info.time}ms`);
  console.log(`🔑 构建哈希: ${info.hash}`);
  
  if (info.assets && info.assets.length > 0) {
    console.log('\n📁 生成的文件:');
    info.assets.forEach(asset => {
      const size = (asset.size / 1024).toFixed(2);
      console.log(`   ${asset.name} (${size} KB)`);
    });
  }

  if (info.chunks && info.chunks.length > 0) {
    console.log('\n🧩 代码块信息:');
    info.chunks.forEach(chunk => {
      console.log(`   ${chunk.names.join(', ')}: ${chunk.files.join(', ')}`);
    });
  }

  console.log('\n🎉 构建完成!');
});
