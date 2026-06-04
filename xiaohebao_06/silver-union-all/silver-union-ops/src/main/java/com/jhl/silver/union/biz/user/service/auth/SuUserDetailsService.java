package com.jhl.silver.union.biz.user.service.auth;

import com.jhl.silver.union.biz.user.dal.entity.SuUserInfoDO;
import com.jhl.silver.union.biz.data.SuOauthUserInfo;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

/**
 * @author: qingren
 * @create_time: 2020/8/6
 */
public interface SuUserDetailsService extends UserDetailsService {
    /**
     * 根据手机号查询oauth用户信息
     *
     * @param userName
     * @return
     */
    SuOauthUserInfo loadUserByUserName(String userName);

    /**
     * 将平台用户信息转换成 oauth用户信息
     *
     * @param juUserInfoDO
     * @return
     */
    UserDetails generateFromJuUserInfo(SuUserInfoDO juUserInfoDO);

}
