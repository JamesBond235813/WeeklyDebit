package com.jhl.silver.union.biz.common.enums;

import com.jhl.silver.union.commons.utils.EnumUtils;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * 业务标识有无枚举
 *
 * @author: qingren
 * @create_time: 2025/3/30
 */
public enum BizFlagEnum {

    UNKNOWN(0, "未知"),
    YES(1, "有"),
    NO(2, "无"),
    ;
    private static final Map<Integer, BizFlagEnum> repo = EnumUtils.enum2Map(BizFlagEnum.values(), e -> e.flag);
    private static final Map<String, BizFlagEnum> descRepo = EnumUtils.enum2Map(BizFlagEnum.values(), e -> e.desc);
    public final Integer flag;
    public final String desc;

    BizFlagEnum(Integer flag, String desc) {
        this.flag = flag;
        this.desc = desc;
    }

    public static BizFlagEnum findByFlag(Integer flag) {
        return repo.get(flag);
    }
    public static BizFlagEnum findByDesc(String desc) {
        return descRepo.get(desc);
    }
    public static String getDescByFlag(Integer flag) {
        return Optional.ofNullable(findByFlag(flag))
                .map(e -> e.desc)
                .orElse(null);
    }

    /**
     * 获取 是 - 否 描述
     *
     * @param flag
     * @return
     */
    public static String getYesNoDescByFlag(Integer flag) {
        BizFlagEnum bizFlagEnum = findByFlag(flag);
        if (Objects.isNull(bizFlagEnum)) {
            return null;
        }
        return switch (bizFlagEnum) {
            case UNKNOWN -> "未知";
            case YES -> "是";
            case NO -> "否";
        };
    }

}
