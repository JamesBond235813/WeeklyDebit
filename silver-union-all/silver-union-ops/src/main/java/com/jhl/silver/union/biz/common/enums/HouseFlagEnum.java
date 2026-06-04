package com.jhl.silver.union.biz.common.enums;

import com.jhl.silver.union.commons.utils.EnumUtils;

import java.util.Map;
import java.util.Optional;

/**
 * 房产标识枚举
 *
 * @author: qingren
 * @create_time: 2025/3/30
 */
public enum HouseFlagEnum {
    // 0:未知; 1:有京房; 2:有房; 3:无
    UNKNOWN(0, "未知"),
    JING_HOUSE(1, "有京房"),
    YES(2, "有房"),
    NO(3, "无"),
    ;
    public final Integer flag;
    public final String desc;
    private static final Map<Integer, HouseFlagEnum> repo = EnumUtils.enum2Map(HouseFlagEnum.values(), e -> e.flag);
    private static final Map<String, HouseFlagEnum> descRepo = EnumUtils.enum2Map(HouseFlagEnum.values(), e -> e.desc);

    HouseFlagEnum(Integer flag, String desc) {
        this.flag = flag;
        this.desc = desc;
    }

    public static HouseFlagEnum findByFlag(Integer flag) {
        return repo.get(flag);
    }

    public static String getDescByFlag(Integer flag) {
        return Optional.ofNullable(findByFlag(flag))
                .map(e -> e.desc)
                .orElse(null);
    }

    public static HouseFlagEnum findByDesc(String desc) {
        return descRepo.get(desc);
    }

}
