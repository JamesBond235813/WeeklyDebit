package com.jhl.silver.union.web.data.order;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 订单列表VO - 包含关联的客户和平台名称
 */
@Data
public class OrderListVO {
    private Long id;
    private Long customerId;
    private Long platformId;
    private String deviceModel;
    private Integer deviceQuantity;
    private String status;
    private BigDecimal downPaymentAmount;
    private Boolean isDownPaymentAdvanced;
    private BigDecimal recyclePrice;
    private BigDecimal agreedRecyclePrice;
    private BigDecimal channelCommission;
    private BigDecimal resalePrice;
    private BigDecimal grossProfit;
    private String trackingNoPlatform;
    private String trackingNoCustomer;
    private String platformRecvProvince;
    private String platformRecvCity;
    private String platformRecvDistrict;
    private String platformRecvStreet;
    private String platformRecvDetail;
    private String selfRecvProvince;
    private String selfRecvCity;
    private String selfRecvDistrict;
    private String selfRecvStreet;
    private String selfRecvDetail;
    private Long ownerUserId;
    private Long ownerDeptId;
    private String remark;
    private String gmtCreate;
    private String gmtModified;

    // 关联信息
    private String customerName;
    private String customerMobile;
    private String platformName;
    private String ownerUserName;
}
