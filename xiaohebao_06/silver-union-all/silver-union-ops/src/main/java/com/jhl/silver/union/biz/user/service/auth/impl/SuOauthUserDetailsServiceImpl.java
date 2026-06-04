package com.jhl.silver.union.biz.user.service.auth.impl;

import com.jhl.silver.union.biz.common.AuthConstance;
import com.jhl.silver.union.biz.common.ResultCode;
import com.jhl.silver.union.biz.common.enums.CommonStatusEnum;
import com.jhl.silver.union.biz.user.dal.entity.SuUserInfoDO;
import com.jhl.silver.union.biz.data.SuOauthUserInfo;
import com.jhl.silver.union.biz.user.manager.SuUserInfoManager;
import com.jhl.silver.union.biz.user.service.auth.SuUserDetailsService;
import com.jhl.silver.union.commons.exception.BizException;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * @author: qingren
 * @create_time: 2020/8/1
 */
@Component("suOauthUserDetailsService")
public class SuOauthUserDetailsServiceImpl implements SuUserDetailsService {

    @Resource
    private SuUserInfoManager userInfoManager;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if (StringUtils.isBlank(username)) {
            return null;
        }
        SuUserInfoDO SuUserInfoDO = null;
        if (StringUtils.startsWith(username, AuthConstance.USERNAME_UID_PREFIX)) {
            //走根据用户ID查询用户信息
            String uidStr = username.replace(AuthConstance.USERNAME_UID_PREFIX, StringUtils.EMPTY);
            if (StringUtils.isNumeric(uidStr)) {
                SuUserInfoDO = userInfoManager.queryUserById(Long.parseLong(uidStr));
            }
        } else {
            SuUserInfoDO = userInfoManager.findUserByArbitraryUsername(username);
        }

        if (Objects.isNull(SuUserInfoDO)) {
            throw new BizException(ResultCode.USER_NOT_EXIST, "username [" + username + "] does not exist");
        }

        //用户状态不正常的情况下，直接视为未找到用户。 简单处理
        if (!Objects.equals(SuUserInfoDO.getStatus(), CommonStatusEnum.OK.status)) {
            throw new BizException(ResultCode.USER_STATUS_NOT_OK, "username: " + username);
        }

        return this.generateFromJuUserInfo(SuUserInfoDO);
    }

    @Override
    public SuOauthUserInfo loadUserByUserName(String userName) {
        SuUserInfoDO suUserInfoDO = userInfoManager.findUserByUsername(userName);
        UserDetails user = this.generateFromJuUserInfo(suUserInfoDO);
        return new SuOauthUserInfo(suUserInfoDO, user);
    }

    @Override
    public UserDetails generateFromJuUserInfo(SuUserInfoDO userInfoDO) {
        if (Objects.isNull(userInfoDO)) {
            return null;
        }
        //用户状态不正常的情况下，直接视为未找到用户。 简单处理
        if (!Objects.equals(userInfoDO.getStatus(), CommonStatusEnum.OK.status)) {
            throw new BizException(ResultCode.USER_STATUS_NOT_OK,
                    "user [ phone:" + userInfoDO.getPhone() + " , id:" + userInfoDO.getId() + ", userName:"
                            + userInfoDO.getUserName() + " ] status is not OK");
        }
        //用户权限暂时全部默认为空
        //不论是用什么字段登录， 反馈给oauth 的用户名均为userName
        return User.builder()
                .username(userInfoDO.getUserName())
                .password(userInfoDO.getPassword())
                .build();
    }

}
