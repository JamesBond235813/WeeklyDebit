package com.jhl.silver.union.web.data.order;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "订单状态更新请求")
public class OrderStatusUpdateRequest {

    @Schema(description = "订单ID")
    private Long id;

    @Schema(description = "目标状态")
    private String targetStatus;

    @Schema(description = "设备型号")
    private String deviceModel;

    @Schema(description = "数量")
    private Integer deviceQuantity;

    @Schema(description = "首付款金额")
    private BigDecimal downPaymentAmount;

    @Schema(description = "是否垫付首付")
    private Boolean isDownPaymentAdvanced;

    @Schema(description = "约定回收价")
    private BigDecimal agreedRecyclePrice;

    @Schema(description = "渠道返佣")
    private BigDecimal channelCommission;

    @Schema(description = "平台发货物流单号 (SHIPPED状态需填)")
    private String trackingNoPlatform;

    @Schema(description = "客户转寄物流单号 (FORWARDED状态需填)")
    private String trackingNoCustomer;

    @Schema(description = "平台收件省")
    private String platformRecvProvince;

    @Schema(description = "平台收件市")
    private String platformRecvCity;

    @Schema(description = "平台收件区县")
    private String platformRecvDistrict;

    @Schema(description = "平台收件街道")
    private String platformRecvStreet;

    @Schema(description = "平台收件详细地址")
    private String platformRecvDetail;

    @Schema(description = "我方收件省")
    private String selfRecvProvince;

    @Schema(description = "我方收件市")
    private String selfRecvCity;

    @Schema(description = "我方收件区县")
    private String selfRecvDistrict;

    @Schema(description = "我方收件街道")
    private String selfRecvStreet;

    @Schema(description = "我方收件详细地址")
    private String selfRecvDetail;

    @Schema(description = "回收价/尾款支出 (RECEIVED/PAID状态需填)")
    private BigDecimal recyclePrice;

    @Schema(description = "出售价/变现收入 (RESOLD状态需填)")
    private BigDecimal resalePrice;

    @Schema(description = "备注")
    private String remark;
}
