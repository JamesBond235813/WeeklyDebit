package com.jhl.silver.union.web.controller.admin;

import com.github.pagehelper.PageInfo;
import com.jhl.silver.union.biz.common.enums.UserAuthRoleEnum;
import com.jhl.silver.union.biz.data.UserContext;
import com.jhl.silver.union.biz.user.service.UserService;
import com.jhl.silver.union.biz.user.service.auth.AuthService;
import com.jhl.silver.union.commons.SuResult;
import com.jhl.silver.union.commons.utils.SuResultUtils;
import com.jhl.silver.union.web.data.RoleItemInfo;
import com.jhl.silver.union.web.data.UserInfoDTO;
import com.jhl.silver.union.web.data.admin.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author: qingren
 * @create_time: 2025/3/20
 */
@RestController
@RequestMapping("/sys/user")
@Tag(name = "系统管理类接口: 用户管理")
public class UserMngController {

    @Resource
    private UserService userService;
    @Resource
    private AuthService authService;

    /**
     * 指定用户退出登录
     *
     * @param userId
     * @return
     */
    @GetMapping(value = "/revoke-by-uid")
    @Operation(summary = "指定用户退出登录", description = "指定用户退出登录")
    public SuResult<Void> revokeByUserId(
            @Parameter(description = "用户ID", required = true)
            @RequestParam(value = "userId") Long userId) {

        if (userId < 1L) {
            return SuResultUtils.successResult();
        }
        authService.revokeByUserId(userId);
        return SuResultUtils.successResult();
    }

    /**
     * 获取权限角色信息列表
     *
     * @return
     */
    @GetMapping("/list-all-roles")
    @Operation(summary = "获取权限角色信息列表")
    public SuResult<List<RoleItemInfo>> listAllRoles() {
        return SuResultUtils.successResult(UserAuthRoleEnum.toRoleItemInfoList());
    }

    /**
     * 分页查询用户信息列表
     *
     * @param request
     * @return
     */
    @PostMapping("/page-list-user-info")
    @Operation(summary = "分页查询用户信息列表")
    public SuResult<PageInfo<UserInfoDTO>> pagedListUserInfo(@RequestBody PagedListUserInfoRequest request) {
        request.autoFix();
        PageInfo<UserInfoDTO> pageInfo = userService.pageListUserInfo(request, UserContext.getUserId());
        return SuResultUtils.successResult(pageInfo);
    }

    /**
     * 统计当前用户可管理范围内的在线用户数
     *
     * @return
     */
    @GetMapping("/online-count")
    @Operation(summary = "统计当前用户可管理范围内的在线用户数")
    public SuResult<Long> countOnlineUsers() {
        return SuResultUtils.successResult(userService.countManageableOnlineUsers(UserContext.getUserId()));
    }

    /**
     * 新增用户信息
     *
     * @param request
     * @return
     */
    @PostMapping("/add-user-info")
    @Operation(summary = "新增用户信息")
    public SuResult<Void> addUserInfo(@RequestBody AddUserInfoRequest request) {
        userService.addUserInfo(request, UserContext.getUserId());
        return SuResultUtils.successResult();
    }

    /**
     * 更新用户信息
     *
     * @param request
     * @return
     */
    @PostMapping("/upd-user-info")
    @Operation(summary = "更新用户信息")
    public SuResult<Void> updateUserInfo(@RequestBody UpdateUserInfoRequest request) {
        userService.updateUserInfo(request, UserContext.getUserId());
        return SuResultUtils.successResult();
    }

    /**
     * 删除用户信息
     *
     * @param request
     * @return
     */
    @PostMapping("/del-user-info")
    @Operation(summary = "删除用户信息")
    public SuResult<Void> deleteUserInfo(@RequestBody DeleteUserInfoRequest request) {
        request.validate();
        userService.deleteUserInfo(request.getId(), UserContext.getUserId());
        return SuResultUtils.successResult();
    }

    /**
     * 重置用户密码
     *
     * @param request
     * @return
     */
    @Operation(summary = "重置用户密码")
    @PostMapping("/reset-user-pswd")
    public SuResult<Void> resetPassword(@RequestBody ResetPasswordRequest request) {
        request.validate();
        userService.resetPassword(request.getId(), request.getPassword(), UserContext.getUserId());
        return SuResultUtils.successResult();
    }

}
