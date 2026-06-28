# 聚信服务迁移记录（2026-06-28）

## 迁移目标

- 将 WeeklyDebit 与小荷包从旧腾讯云服务器迁移至新服务器 `66.94.122.149`。
- 新服务器独立承载业务，不再依赖旧腾讯云后端服务。
- 同时切换域名：
  - `fuwu.juxin.pro`
  - `xhb.juxin.pro`
  - `xhbadmin.juxin.pro`

## 新服务器入口

- 服务器：`66.94.122.149`
- HTTPS 入口：Caddy 接管 `80/443`
- 证书：使用 `juxin.pro` 泛域名证书，部署在新服务器 Caddy 证书目录
- 旧 443 上的 Xray 直连入口已停用，Caddy 作为业务 HTTPS 入口

## WeeklyDebit

- systemd 服务：`weeklydebit-backend`
- 后端端口：`32802`
- API 前缀：`/api`
- 后端 jar：`/data/servers/weeklydebit/current/silver-union-ops.jar`
- 前端目录：`/data/www/fuwu.juxin.pro/dist`
- 数据库：新服务器 Docker MySQL 中的 `weeklydebit`
- HYY 撞库服务：新服务器本机 `127.0.0.1:18080`

## 小荷包

- systemd 服务：`xiaohebao-backend`
- 运行方式：Docker 容器，容器名 `xiaohebao-backend`
- 后端端口：`28001`
- 后端目录：`/opt/xiaohebao/current/backend`
- 用户前端目录：`/data/www/xhb.juxin.pro/dist`
- 管理前端目录：`/data/www/xhbadmin.juxin.pro/dist`
- 数据库：新服务器 Docker MySQL 中的 `xiaohebao`

## 新服务器基础组件

- MySQL 容器：`juxin-mysql`
- Caddy 容器：`talking202605-caddy`
- HYY 撞库容器：`hyy-collision-api`
- HYY 撞库数据库容器：`hyy-collision-mysql`

## 已完成验证

- `https://fuwu.juxin.pro/` 返回 `200`
- `POST https://fuwu.juxin.pro/api/public/third/access-check` 返回预期参数校验：`appId不能为空`
- `https://xhb.juxin.pro/` 返回 `200`
- `https://xhbadmin.juxin.pro/` 返回 `200`
- 新服务器服务状态：
  - `weeklydebit-backend`: active
  - `xiaohebao-backend`: active
  - `x-ui`: active
  - `juxin-mysql`: running
  - `talking202605-caddy`: running
  - `hyy-collision-api`: running
- 旧腾讯云后端已停止并禁用：
  - `weeklydebit-backend`: inactive/failed
  - `xiaohebao-backend`: inactive

## 数据核对快照

WeeklyDebit：

- `customer_info_item`: 44522 条，最大 ID 69041，最大创建时间 `2026-06-23 14:23:20`
- `cust_push_record`: 9384 条，最大 ID 9384，最大创建时间 `2026-06-23 14:23:20`
- `risk_control_report`: 23428 条，最大 ID 36006，最大创建时间 `2026-06-24 18:07:48`

小荷包：

- `users`: 433 条，最大 ID 436，最大创建时间 `2026-06-11 11:47:16`
- `loan_transactions`: 72 条，最大 ID 79，最大创建时间 `2026-06-14 15:10:43`
- `risk_control_report`: 1158 条，最大 ID 1231，最大创建时间 `2026-06-28 10:33:28`

## SSH 与防火墙收尾

- 已在 Contabo 防火墙 `Rongshu_fuwu` 中绑定 VPS `vmi3370489 / 66.94.122.149`。
- 已补齐业务入口规则：`80/tcp`、`443/tcp`、必要的 TCP/UDP 服务端口，以及 SSH 白名单。
- 新服务器内部 `ufw` 已取消 `22/tcp` 全网放行，SSH 只允许：
  - `152.136.236.144`：旧腾讯云跳板
  - `27.47.33.167`：本机公网 IP
  - `155.117.127.214`：观测到的本机 SSH 出口
- 本机直连已验证成功：
  - `ssh -i ~/.ssh/vpn_deploy root@66.94.122.149`
- 临时迁移钥匙已删除：
  - 旧腾讯云：`/root/.ssh/wd_migration_66_94_122_149`
  - 旧腾讯云：`/root/.ssh/wd_migration_66_94_122_149.pub`
  - 新服务器：`/root/.ssh/authorized_keys` 中注释为 `wd-migration-20260628133852` 的公钥行
- 本文档不保存数据库密码、API 密钥、证书私钥或其他敏感正文。
