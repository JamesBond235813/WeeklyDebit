package com.jhl.silver.union.biz.common.enums;

import com.jhl.silver.union.commons.utils.EnumUtils;

import java.util.Map;

/**
 * JWT 状态枚举
 */
public enum JwtStatusEnum {

    /**
     * JWT 状态正常。
     */
    OK,
    /**
     * 用户自主注销（退出登录）
     */
    REVOKE,
    /**
     * 在其它地方登录或重新登录，被动注销
     */
    REPLACED_REVOKE,
    /**
     * 强制注销（把人踢出登录）
     */
    FORCE_REVOKE;

    private static final Map<String, JwtStatusEnum> repo = EnumUtils.enum2Map(JwtStatusEnum.values(), e -> e.name());

    /**
     * 根据枚举名称 查找 对应的对举类
     *
     * @param statusName
     * @return
     */
    public static JwtStatusEnum findByName(String statusName) {
        return repo.get(statusName);
    }
}
