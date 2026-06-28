package com.jhl.silver.union.biz.common.enums;

import com.jhl.silver.union.commons.utils.EnumUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

/**
 * 业务字典配置的类型
 */
public enum BizDictConfigTypeEnum {
    /**
     * 客户信息来源的渠道
     */
    DATA_CHANNEL("客户信息来源的渠道"),
    /**
     * 电话结果标签
     */
    CALL_RESULT_TIPS("电话结果标签"),

    /**
     * 客户星级组别
     */
    CUSTOMER_STAR_GROUP("客户星级组别"),

    /**
     * 沟通(跟进)进度
     */
    PROGRESS("沟通(跟进)进度"),

    /**
     * 客户信息列表字段配置
     */
    CUSTOMER_LIST_FIELD("客户信息列表字段配置"),
    /**
     * 芝麻分阈值
     */
    ZHIMA_SCORE_THRESHOLD("芝麻分阈值"),
    /**
     * 风险地区提醒
     */
    CUSTOMER_RISK_REGION("风险地区提醒"),
    /**
     * 黑名单地区提醒
     */
    CUSTOMER_BLACK_REGION("黑名单地区提醒"),
    ;

    BizDictConfigTypeEnum(String desc) {
        this.desc = desc;
    }

    public final String desc;

    private static final Map<String, BizDictConfigTypeEnum> repo =
            EnumUtils.enum2Map(BizDictConfigTypeEnum.values(), e -> e.name());

    /**
     * @param name
     * @return
     */
    public static final BizDictConfigTypeEnum findByName(String name) {
        return repo.get(StringUtils.upperCase(name));
    }

}
