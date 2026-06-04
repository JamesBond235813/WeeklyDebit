package com.jhl.silver.union.biz.common.enums;

import java.util.Objects;

/**
 * 客户信息收藏列表
 *
 * @author: qingren
 * @create_time: 2025/4/10
 */
public enum FavoriteTypeEnum {
    /**
     * 再分配客户，或者没有标属性的客户
     */
    NORMAL(0, "再分配客户"),
    /**
     * 我的客户
     */
    MY_CUST(1, "我的客户"),
    /**
     * 重点客户
     */
    KEY_CUST(2, "重点客户");
    public final Integer code;
    public final String desc;

    FavoriteTypeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static FavoriteTypeEnum findBy(Integer code) {
        if (Objects.isNull(code)) {
            return null;
        }
        for (FavoriteTypeEnum value : FavoriteTypeEnum.values()) {
            if (Objects.equals(code, value.code)) {
                return value;
            }
        }
        return null;
    }
}
