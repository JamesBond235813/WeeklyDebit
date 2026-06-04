package com.jhl.silver.union.web.data.user;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author: qingren
 * @create_time: 2025/3/19
 */
@Data
@Accessors(chain = true)
public class JwtUserInfo {
    /**
     * 用户名（登录账号）
     */
    private String userName;
    /**
     * 用户登录ID
     */
    private Long loginTraceId;
    /**
     * 用户 ID
     */
    private Long userId;

    /**
     * 刷新JWT 的令牌
     */
    private String refreshToken;

    public static JwtUserInfo of(String userName, Long userId,Long loginTraceId, String refreshToken) {
        return new JwtUserInfo()
                .setUserName(userName)
                .setLoginTraceId(loginTraceId)
                .setUserId(userId)
                .setRefreshToken(refreshToken);
    }
}
