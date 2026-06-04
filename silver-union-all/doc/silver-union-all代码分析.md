# silver-union-all 代码分析

> 目标：梳理项目结构与核心业务逻辑，为从 Java 迁移到其他语言提供定位与边界认知。

## 1. 项目概览

- 业务定位：企业客户信息管理（含用户/部门/客户信息、客户分配、公海流转、客户跟进历史、业务字典、导入等）。
- 运行形态：Spring Boot 单体服务，REST API，对外暴露 `/auth`、`/user`、`/cust` 与 `/sys/**` 管理接口。
- 数据持久化：MyBatis-Plus + XML Mapper，分页使用 PageHelper，乐观锁使用 MyBatis-Plus OptimisticLocker。
- 权限模型：JWT + 角色（超管/部门管理员/用户管理员等），并有“部门层级可见范围”与“在线状态”控制。

## 2. 模块与目录结构

### 2.1 根模块（`silver-union-all`）
- Maven 多模块聚合（见 `pom.xml`）：
  - `silver-union-ops`：主业务服务（Spring Boot）
  - `silver-union-common`：通用工具、异常、结果结构、基础工具
  - `silver-union-z-generator`：MyBatis-Plus 代码生成与 SQL 资源

### 2.2 `silver-union-ops`
- 入口：`com.jhl.silver.union.SilverUnionOpsApplication`
- 关键包结构：
  - `web/controller`：HTTP API
  - `web/data`：DTO/Request/Response
  - `web/filter`：JWT 认证过滤器
  - `web/handler`：全局异常处理
  - `biz/*`：核心业务（service/manager/dal/entity/mapper）
  - `biz/config`：安全、DB、异步、属性配置等

### 2.3 `silver-union-common`
- 结果结构：`SuResult`、`IResultCode`、`CommonResultCode`
- 异常体系：`BizException`、`BizExceptionUtils`、`ExceptionLogPrinter`
- 工具集：日期、字符串、校验、加密、SQL、线程、Trace 等

### 2.4 `silver-union-z-generator`
- MyBatis-Plus 生成器（User/Customer/Dept 等）
- SQL 初始化脚本（表结构、数据字典等）

## 3. 技术与基础设施

- Spring Boot 3.4.3（`spring-boot-starter-parent`）
- JDK 17
- MyBatis-Plus 3.5.10 + PageHelper
- Spring Security + JWT（`io.jsonwebtoken`）
- EasyExcel（导入客户数据）
- MapStruct（DO <-> DTO 转换）
- 其他：Guava、Gson、Apache Commons

## 4. 主要数据模型（DO 实体）

以下实体与数据库表一一对应（位于 `silver-union-ops/src/main/java/.../dal/entity`）：

- `SuUserInfoDO`：用户信息（用户名、密码、角色、在线状态等）
- `SuUserLoginTraceDO`：登录日志/令牌状态（支持 revoke 与互斥登录）
- `SuOrgDepartmentInfoDO`：部门信息
- `CustomerInfoItemDO`：客户主信息（归属人/部门、跟进状态、收藏等）
- `CustomerInfoItemTraceDO`：客户修改历史（跟进/领导点评等）
- `BizDictConfigDO`：业务字典（客户分组、跟进状态、渠道等）
- `CustomerImportRecordDO`：导入文件记录
- `CustPushRecordDO`：客户推送记录（用于外部推送接口）

## 5. 分层结构与调用链

典型的业务调用链为：

```
Controller -> Service -> Manager -> Mapper(XML) -> DB
```

- Controller：参数校验 + 调用 service + 返回 `SuResult`
- Service：核心业务规则与权限控制
- Manager：MyBatis-Plus 的 DAO 封装
- Mapper：SQL 映射（XML）
- Convert：MapStruct 在 DTO/DO 之间转换

## 6. 核心业务流程分析

### 6.1 认证与权限
- 登录入口：`/auth/login`（`AuthController`）
- 逻辑：
  1. 校验 `clientId`
  2. 校验用户名与密码（BCrypt）
  3. 登录互斥：先撤销旧 Token（登录日志的 `jwtStatus` 标记）
  4. 生成 JWT + refreshToken
  5. refreshToken 写入 HttpOnly Cookie
- JWT 校验：`JwtAuthenticationFilter`
  - 从 `Authorization` 头取 token
  - 解析并验证过期
  - 校验登录日志中 jwtStatus
  - 设置 SecurityContext + UserContext
- 权限控制：`SecurityConfig` 中对 `/sys/**` 进行角色限制

### 6.2 用户管理
- 管理端 API（`/sys/user/**`）：新增/更新/删除/分页/重置密码/强制下线
- 服务逻辑：
  - `UserServiceImpl` 使用 DB 唯一键避免重复用户名/手机号
  - 删除为逻辑删除（`deleteFlag`）
  - 更新缓存（`userInfoManager.clearCacheBy`）

### 6.3 部门管理
- 管理端 API（`/sys/dpt/**`）：新增/更新/删除/查询
- 部门树缓存：`DeptServiceImpl` 使用 Guava `LoadingCache` 缓存部门树
- 删除规则：存在子部门时不可删除
- 数据权限基于部门树拓扑实现

### 6.4 客户信息管理（核心业务）
- 查询：`/cust/page-list-customers`
  - 按角色/部门过滤数据可见范围（`adaptQryByRoles`）
  - 组装业务字典文本（客户分组、进度、渠道、电话标签）

- 客户详情：`/cust/detail`
  - 优先查自己归属客户
  - 否则按角色权限查询
  - 返回跟进记录/领导点评历史（`CustomerInfoItemTraceDO`）

- 业务更新：`/cust/upd-biz-cust-info`
  - 限制：未分配客户不可编辑
  - 领导点评仅上级可写
  - 更新会生成“更新历史”记录

- 分配客户：`/sys/cust/dispatch-cust`
  - 只允许超管或部门数据管理员
  - 只能在权限可见部门内分配
  - 支持企业公海（ownerDeptId=0）或部门公海

- 批量退回公海：`/cust/btc-rtn-cust`
- 收藏切换：`/cust/btc-swt-fav`

### 6.5 客户更新历史（Trace）机制
- 更新时会计算字段差异（`FieldUtils`）
- 按字段类型归入：
  - 跟进（progress/followRemark/callRemark 等）
  - 领导点评（leaderRemark）
  - 其它
- 生成 `CustomerInfoItemTraceDO`，记录变更文本

### 6.6 业务字典管理
- `/sys/biz-cnf`：新增/更新字典项
- 支持动态新增“渠道”字典（导入时自动补充）

### 6.7 导入与推送客户数据
- 文件导入：`/sys/cust/import-user-info`
  - EasyExcel 读取校验
  - 保存导入记录
  - 异步入库（批量 1000）
  - 重复客户写入“重复数据文件”

- 外部推送：`/sys/cust/push-cust-info`
  - 需 `extraToken`
  - 直接批量插入或更新渠道/来源

### 6.8 登录时段控制
- `DurationOptionsAllowanceTask`：每分钟计算是否允许登录/操作
- 特定 URI 白名单放行（`/api/user/info`, `/api/auth/revoke`）

## 7. 配置要点

- `BizProperty` 配置（`biz.*`）：JWT 秘钥、有效期、允许登录时段、文件路径、extraToken
- `LoginLockConfig`：登录失败锁定策略（目前主要为配置结构，逻辑未完善）
- `DBConfiguration`：MyBatis-Plus + PageHelper
- `CustomizedConfig`：异步线程池（导入任务使用）

## 8. 迁移到其他语言的边界与重点

迁移时可将系统拆成以下子域模块，便于分批替换：

1. 认证与权限模块
   - JWT 校验、登录日志与互斥登录逻辑
   - 权限角色与部门层级可见范围

2. 用户/部门管理
   - 用户 CRUD、部门树构建
   - 部门缓存与权限查询逻辑

3. 客户信息域
   - 客户分配、公海/归属逻辑
   - 跟进与历史记录（Trace 机制）

4. 业务字典
   - 动态字典与渠道补充逻辑

5. 导入与批量处理
   - Excel 文件校验 + 异步入库
   - 重复数据文件导出

6. 公共支撑
   - 统一异常与返回结构
   - 工具类（日期/字符串/SQL/校验/加密）

## 9. 迁移实现建议（语言无关）

- 明确领域边界：认证、用户/部门、客户、字典、导入为 5 个主要子域
- 抽象服务接口：优先定义 DTO + Service Contract，便于多语言实现
- 持久层抽象：Mapper/SQL 可迁移为 Repository/DAO 模式
- Trace 机制：保留“字段差异 -> 变更历史”逻辑（核心业务审计）
- 数据权限：部门层级 + 角色判定是系统主干逻辑，必须单元化验证

## 10. API 清单（按模块）

> 说明：以下为控制器层面 API，总结自 `silver-union-ops`。

### 10.1 认证 Auth（`/auth`）

- `POST /auth/login`：管理端登录，返回 JWT
- `GET /auth/revoke`：退出登录（撤销当前 JWT）

### 10.2 用户 User（`/user`）

- `GET /user/info`：获取当前登录用户信息
- `POST /user/online-status`：更新在线状态
- `GET /user/list-team-user`：获取可见部门范围内用户列表（可选仅在线）
- `POST /user/upd-pwd`：修改当前用户密码
- `GET /user/get-dept`：查询当前用户可见部门列表

### 10.3 客户业务 Customer（`/cust`）

- `POST /cust/page-list-customers`：分页客户列表
- `GET /cust/list-dict-items`：获取业务字典（进度/分组/渠道/电话标签）
- `POST /cust/upd-biz-cust-info`：更新客户业务信息（跟进/备注等）
- `GET /cust/detail`：客户详情
- `GET /cust/rtn-cust`：客户退回公海
- `POST /cust/btc-rtn-cust`：批量退回公海
- `POST /cust/btc-swt-fav`：批量切换收藏类型
- `POST /cust/ensure-data-channel`：确保渠道字典存在（无需登录）

### 10.4 系统管理-用户（`/sys/user`）

- `GET /sys/user/revoke-by-uid`：强制指定用户下线
- `GET /sys/user/list-all-roles`：角色列表
- `POST /sys/user/page-list-user-info`：分页用户列表
- `POST /sys/user/add-user-info`：新增用户
- `POST /sys/user/upd-user-info`：更新用户
- `POST /sys/user/del-user-info`：删除用户（逻辑删除）
- `POST /sys/user/reset-user-pswd`：重置用户密码

### 10.5 系统管理-部门（`/sys/dpt`）

- `POST /sys/dpt/add-dpt-info`：新增部门
- `POST /sys/dpt/upd-dpt-info`：更新部门
- `POST /sys/dpt/del-dpt-info`：删除部门
- `POST /sys/dpt/list-dpt-info`：部门列表查询

### 10.6 系统管理-客户维护（`/sys/cust`）

- `POST /sys/cust/add-cust`：新增客户事实信息
- `POST /sys/cust/upd-cust`：更新客户事实信息
- `POST /sys/cust/dispatch-cust`：分配客户
- `POST /sys/cust/import-user-info`：上传 Excel 导入客户
- `POST /sys/cust/paged-list-imp-rec`：导入记录分页
- `GET /sys/cust/download-existed-rec`：下载重复数据文件
- `GET /sys/cust/download-tmpl`：下载导入模板
- `POST /sys/cust/push-cust-info`：外部推送客户数据（需 token）

### 10.7 系统管理-业务字典（`/sys/biz-cnf`）

- `POST /sys/biz-cnf/add-biz-config`：新增字典项
- `POST /sys/biz-cnf/upd-biz-config`：更新字典项状态/描述

## 11. 权限矩阵（入口约束 + 业务校验）

> 入口约束来自 `SecurityConfig`，业务校验来自各 Service 内部规则。

### 11.1 公开或白名单接口（无需登录）

- `/auth/**`：登录、退出
- `/cust/ensure-data-channel`：渠道字典补齐（需 `extraToken`）
- `/sys/cust/push-cust-info`：外部推送客户（需 `extraToken`）
- Swagger/Doc 相关：`/v3/api-docs/**`、`/swagger-ui.html`、`/doc.html**` 等

### 11.2 角色入口权限（SecurityFilterChain）

- `/sys/user/**`：`ROLE_SUPPER` 或 `ROLE_USER_INFO_ADMIN`
- `/sys/dpt/list-dpt-info`：`ROLE_SUPPER` 或 `ROLE_DEPT_INFO_ADMIN` 或 `ROLE_USER_INFO_ADMIN`
- `/sys/dpt/**`：`ROLE_SUPPER` 或 `ROLE_DEPT_INFO_ADMIN`
- `/sys/cust/add-leader-remark`：`ROLE_SUPPER` 或 `ROLE_DEPT_DATA_ADMIN`
- `/sys/cust/dispatch-cust`：`ROLE_SUPPER` 或 `ROLE_DEPT_DATA_ADMIN`
- `/sys/**` 其它：`ROLE_SUPPER`

### 11.3 业务层权限与约束（Service 内校验）

- 客户列表与详情：按角色限定可见部门/归属人（`CustomerInfoServiceImpl.adaptQryByRoles`）
- 更新客户业务信息：
  - 未分配客户不可编辑
  - 归属人不能写领导点评
  - 非归属人需超管或部门数据管理员，且必须在其部门树内
- 客户分配：仅超管/部门数据管理员；非超管不可越权分配到不在部门树范围的部门
- 批量操作：退回公海/收藏切换只允许操作可见客户
- 导入与下载：仅超管/部门数据管理员可见；非超管需校验部门关系与操作人
- 登录时段限制：`DurationOptionsAllowanceTask` 根据时间窗拒绝操作（部分 URI 白名单放行）

## 12. 请求/响应字段与示例

### 12.1 通用响应结构（`SuResult`）

字段：
- `code`：0 表示成功，其它为失败码
- `msg`：错误消息（成功时可为空）
- `msgTitle`：错误标题（可选）
- `data`：业务数据
- `version`：返回版本（默认 `1.0`）

示例：
```json
{
  "code": 0,
  "msg": "SUCCESS",
  "data": { },
  "version": "1.0"
}
```

### 12.2 Auth（`/auth`）

**POST /auth/login**  
请求体：
- `username`：用户名（必填）
- `password`：密码（必填）
请求头：
- `ClientId`：客户端标识（必填，需与配置一致）
响应：
- `data`：JWT 字符串

示例：
```json
{
  "username": "admin",
  "password": "Abc12345"
}
```

**GET /auth/revoke**  
请求头：
- `Authorization: Bearer <jwt>`
响应：
- `data`：`null`

### 12.3 User（`/user`）

**GET /user/info**  
响应 `data`（示意字段）：
- `userId`、`username`、`realName`、`roles`、`departmentId`、`onlineStatus`

**POST /user/online-status**  
请求体：
- `onlineStatus`：0/1
响应：`BriefUserInfoDTO`

**GET /user/list-team-user?ol-only=1**  
响应 `data`：`UserItemInfo[]`（用户简要信息列表）

**POST /user/upd-pwd**  
请求体：
- `oldPassword`（必填）
- `newPassword`（必填，8~16 位，数字+字母）

**GET /user/get-dept**  
响应 `data`：`DeptNameItemInfo[]`（id + deptName）

### 12.4 Customer（`/cust`）

**POST /cust/page-list-customers**  
请求体（分页 + 条件）：
- `page` / `pageSize`
- `namePrefix`、`mobile`、`progress`、`callTips`、`customerGroup`
- `followTimeStart` / `followTimeEnd`
- `applyDateStart` / `applyDateEnd`
- `ownerDeptIds` / `ownerUserId` / `userIdList`
- `ownerFavorite`、`channel`、`ignoreDays`、`selfOnly`
响应 `data`：`PageInfo<CustomerItemDTO>`

**GET /cust/list-dict-items**  
响应 `data`：`Map<String, List<BizDictItem>>`

**POST /cust/upd-biz-cust-info**  
请求体（部分字段）：
- `id`、`version`（必填）
- 跟进字段：`progress`、`followRemark`、`callRemark`
- 业务字段：`customerGroup`、`callTips`、`ownerFavorite`
- 资质字段：`houseFlag`、`carFlag`、`socialInsuranceFlag`、`providentFlag` 等
响应：`null`

**GET /cust/detail?cid=123**  
响应 `data`：`CustomerItemDetailDTO`（含跟进历史与领导点评）

**GET /cust/rtn-cust?cid=123**  
响应：`null`

**POST /cust/btc-rtn-cust**  
请求体：`custIdList`（1~100）  
响应：`null`

**POST /cust/btc-swt-fav**  
请求体：
- `custIdList`（1~100）
- `favoriteType`（0:再分配 1:我的 2:重点）

**POST /cust/ensure-data-channel**  
请求体：
- `token`
- `channelName`
响应：`BizDictItem`

### 12.5 Sys/User（`/sys/user`）

**POST /sys/user/page-list-user-info**  
请求体：
- `page` / `pageSize`
- `userId`、`userRealNamePrefix`、`userName`、`phone`
- `departmentId`、`needExtendQry`
响应：`PageInfo<UserInfoDTO>`

**POST /sys/user/add-user-info**  
请求体：
- `userName`、`password`、`realName`、`phone`（必填）
- `departmentId`、`roles`、`jobName` 等
响应：`null`

**POST /sys/user/upd-user-info**  
请求体：
- `id`（必填）
- `realName`、`phone`、`departmentId`、`roles` 等

**POST /sys/user/del-user-info**  
请求体：`id`（必填）

**POST /sys/user/reset-user-pswd**  
请求体：`id`、`password`（必填）

**GET /sys/user/revoke-by-uid?userId=123**  
响应：`null`

**GET /sys/user/list-all-roles**  
响应 `data`：`RoleItemInfo[]`

### 12.6 Sys/Dept（`/sys/dpt`）

**POST /sys/dpt/add-dpt-info**  
请求体：`deptName`（必填）、`deptCode`、`parentDeptId`、`introduction`

**POST /sys/dpt/upd-dpt-info**  
请求体：`id`、`deptName`（必填）、`deptCode`、`parentDeptId`、`introduction`

**POST /sys/dpt/del-dpt-info**  
请求体：`id`（必填）

**POST /sys/dpt/list-dpt-info**  
请求体：`id`、`deptNamePrefix`、`status`、`parentDeptNamePrefix`、`needExtendQry`
响应：`DeptInfoDTO[]`

### 12.7 Sys/Customer（`/sys/cust`）

**POST /sys/cust/add-cust**  
请求体：`AddCustomerInfoRequest`（姓名/手机号/来源/归属部门/客户资质等）

**POST /sys/cust/upd-cust**  
请求体：`UpdCustomerInfoRequest`（含 `id`）

**POST /sys/cust/dispatch-cust**  
请求体：
- `cids`（1~100）
- `ownerDeptId`（必填）
- `ownerId`（可选）

**POST /sys/cust/import-user-info**  
请求体：`multipart/form-data`  
- `file`（必填）  
- `targetDeptId`（可选）  
- `targetUserId`（可选，默认 0）

**POST /sys/cust/paged-list-imp-rec**  
请求体：`page/pageSize/selfOnly`  
响应：`PageInfo<ImportFileRecordDTO>`

**GET /sys/cust/download-existed-rec?id=123**  
响应：文件流或错误消息

**GET /sys/cust/download-tmpl**  
响应：模板文件

**POST /sys/cust/push-cust-info**  
请求体：`token` + `itemList` + `targetDeptId` + `targetUserId`

### 12.8 Sys/Biz（`/sys/biz-cnf`）

**POST /sys/biz-cnf/add-biz-config**  
请求体：`bizType`、`intValue`、`label`（必填）、`status`、`description`

**POST /sys/biz-cnf/upd-biz-config**  
请求体：`id`、`bizType`、`label`（必填）、`status`、`description`

## 13. API-权限-业务规则表

| API | 入口权限 | 关键业务规则/校验 |
| --- | --- | --- |
| `/auth/login` | 白名单 | 校验 `ClientId`，登录互斥，生成 JWT+refresh token |
| `/auth/revoke` | 白名单 | token 可为空，始终返回成功 |
| `/user/info` | 登录用户 | 获取自身信息 |
| `/user/online-status` | 登录用户 | `onlineStatus` 仅 0/1 |
| `/user/list-team-user` | 登录用户 | 非数据管理员仅自身；管理员按部门树 |
| `/cust/page-list-customers` | 登录用户 | 按角色/部门限制可见范围 |
| `/cust/upd-biz-cust-info` | 登录用户 | 未分配客户不可编辑；上级点评权限校验 |
| `/cust/rtn-cust` | 登录用户 | 仅归属人可退回 |
| `/cust/btc-rtn-cust` | 登录用户 | ID 1~100 |
| `/cust/btc-swt-fav` | 登录用户 | ID 1~100；收藏类型 0/1/2 |
| `/cust/ensure-data-channel` | 白名单 | `extraToken` 校验 |
| `/sys/user/**` | `ROLE_SUPPER` 或 `ROLE_USER_INFO_ADMIN` | 逻辑删除；用户名/手机号去重 |
| `/sys/dpt/**` | `ROLE_SUPPER` 或 `ROLE_DEPT_INFO_ADMIN` | 有子部门不可删除 |
| `/sys/cust/dispatch-cust` | `ROLE_SUPPER` 或 `ROLE_DEPT_DATA_ADMIN` | 非超管不可越权分配 |
| `/sys/cust/import-user-info` | `ROLE_SUPPER` 或 `ROLE_DEPT_DATA_ADMIN` | 文件校验 + 异步导入 |
| `/sys/cust/download-existed-rec` | `ROLE_SUPPER` 或 部门管理员 | 需部门/操作人校验 |
| `/sys/cust/push-cust-info` | 白名单 | `extraToken` 校验 |
| `/sys/biz-cnf/**` | `ROLE_SUPPER` | 字典类型与状态校验 |

## 14. 调用链与数据权限判定流程图

### 14.1 请求调用链（简化）

```
HTTP Request
  -> JwtAuthenticationFilter
     -> AuthService.getUserInfoByJWT
     -> UserContext.setUserInfo
  -> Controller
     -> Service (校验 + 业务规则)
        -> Manager (MyBatis-Plus)
           -> Mapper(XML) -> DB
  -> SuResult Response
```

### 14.2 客户数据权限判定（简化）

```
request -> CustomerInfoService.pageListCustomerInfo
  -> build CustomerInfoQry from request
  -> adaptQryByRoles(qry, userId, userDeptId, roles)
     -> if ROLE_SUPPER: no restriction
     -> if ROLE_DEPT_DATA_ADMIN:
          allowedDeptIds = dept tree (self + children)
          if no dept filter: allow deptIds + (or ownerUserId=self)
          else: remove out-of-scope deptIds
     -> else: force ownerUserId=self
  -> query DB
```

### 14.3 客户分配权限判定（简化）

```
dispatch-cust
  -> verify role (ROLE_SUPPER / ROLE_DEPT_DATA_ADMIN)
  -> allowedDeptIds = dept tree (if not super)
  -> if target dept not in allowedDeptIds: reject
  -> if move across dept with existing owner:
       require ownerId provided
```
