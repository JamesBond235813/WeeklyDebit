package com.jhl.silver.union.biz.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OrderStatusEnum {
    APPLIED("申请中"),
    APPROVED("已通过/待发货"),
    SHIPPED("平台已发货"),
    FORWARDED("客户已转寄"),
    RECEIVED("我方已收货"),
    PAID("已付尾款/结清"),
    RESOLD("已出售/变现"),
    CANCELLED("已取消"),
    REJECTED("平台拒绝");

    private final String desc;
}
