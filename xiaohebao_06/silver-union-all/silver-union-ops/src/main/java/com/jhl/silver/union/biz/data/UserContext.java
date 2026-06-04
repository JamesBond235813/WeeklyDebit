package com.jhl.silver.union.biz.data;

import com.google.gson.reflect.TypeToken;
import com.jhl.silver.union.biz.common.enums.UserAuthRoleEnum;
import com.jhl.silver.union.biz.user.dal.entity.SuUserInfoDO;
import com.jhl.silver.union.commons.exception.ExceptionLogPrinter;
import com.jhl.silver.union.commons.gson.GsonHelper;
import com.jhl.silver.union.commons.utils.OtherUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author: qingren
 * @create_time: 2025/3/19
 */
@Slf4j
public class UserContext {

    private static final ThreadLocal<SuUserInfoDO> threadLocalUserInfo = new ThreadLocal<>();
    private static final ThreadLocal<String> threadLocalClientIp = new ThreadLocal<>();
    private static final ThreadLocal<Set<UserAuthRoleEnum>> threadLocalUserRoles = new ThreadLocal<>();

    public static SuUserInfoDO getUserInfo() {
        return threadLocalUserInfo.get();
    }

    public static void setUserInfo(SuUserInfoDO userInfo) {
        threadLocalUserInfo.set(userInfo);
        if (StringUtils.isNotBlank(userInfo.getRoles())) {

            Set<String> roleStrs = null;
            try {
                roleStrs = GsonHelper.fromJson(userInfo.getRoles(), new TypeToken<Set<String>>() {
                }.getType());
                if (CollectionUtils.isEmpty(roleStrs)) {
                    threadLocalUserRoles.set(Set.of());
                    return;
                }
                Set<UserAuthRoleEnum> roles = roleStrs.stream()
                        .map(UserAuthRoleEnum::findByName)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet());
                threadLocalUserRoles.set(roles);
            } catch (Exception e) {
                ExceptionLogPrinter.printExceptionLog(e, log);
            }
        }
    }

    public static String getClientIp() {
        return threadLocalClientIp.get();
    }

    /**
     * 获取当前登录用户的角色集合
     *
     * @return
     */
    public static Set<UserAuthRoleEnum> getRoles() {
        return OtherUtils.defaultIfNull(threadLocalUserRoles.get(), Set.of());
    }

    /**
     * 判断用户角色中是包含给定的任一角色
     *
     * @param roleEnums
     * @return
     */
    public static boolean hasAnyRole(UserAuthRoleEnum... roleEnums) {
        Set<UserAuthRoleEnum> roles = threadLocalUserRoles.get();
        if (CollectionUtils.isEmpty(roles) || ArrayUtils.isEmpty(roleEnums)) {
            return false;
        }
        Optional<UserAuthRoleEnum> found = Arrays.stream(roleEnums)
                .filter(e -> roles.contains(e))
                .findAny();
        return found.isPresent();
    }

    /**
     * 获取当前登录用户 ID
     *
     * @return
     */
    public static Long getUserId() {
        if (Objects.isNull(threadLocalUserInfo.get())) {
            return null;
        }
        return threadLocalUserInfo.get().getId();
    }

    /**
     * 获取当前登录用户归属的部门 ID
     *
     * @return
     */
    public static Long getDeptId() {
        if (Objects.isNull(threadLocalUserInfo.get())) {
            return null;
        }
        return threadLocalUserInfo.get().getDepartmentId();
    }

    public static void setClientIp(String clientIp) {
        threadLocalClientIp.set(clientIp);
    }

    public static void clear() {
        threadLocalUserInfo.remove();
        threadLocalClientIp.remove();
        threadLocalUserRoles.remove();
    }

}
