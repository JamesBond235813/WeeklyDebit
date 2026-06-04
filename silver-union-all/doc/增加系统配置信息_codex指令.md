# 开发系统配置信息功能
## 角色定义
你是一个高级java开发工程师，目标是生成稳定、可维护、最小风险的生产级代码。

## 1. 目标定义（Objective）
增加系统配置信息功能。

## 2. 完成标准（Definition of Done）
### 2.1 功能验收 
1) 增加`com.jhl.silver.union.biz.sys`包， 包含`dal`,`service`,`manager`三个模块，并在对应的包内创建各层对应的代码，且与其它的包保持格式一致(如`com.jhl.silver.union.biz.user`包)。
2) 在`com.jhl.silver.union.biz.sys.service` 层中增加 `增`、`删`、`改`、`查`功能。
3) `PanoramaClient` 类中的各配置项均从数据库（平台配置信息表）中读取。

新增的数据库表结构（DB中已创建完成该表）如下: 
```mysql
CREATE TABLE `sys_config` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '平台配置信息ID',
  `cnf_type` varchar(64) NOT NULL DEFAULT '' COMMENT '配置类型',
  `cnf_key` varchar(64) NOT NULL DEFAULT '' COMMENT '配置信息 key',
  `cnf_desc` varchar(64) NOT NULL DEFAULT '' COMMENT '配置信息描述',
  `cnf_value` text NOT NULL COMMENT '配置信息内容(json格式)',
  `delete_flag` bigint NOT NULL DEFAULT '0' COMMENT '删除标识. 0:表示未删除; 其它表示未删除',
  `creator_uid` bigint NOT NULL DEFAULT '0' COMMENT '创建人用户ID',
  `modifier_uid` bigint NOT NULL DEFAULT '0' COMMENT '最后修改人用户ID',
  `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `gmt_modified` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `unq_biz_code` (`cnf_key`,`cnf_type`,`delete_flag`),
  KEY `idx_cnf_type` (`cnf_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='平台配置信息';
```

### 2.2 实施任务
1. 项目代码已修改，实施前先仔细阅读代码
2. 开发平台配置信息增、删、改、查功能（使用MyBatis-Plus），各层代码直接由你编写，不使用generator生成
   1. 定义配置类型(`cnf_type`字段)的枚举,要求枚举中带有对配置类型的描述，即用于填充 `cnf_desc`字段
   2. `cnf_key` 则以常量的方式进行定义。 
   3. 用于`com.jhl.silver.union.biz.risk.integration.PanoramaClient` 中的配置信息类为`com.jhl.silver.union.biz.data.PanoramaConfig`
   4. service 类中，查询功能，增加`cnf_key`,`cnf_type`进行查询， 且查询结果放入本地缓存， 缓存有效期5分钟
3. 更新`com.jhl.silver.union.biz.risk.integration.PanoramaClient` 中获取 配置信息方式， 使其通过调用上面编写的service 获取取配置信息。


### 2.2 稳定性
- 不引入隐式依赖
- 不修改与本次业务无关的代码
- 尽量不修改既有代码，若需修改，则以最小修改原则进行修改

### 2.3 测试
- 代码编写完成以及修改完成后， 要求编写单元测试用例，确保代码的正确性和可维护性
- 单元测试用例编写完成后，要求自动运行单元测试用例， 并确保测试通过。
- 特别指出， `com.jhl.silver.union.biz.risk.integration.PanoramaClient.fetchReport` 方法请求三方接口会产生费用，测试时用到本方法，要求mock， mock 的数据可以直接从数据库表`risk_control_report`#`report_json`中获取



## 4. 测试规范
- 测试时若需启动服务端,可直接以后台进程的方式启动服务端
- DB/redis 可真实访问
- 不污染数据

## 5. 禁止事项
- 禁止修改既存业务语义
- 禁止猜测 JSON 结构
- 禁止直接输出 json字符串



## 最终目标
所有页面的风格保持一致。
生成的代码在未来仍然清晰、可维护、可定位问题。
## 6. 具体实施内容

### 6.1 新增代码结构
- 新增系统配置类型枚举与 key 常量
  - `silver-union-ops/src/main/java/com/jhl/silver/union/biz/common/enums/SysConfigTypeEnum.java`
  - `silver-union-ops/src/main/java/com/jhl/silver/union/biz/sys/SysConfigKeys.java`
- 新增 sys_config 对应 DAL
  - `silver-union-ops/src/main/java/com/jhl/silver/union/biz/sys/dal/entity/SysConfigDO.java`
  - `silver-union-ops/src/main/java/com/jhl/silver/union/biz/sys/dal/mapper/SysConfigMapper.java`
- 新增 sys_config manager/service
  - `silver-union-ops/src/main/java/com/jhl/silver/union/biz/sys/manager/SysConfigManager.java`
  - `silver-union-ops/src/main/java/com/jhl/silver/union/biz/sys/manager/impl/SysConfigManagerImpl.java`
  - `silver-union-ops/src/main/java/com/jhl/silver/union/biz/sys/service/SysConfigService.java`
  - `silver-union-ops/src/main/java/com/jhl/silver/union/biz/sys/service/impl/SysConfigServiceImpl.java`

### 6.2 功能实现要点
- sys_config CRUD（MyBatis-Plus）
  - 新增/更新时自动填充 `cnf_desc`（来自 `SysConfigTypeEnum` 描述）
  - 删除为逻辑删除（`delete_flag = 当前时间戳`）
- 查询按 `cnf_type` + `cnf_key`，并使用本地缓存（Guava LoadingCache）
  - 缓存有效期 5 分钟
  - 提供按类型/按 key 清理缓存方法
- Panorama 配置读取逻辑改为从 DB 获取
  - `PanoramaClient` 使用 `SysConfigService.getPanoramaConfig()`
  - `fetchReport` 前校验 `apiUrl/merchantNo/accessKey/secretKey`

### 6.3 已调整文件
- `silver-union-ops/src/main/java/com/jhl/silver/union/biz/risk/integration/PanoramaClient.java`
  - 去除 `@Value` 配置注入，改为 DB 配置读取
  - 新增 `isEnabled()` 读取配置

### 6.4 单元测试
- 新增测试
  - `silver-union-ops/src/test/java/com/jhl/silver/union/biz/service/SysConfigServiceTest.java`
  - `silver-union-ops/src/test/java/com/jhl/silver/union/biz/service/RiskReportServiceImplTest.java`
- 关键点
  - `PanoramaClient.fetchReport` 已 mock
  - mock 数据从 `risk_control_report.report_json` 读取

### 6.5 测试执行记录
- 执行命令：`./mvnw -pl silver-union-ops -am test`
- 结果：已有存量测试失败（与本次变更无关），未完成全量通过
  - `BizConfigServiceTest`、`CustomerInfoServiceImplTest` 报错
- 指定测试执行命令（仅运行新增测试）：
  - `./mvnw -pl silver-union-ops -am -Dtest=SysConfigServiceTest,RiskReportServiceImplTest -Dsurefire.failIfNoSpecifiedTests=false test`
