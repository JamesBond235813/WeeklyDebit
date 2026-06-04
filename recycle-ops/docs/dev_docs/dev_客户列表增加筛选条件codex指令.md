# 为客户列表页增加筛选条件

## 角色定义
你是一个高级前端开发工程师，精通vue3.5 以及antd等各种前端框架，目标是生成稳定、可维护、最小风险的生产级代码。

## 1. 目标定义（Objective）
为客户列表页增加 `沟通结果` 的筛选条件，并且能正常查询符合沟通结果的客户数据。

## 2. 完成标准（Definition of Done）
1. `客户信息`,`我的客户`,`再分配客户`,`重点客户` 四个菜单项对应页面的筛选条件中， 都增加 `沟通结果` 的筛选条件，并且能正常查询符合沟通结果的客户数据。
   - 上述四个菜单的客户信息列表页共用 `apps/web-antd/src/views/bizwork/customer/cust-list-table-v2.vue`
   - `沟通结果` 筛选条件字段对应为服务端请求参数 `callTips`， 且为单选下拉框。
   - `沟通结果` 下拉框的数据源取自页面中的`callTipsOptions`变量 
   - 新增的筛选条件 `沟通结果` 要求样式符合页面整体风格
   


## 3. 测试规则
1. 要求编写UI测试用例，并且要求运行通过。
2. 当前服务端、前端vite已启动, 可直接编写脚本进行测试。
3. 当前可登录的账号为 `qr8` , 密码为 `abc123456`

## 4. 禁止事项
- 禁止修改既存业务语义
- 禁止猜测 JSON 结构
- 禁止直接输出 json字符串
- 禁止修改与本次页面无关以及业务无关的代码


## 5. 补充说明：
1. 服务端项目路径在 `/Users/liwei/shared/projects/jhl/silver-union-all`， 本次任务仅涉及前端，无需修改服务端代码。

## 6. 实施步骤（Execution Steps）
1. 在 `apps/web-antd/src/views/bizwork/customer/cust-list-table-v2.vue` 的 `searchFieldKeyMap` 增加 `callTips` 映射，并在 `initSchemaList` 中新增“沟通结果”单选下拉（数据源 `callTipsOptions`）。
2. 在 `pagedListCustomerInfo` 中读取表单 `callTips` 值并清洗为数字，随请求参数 `callTips` 一起提交到 `/cust/page-list-customers`。
3. 新增 UI 用例 `apps/web-antd/e2e/customer-calltips-filter.spec.ts`：登录后依次访问四个菜单页，选择“沟通结果”选项，点击“查询”，断言请求包含 `callTips` 数值。
4. 运行测试：`PLAYWRIGHT_BASE_URL=http://localhost:5666 pnpm -C apps/web-antd exec playwright test e2e/customer-calltips-filter.spec.ts`。
