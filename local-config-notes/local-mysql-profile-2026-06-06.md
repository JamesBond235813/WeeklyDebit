# 本地 MySQL 与 Profile 约定

更新时间：2026-06-06

## 统一约定

- 本地开发 profile：`local`
- 本地 MySQL 地址：`127.0.0.1:3306`
- 本地数据库：`weeklydebit_su_ops_plus_20260126`

## 已处理

- `silver-union-ops/src/main/resources/bootstrap.yml` 已改为本地数据库口径。
- `silver-union-ops/src/main/resources/bootstrap-local.yml` 保持本地数据库口径。
- `silver-union-ops/script/start.sh` 默认 profile 已改为 `local`。
- 其它非本地 profile 已移出 `src/main/resources`，归档到：
  `xiaohebao_06/silver-union-all/config-archive/non-local-profiles/`

## 启动方式

推荐：

```bash
./mvnw -pl silver-union-ops -am spring-boot:run -Dspring-boot.run.profiles=local
```

或使用脚本：

```bash
cd silver-union-ops
./script/start.sh
```
