package com.jhl.silver.union.web.filter;

import com.jhl.silver.union.biz.common.ResultCode;
import com.jhl.silver.union.biz.common.utils.TokenUtils;
import com.jhl.silver.union.biz.data.SuOauthUserInfo;
import com.jhl.silver.union.biz.data.UserContext;
import com.jhl.silver.union.biz.user.service.auth.AuthService;
import com.jhl.silver.union.biz.user.service.task.DurationOptionsAllowanceTask;
import com.jhl.silver.union.commons.CommonConstant;
import com.jhl.silver.union.commons.SuResult;
import com.jhl.silver.union.commons.exception.BizException;
import com.jhl.silver.union.commons.gson.GsonHelper;
import com.jhl.silver.union.commons.web.IpUtils;
import com.jhl.silver.union.web.handler.BizGlobalExceptionHandler;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Objects;

/**
 * @author: qingren
 * @create_time: 2025/3/19
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private AuthService authService;
    private BizGlobalExceptionHandler bizGlobalExceptionHandler;
    private String[] whiteUrlList;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public JwtAuthenticationFilter(AuthService authService, BizGlobalExceptionHandler bizGlobalExceptionHandler,
            String[] whiteUrlList) {
        this.authService = authService;
        this.bizGlobalExceptionHandler = bizGlobalExceptionHandler;
        this.whiteUrlList = whiteUrlList;
    }

    private boolean inWhiteList(String reqeustUri) {
        for (String pattern : whiteUrlList) {
            if (pathMatcher.match(pattern, reqeustUri)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        // 设置请求源 IP地址
        String ip = IpUtils.getIpFromRequest(request);
        UserContext.setClientIp(ip);
        try {
            if (inWhiteList(request.getRequestURI())) {
                filterChain.doFilter(request, response);
                return;
            }
            String token = this.getToken(request);
            String clientId = request.getHeader(CommonConstant.HEADER_CLIENT_ID);
            SuOauthUserInfo userInfo = authService.getUserInfoByJWT(token, clientId);
            if (Objects.isNull(userInfo) || Objects.isNull(userInfo.getSuUserInfoDO())) {
                throw new BizException(ResultCode.UNAUTHENTICATED, "invalid token：");
            }

            UserDetails userDetails = userInfo.getUserDetails();
            UserContext.setUserInfo(userInfo.getSuUserInfoDO());
            if (!DurationOptionsAllowanceTask.isOptionsAllowed(UserContext.getRoles(),request.getRequestURI())) {
                throw new BizException(ResultCode.DURATION_OPTS_FORBIDDEN, "ip: " + ip);
            }
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            ResponseEntity<SuResult<Void>> re = bizGlobalExceptionHandler.handlerAllException(request, e);
            response.setStatus(re.getStatusCode().value());
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(GsonHelper.toJson(re.getBody(), true));
        } finally {
            UserContext.clear();
        }
    }

    private String getToken(HttpServletRequest request) {
        String header = request.getHeader(CommonConstant.HEADER_AUTHORIZATION);
        if (StringUtils.isBlank(header) || !header.startsWith(CommonConstant.AUTHORIZATION_PREFIX)) {
            return null;
        }
        String token = TokenUtils.getJwtToken(header);
        if (StringUtils.isBlank(token)) {
            throw new BizException(ResultCode.UNAUTHENTICATED);
        }
        return token;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        return this.inWhiteList(request.getRequestURI());
    }
}
