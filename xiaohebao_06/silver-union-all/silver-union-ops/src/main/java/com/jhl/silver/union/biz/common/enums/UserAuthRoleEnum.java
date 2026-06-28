package com.jhl.silver.union.biz.common.enums;

import com.jhl.silver.union.commons.utils.EnumUtils;
import com.jhl.silver.union.web.data.RoleItemInfo;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 用户权限角色枚举. <BR>
 * 各枚举均要求以 ROLE_ 打头
 *
 * @author: qingren
 * @create_time: 2025/3/27
 */
public enum UserAuthRoleEnum {

    ROLE_SUPPER("SUPPER", "超级管理员", "拥有所有权限"),
    ROLE_DEPT_INFO_ADMIN("DEPT_INFO_ADMIN", "部门信息管理员", "拥有创建、编辑以及删除部门信息的权限"),
    ROLE_DEPT_DATA_ADMIN("DEPT_DATA_ADMIN", "部门数据管理员", "管理本部门、下属部门以及对应部门人员的客户信息"),
    ROLE_USER_INFO_ADMIN("USER_INFO_ADMIN", "用户信息管理员", "拥有创建、编辑以及删除用户的权限"),
    ROLE_SALES("SALES", "普通业务员", "拥有我的客户、业务订单等基础作业权限"),
    ROLE_OBSERVER("OBSERVER", "观察员", "只能查看公海客户和已有缓存风控报告"),
    ;

    UserAuthRoleEnum(String role, String dispName, String desc) {
        this.role = role;
        this.dispName = dispName;
        this.desc = desc;
    }

    /**
     * 权限角色简称， 用于配合 spring-security 中过滤权限
     */
    public final String role;
    /**
     * 用于展示的权限角色名称
     */
    public final String dispName;
    /**
     * 权限说明
     */
    public final String desc;
    /**
     * UserAuthRoleEnum.name() - UserAuthRoleEnum 映射
     */
    private static final Map<String, UserAuthRoleEnum> nameRepo = EnumUtils.enum2Map(UserAuthRoleEnum.values(),
            e -> e.name());

    /**
     * UserAuthRoleEnum.role - UserAuthRoleEnum 映射
     */
    private static final Map<String, UserAuthRoleEnum> roleRepo = EnumUtils.enum2Map(UserAuthRoleEnum.values(),
            e -> e.role);

    /**
     * 根据权限角色简称，找权限角色枚举
     *
     * @param role
     * @return
     */
    public static final UserAuthRoleEnum findByRole(String role) {
        return roleRepo.get(StringUtils.trim(role));
    }

    /**
     * 根据权限角色枚举名称，找权限角色枚举
     *
     * @param name
     * @return
     */
    public static final UserAuthRoleEnum findByName(String name) {
        return nameRepo.get(StringUtils.trim(name));
    }

    /**
     * 根据权限角色枚举名称，找权限角色显示名称
     *
     * @param name
     * @return
     */
    public static final String getDispNameBy(String name) {
        UserAuthRoleEnum roleEnum = nameRepo.get(StringUtils.trim(name));
        return roleEnum == null ? null : roleEnum.dispName;
    }

    /**
     * @return
     */
    public static final List<RoleItemInfo> toRoleItemInfoList() {
        return Arrays.stream(UserAuthRoleEnum.values())
                .map(e -> RoleItemInfo.of(e.name(), e.dispName, e.desc))
                .collect(Collectors.toList());

    }

}
