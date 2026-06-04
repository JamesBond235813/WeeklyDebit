package com.jhl.silver.union.biz.order.dal.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Getter
@Setter
@ToString
@Accessors(chain = true)
@TableName("biz_order")
@Schema(name = "BizOrderDO", description = "业务订单")
public class BizOrderDO implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "客户ID")
    private Long customerId;

    @Schema(description = "平台ID")
    private Long platformId;

    @Schema(description = "设备型号")
    private String deviceModel;

    @Schema(description = "数量")
    private Integer deviceQuantity;

    @Schema(description = "订单状态")
    private String status;

    @Schema(description = "首付款金额")
    private BigDecimal downPaymentAmount;

    @Schema(description = "是否垫付首付")
    private Boolean isDownPaymentAdvanced;

    @Schema(description = "回收价/尾款支出")
    private BigDecimal recyclePrice;

    @Schema(description = "约定回收价")
    private BigDecimal agreedRecyclePrice;

    @Schema(description = "渠道返佣")
    private BigDecimal channelCommission;

    @Schema(description = "出售价/变现收入")
    private BigDecimal resalePrice;

    @Schema(description = "毛利润")
    private BigDecimal grossProfit;

    @Schema(description = "平台发货物流单号")
    private String trackingNoPlatform;

    @Schema(description = "客户转寄物流单号")
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

    @Schema(description = "归属人ID")
    private Long ownerUserId;

    @Schema(description = "归属部门ID")
    private Long ownerDeptId;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private Date gmtCreate;

    @Schema(description = "修改时间")
    private Date gmtModified;
}
