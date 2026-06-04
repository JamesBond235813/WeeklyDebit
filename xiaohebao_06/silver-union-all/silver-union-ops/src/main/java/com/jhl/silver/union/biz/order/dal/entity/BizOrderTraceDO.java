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
import java.util.Date;

@Getter
@Setter
@ToString
@Accessors(chain = true)
@TableName("biz_order_trace")
@Schema(name = "BizOrderTraceDO", description = "订单状态日志")
public class BizOrderTraceDO implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "订单ID")
    private Long orderId;

    @Schema(description = "变更前状态")
    private String preStatus;

    @Schema(description = "变更后状态")
    private String currStatus;

    @Schema(description = "操作人ID")
    private Long optUserId;

    @Schema(description = "操作人姓名")
    private String optUserName;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private Date gmtCreate;
}
