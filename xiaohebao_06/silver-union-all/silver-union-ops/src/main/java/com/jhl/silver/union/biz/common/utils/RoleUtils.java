package com.jhl.silver.union.biz.common.utils;

import com.jhl.silver.union.biz.common.enums.UserAuthRoleEnum;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.Set;

/**
 * 权限相关工具类
 *
 * @author: qingren
 * @create_time: 2025/4/2
 */
public abstract class RoleUtils {

    /**
     * 是否包含给定的任一权限
     *
     * @param roles
     * @param searchRoles
     * @return
     */
    public static boolean hasAny(Set<UserAuthRoleEnum> roles, UserAuthRoleEnum... searchRoles) {
        if (CollectionUtils.isEmpty(roles)) {
            return false;
        }
        boolean had = false;
        for (UserAuthRoleEnum sr : searchRoles) {
            had = roles.contains(sr);
            if (had) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断是否没有业务数据管理类权限
     *
     * @param roles
     * @return
     */
    public static boolean nonBizDataAdmin(Set<UserAuthRoleEnum> roles) {
        return !hasAny(roles, UserAuthRoleEnum.ROLE_SUPPER, UserAuthRoleEnum.ROLE_DEPT_DATA_ADMIN);
    }

    /**
     * 判断是否拥有超管权限
     *
     * @param roles
     * @return
     */
    public static boolean isSupper(Set<UserAuthRoleEnum> roles) {
        return hasAny(roles, UserAuthRoleEnum.ROLE_SUPPER);
    }

    /**
     * 判断是否拥有超管权限
     *
     * @param roles
     * @return
     */
    public static boolean isSupper(String roles) {
        return StringUtils.contains(roles, UserAuthRoleEnum.ROLE_SUPPER.name());
    }

}
