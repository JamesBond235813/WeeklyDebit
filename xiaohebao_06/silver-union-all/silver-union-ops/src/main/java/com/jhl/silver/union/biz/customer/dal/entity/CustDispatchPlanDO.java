package com.jhl.silver.union.biz.customer.dal.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * 数据分配方案
 */
@Getter
@Setter
@ToString
@Accessors(chain = true)
@TableName("cust_dispatch_plan")
@Schema(name = "CustDispatchPlanDO", description = "数据分配方案")
public class CustDispatchPlanDO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "分配模式 MANUAL/AUTO")
    private String mode;

    @Schema(description = "生效开始时间")
    private Date effectStart;

    @Schema(description = "生效结束时间")
    private Date effectEnd;

    @Schema(description = "状态 1:启用 0:停用")
    private Integer status;

    @Schema(description = "最后操作人用户ID")
    private Long optUserId;

    @Schema(description = "创建时间")
    private Date gmtCreate;

    @Schema(description = "更新时间")
    private Date gmtModified;
}
