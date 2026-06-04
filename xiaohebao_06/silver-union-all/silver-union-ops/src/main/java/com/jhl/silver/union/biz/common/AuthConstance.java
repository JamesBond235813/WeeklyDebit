package com.jhl.silver.union.biz.common;

/**
 * 认证业务常量类
 *
 * @author: qingren
 * @create_time: 2020/8/2
 */
public abstract class AuthConstance {
    /**
     * 未关联平台用户的默认字段
     */
    public static final Long NONE_USER_ID = 0L;
    /**
     * 密码字段
     */
    public static final String PASSWORD = "password";

    /**
     * 用户名字段
     */
    public static final String USERNAME = "username";
    /**
     * refresh_token 字段
     */
    public static final String REFRESH_TOKEN = "refresh_token";

    /**
     * code 字段
     */
    public static final String CODE = "code";
    /**
     * grant type : refresh_token
     */
    public static final String GRANT_TYPE_REFRESH_TOKEN = "refresh_token";

    /**
     * token
     */
    public static final String JING_ADDITIONAL_UID_FIELD = "jing_uid";

    /**
     * 为access token 自动续期的锁前缀
     */
    public static final String LOCK_PREFIX_RENEWAL = "ATK_RENEWAL";

    /**
     * redis 中ACCESS token信息前缀
     */
    public static final String REDIS_TOKEN_PREFIX = "JU_AC_TK_";
    /**
     * redis refresh token信息前缀 (预留)
     */
    public static final String REDIS_REFRESH_TOKEN_PREFIX = "JU_RF_TK_";

    /**
     * redis authentication 信息前缀
     */
    public static final String REDIS_AUTHENTICATION_PREFIX = "JU_AUTHN_";

    /**
     * redis client detail 信息前缀
     */
    public static final String REDIS_CLIENT_ID_PREFIX = "JU_CLIENT_ID_";
    /**
     * redis 通过authentication 查询 access token 的key 前缀
     */
    public static final String REDIS_AUTHENTICATION_TOKEN_KEY_PREFIX = "JU_AUTH_TOKEN_KEY_";

    /**
     * client detail 信息缓存有效期 单位秒
     */
    public static final int CLIENT_DETAIL_TIMEOUT = 3600 * 10;

    /**
     * 用户ID前缀<br>
     * 在access_token表中为username 为user_id
     */
    public static final String USERNAME_UID_PREFIX = "UID:#:";

    /**
     * 手机号国家代码： 中国
     */
    public static final String COUNTRY_CODE_CHINA = "86";

}
