# 增加控制数据进量的开关

## 服务端项目 `silver-union-all`

1. 在`com.jhl.silver.union.biz.common.enums.SysConfigTypeEnum`中，增加配置类型: `ENABLE_RECV`,表示`接收上游流量`
   的开关，并定义该配置信息的数据类，类中仅包含一个 `private Boolean enable;`字段即可，`true`表示开启状态。
2. 在 `com.jhl.silver.union.web.controller.customer.RecvThirdPlatDataController.accessCheck` 中， 先判断 `接收上游流量`
   开关是否开启：
    1. 若`开启`, 则正常走后续的逻辑；
    2. 若`未开启`， 则返回`com.jhl.silver.union.biz.common.enums.ThirdPlatResultCode.REFUSE_APPLY` 对应的response。
3. 在`com.jhl.silver.union.web.controller.admin.BizConfigController` 类中增加`接收上游流量`配置相关的功能：
   1. 获取配置信息：
      - `接收上游流量` 配置信息不存在， 则默认为开启的状态，且向system_config中增加该配置信息（开启状态的配置信息）
      - 获取配置信息优先走缓存
   2. 更新配置信息：
      - 这个接口只有超管权限（`com.jhl.silver.union.biz.common.enums.UserAuthRoleEnum.ROLE_SUPPER`）的用户可进行更新

## 前端项目 `recycle-ops`
1. 在 `【管理中心】-【业务配置】`页面`参数配置`这个tab页中， 插入一个区域：
   - 区域内存放 `接收上游流量` 的开关(switcher)，并明确给出`开启`(`true`)、`关闭`(`false`)的文案 
   - 用户点击开关后，请求服务端更新`接收上游流量`配置信息的Rest接口， 并告知用户是否更新结果。
2. `【管理中心】-【业务配置】`页面 的tab项，左侧顶格了，看上去非常难受，需要你用设计师的眼光，去优化一下tab项的间隔。


## 准备工作
1. 重新读取前后端项目各文件，并更新记忆

## 测试工作
1. 代码写完后，要求编译通过，不能有语法错误。
2. 要求编写测试用例， 要求自动运行测试用例，且需要运行通过。 
   - 例外情况：`RecvThirdPlatDataController.accessCheck` 中的代码变更无需写侧试用例，仅检查代码是逻辑是否正确即可，由我手动去测试。 

---

## 实施记录（2026-03-11）

### 1. 准备工作
1. 重新激活并重读后端项目 `silver-union-all` 任务相关文件：
   - `SysConfigTypeEnum`
   - `RecvThirdPlatDataController`
   - `BizConfigController`
   - `SysConfigService` / `SysConfigServiceImpl`
   - 相关测试类
2. 重读前端项目 `recycle-ops` 任务相关文件：
   - `apps/web-antd/src/views/management/bizConfig/index.vue`
   - `apps/web-antd/src/api/biz/config.ts`

### 2. 后端实施步骤（silver-union-all）
1. 新增系统配置类型 `ENABLE_RECV`。
2. 新增配置数据类 `EnableRecvConfig`，字段为 `private Boolean enable;`。
3. 在 `SysConfigKeys` 中新增 `ENABLE_RECV` 常量。
4. 在 `SysConfigService` 增加：
   - `EnableRecvConfig getEnableRecvConfig()`
   - `void updateEnableRecvConfig(EnableRecvConfig config, Long optUserId)`
5. 在 `SysConfigServiceImpl` 实现：
   - 查询优先走缓存；
   - 配置不存在时默认创建 `{ "enable": true }`；
   - 提供开关更新逻辑（不存在则新增，存在则更新）。
6. 在 `RecvThirdPlatDataController.accessCheck` 增加开关前置判断：
   - `enable=false` 时直接返回 `ThirdPlatResultCode.REFUSE_APPLY`。
7. 在 `BizConfigController` 增加接口：
   - `POST /sys/biz-cnf/get-enable-recv`
   - `POST /sys/biz-cnf/upd-enable-recv`
8. `upd-enable-recv` 增加权限控制：
   - 仅 `ROLE_SUPPER` 可更新。

### 3. 前端实施步骤（recycle-ops）
1. 在 `bizConfigApi` 增加接口封装：
   - `getEnableRecv()`
   - `updateEnableRecv(config)`
2. 在 `管理中心 -> 业务配置 -> 参数配置` 页增加“接收上游流量”开关区域：
   - 开关文案：`开启` / `关闭`
   - 切换后调用后端更新接口，并提示成功/失败。
3. 优化该页面 Tabs 左侧视觉间距：
   - 对 `.ant-tabs-nav` 增加左侧 margin/padding，避免“顶格”。

### 4. 测试与验证
1. 后端编译通过：
   - `./mvnw -pl silver-union-ops -am -DskipTests compile`
2. 后端自动化测试通过：
   - `./mvnw -pl silver-union-ops -Dtest=SysConfigServiceImplTest,BizConfigControllerTest,RecvThirdPlatDataControllerTest test`
3. 前端构建通过：
   - `pnpm -F @vben/web-antd run build:dev`
4. 前端单测通过：
   - `pnpm test:unit`
5. 前端全量 `typecheck` 当前仓库存在历史问题（非本次改动引入），不作为本任务阻断项。

### 5. 手动联调结果
1. `RecvThirdPlatDataController.accessCheck` 的手动联调已通过（由需求方确认）。

### 6. 后续补充修复（2026-03-11）
1. 现象：
   - 用户登录过期后，若前端本地 `accessToken` 未及时清空，页面仍会持续尝试建立 `ws/notice` 连接，导致服务端出现大量无效握手失败日志。
2. 修复内容（前端 `recycle-ops`）：
   - 在 `apps/web-antd/src/layouts/basic.vue` 增加 JWT 过期校验逻辑：
     - 解析 token 的 payload；
     - 判断 `exp` 是否大于当前时间；
     - 仅当 token 未过期且存在 `userId` 时才允许发起 WebSocket 连接与重连。
3. 结果：
   - 登录过期且 token 未清空时，不再发起无效 WS 请求，不再持续触发服务端 `WS handshake auth failed` 日志。
4. 验证：
   - 前端构建验证通过：
     - `pnpm -F @vben/web-antd run build:dev`
