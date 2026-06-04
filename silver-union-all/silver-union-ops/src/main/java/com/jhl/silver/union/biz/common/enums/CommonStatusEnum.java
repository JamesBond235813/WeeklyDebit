package com.jhl.silver.union.biz.common.enums;

import com.google.common.collect.Maps;

import java.util.Map;
import java.util.Objects;

/**
 * 状态枚举【状态类通用枚举】
 *
 * @author: qingren
 * @create_time: 2022/4/15
 */
public enum CommonStatusEnum {
    /**
     * 状态。 1：正常。
     */
    OK(1, "正常"),
    FORBIDDEN(0, "禁用");

    private static Map<Integer, CommonStatusEnum> repo = Maps.newHashMap();

    static {
        for (CommonStatusEnum value : CommonStatusEnum.values()) {
            repo.put(value.status, value);
        }
    }

    public final int status;
    public final String desc;

    CommonStatusEnum(int status, String desc) {
        this.status = status;
        this.desc = desc;
    }

    public static CommonStatusEnum findBy(Integer status) {
        return repo.get(status);
    }

    public static String getDescBy(Integer status) {
        CommonStatusEnum commonStatusEnum = findBy(status);
        if (Objects.isNull(commonStatusEnum)) {
            return null;
        }
        return commonStatusEnum.desc;
    }

}
