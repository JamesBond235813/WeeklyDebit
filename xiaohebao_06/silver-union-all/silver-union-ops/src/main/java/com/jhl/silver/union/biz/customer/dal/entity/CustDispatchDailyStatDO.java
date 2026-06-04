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
 * 数据分配每日统计
 */
@Getter
@Setter
@ToString
@Accessors(chain = true)
@TableName("cust_dispatch_daily_stat")
@Schema(name = "CustDispatchDailyStatDO", description = "数据分配每日统计")
public class CustDispatchDailyStatDO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "统计日期")
    private Date statDate;

    @Schema(description = "部门ID")
    private Long deptId;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "自动分配数量")
    private Integer autoCount;

    @Schema(description = "手动分配数量")
    private Integer manualCount;

    @Schema(description = "创建时间")
    private Date gmtCreate;

    @Schema(description = "更新时间")
    private Date gmtModified;
}
