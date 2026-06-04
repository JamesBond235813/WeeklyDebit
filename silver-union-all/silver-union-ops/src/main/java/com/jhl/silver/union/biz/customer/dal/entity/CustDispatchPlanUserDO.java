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
 * 数据分配方案人员配置
 */
@Getter
@Setter
@ToString
@Accessors(chain = true)
@TableName("cust_dispatch_plan_user")
@Schema(name = "CustDispatchPlanUserDO", description = "数据分配方案人员配置")
public class CustDispatchPlanUserDO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "方案ID")
    private Long planId;

    @Schema(description = "部门ID")
    private Long deptId;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "每日上限")
    private Integer dailyLimit;

    @Schema(description = "排序号")
    private Integer sortNo;

    @Schema(description = "状态 1:启用 0:停用")
    private Integer status;

    @Schema(description = "创建时间")
    private Date gmtCreate;

    @Schema(description = "更新时间")
    private Date gmtModified;
}
