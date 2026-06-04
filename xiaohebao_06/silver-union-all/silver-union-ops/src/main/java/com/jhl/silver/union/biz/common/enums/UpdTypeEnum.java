package com.jhl.silver.union.biz.common.enums;

import com.jhl.silver.union.commons.utils.OtherUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * 更新记录的类型
 *
 * @author: qingren
 * @create_time: 2025/4/1
 */
public enum UpdTypeEnum {

    /**
     * 更新跟进状态或跟进记录
     */
    PROGRESS,
    /**
     * 上级评价
     */
    LEAD_REMARK,
    /**
     * 更新其它字段
     */
    OTHER;

    public static UpdTypeEnum findByName(String name) {
        if (StringUtils.isBlank(name)) {
            return null;
        }
        for (UpdTypeEnum updTypeEnum : UpdTypeEnum.values()) {
            if (StringUtils.equals(updTypeEnum.name(), name)) {
                return updTypeEnum;
            }
        }
        return null;
    }

    /**
     * 根据枚举的名称取枚举的描述
     *
     * @param name
     * @return
     */
    public static String getDescByName(String name) {
        UpdTypeEnum updTypeEnum = findByName(name);
        updTypeEnum = OtherUtils.defaultIfNull(updTypeEnum, UpdTypeEnum.OTHER);
        return switch (updTypeEnum) {
            case PROGRESS -> "跟进记录";
            default -> "更新记录";
        };
    }
}
