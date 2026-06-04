package com.jhl.silver.union.biz.common;

/**
 * @author: qingren
 * @create_time: 2025/3/20
 */
public abstract class BizConstance {
    public static long NOT_DELETED = 0L;
    /**
     * 根节点的父节点。 用于占位，无业务意义
     */
    public static long SPEC_ROOT_PARENT_ID = -1L;

    /**
     * 不存在的部门 ID
     */
    public static long NONE_DEPT_ID = -99L;

    /**
     * http only cookie , 用于存放 refresh token  的cookie 名称
     */
    public static final String REFRESH_TOKEN_COOKIE_NAME = "cn_refresh_token";
}
