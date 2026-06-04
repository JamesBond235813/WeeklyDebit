package com.jhl.silver.union.web.websocket;

import com.jhl.silver.union.biz.common.utils.TokenUtils;
import com.jhl.silver.union.biz.data.SuOauthUserInfo;
import com.jhl.silver.union.biz.user.service.auth.AuthService;
import com.jhl.silver.union.commons.CommonConstant;
import jakarta.annotation.Resource;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@Slf4j
public class CustNoticeHandshakeInterceptor implements HandshakeInterceptor {
    @Resource
    private AuthService authService;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
            WebSocketHandler wsHandler, Map<String, Object> attributes) {
        String token = extractToken(request);
        if (StringUtils.isBlank(token)) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }
        try {
            SuOauthUserInfo userInfo = authService.getUserInfoByJWT(token, null);
            if (userInfo == null || userInfo.getSuUserInfoDO() == null) {
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                return false;
            }
            attributes.put("userId", userInfo.getSuUserInfoDO().getId());
            return true;
        } catch (Exception e) {
            log.warn("WS handshake auth failed: {}", e.getMessage());
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler,
            Exception exception) {
    }

    private String extractToken(ServerHttpRequest request) {
        String auth = request.getHeaders().getFirst(CommonConstant.HEADER_AUTHORIZATION);
        String token = TokenUtils.getJwtToken(auth);
        if (StringUtils.isNotBlank(token)) {
            return token;
        }
        String raw = UriComponentsBuilder.fromUri(request.getURI()).build().getQueryParams().getFirst("token");
        if (StringUtils.isBlank(raw)) {
            return null;
        }
        if (raw.startsWith(CommonConstant.AUTHORIZATION_PREFIX)) {
            return raw.substring(CommonConstant.AUTHORIZATION_PREFIX.length());
        }
        return raw;
    }
}
