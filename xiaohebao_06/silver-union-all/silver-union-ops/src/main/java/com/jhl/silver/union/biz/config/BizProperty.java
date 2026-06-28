package com.jhl.silver.union.biz.config;

import lombok.Data;

import java.io.File;

/**
 * @author: qingren
 * @create_time: 2025/3/19
 */
@Data
public class BizProperty {
    /**
     *
     */
    private String clientId = "su-ops";

    private String jwtKeySeed = "a$nj8@!h&8w94EqB2hSjN*d$gw$$4tmb^3%^!$rpSh#SS2x&URKGSES*EbyY&k5y";

    /**
     * jwt 有效时长， 单位秒
     */
    private Integer jwtExpireSeconds = 3600 * 8;

    /**
     * JWT refresh token 有效时长， 单位秒
     */
    private Integer jwtRefreshTokenExpireSeconds = 3600 * 9;

    /**
     * 允许登录的开始时间 HH:mm:ss 格式
     */
    private String allowLoginTimeBegin = "08:00:00";

    /**
     * 允许登录的截止时间 HH:mm:ss 格式
     */
    private String allowLoginTimeEnd = "22:59:59";

    /**
     * 是否开启时间登录限制
     */
    private boolean enableLoginTimeLimit = false;

    /**
     * 上传文件临时保存的路径
     */
    private String uploadTmpDir = "/tmp";

    /**
     * 导入客户时，过滤出来重复客户信息的文件保存路径
     */
    private String duplicatedCustFileDir = System.getProperty("user.home") + File.separator + "duplicate_cust";

    /**
     * 导入客户信息模板文件存放的路径
     */
    private String importCustTemplateFilePath = "./客户信息模板1.0.xlsx";

    /**
     * 额外约定的令牌信息
     */
    private String extraToken;

    /**
     * 外部推送客户信息的AES密钥（16/24/32字节）
     */
    private String pushCustAesKey;

    /**
     * 飞书机器人 webhook。为空时不发送上游新增客户群通知。
     */
    private String feishuBotWebhook;

    /**
     * 进入公海专用飞书机器人 webhook。为空时不发送进入公海通知。
     */
    private String feishuPublicPoolBotWebhook;

    /**
     * HYY 存量客户撞库服务地址。为空时不调用远程撞库库。
     */
    private String hyyCollisionServiceUrl;

    /**
     * HYY 存量客户撞库服务访问令牌。
     */
    private String hyyCollisionServiceToken;

    /**
     * HYY 存量客户撞库服务请求超时时间，单位毫秒。
     */
    private Integer hyyCollisionServiceTimeoutMs = 1000;

}
