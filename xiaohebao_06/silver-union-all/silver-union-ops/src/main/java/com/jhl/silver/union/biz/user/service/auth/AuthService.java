package com.jhl.silver.union.biz.user.service.auth;

import com.jhl.silver.union.biz.data.SuOauthUserInfo;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 用户认证相关服务
 *
 * @author: qingren
 * @create_time: 2025/3/19
 */
public interface AuthService {

    /**
     * 通过用户名密码以及客户端 ID 颁发登录后的准入 token
     *
     * @param userName
     * @param password
     * @param clientId
     * @param response
     * @return
     */
    String postJwtTokenBy(String userName, String password, String clientId, HttpServletResponse response);

    /**
     * 验证 JWT 并获取登录的用户信息
     *
     * @param token
     * @return
     */
    SuOauthUserInfo getUserInfoByJWT(String token, String clientId);

    /**
     * 吊销 JWT
     *
     * @param token
     */
    void revokeToken(String token);

    /**
     * 吊销 指定用户的所有 JWT
     * @param userId
     */
    void revokeByUserId(Long userId);
}
