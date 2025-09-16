# HTTPS 配置说明

## 证书信息
- **证书文件**: `backend/src/main/resources/ssl/keystore.p12`
- **证书类型**: PKCS12
- **证书别名**: dialtestcenter
- **证书密码**: dialtest123
- **有效期**: 365天
- **域名**: localhost

## 服务器配置
- **端口**: 8087
- **协议**: HTTPS
- **SSL版本**: TLSv1.2, TLSv1.3
- **加密套件**: 支持现代加密算法

## 前端配置
- **开发服务器**: https://localhost:4396
- **代理目标**: https://localhost:8087
- **API路径**: /dialingtest/api/*

## 浏览器安全警告
由于使用的是自签名证书，浏览器会显示安全警告。解决方法：

### Chrome/Edge
1. 访问 `https://localhost:8087`
2. 点击"高级"
3. 点击"继续访问localhost（不安全）"

### Firefox
1. 访问 `https://localhost:8087`
2. 点击"高级"
3. 点击"接受风险并继续"

## 启动步骤
1. 启动后端服务器（HTTPS模式）
2. 启动前端开发服务器
3. 访问 `https://localhost:4396`

## 证书重新生成
如果需要重新生成证书：
```bash
keytool -genkeypair -alias dialtestcenter -keyalg RSA -keysize 4096 -storetype PKCS12 -keystore backend/src/main/resources/ssl/keystore.p12 -validity 365 -storepass dialtest123 -keypass dialtest123 -dname "CN=localhost, OU=IT, O=Huawei, L=Beijing, ST=Beijing, C=CN"
```
