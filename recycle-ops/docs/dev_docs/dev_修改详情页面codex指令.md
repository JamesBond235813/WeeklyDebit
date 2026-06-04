# 修改界面内容

## 角色定义
你是一个高级前端开发工程师，精通vue3.5 以及antd等各种前端框架，目标是生成稳定、可维护、最小风险的生产级代码。

## 1. 目标定义（Objective）
修改客户详情的Modal框，让Modal框的布局看上去更自然。

## 2. 完成标准（Definition of Done）
1. `客户信息详情` 的Modal 界面：
   - `主管评价`, `跟进历史` 缩小宽度， 让左侧的界面放大些
   - `客户信息` 中的 `重置`, `保存` 两个按钮移到Modal的底部， 放到`关闭`按钮的左侧， 并且使用button风格（与`关闭`按钮保持风格一致）。其中， `保存`按钮使用`primary`风格。
2. 支持响应式布局，即当浏览器宽度变小时，`主管评价`, `跟进历史` 布局会自动换到 `基本信息`的下方，且宽度与`基本信息` 保持一致。

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
1. 项目路径在 `/Users/liwei/shared/projects/jhl/recycle-ops`
2. 修改的主要目标页面文件为：`/Users/liwei/shared/projects/jhl/recycle-ops/apps/web-antd/src/views/bizwork/customer/cust-list-table-v2.vue`

## 6. 实施步骤（回写）
1. 调整客户详情弹窗布局：缩窄右侧“主管评价/跟进历史”区域宽度，并优化响应式，在窄屏下改为纵向排列且右侧区域宽度与左侧一致。
2. 将“客户信息”中的“重置/保存”按钮从头部移至弹窗底部，放置在“关闭”按钮左侧，保持与“关闭”一致的按钮风格；其中“保存”为 primary 风格。
3. 新增 Playwright 配置与 UI 用例：覆盖“右侧栏更窄 + 底部按钮布局”与“窄屏下纵向堆叠”两项验收。
4. 运行 UI 测试并确认通过：`pnpm exec playwright test --config apps/web-antd/playwright.config.ts`。

### 变更文件
- `apps/web-antd/src/views/bizwork/customer/cust-form.vue`
- `apps/web-antd/playwright.config.ts`
- `apps/web-antd/e2e/customer-detail-modal.spec.ts`
