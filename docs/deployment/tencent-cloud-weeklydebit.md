# WeeklyDebit 腾讯云部署记录

更新时间：2026-06-04 23:41 CST

## 服务定位

- 服务名称：WeeklyDebit / Silver Union Ops Backend
- 当前用途：正式云端生产环境，承接上游数据推送接口联调与后台运营
- 后端模块：`xiaohebao_06/silver-union-all/silver-union-ops`
- 前端模块：`xiaohebao_06/recycle-ops`
- 当前状态：后端、前端、Nginx、HTTPS 代理链路均已发布并验证通过

## 腾讯云服务器

- SSH 别名：`tencent-superapi`
- SSH 用户：`root`
- 服务器 IP：`152.136.236.144`
- 主机名：`VM-20-6-opencloudos`
- 系统：OpenCloudOS 9
- Java：Java 17 Konajdk headless
- Nginx：已运行，监听 80 / 443
- MySQL：本机 `127.0.0.1:3306`

## 域名

- 暂定域名：`fuwu.juxin.pro`
- 目标 A 记录：`152.136.236.144`
- 当前检查结果：2026-06-04 23:41 CST 仍解析到 `198.18.0.58`
- 证书文件：
  - `/etc/nginx/ssl/juxin.pro.20260922.pem`
  - `/etc/nginx/ssl/juxin.pro.20260922.key`

## 云端目录

- 应用根目录：`/data/servers/weeklydebit`
- 当前版本目录：`/data/servers/weeklydebit/current`
- 后端 jar：`/data/servers/weeklydebit/current/silver-union-ops.jar`
- 配置目录：`/data/servers/weeklydebit/config`
- 服务环境文件：`/data/servers/weeklydebit/config/weeklydebit.env`
- 日志目录：`/data/servers/weeklydebit/logs`
- 数据库 schema 备份：`/data/servers/weeklydebit/db/schema.sql`
- 客户模板文件：`/data/servers/ops_template/客户信息模板1.0.xlsx`

## Systemd

- 服务名：`weeklydebit-backend`
- 服务文件：`/etc/systemd/system/weeklydebit-backend.service`
- 当前状态：已启动，已设置开机自启
- 监听端口：`32802`
- context path：`/api`
- active profile：`prod-mini`

常用操作：

```bash
ssh tencent-superapi
systemctl status weeklydebit-backend --no-pager
systemctl restart weeklydebit-backend
journalctl -u weeklydebit-backend -n 200 --no-pager
tail -n 200 /data/servers/weeklydebit/current/logs/shaowen-ops.log
```

## Nginx

- 配置文件：`/etc/nginx/conf.d/fuwu.juxin.pro.conf`
- `/api/` 代理到：`http://127.0.0.1:32802/api/`
- `/api/ws/` 代理到：`http://127.0.0.1:32802/api/ws/`，用于前端生产环境 WebSocket 通知
- `/ws/` 代理到：`http://127.0.0.1:32802/ws/`
- `/` 静态目录：`/data/www/fuwu.juxin.pro/dist`
- 前端生产配置：同域 `/api`

验证配置：

```bash
ssh tencent-superapi
nginx -t
systemctl reload nginx
```

## 数据库

- 数据库名：`weeklydebit`
- 应用账号：`weeklydebit_user`
- MySQL 地址：`127.0.0.1:3306`
- 已导入表数：`23`
- 字符集：`utf8mb4`
- 敏感密码记录：仅保存在本机私有文件，不写入本说明。

本机私有敏感配置文件：

```text
/Users/littej/.codex/private/weeklydebit/tencent-cloud-secrets.md
```

## 已验证

通过公网 IP 强制解析验证 HTTPS：

```bash
curl -k --resolve fuwu.juxin.pro:443:152.136.236.144 \
  https://fuwu.juxin.pro/api/public/third/access-check \
  -H 'Content-Type: application/json' \
  -d '{}'
```

预期返回：

```json
{"code":400,"msg":"appId不能为空","data":null}
```

这个返回表示服务、数据库、Nginx、HTTPS 代理链路均已打通。

同样通过公网 IP 强制解析验证：

- `POST https://fuwu.juxin.pro/api/public/third/apply-credit` 返回 `{"code":400,"msg":"appId不能为空","data":null}`
- `POST https://fuwu.juxin.pro/api/public/third/access-check` 返回 `{"code":400,"msg":"appId不能为空","data":null}`
- `GET https://fuwu.juxin.pro/` 返回前端首页 HTML，页面标题为 `银聚 CRM`

云端本机验证：

- `weeklydebit-backend`：`active`
- 后端监听：`32802`
- `nginx -t`：通过
- 前端 `_app.config.js`：`VITE_GLOB_API_URL=/api`
- `wss://fuwu.juxin.pro/api/ws/notice`：WebSocket 握手返回 `101 Switching Protocols`

## 上游推送接口

正式对外路径：

- `POST https://fuwu.juxin.pro/api/public/third/access-check`
- `POST https://fuwu.juxin.pro/api/public/third/apply-credit`

后端控制器：

```text
xiaohebao_06/silver-union-all/silver-union-ops/src/main/java/com/jhl/silver/union/web/controller/customer/RecvThirdPlatDataController.java
```

## 当前待办

- 将 DNS A 记录 `fuwu.juxin.pro` 改为 `152.136.236.144`
- 在云端数据库配置真实上游 appId / 密钥 / AES 参数
- 使用真实 HTTP 请求完成云端联调自测
