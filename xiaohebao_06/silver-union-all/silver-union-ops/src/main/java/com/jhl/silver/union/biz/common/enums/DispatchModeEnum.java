package com.jhl.silver.union.biz.common.enums;

import java.util.Arrays;

/**
 * 数据分配模式
 */
public enum DispatchModeEnum {
    MANUAL("MANUAL", "人工分配"),
    AUTO("AUTO", "自动分配");

    public final String code;
    public final String desc;

    DispatchModeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static DispatchModeEnum fromCode(String code) {
        if (code == null) {
            return null;
        }
        return Arrays.stream(values())
                .filter(e -> e.code.equalsIgnoreCase(code))
                .findFirst()
                .orElse(null);
    }

    public static boolean isAuto(String code) {
        DispatchModeEnum mode = fromCode(code);
        return mode == AUTO;
    }
}
