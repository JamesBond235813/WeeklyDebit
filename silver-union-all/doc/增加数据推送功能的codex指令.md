# 开发接收来自三方推送数据的功能

## 角色定义

你是一个高级java开发工程师，目标是生成稳定、可维护、最小风险的生产级代码。

## 1. 目标定义（Objective）

顺利接收来自外部的推送数据，并将数据按规则分配到指定用户或公海。

## 2. 完成标准（Definition of Done）

### 2.1 功能验收

1) 增加三方平台（推送数据的平台）的公私钥信息配置
2) 顺利接收来自外部的推送数据：
    - 签名验证通过
    - 数据解密正常
3) 数据接收后， 正确入库，并按规则分配给指定的用户， 或者分配到公海

### 2.2 稳定性

- 不引入隐式依赖
- 不修改与本次业务无关的代码
- 尽量不修改既有代码，若需修改，则以最小修改原则进行修改

### 2.3 测试

- 代码编写完成以及修改完成后， 要求编写单元测试用例，确保代码的正确性和可维护性
- 单元测试用例编写完成后，要求自动运行单元测试用例， 并确保测试通过。

## 3. 开发任务

1. 项目文件已发生变更，需要重新仔细阅读
2. 通过`com.jhl.silver.union.biz.sys.service.SysConfigService`增加接收三方平台数据的配置信息：
    - 定义 SysConfigTypeEnum 对应的枚举
    - `cnf_value` 的数据结构为： `com.jhl.silver.union.biz.data.RecvThirdPlatDataConfig`
    - `cnf_key` 不设置为常量，增加配置时指定, 使用 `RecvThirdPlatDataConfig#appId`即可
    - 新增配置信息时， 要求`RecvThirdPlatDataConfig.channelName`必填,并去除前后空白， 并通过如下逻辑（伪代码，节选自
      `com.jhl.silver.union.web.controller.customer.CustomerController.ensureDataChannel`）确保创建渠道信息，并将渠道ID赋值给
      `RecvThirdPlatDataConfig.channelId`
   ```java
        bizConfigService.generateChannelDictByNames(channelNameSet, 0L);
        BizDictItem item = bizConfigService.getSingleBizDictItemByLabel(BizDictConfigTypeEnum.DATA_CHANNEL,
                request.getChannelName());
   ```

3. 新增 `com.jhl.silver.union.web.controller.customer.RecvThirdPlatDataController`:
    - 三方平台的接口文档在 `/Users/liwei/shared/projects/jhl/silver-union-all/doc/CRM三要素信息API接入文档V-1.0.0.docx`
      文件中：
        - 包含三方平台的通用请求数据结构以及本平台需返回的通用响应数据结构
        - 文档中涉及aes ecb加密、对aesKey 进行Rsa非对称加密， 以及签名验签的方式，要求仔细阅读并理解。
        - 三方平台的demo代码 在
          `/Users/liwei/shared/projects/jhl/data_transfer_client_demo/src/main/java/top/dataplayer/client/demo/RsaAesDemo.java`
          文件中可查看
    - 增加`撞库准入`检查的rest接口:
        - 收到数据后，进行验签，解密数据，得到手机号md5字符串。
        - 根据手机号md5字符串，根据`mobile_md5`字段查询`customer_info_item`表数据， 若存在，则返回`准入失败`，且撞库；若不存在则返回
          `准入`(成功)。
        - 接收到请求后，打印 info 级别日志，日志内容为 通用请求信息的json字符串以及解密后的内容 ，新增`CustPushRecordDO`记录
        - 返回响应之前，打印 info 级别日志，日志内容为 通用响应数据结构的json字符串，更新`CustPushRecordDO`记录
        -
    - 增加接收 `申请授信` 数据的rest接口
        - 收到数据后进行进行验签，解密数据，得到手机号md5字符串。`线索订单号`, `用户姓名`, `身份证号码`, `手机号码`等明文内容
        - 通过`com.jhl.silver.union.biz.customer.service.ImportCustDataService.addCustInfo`
          方法添加客户信息并完成客户分配。目标部门ID，目标用户ID先全部填`0L`
        - 接收到请求后，打印 info 级别日志，日志内容为 通用请求信息的json字符串以及解密后的内容，新增`CustPushRecordDO`记录
        - 返回响应之前，打印 info 级别日志，日志内容为 通用响应数据结构的json字符串，更新`CustPushRecordDO`记录
    - `com.jhl.silver.union.web.controller.customer.RecvThirdPlatDataController` 下的所有rest接口不走用户登录认证，也不验证clientId

## 4. 测试规范

- 测试时若需启动服务端,可直接以后台进程的方式启动服务端
- DB/redis 可真实访问
- 不污染数据
- 对所有变更以及新增的代码编写测试用例，并尽可能覆盖变更的代码
- 测试用例编写完成后， 要求运行， 且运行通过。 其中，忽略 `BizConfigServiceTest`、`CustomerInfoServiceImplTest` 这两个测试类，
  且不执行测试。
- 用于联调的信息如下：
  - 接收三方平台数据的配置信息`cnf_value`json字符串如下： 
  ```json
    {"appId":"TCRM01","privateKey":"MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQCfjumdj0XcHwsMjj+mWngaB1+En+b3PJBwT9e1l8EczbogoPpfdZWoJWyrMAu0U/87LtbqOsc14bLZ13NX7j+tgsWZ86dgFrokdS61uIg8v4tHIZdKFytW0kWeYS7g8FH+a8qsySJm5iBvGGoHsR73Jwt1fequj+tVHz4lFHUwi8q4dU0FzoHVkwwYRyncgigtDUJER3Kx9wZ0CiXbozgvkUSxfBjR5Ye+1fy+vVrOQzi1GsCEIUf71i4jyRxLqsBI8nGqxQQZc1gn6u7bgxzPd6qF+Xet7vtF2SUb54DrkiDhELqL51rH3Gs3WeMCCqZDQU69eq05PiiIk7dXNcKLAgMBAAECggEASfsmoNKUjrqqEdlG8+gQtejjRggqPEqNojWzC9TgSm2tNoHNdUN876jimQE+/A7SUeum5JX6ViZfGhiGt6eVSOtQmdBas/f1uP/Id6OnL5uUhZeyoTza8Hewpf3jkZJ8Qh5SrAjadaGQOlK0nvpmJCyraH/It8WtVRuWYfT5XVdIYpgV3BerczIgB9JKeuhuPgpcwNcQ0CmI2g7wEQ7ES+IaZFPuvG6bIAje/kPrK/uoLequO3UpcGY91rSRSN4bVpvEWYQuwdaRPwW4sekNyKvkAo2x65xnuBsy07/OW64m0HeRJa6yBWZsW090/jeP6DwTipad32kyav62ZulA4QKBgQDOUQkd+60TqeoGyrGoP4ha2erRePBijKvJe/Huf1AmOBAZ4MvX0+G/9Anv7LgcBwsbh+sJ4LxhzsqQaKGrmZcdkyXsuEVBLbfGx+3uSqae6oZWrjOhrJg6dgLcvJLsw7pebt4ozJ2mECmrveXGzuGVIteIOhZcnwpHodJbVgPzEQKBgQDF+1SOpByqdWEqOGj/DfwgQMn5m+cCeHWL8f0N2jcsVJxIrU1dXzC2fa+s9IDbRFofeLs/7wcB2KINno6vOEC9TP7MSxRv+txSfQUZxolxsHW3GTFWyCzDDGtf2Aq8WHczTlSgAhAki68gnOocm2K1FJ5qIZ0taY7f6gGRmwGj2wKBgAk8i4HyIH9+3eFL9cQog/w9QUv7dBeVYKN2jxA0Vuw/GkluTPHupG6piEBbgqqOjiq/XQBmNUjTrzHj3UkHaUKDsfD1FvSiDVYy4S4H3YnDyhvbVKhqR65mVh53usQqxw8vO3bsIiqrEpKDv+O0o1i/5JJOt22SGS23yukX4rlhAoGBAJZgz5pE1y02WSZLkJzij3YkIAXDQFVlD8vLc6246R136vldAR2B9ys2DmDtmo5xvY6YEop+UTE6zeRQYgp/TNU8jXC5On3P6teQ9HXeknlTUiZQMWS8SRuh7FDxdT4YZ/oFbkvXJVHM86lu5nfyIqhuT+FHRO7AdfBn+ucQ+M7vAoGBAIoZUbR4RW0QQxQuPu6IHkSo8nM7Vc9DW0Q8yTSjDbz7NincnAW3hcqPpVEoEqJFLv0QtK44lVQbkh2Q6P/XFqbqdnL+rij7/5dQbMuZG4IzCD1dKaf/t94Gi0OWTTqkmFyG1nbBmxEZPry1mcIJc8kUo62GpfyCmGfVZutQIP2W","publicKey":"MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAn47pnY9F3B8LDI4/plp4GgdfhJ/m9zyQcE/XtZfBHM26IKD6X3WVqCVsqzALtFP/Oy7W6jrHNeGy2ddzV+4/rYLFmfOnYBa6JHUutbiIPL+LRyGXShcrVtJFnmEu4PBR/mvKrMkiZuYgbxhqB7Ee9ycLdX3qro/rVR8+JRR1MIvKuHVNBc6B1ZMMGEcp3IIoLQ1CREdysfcGdAol26M4L5FEsXwY0eWHvtX8vr1azkM4tRrAhCFH+9YuI8kcS6rASPJxqsUEGXNYJ+ru24Mcz3eqhfl3re77RdklG+eA65Ig4RC6i+dax9xrN1njAgqmQ0FOvXqtOT4oiJO3VzXCiwIDAQAB","thirdPlatPublicKey":"MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAqm6R5AZp1Jt39jT381fPZZ4F2q2ToK84DsmDG8X4WJKfn0GRpJ2GNnhTBJP0O+7ic64jLleXJKh87Sg87qyFy2G4W0i45cdhTo4+nRI+nkNzdDeeCaabsGbAQZZReMRnrUatQBRYSMYDwYZxc9TT8NVguG5WTmjOWEKt94vrp+Pfh6SKjZIJtCwBDfo8vPydNSWYSD1xRQdQPMa/uYMWyYodG0XZB7dw0/BQeK44w1WbV+haXm3p3fl27zg7B7o+6Zc8C2vaq4CTIymFSL/54Lv/E+u7HLVI40SnPRnO75zedQlpU9aoL+qAZmE/UPPY45NWwQ3BzDb6su1k0H2FpwIDAQAB","channelName":"CRM测试通道1"}
  ```
  - 撞库准入接口请求
   ```json
   {"appId":"TCRM01","data":"iKPlg4zbpJogDOUUWwo8W+BzOnY5wk4jf8APcC6U0goaQUz4u/kfbZC4R4cL69GsWTnjFI8sT6Dr3BjO3QuU1Q==","requestId":"TCRM0120260126225530279UY7ZUMUYH","timestamp":1769439330279,"secretKey":"CKQ94PPh/eDsU1JeTirRKm8QyuXOSzZ5b/nndL8QIiXCH+ZpxZSMcQ+9jLOjuV+GDdd8sWkK8yd+fJpFsMW0BQLYnGht+6fTrvL0Qg9ap1tpBuBo7aXHeSkPf/tZ8VxHS9XV0BE3dw/jT4D9NIcq8RTedS67UvEfsKt8nQCUtsHKe92zxS34nFeTV5xP/zybX0dcaF4cMhbVvVg3DDqTMioSvwPK/1gn3qVyvfzwSdgl4C0h/OVaZPQLnHn/b6lBZKt5i4f50vQy43L/eJlgPKG6nrU0J4jAxzpb0lZcTtZLNjQGh+FvS9Tb43LuUFFrGu5PanNV5DYITuwqllSfng==","sign":"MDgSqmKghYoWmqU5hY+d/7wMnQ0fnjCzC1Ozz4OEGvrhtM6WHjv+Tu2LFuH9HAUfPfmlbj3O8t3ZbYteEBiqa+1dqPCkWzUBx+spY66MwbbJSjkCN2X7NdLT4jG8W7XV/WpuVMgKnHKqevoH4WOvd6Nur5mvLoQ9Bk8u4vY996TyhfcKyrT7/uv7lPqa1c8C+yjY97J4uyQmqhqVLAFQt6FwyGpzrL/V/WN8IEtTuh4E6SAt5KrwNga4B4/InhpOx+P8jdfLnxUnm8/q8Fc9lebr452dxEZyOaQt6QS1GQZvk2bKe8e+ht1d1jTrPHSk/w91WIYPvVGV3WpBZyc4yA=="}
   ```
  - 申请授信接口请求
  ```json
  {"appId":"TCRM01","data":"KobuZEG6/sC9J2k9H2RzcduBEnAo5yRFeMlUia/COUwUtvyIlkYi6Pinv5jOxfS7kiOIiqELdtsgewKOYQ7r4Bq1HPrXoJwsE+XQDLU7EhncYVmBali+FI4Y8QZ7OrEMozrpsp35Vyi8230aDWvUCsBqp3/G6zMvqrNssgUeGHoNGLBlfGPDwJMcNtyq4FWOM9u3zYkw7tz5ICgQwNn362t/Hhg+jYoc7eFYdvXuZTA+6X6vIIjFqfHsNpxH5Bhg1nt2Y+VUqDLrZRcFbrIb6CrcM2N3mj82o1w8HF5BKOgOKpsNaHbkamT34OpB2+bwclRvZfqVw1uhqWhZ1ZfB28rav0RVQMBpl3RJk7kX0yGqC+dqBsbQwnE6voi1bYWGOOqLxpcd/M6d5NU685WscnE+YUZgOOytmSEn0hLImEkqweBpelOrkqxfr9q+N8LsmNIOt6nw4jkkqnHKeEW5Gf59paYscR11fWYGRtE4CYLZ2nEDoZl2jxs8vAQgN7AK6ZsULHh2Ply7necBkvPADrKEsQojAocEMowWTcaK6zbdVETSzBXqgqoV+cOlRYiSsggN2cC+JPu1+oqiQn+wGDeyxbi9TT2ELSWn4uNX1DW71uk3S2d9p3vsuHYaekBQFzfRP4FYPF+rKy0itFPRzMNW7PovWlOKxHRvD095Mso=","requestId":"TCRM01202601262255317843LO613UIW","timestamp":1769439331784,"secretKey":"RPag7yT7Iibk4Mg4OQmTfGdtzs04oe8SLYr9yq2idhdGmVyjaRLenWGMZ4Ved+NFcFEqu2yGVfYqAdps9v1GNxH0IXmoD65lsnV4aaKNLefSMFMEkUsdIpjgpIvYB+REh7m0fyeX6xlBRTnR/D8AizNqtFCo35vHKPsP4FULr3hzsnywEupB7t8LowgwAJ7U8FYJxEGXd21Re2WUeEm6J/vOLFqt95/F3ed06iB4UXvS6vHV013/74GjnLaDcfcwCgeIofo8MWl/1eSXJa8aXJzPj7nStTsHB9LxkQJjJjDmdE37ZhwBE+21W7ym0jJrOlTzb7TDqDq3OVyoQRNyZg==","sign":"WR4DPx7BQgjMGP2C/izbQRR7VoyEQlOa5Hb8cKbk6YNiALohT5mH2z1zBZMLqADV5IN569VEqflApdOGCMsq6cTVa5FaB4xLogIxTbKVU9c/tdtRMYWSKxkVhMPO5Ijj+sxsZmJVtUTTh2p4LC9HLHqEzA5WV4k6peRKqnBw6ayu+8g1SwfMxzgkPrY3eDwhVyVWfbom+nnNiS6BnzPu2WICTIS7/dRmKG+aRLqNmVimd2feKMVZPu/fTqFjSOePeyln2xtDMI3ipoLNwXywsgBTcL3gNCbW46YtIdfSgS1ucJJSyGfbFzPk45XP9o40HH1SKQON2uTJDOXlk++XPg=="}
  ```
  

## 5. 禁止事项

- 禁止修改既存业务语义
- 禁止猜测 JSON 结构
- 禁止直接输出 json字符串

## 最终目标

生成的代码在未来仍然清晰、可维护、可定位问题，且风格与现有代码保持一致。
各逻辑中要求详细注释，且注释内容清晰、准确、完整。
新定义的数据类， 要求有类注释， 字段注释（均为doc注释）



## 6. 具体实施内容

### 6.1 新增/调整代码结构
- 配置类型扩展与三方返回码枚举
  - `silver-union-ops/src/main/java/com/jhl/silver/union/biz/common/enums/SysConfigTypeEnum.java`
  - `silver-union-ops/src/main/java/com/jhl/silver/union/biz/common/enums/ThirdPlatResultCode.java`
- RSA 工具
  - `silver-union-ops/src/main/java/com/jhl/silver/union/biz/common/utils/RsaUtils.java`
- 三方接收接口与 DTO
  - `silver-union-ops/src/main/java/com/jhl/silver/union/web/controller/customer/RecvThirdPlatDataController.java`
  - `silver-union-ops/src/main/java/com/jhl/silver/union/web/data/customer/recv/RecvThirdPlatCommonRequest.java`
  - `silver-union-ops/src/main/java/com/jhl/silver/union/web/data/customer/recv/RecvThirdPlatCommonResponse.java`
  - `silver-union-ops/src/main/java/com/jhl/silver/union/web/data/customer/recv/RecvThirdPlatAccessCheckRequest.java`
  - `silver-union-ops/src/main/java/com/jhl/silver/union/web/data/customer/recv/RecvThirdPlatAccessCheckResult.java`
  - `silver-union-ops/src/main/java/com/jhl/silver/union/web/data/customer/recv/RecvThirdPlatApplyCreditRequest.java`
- SysConfigService 扩展
  - `silver-union-ops/src/main/java/com/jhl/silver/union/biz/sys/service/SysConfigService.java`
  - `silver-union-ops/src/main/java/com/jhl/silver/union/biz/sys/service/impl/SysConfigServiceImpl.java`

### 6.2 关键实现点
- `SysConfigTypeEnum` 新增 `RECV_THIRD_PLAT_DATA` 类型。
- 新增 `SysConfigService#addRecvThirdPlatDataConfig`：
  - `cnf_key` 使用 `appId`（非常量）。
  - `channelName` 必填，自动 trim。
  - 调用 `BizConfigService.generateChannelDictByNames` 自动创建渠道。
  - 通过 `BizDictConfigTypeEnum.DATA_CHANNEL` 获取 `channelId` 并写回 `RecvThirdPlatDataConfig`。
- 新增 `SysConfigService#getRecvThirdPlatDataConfig`：
  - 按 `cnf_type` + `appId` 查询并解析 `RecvThirdPlatDataConfig`。
- 新增 RSA 验签与私钥解密 AES Key 的工具类，配合现有 `AesUtils` 完成解密。

### 6.3 接口实现
- 新增 `RecvThirdPlatDataController`，路径：`/api/public/third`，均不做登录认证。
- 撞库准入接口：`POST /api/public/third/access-check`
  - 验签 → 解密 → 解析 `phoneMd5` → 查询 `customer_info_item.mobile_md5`。
  - 存在返回 `result=0` + `reason=撞库`，否则 `result=1`。
  - 记录 `CustPushRecordDO`（先保存再补充明文/状态/响应日志）。
- 申请授信接口：`POST /api/public/third/apply-credit`
  - 验签 → 解密 → 解析 `orderNo/userName/idNo/mobile`。
  - 调用 `ImportCustDataService.addCustInfo`，目标部门/用户均为 `0L`。
  - 记录 `CustPushRecordDO`（先保存再补充明文/状态/响应日志）。
- 统一响应结构 `RecvThirdPlatCommonResponse`，返回码使用 `ThirdPlatResultCode`。

### 6.4 日志与数据记录
- 接收到请求后：打印 info 日志（通用请求 JSON + 解密后明文）。
- 返回响应前：打印 info 日志（通用响应 JSON）。
- `CustPushRecordDO` 记录：
  - `received_req` 保存解密后的明文请求。
  - `remark` 保存响应 JSON。
  - `type` 区分 0(撞库)/1(进件)。
  - `existed_flag` 记录撞库结果。

### 6.5 单元测试
- 新增测试
  - `silver-union-ops/src/test/java/com/jhl/silver/union/biz/service/RecvThirdPlatDataControllerTest.java`
  - `silver-union-ops/src/test/java/com/jhl/silver/union/biz/service/SysConfigServiceImplTest.java`
- 测试策略
  - RSA/AES 采用本地生成密钥对进行验签/解密验证。
  - `ImportCustDataService` 等依赖使用 Mockito mock。

### 6.6 测试执行记录
- 执行命令：
  - `./mvnw -pl silver-union-ops -am -Dtest=RecvThirdPlatDataControllerTest,SysConfigServiceImplTest -Dsurefire.failIfNoSpecifiedTests=false test`
- 结果：通过。
- 说明：编译过程存在既有 `MockBean` 过时警告，不影响本次测试结论。
