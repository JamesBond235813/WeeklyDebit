package com.jhl.silver.union.biz.common.enums;

import com.jhl.silver.union.commons.utils.EnumUtils;

import java.util.Map;

/**
 * 购买方式
 *
 * @author: qingren
 * @create_time: 2025/12/7
 */
public enum PurchaseTypeEnum {
    FULL_PAYMENT("0", "全款"),
    LOAN_CLEAR("1", "贷款-已结清"),
    LOAN("2", "贷款-未结清"),
    UNKNOWN("", "未知");
    private static final Map<String, PurchaseTypeEnum> repo =
            EnumUtils.enum2Map(PurchaseTypeEnum.values(), e -> e.purchaseType);
    private static final Map<String, PurchaseTypeEnum> descRepo =
            EnumUtils.enum2Map(PurchaseTypeEnum.values(), e -> e.desc);
    public final String purchaseType;
    public final String desc;

    PurchaseTypeEnum(String purchaseType, String desc) {
        this.purchaseType = purchaseType;
        this.desc = desc;
    }

    public static PurchaseTypeEnum findByPurchaseType(String purchaseType) {
        return repo.get(purchaseType);
    }

    public static PurchaseTypeEnum findByDesc(String desc) {
        return descRepo.getOrDefault(desc, UNKNOWN);
    }
}
