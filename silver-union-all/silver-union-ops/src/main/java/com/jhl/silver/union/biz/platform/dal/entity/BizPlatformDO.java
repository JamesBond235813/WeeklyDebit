package com.jhl.silver.union.biz.platform.dal.entity;

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
@TableName("biz_platform")
@Schema(name = "BizPlatformDO", description = "分期/租赁平台")
public class BizPlatformDO implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "平台名称")
    private String name;

    @Schema(description = "平台类型: INSTALLMENT(分期), RENTAL(租赁)")
    private String type;

    @Schema(description = "平台链接")
    private String link;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "状态: 1-启用, 0-禁用")
    private Integer status;

    @Schema(description = "创建时间")
    private Date gmtCreate;

    @Schema(description = "修改时间")
    private Date gmtModified;
}
