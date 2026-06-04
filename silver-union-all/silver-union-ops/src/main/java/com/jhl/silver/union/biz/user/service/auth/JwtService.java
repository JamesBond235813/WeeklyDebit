package com.jhl.silver.union.biz.user.service.auth;

import com.jhl.silver.union.web.data.user.JwtUserInfo;

/**
 * @author: qingren
 * @create_time: 2025/3/19
 */
public interface JwtService {
    /**
     * 生成 JWT token
     *
     * @param loginUser 用户登录信息
     * @return
     */
    String generateToken(JwtUserInfo loginUser);

    /**
     * 从 jwtToken 中解析 userName
     *
     * @param jwtToken
     * @return
     */
    JwtUserInfo extractJwtUserInfo(String jwtToken);

    /**
     * 验证 jwt 是合法性，并解析 JWT 中的数据。
     *
     * @param jwtToken
     * @return 若解析成功， 则返回 jwtUserInfo 数据， 否则返回 null
     */
    JwtUserInfo validateTokenAndGetJwtUserInfo(String jwtToken);

    /**
     * 刷新JWT token
     *
     * @param jwtToken
     * @return
     */
    String refreshJwtToken(String jwtToken);
}
