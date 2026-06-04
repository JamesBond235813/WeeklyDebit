package com.jhl.silver.union.biz.common.utils;

import com.jhl.silver.union.commons.CommonConstant;
import org.apache.commons.lang3.StringUtils;

/**
 * @author: qingren
 * @create_time: 2025/3/20
 */
public abstract class TokenUtils {

    /**
     * 从 authorization 中取 JWT
     * @param authorization
     * @return
     */
    public static String getJwtToken(String authorization) {
        if (StringUtils.isBlank(authorization)) {
            return StringUtils.EMPTY;
        }
        if (authorization.startsWith(CommonConstant.AUTHORIZATION_PREFIX)) {
            return authorization.substring(CommonConstant.AUTHORIZATION_PREFIX.length());
        }
        return StringUtils.EMPTY;
    }
}
