package com.jhl.silver.union.biz.config;

import lombok.Data;

/**
 * 登录失败锁定配置信息
 */
@Data
public class LoginLockConfig {

    /**
     * 最大失败次数
     */
    int maxFailNum = 5;

    /**
     *
     */
    int lockExpire = 60;

    /**
     * 操作时间，按当前时间前移 period 分钟，此期间内失败 maxFailNum，即锁定用户， lockExpire 分钟之后解锁
     */
    int period = 60;

    /**
     * 短信验证码长度
     */
    Integer accountCancelCodeLength;

    /**
     * 注销账户短信验证码有效期
     */
    Integer accountCancelExpireMinute;

    public String getTip() {
        return String.format("您如果在%d分钟内失败%d次，账号将被锁定%d分钟", period, maxFailNum, lockExpire);
    }

    public String getLockTip() {
        return String.format("您在%d分钟内失败%d次，账号已被锁定%d分钟，请稍后重试", period, maxFailNum, lockExpire);
    }

}
