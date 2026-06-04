package com.jhl.silver.union.biz.common.enums;

import com.jhl.silver.union.commons.utils.EnumUtils;

import java.util.Map;
import java.util.Optional;

/**
 * 婚姻状态枚举
 *
 * @author, qingren
 * @create_time, 2025/3/30
 */
public enum MarriageFlagEnum {
    /**
     * 未知
     */
    UNKNOWN(0, "未知"),
    /**
     * 已婚
     */
    MARRIED(1, "已婚"),
    /**
     * 未婚
     */
    NOT_MARRIED(2, "未婚"),
    /**
     * 离异
     */
    DIVORCED(3, "离异"),
    ;
    public final Integer flag;
    public final String desc;
    private static final Map<Integer, MarriageFlagEnum> repo =
            EnumUtils.enum2Map(MarriageFlagEnum.values(), e -> e.flag);

    MarriageFlagEnum(Integer flag, String desc) {
        this.flag = flag;
        this.desc = desc;
    }

    public static MarriageFlagEnum findByFlag(Integer flag) {
        return repo.get(flag);
    }

    public static String getDescByFlag(Integer flag) {
        return Optional.ofNullable(findByFlag(flag))
                .map(e -> e.desc)
                .orElse(null);
    }
}
