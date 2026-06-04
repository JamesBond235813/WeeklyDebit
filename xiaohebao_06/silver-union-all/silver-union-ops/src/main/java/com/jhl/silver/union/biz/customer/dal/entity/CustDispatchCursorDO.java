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
 * 数据分配轮转游标
 */
@Getter
@Setter
@ToString
@Accessors(chain = true)
@TableName("cust_dispatch_cursor")
@Schema(name = "CustDispatchCursorDO", description = "数据分配轮转游标")
public class CustDispatchCursorDO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "方案ID")
    private Long planId;

    @Schema(description = "部门ID")
    private Long deptId;

    @Schema(description = "上次分配用户ID")
    private Long lastUserId;

    @Schema(description = "创建时间")
    private Date gmtCreate;

    @Schema(description = "更新时间")
    private Date gmtModified;
}
