package com.jhl.silver.union.biz.user.service.auth.impl;

import com.jhl.silver.union.biz.config.BizProperty;
import com.jhl.silver.union.biz.user.service.auth.JwtService;
import com.jhl.silver.union.commons.gson.GsonHelper;
import com.jhl.silver.union.commons.utils.VerifyUtils;
import com.jhl.silver.union.web.data.user.JwtUserInfo;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Objects;

/**
 * @author: qingren
 * @create_time: 2025/3/19
 */
@Service
@Slf4j
public class JwtServiceImpl implements JwtService {
    @Resource
    private BizProperty bizProperty;

    @Override
    public String generateToken(JwtUserInfo loginUser) {
        VerifyUtils.notNull(loginUser, "loginUser", "loginUser should not be null", true);
        return Jwts.builder()
                .subject(GsonHelper.toJson(loginUser))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + bizProperty.getJwtExpireSeconds() * 1000L))
                .signWith(this.getSecretKey())
                .compact();
    }

    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(bizProperty.getJwtKeySeed().getBytes(StandardCharsets.UTF_8));

    }

    @Override
    public JwtUserInfo extractJwtUserInfo(String jwtToken) {

        String loginUserStr = null;
        try {
            loginUserStr = Jwts.parser()
                    .verifyWith(this.getSecretKey())
                    .build()
                    .parseSignedClaims(jwtToken)
                    .getPayload()
                    .getSubject();
        } catch (JwtException e) {
            log.info("Invalid JWT: {}: {}, token: {}", e.getClass().getSimpleName(), e.getMessage(), jwtToken);
        }
        if (StringUtils.isBlank(loginUserStr)) {
            return null;
        }
        return GsonHelper.fromJson(loginUserStr, JwtUserInfo.class);
    }

    @Override
    public JwtUserInfo validateTokenAndGetJwtUserInfo(String jwtToken) {
        Jws<Claims> jws = null;
        try {
            jws = Jwts.parser()
                    .verifyWith(this.getSecretKey())
                    .build()
                    .parseSignedClaims(jwtToken);
        } catch (Exception e) {
            // 解析抛异常，直接视为不合法，不出日志
            log.debug("Invalid JWT: {}: {}, token: {}", e.getClass().getSimpleName(), e.getMessage(), jwtToken);
            return null;
        }
        // 判断是否过期
        Date expireAt = jws.getPayload().getExpiration();
        if (Objects.isNull(expireAt)) {
            return null;
        }
        if (!expireAt.after(new Date())) {
            return null;
        }
        String loginUserStr = jws.getPayload().getSubject();
        JwtUserInfo loginUser = null;
        try {
            loginUser = GsonHelper.fromJson(loginUserStr, JwtUserInfo.class);
        } catch (Exception e) {
            log.error("Parsing jwtUserInfo string failed: {}", loginUserStr);
        }
        return loginUser;
    }

    @Override
    public String refreshJwtToken(String jwtToken) {
        if (StringUtils.isBlank(jwtToken)) {
            return null;
        }
        JwtUserInfo userInfo = this.extractJwtUserInfo(jwtToken);
        if (userInfo != null) {
            return this.generateToken(userInfo);
        }
        return null;
    }
}
