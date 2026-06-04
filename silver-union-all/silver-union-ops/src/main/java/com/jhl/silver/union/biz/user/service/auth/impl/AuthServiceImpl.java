package com.jhl.silver.union.biz.user.service.auth.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.google.common.collect.Lists;
import com.google.gson.reflect.TypeToken;
import com.jhl.silver.union.biz.common.BizConstance;
import com.jhl.silver.union.biz.common.ResultCode;
import com.jhl.silver.union.biz.common.enums.JwtStatusEnum;
import com.jhl.silver.union.biz.common.enums.LoginResultEnum;
import com.jhl.silver.union.biz.config.BizProperty;
import com.jhl.silver.union.biz.data.SuOauthUserInfo;
import com.jhl.silver.union.biz.data.UserContext;
import com.jhl.silver.union.biz.user.dal.entity.SuUserInfoDO;
import com.jhl.silver.union.biz.user.dal.entity.SuUserLoginTraceDO;
import com.jhl.silver.union.biz.user.manager.SuUserInfoManager;
import com.jhl.silver.union.biz.user.manager.SuUserLoginTraceManager;
import com.jhl.silver.union.biz.user.service.UserService;
import com.jhl.silver.union.biz.user.service.auth.AuthService;
import com.jhl.silver.union.biz.user.service.auth.JwtService;
import com.jhl.silver.union.biz.user.service.task.DurationOptionsAllowanceTask;
import com.jhl.silver.union.commons.CommonResultCode;
import com.jhl.silver.union.commons.db.TransactionUtils;
import com.jhl.silver.union.commons.exception.BizException;
import com.jhl.silver.union.commons.exception.ExceptionLogPrinter;
import com.jhl.silver.union.commons.gson.GsonHelper;
import com.jhl.silver.union.commons.utils.SuDateUtils;
import com.jhl.silver.union.web.data.user.JwtUserInfo;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.CollectionUtils;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author: qingren
 * @create_time: 2025/3/19
 */
@Service
@Slf4j
public class AuthServiceImpl implements AuthService {
    @Resource
    private JwtService jwtService;
    @Resource
    private BizProperty bizProperty;
    @Resource
    private UserService userService;
    @Resource
    private SuUserLoginTraceManager loginTraceManager;
    @Resource
    private SuUserInfoManager userInfoManager;
    @Resource
    private BCryptPasswordEncoder passwordEncoder;
    @Resource
    private TransactionTemplate transactionTemplate;

    @Override
    public String postJwtTokenBy(String userName, String password, String clientId, HttpServletResponse response) {
        if (!StringUtils.equals(bizProperty.getClientId(), clientId)) {
            throw new BizException(CommonResultCode.INVALID_REQUEST, "client id is invalid: " + clientId);
        }
        // TODO: 待追加多次登录失败锁定 后续再做

        SuUserInfoDO user = userInfoManager.getValidUserInfoByUsernameCacheFirst(userName);
        if (Objects.isNull(user)) {
            this.saveLoginTrace(userName, null, clientId, LoginResultEnum.FAILED,
                    ResultCode.LOGIN_USER_NOT_EXISTS.name());
            throw new BizException(ResultCode.LOGIN_USER_NOT_EXISTS, "用户不存在或已被注销: " + userName);
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            this.saveLoginTrace(user.getUserName(), user.getId(), clientId, LoginResultEnum.FAILED,
                    ResultCode.USER_OR_PASSWORD_INVALID.name());
            throw new BizException(ResultCode.USER_OR_PASSWORD_INVALID, "密码不正确");
        }
        if (!DurationOptionsAllowanceTask.isOptionsAllowed(user.getRoles())) {
            throw new BizException(ResultCode.DURATION_OPTS_FORBIDDEN, "userId: " + user.getId());
        }

        List<SuUserLoginTraceDO> innerList = Lists.newArrayList();
        // 登录成功，记录日志
        TransactionUtils.executeTransaction(transactionTemplate, () -> {
            // 踢掉原来已经登录的 JWT
            this.revokeEffectedJWT(userName);
            SuUserLoginTraceDO traceInfo = this.saveLoginTrace(user.getUserName(), user.getId(), clientId,
                    LoginResultEnum.SUCC,
                    StringUtils.EMPTY);
            innerList.add(traceInfo);
        });
        SuUserLoginTraceDO traceInfo = innerList.get(0);
        JwtUserInfo jwtUserInfo = JwtUserInfo.of(user.getUserName(), user.getId(), traceInfo.getId(),
                traceInfo.getRefreshToken());
        this.setupRefreshTokenCookie(traceInfo.getRefreshToken(), response);
        // 生成日志
        return jwtService.generateToken(jwtUserInfo);
    }

    public void setupRefreshTokenCookie(String refreshToken, HttpServletResponse response) {
        if (Objects.isNull(response)) {
            return;
        }
        ResponseCookie cookie = ResponseCookie.from(BizConstance.REFRESH_TOKEN_COOKIE_NAME, refreshToken)
                .httpOnly(true)
                .path("/")
                .maxAge(Duration.of(90, ChronoUnit.MINUTES))
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    /**
     * 注销生效中的 JWT
     *
     * @param userName
     */
    private void revokeEffectedJWT(String userName) {
        LambdaQueryWrapper<SuUserLoginTraceDO> qw = new LambdaQueryWrapper<>();
        qw.eq(SuUserLoginTraceDO::getUserName, userName).eq(SuUserLoginTraceDO::getJwtStatus, JwtStatusEnum.OK.name())
                .ge(SuUserLoginTraceDO::getJwtExpiredAt, new Date())
                .eq(SuUserLoginTraceDO::getLoginResult, LoginResultEnum.SUCC.name());
        List<SuUserLoginTraceDO> list = loginTraceManager.list(qw);
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        List<Long> traceIds = list.stream().map(SuUserLoginTraceDO::getId).collect(Collectors.toList());
        LambdaUpdateWrapper<SuUserLoginTraceDO> uw = new LambdaUpdateWrapper<>();
        uw.set(SuUserLoginTraceDO::getJwtStatus, JwtStatusEnum.REPLACED_REVOKE.name())
                .in(SuUserLoginTraceDO::getId, traceIds);
        loginTraceManager.update(uw);
        loginTraceManager.clearCacheBy();
    }

    /**
     * 保存登录日志信息
     *
     * @param username
     * @param userId
     * @param clientId
     * @param loginResultEnum
     * @return
     */
    private SuUserLoginTraceDO saveLoginTrace(String username, Long userId, String clientId,
            LoginResultEnum loginResultEnum, String remark) {
        Date now = new Date();
        SuUserLoginTraceDO trace = new SuUserLoginTraceDO().setUserId(userId).setUserName(username)
                .setLoginIp(UserContext.getClientIp())
                .setLoginDate(SuDateUtils.format(now, SuDateUtils.DF_YYYY_MM_DD)).setClientId(clientId)
                .setLoginResult(loginResultEnum.name()).setLoginRemark(remark);
        switch (loginResultEnum) {
            case SUCC -> {
                Date refreshExpireAt = SuDateUtils.add(now, Calendar.SECOND,
                        bizProperty.getJwtRefreshTokenExpireSeconds());
                Date jwtExpireAt = SuDateUtils.add(now, Calendar.SECOND, bizProperty.getJwtExpireSeconds());
                String refreshToken = UUID.randomUUID().toString();
                trace.setRefreshToken(refreshToken).setRefreshTokenExpiredAt(refreshExpireAt)
                        .setJwtExpiredAt(jwtExpireAt).setJwtStatus(JwtStatusEnum.OK.name());
            }
            case FAILED -> {
                // Do nothing for failed login in terms of token generation
            }
        }
        loginTraceManager.save(trace);
        loginTraceManager.clearCacheBy(trace.getId());
        return trace;
    }

    @Override
    public SuOauthUserInfo getUserInfoByJWT(String token, String clientId) {
        JwtUserInfo jwtUserInfo = jwtService.validateTokenAndGetJwtUserInfo(token);
        if (Objects.isNull(jwtUserInfo)) {
            // 此处可能为 token 过期了
            throw new BizException(ResultCode.UNAUTHENTICATED);
        }
        // 解析 token 成功后， 需要看这个 token 是否有进黑名单
        SuUserLoginTraceDO loginTrace = loginTraceManager.getById(jwtUserInfo.getLoginTraceId(), true);
        if (Objects.isNull(loginTrace)) {
            throw new BizException(ResultCode.UNAUTHENTICATED,
                    "loginTrace is null. login trace id:" + jwtUserInfo.getLoginTraceId());
        }
        JwtStatusEnum statusEnum = JwtStatusEnum.findByName(loginTrace.getJwtStatus());
        switch (statusEnum) {
            case OK -> {
            }
            case REPLACED_REVOKE -> throw new BizException(ResultCode.UNAUTHENTICATED_BY_ANTHER_AUTH,
                    "login trace id:" + jwtUserInfo.getLoginTraceId());

            default -> throw new BizException(ResultCode.UNAUTHENTICATED,
                    "login trace id:" + jwtUserInfo.getLoginTraceId());
        }

        SuUserInfoDO userInfoDO = userInfoManager.getValidUserInfoByUsernameCacheFirst(jwtUserInfo.getUserName());
        if (Objects.isNull(userInfoDO)) {
            throw new BizException(ResultCode.UNAUTHENTICATED, "userInfo is null. JWT: " + token);
        }
        Set<SimpleGrantedAuthority> authorities = this.makeupWith(userInfoDO.getRoles());
        SuOauthUserInfo info = new SuOauthUserInfo(userInfoDO, authorities);
        return info;
    }

    private Set<SimpleGrantedAuthority> makeupWith(String roleStr) {
        if (StringUtils.isBlank(roleStr)) {
            return Set.of();
        }
        Set<String> roles = GsonHelper.fromJson(roleStr, new TypeToken<Set<String>>() {
        }.getType());
        if (CollectionUtils.isEmpty(roles)) {
            return Set.of();
        }
        return roles.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toSet());
    }

    @Override
    public void revokeToken(String token) {
        if (StringUtils.isBlank(token)) {
            // do nothing
            return;
        }
        try {
            JwtUserInfo jwtUserInfo = jwtService.extractJwtUserInfo(token);
            Optional.ofNullable(jwtUserInfo).map(JwtUserInfo::getLoginTraceId).ifPresent(loginTraceId -> {
                SuUserLoginTraceDO upd = new SuUserLoginTraceDO().setId(loginTraceId)
                        .setJwtStatus(JwtStatusEnum.REVOKE.name());
                loginTraceManager.updateById(upd);
                loginTraceManager.clearCacheBy(loginTraceId);
            });

        } catch (Exception e) {
            ExceptionLogPrinter.printExceptionLog(e, log);
        }

    }

    @Override
    public void revokeByUserId(Long userId) {
        if (Objects.isNull(userId)) {
            return;
        }
        List<SuUserLoginTraceDO> list = loginTraceManager.getByUserId(userId, JwtStatusEnum.OK, true);
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        List<SuUserLoginTraceDO> updList = list.stream()
                .map(e -> new SuUserLoginTraceDO().setId(e.getId()).setJwtStatus(JwtStatusEnum.FORCE_REVOKE.name())
                        .setLoginRemark("用户被动下线。 操作人UID: " + UserContext.getUserInfo().getId() + ": "
                                + UserContext.getUserInfo().getUserName()))
                .collect(Collectors.toList());
        loginTraceManager.updateBatchById(updList);
        List<Long> idList = updList.stream()
                .map(SuUserLoginTraceDO::getId)
                .collect(Collectors.toList());
        loginTraceManager.clearCacheBy(idList);
    }
}
