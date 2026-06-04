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
 * 客户通知
 */
@Getter
@Setter
@ToString
@Accessors(chain = true)
@TableName("cust_notice")
@Schema(name = "CustNoticeDO", description = "客户通知")
public class CustNoticeDO implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "接收用户ID")
    private Long userId;

    @Schema(description = "接收部门ID")
    private Long deptId;

    @Schema(description = "客户ID")
    private Long custId;

    @Schema(description = "客户姓名")
    private String custName;

    @Schema(description = "客户手机号")
    private String custMobile;

    @Schema(description = "客户身份证号")
    private String custIdCard;

    @Schema(description = "客户归属用户ID")
    private Long ownerUserId;

    @Schema(description = "客户归属部门ID")
    private Long ownerDeptId;

    @Schema(description = "客户收藏类型")
    private Integer ownerFavorite;

    @Schema(description = "通知类型")
    private String noticeType;

    @Schema(description = "来源")
    private String source;

    @Schema(description = "状态 0未读 1已读")
    private Integer status;

    private Date gmtCreate;

    private Date gmtModified;
}
