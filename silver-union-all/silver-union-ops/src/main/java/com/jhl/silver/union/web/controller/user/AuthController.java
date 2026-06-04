package com.jhl.silver.union.web.controller.user;

import com.jhl.silver.union.biz.common.utils.TokenUtils;

import com.jhl.silver.union.biz.user.service.UserService;
import com.jhl.silver.union.biz.user.service.auth.AuthService;
import com.jhl.silver.union.commons.CommonConstant;
import com.jhl.silver.union.commons.SuResult;
import com.jhl.silver.union.commons.utils.SuResultUtils;
import com.jhl.silver.union.web.data.user.EntUserLoginRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

/**
 * 用户认证类接口
 *
 * @author: qingren
 * @create_time: 2025/3/17
 */
@RestController
@Tag(name = "用户认证类接口")
@RequestMapping("/auth")
public class AuthController {

    @Resource
    private UserService userService;
    @Resource
    private AuthService authService;

    /**
     * 用于企业后端登录
     *
     * @param request
     * @return
     */
    @PostMapping(value = "/login")
    @Operation(summary = "用于管理平台端登录,返回 JWT")
    SuResult<String> entLogin(@RequestBody EntUserLoginRequest request,
            @RequestHeader(name = CommonConstant.HEADER_CLIENT_ID, required = false) String clientId,
            HttpServletResponse response) {
        request.validate();
        String token = authService.postJwtTokenBy(request.getUsername(), request.getPassword(), clientId, response);

        return SuResultUtils.successResult(token);
    }

    /**
     * 退出登录。
     *
     * @param tokenValue
     * @return
     */
    @GetMapping(value = "/revoke")
    @Operation(summary = "退出登录", description = "退出登录。本接口永远返回成功")
    SuResult<Void> revoke(
            @RequestHeader(name = CommonConstant.HEADER_AUTHORIZATION, required = false) String tokenValue) {
        String token = TokenUtils.getJwtToken(tokenValue);
        if (StringUtils.isBlank(token)) {
            return SuResultUtils.successResult();
        }
        try {
            authService.revokeToken(token);
        } catch (Exception e) {
            // do nothing
        }
        return SuResultUtils.successResult();
    }

}
