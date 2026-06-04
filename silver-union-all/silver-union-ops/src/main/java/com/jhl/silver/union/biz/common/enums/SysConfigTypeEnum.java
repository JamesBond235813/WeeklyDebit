package com.jhl.silver.union.biz.common.enums;

import com.jhl.silver.union.commons.utils.EnumUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

/**
 * 系统配置类型
 */
public enum SysConfigTypeEnum {
    /**
     * Panorama 风控配置
     */
    RISK_PANORAMA("Panorama 风控配置"),

    /**
     * 接收三方平台推送数据配置
     */
    RECV_THIRD_PLAT_DATA("接收三方平台推送数据配置"),

    /**
     * 接收上游流量配置
     */
    ENABLE_RECV("接收上游流量配置");

    SysConfigTypeEnum(String desc) {
        this.desc = desc;
    }

    public final String desc;

    private static final Map<String, SysConfigTypeEnum> REPO =
            EnumUtils.enum2Map(SysConfigTypeEnum.values(), SysConfigTypeEnum::name);

    public static SysConfigTypeEnum findByName(String name) {
        return REPO.get(StringUtils.upperCase(name));
    }
}
