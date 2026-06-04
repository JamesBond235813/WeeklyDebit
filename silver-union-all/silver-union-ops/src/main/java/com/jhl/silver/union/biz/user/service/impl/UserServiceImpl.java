package com.jhl.silver.union.biz.user.service.impl;

import com.github.pagehelper.PageInfo;
import com.google.common.collect.Sets;
import com.jhl.silver.union.biz.common.ResultCode;
import com.jhl.silver.union.biz.common.enums.UserAuthRoleEnum;
import com.jhl.silver.union.biz.common.utils.BizHelper;
import com.jhl.silver.union.biz.data.DeptInfo;
import com.jhl.silver.union.biz.data.UserQry;
import com.jhl.silver.union.biz.data.convert.UserInfoConvert;
import com.jhl.silver.union.biz.dept.manager.SuOrgDepartmentInfoManager;
import com.jhl.silver.union.biz.dept.service.DeptService;
import com.jhl.silver.union.biz.user.dal.entity.SuUserInfoDO;
import com.jhl.silver.union.biz.user.manager.SuUserInfoManager;
import com.jhl.silver.union.biz.user.service.UserService;
import com.jhl.silver.union.commons.CommonConstant;
import com.jhl.silver.union.commons.db.PageInfoUtils;
import com.jhl.silver.union.commons.db.SuSqlUtils;
import com.jhl.silver.union.commons.exception.BizException;
import com.jhl.silver.union.commons.gson.GsonHelper;
import com.jhl.silver.union.commons.utils.OtherUtils;
import com.jhl.silver.union.commons.utils.VerifyUtils;
import com.jhl.silver.union.web.data.UserInfoDTO;
import com.jhl.silver.union.web.data.admin.AddUserInfoRequest;
import com.jhl.silver.union.web.data.admin.PagedListUserInfoRequest;
import com.jhl.silver.union.web.data.admin.UpdateUserInfoRequest;
import com.jhl.silver.union.web.data.user.BriefUserInfoDTO;
import com.jhl.silver.union.web.data.user.UserItemInfo;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author: qingren
 * @create_time: 2025/3/17
 */
@Service
@Slf4j
public class UserServiceImpl implements UserService {
    @Resource
    private UserInfoConvert convert;
    @Resource
    private SuUserInfoManager userInfoManager;

    @Resource
    private SuOrgDepartmentInfoManager deptManager;
    @Resource
    private DeptService deptService;

    @Resource
    private PasswordEncoder passwordEncoder;

    @Override
    public BriefUserInfoDTO getUserInfo(Long userId) {
        if (Objects.isNull(userId)) {
            return null;
        }
        SuUserInfoDO infoDO = userInfoManager.queryUserById(userId);
        if (Objects.isNull(infoDO)) {
            // throw new BizException(ResultCode.UNAUTHENTICATED, "找不到合法的用户。 userId: " +
            // userId);
            return null;
        }
        List<String> roles = List.of();
        if (StringUtils.isNotBlank(infoDO.getRoles())) {
            roles = GsonHelper.fromJson(infoDO.getRoles(), new com.google.gson.reflect.TypeToken<List<String>>() {
            }.getType());
        }

        BriefUserInfoDTO info = new BriefUserInfoDTO()
                .setUserId(infoDO.getId() + "")
                .setUsername(infoDO.getUserName())
                .setRealName(infoDO.getRealName())
                .setAvatar(infoDO.getHeadImg())
                .setRoles(roles)
                .setDepartmentId(infoDO.getDepartmentId())
                .setOnlineStatus(infoDO.getOnlineStatus());
        return info;
    }

    @Override
    public PageInfo<UserInfoDTO> pageListUserInfo(PagedListUserInfoRequest request) {
        if (Objects.isNull(request)) {
            return PageInfoUtils.blankPageInfo();
        }
        UserQry userQry = UserQry.makeupFrom(request);
        PageInfo<SuUserInfoDO> innerPage = SuSqlUtils.pagedListQuery(request.getPage(), request.getPageSize(), true,
                () -> userInfoManager.list(userQry.toQryWrapper(false)));
        PageInfo<UserInfoDTO> pageInfo = PageInfoUtils.copyPageInfoWithoutListFrom(innerPage);
        if (PageInfoUtils.isEmpty(innerPage)) {
            return pageInfo;
        }
        List<UserInfoDTO> list = convert.convert2UserInfoDTOList(innerPage.getList());
        pageInfo.setList(list);
        if (!Boolean.TRUE.equals(request.getNeedExtendQry())) {
            return pageInfo;
        }
        // 查询部门信息 以及 创建人用户
        Set<Long> deptIds = Sets.newHashSet();
        Set<Long> userIds = Sets.newHashSet();
        list.stream().forEach(u -> {
            OtherUtils.processIf(u.getDepartmentId() > 0L, () -> deptIds.add(u.getDepartmentId()));
            userIds.add(u.getId());
        });
        List<DeptInfo> deptInfoList = deptManager.listDeptInfoByIds(deptIds);
        Map<Long, String> deptInfoNameMap = BizHelper.makeupDeptInfoIdName(deptInfoList);
        List<SuUserInfoDO> userList = userInfoManager.listByIds(userIds);
        Map<Long/* userID */, String/* realName+username */> userNameMap = BizHelper.makeupUserNameMap(userList);
        for (UserInfoDTO userInfo : list) {
            userInfo.setCreateByName(userNameMap.getOrDefault(userInfo.getCreateBy(), CommonConstant.HYPHEN))
                    .setDepartmentName(deptInfoNameMap.getOrDefault(userInfo.getDepartmentId(), CommonConstant.HYPHEN));
            List<String> roleDispNames = OtherUtils.defaultIfNull(userInfo.getRoles(), List.<String>of())
                    .stream()
                    .map(e -> UserAuthRoleEnum.getDispNameBy(e))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            userInfo.setRoleDispNames(roleDispNames);
        }

        return pageInfo;
    }

    @Override
    public void addUserInfo(AddUserInfoRequest request, Long optUserId) {
        VerifyUtils.notNull(optUserId, "optUserId", "请指定操作人", true);
        request.validate();
        SuUserInfoDO infoDO = convert.convert2SuUserInfoDO(request);
        // 密码散列后入库
        infoDO.setPassword(passwordEncoder.encode(infoDO.getPassword()));
        infoDO.setCreateBy(optUserId);
        // 用户排重直接交给 DB
        // SuUserInfoDO existed =
        // userInfoManager.findUserByUsername(request.getUserName());
        // if (Objects.nonNull(existed)) {
        // throw new BizException(ResultCode.USERNAME_ALREADY_EXISTED, "username: " +
        // request.getUserName());
        // }
        try {
            userInfoManager.save(infoDO);
        } catch (DuplicateKeyException e) {
            throw BizException.makeupBy(ResultCode.USER_ALREADY_EXISTED,
                    List.of(request.getUserName(), request.getPhone()), "request: " + GsonHelper.toJson(request));
        }
        infoDO.setPassword(null);
        log.warn("操作人: {} 新增用户信息: {}", optUserId, GsonHelper.toJson(infoDO));
        userInfoManager.clearCacheBy(infoDO.getId(), infoDO.getUserName());
    }

    @Override
    public void updateUserInfo(UpdateUserInfoRequest request, Long optUserId) {
        VerifyUtils.notNull(optUserId, "optUserId", "请指定操作人", true);
        request.validate();
        SuUserInfoDO infoDO = convert.convert2SuUserInfoDO(request);
        infoDO.setCreateBy(optUserId);
        userInfoManager.updateById(infoDO);
        log.warn("操作人: {} 更新用户信息: {}", optUserId, GsonHelper.toJson(infoDO));
        SuUserInfoDO existed = userInfoManager.getById(request.getId());
        if (Objects.nonNull(existed)) {
            userInfoManager.clearCacheBy(existed.getId(), existed.getUserName());
        }
    }

    @Override
    public void deleteUserInfo(Long id, Long optUserId) {
        VerifyUtils.notNull(optUserId, "optUserId", "请指定操作人", true);
        if (Objects.isNull(id)) {
            return;
        }

        SuUserInfoDO existed = userInfoManager.getById(id);
        if (Objects.isNull(existed)) {
            return;
        }
        SuUserInfoDO upd = new SuUserInfoDO()
                .setId(existed.getId())
                .setDeleteFlag(System.currentTimeMillis());
        userInfoManager.updateById(upd);
        log.warn("操作人: {} 删除用户信息: {}", optUserId, GsonHelper.toJson(existed));
        userInfoManager.clearCacheBy(existed.getId(), existed.getUserName());
    }

    @Override
    public void resetPassword(Long userId, String password, Long optUserId) {
        VerifyUtils.notNull(optUserId, "optUserId", "请指定操作人", true);
        VerifyUtils.notNull(userId, "userId", "请指定重置密码的目标用户", true);
        String encodedPassword = passwordEncoder.encode(password);
        SuUserInfoDO existed = userInfoManager.getById(userId);
        if (Objects.isNull(existed)) {
            return;
        }
        SuUserInfoDO upd = new SuUserInfoDO()
                .setId(existed.getId())
                .setPassword(encodedPassword);
        userInfoManager.updateById(upd);
        log.warn("操作人: {} 重置用户{} - {}({}) 密码信息", optUserId, existed.getId(), existed.getRealName(),
                existed.getUserName());
        userInfoManager.clearCacheBy(existed.getId(), existed.getUserName());
    }

    @Override
    public void updatePassword(String oldPassword, String newPassword, Long optUserId) {
        VerifyUtils.notNull(optUserId, "optUserId", "请指定操作人", true);
        if (StringUtils.equals(oldPassword, newPassword)) {
            throw new BizException(ResultCode.SAME_PASSWORD,
                    "oldPassword: " + oldPassword + ", newPassword: " + newPassword);
        }

        SuUserInfoDO existed = userInfoManager.getById(optUserId);
        if (Objects.isNull(existed)) {
            new BizException(ResultCode.USER_NOT_EXIST, "userId: " + optUserId);
        }
        if (!passwordEncoder.matches(oldPassword, existed.getPassword())) {
            new BizException(ResultCode.OLD_PASSWORD_INVALID, "userId: " + optUserId);
        }

        String encodedPassword = passwordEncoder.encode(newPassword);
        SuUserInfoDO upd = new SuUserInfoDO()
                .setId(existed.getId())
                .setPassword(encodedPassword);
        userInfoManager.updateById(upd);
        log.warn("操作人: {} - {}({}) 修改密码完成", optUserId, existed.getId(), existed.getRealName(),
                existed.getUserName());
        userInfoManager.clearCacheBy(existed.getId(), existed.getUserName());
    }

    @Override
    public Map<Long, String> getUserRealNames(Collection<Long> userIds) {
        if (CollectionUtils.isEmpty(userIds)) {
            return Map.of();
        }
        List<SuUserInfoDO> userList = userInfoManager.listByIds(userIds);
        return userList.stream()
                .collect(Collectors.toMap(SuUserInfoDO::getId, SuUserInfoDO::getRealName));

    }

    @Override
    public String getUserRealName(Long userId) {
        SuUserInfoDO user = userInfoManager.queryUserById(userId);
        return Objects.nonNull(user) ? user.getRealName() : null;
    }

    @Override
    public void updateOnlineStatus(Long userId, boolean online) {
        SuUserInfoDO existed = userInfoManager.queryUserById(userId);
        VerifyUtils.notNull(existed, "existedUser for updateOnlineStatus", ResultCode.USER_NOT_EXIST, true);
        Integer targetOnlineStatus = online ? CommonConstant.YES : CommonConstant.NO;
        if (Objects.equals(targetOnlineStatus, existed.getOnlineStatus())) {
            // 防恶意刷请求
            return;
        }
        SuUserInfoDO toUpdate = new SuUserInfoDO()
                .setId(userId)
                .setOnlineStatus(targetOnlineStatus);

        userInfoManager.updateById(toUpdate);
        userInfoManager.clearCacheBy(existed.getId(), existed.getUserName());
    }

    @Override
    public List<UserItemInfo> listUserItemByDeptIds(List<Long> deptIds, Long userId, boolean onlineOnly) {
        UserQry qry = new UserQry()
                .setDeptIds(deptIds)
                .setId(userId);
        if (onlineOnly) {
            qry.setOnlineStatus(CommonConstant.YES);
        }
        List<SuUserInfoDO> innerList = userInfoManager.list(qry.toQryWrapper(true));
        if (CollectionUtils.isEmpty(innerList)) {
            return List.of();
        }
        List<UserItemInfo> list = innerList.stream()
                .map(e -> {
                    UserItemInfo item = convert.convert2UserItemInfo(e);
                    item.setDeptName(deptService.getDeptNameById(e.getDepartmentId()));
                    return item;
                }).collect(Collectors.toList());
        return list;
    }

}
