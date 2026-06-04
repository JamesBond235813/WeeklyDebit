package com.jhl.silver.union.biz.data;

import com.jhl.silver.union.biz.user.dal.entity.SuUserInfoDO;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

/**
 * OAUTH2.0 用户信息
 *
 * @author: qingren
 * @create_time: 2020/8/6
 */
@Getter
public class SuOauthUserInfo {

    private SuUserInfoDO suUserInfoDO;
    private UserDetails userDetails;

    public SuOauthUserInfo(SuUserInfoDO suUserInfoDO, UserDetails userDetails) {
        this.suUserInfoDO = suUserInfoDO;
        this.userDetails = userDetails;
    }

    public SuOauthUserInfo(SuUserInfoDO suUserInfoDO, Collection<? extends GrantedAuthority> authorities) {
        this.suUserInfoDO = suUserInfoDO;
        this.userDetails = new User(suUserInfoDO.getUserName(), suUserInfoDO.getPassword(), authorities);
    }

    public SuOauthUserInfo(SuUserInfoDO suUserInfoDO, boolean enabled, boolean accountNonExpired,
            boolean credentialsNonExpired,
            boolean accountNonLocked,
            Collection<? extends GrantedAuthority> authorities) {
        this.suUserInfoDO = suUserInfoDO;
        this.userDetails =
                new User(suUserInfoDO.getUserName(), suUserInfoDO.getPassword(), enabled, accountNonExpired,
                        credentialsNonExpired, accountNonLocked, authorities);
    }
}
