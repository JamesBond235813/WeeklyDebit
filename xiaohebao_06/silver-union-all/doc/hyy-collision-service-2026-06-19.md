# HYY 存量客户撞库服务配置说明

## 目标

将本地存量客户手机号清单作为花易用 HYY `access-check` 的前置撞库屏障。

严格按 HYY 文档执行：

- 上游传入 `phone_code`、`name_md5`、`idno_md5`。
- 本系统按 `phone_code` 查询同手机号前八位的完整手机号 MD5 列表，返回到 `data.md5_list`。
- 本系统同时用 `phone_code + name_md5/idno_md5` 做直接重复判断，命中则返回撞库失败。

## 数据源

本地明文数据目录：

```text
/Volumes/littlejiang02/Dataplayer/mobile
```

当前扫描结果：

```text
CSV 文件数：34
总行数：16691564
字段：第一列手机号，第二列姓名
身份证号：未发现身份证号列
有效手机号：16691564
有姓名行数：5289955
手机号前八位 distinct：3322295
单个前八位最大命中数：317
```

## 安全策略

本地明文文件不修改、不删除。

部署到服务器后，撞库库只保存：

```text
phone_prefix8
mobile_md5
name_md5
idno_md5
batch_no
```

不保存明文手机号、姓名、身份证号。

## 部署位置

建议部署在：

```text
66.94.122.149:/data/hyy-collision
```

部署内容：

```text
MySQL 8.0 容器
HYY collision API 容器
导入脚本
```

## CRM 配置

CRM 后端新增配置：

```yaml
biz:
  hyyCollisionServiceUrl: ''
  hyyCollisionServiceToken: ''
  hyyCollisionServiceTimeoutMs: 1000
```

生产环境通过外部配置注入，不在代码仓库保存真实 token。

## 故障策略

用户已确认：如果远程撞库服务不可用，HYY `access-check` 返回失败，不放行。

原因：撞库屏障失效时继续放行会破坏“只接收撞库后不重复客户”的目标。
