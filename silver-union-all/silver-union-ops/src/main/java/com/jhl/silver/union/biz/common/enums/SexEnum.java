package com.jhl.silver.union.biz.common.enums;

import java.util.Objects;

/**
 * @author: qingren
 * @create_time: 2025/3/18
 */
public enum SexEnum {
    /**
     * 保密
     */
    UNKNOWN(0), MALE(1), FEMALE(2);
    public final int sex;

    SexEnum(int sex) {
        this.sex = sex;
    }

    public static String getDesc(Integer sex) {
        if (Objects.isNull(sex)) {
            return "保密";
        }
        return switch (sex) {
            case 1 -> "男";
            case 2 -> "女";
            default -> "保密";
        };
    }
}
