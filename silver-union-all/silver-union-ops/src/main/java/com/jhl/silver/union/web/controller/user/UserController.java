package com.jhl.silver.union.web.controller.user;

import com.jhl.silver.union.biz.common.ResultCode;
import com.jhl.silver.union.biz.common.enums.UserAuthRoleEnum;
import com.jhl.silver.union.biz.common.utils.RoleUtils;
import com.jhl.silver.union.biz.data.DeptQry;
import com.jhl.silver.union.biz.data.UserContext;
import com.jhl.silver.union.biz.dept.service.DeptService;
import com.jhl.silver.union.biz.user.service.UserService;
import com.jhl.silver.union.commons.CommonConstant;
import com.jhl.silver.union.commons.SuResult;
import com.jhl.silver.union.commons.exception.BizException;
import com.jhl.silver.union.commons.utils.OtherUtils;
import com.jhl.silver.union.commons.utils.SuResultUtils;
import com.jhl.silver.union.web.data.DeptNameItemInfo;
import com.jhl.silver.union.web.data.user.BriefUserInfoDTO;
import com.jhl.silver.union.web.data.user.MarkOnlineStatusRequest;
import com.jhl.silver.union.web.data.user.UpdatePasswordRequest;
import com.jhl.silver.union.web.data.user.UserItemInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 用户信息类接口
 *
 * @author: qingren
 * @create_time: 2025/3/24
 */
@RestController
@Tag(name = "用户信息类接口")
@RequestMapping("/user")
@Slf4j
public class UserController {

    @Resource
    private UserService userService;
    @Resource
    private DeptService deptService;

    @GetMapping("/info")
    @Operation(summary = "获取当前登录的用户信息")
    public SuResult<BriefUserInfoDTO> getUserInfo(Authentication authentication) {
        // log.debug("=============> authentication: {}", authentication.getAuthorities());
        BriefUserInfoDTO briefUserInfoDTO = userService.getUserInfo(UserContext.getUserId());
        if (Objects.isNull(briefUserInfoDTO)) {
            throw new BizException(ResultCode.UNAUTHENTICATED, "找不到合法的用户。 userId: " + UserContext.getUserId());
        }
        return SuResultUtils.successResult(briefUserInfoDTO);
    }

    /**
     * 更新在线状态
     *
     * @return
     */
    @PostMapping("/online-status")
    @Operation(summary = "更新在线状态")
    public SuResult<BriefUserInfoDTO> markOnlineStatus(@RequestBody MarkOnlineStatusRequest request) {
        request.validate();
        userService.updateOnlineStatus(UserContext.getUserId(),
                Objects.equals(request.getOnlineStatus(), CommonConstant.YES));
        BriefUserInfoDTO briefUserInfoDTO = userService.getUserInfo(UserContext.getUserId());
        return SuResultUtils.successResult(briefUserInfoDTO);
    }

    /**
     * 获取用户所属部门以及下属部门当前在线的用户简要信息
     *
     * @return
     */
    @GetMapping("/list-team-user")
    @Operation(summary = "获取用户所属部门以及下属部门当前在线的用户简要信息",
               description = "要求拥有对应的权限，返回当前处于正常状态的用户信息")
    public SuResult<List<UserItemInfo>> getTeamUserList(
            @RequestParam(name = "ol-only", required = false) Integer onlineOnly) {
        List<Long> deptIds = null;
        Set<UserAuthRoleEnum> roles = UserContext.getRoles();
        List<UserItemInfo> list;
        onlineOnly = OtherUtils.defaultIfNull(onlineOnly, CommonConstant.YES);
        if (RoleUtils.nonBizDataAdmin(roles)) {
            // 无数据管理权限直接取自身
            list = userService.listUserItemByDeptIds(List.of(UserContext.getDeptId()), UserContext.getUserId(),
                    Objects.equals(onlineOnly, CommonConstant.YES));
            return SuResultUtils.successResult(list);
        }

        if (RoleUtils.hasAny(roles, UserAuthRoleEnum.ROLE_SUPPER)) {
            // 超管不设限
            deptIds = List.of();
        } else {
            deptIds = deptService.getAllChildrenIdByParentDeptId(UserContext.getDeptId(), roles);
        }

        list = userService.listUserItemByDeptIds(deptIds, null, Objects.equals(onlineOnly, CommonConstant.YES));
        return SuResultUtils.successResult(list);
    }

    /**
     * 修改密码
     *
     * @param request
     * @return
     */
    @Operation(summary = "修改密码")
    @PostMapping("/upd-pwd")
    public SuResult<Void> updatePassword(@RequestBody UpdatePasswordRequest request) {
        request.validate();
        userService.updatePassword(request.getOldPassword(), request.getNewPassword(), UserContext.getUserId());
        return SuResultUtils.successResult();
    }

    /**
     * 查询登录用户的部门以及下级部门信息列表
     *
     * @return
     */
    @Operation(summary = "查询登录用户的部门以及下级部门信息列表")
    @GetMapping("/get-dept")
    public SuResult<List<DeptNameItemInfo>> getDeptInfoList() {
        Long deptId = UserContext.getDeptId();
        Set<UserAuthRoleEnum> roles = UserContext.getRoles();
        List<DeptNameItemInfo> list = null;
        if (roles.contains(UserAuthRoleEnum.ROLE_SUPPER)) {
            //直接查询所有部门信息
            DeptQry qry = new DeptQry()
                    .setDeleteFlag((long) CommonConstant.NO);
            list = deptService.listByDeptInfo(qry, false)
                    .stream()
                    .map(e -> DeptNameItemInfo.of(e.getId(), e.getDeptName()))
                    .collect(Collectors.toList());
            return SuResultUtils.successResult(list);
        }
        List<Long> deptIds = deptService.getAllChildrenIdByParentDeptId(deptId, roles);
        Map<Long, String> map = deptService.getDeptNameByIds(deptIds);
        list = deptIds.stream()
                .map(e -> DeptNameItemInfo.of(e, map.get(e)))
                .toList();
        return SuResultUtils.successResult(list);
    }
}
