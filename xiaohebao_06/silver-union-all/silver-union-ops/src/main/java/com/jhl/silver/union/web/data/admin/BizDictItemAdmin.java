package com.jhl.silver.union.web.data.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 后台管理-业务字典信息（完整）
 */
@Data
@Accessors(chain = true)
@Schema(description = "后台管理-业务字典信息")
public class BizDictItemAdmin {
    @Schema(description = "ID")
    private Long id;

    @Schema(description = "业务类型")
    private String bizType;

    @Schema(description = "类型描述")
    private String typeDesc;

    @Schema(description = "字典值")
    private Integer intValue;

    @Schema(description = "显示名称")
    private String label;

    @Schema(description = "说明")
    private String description;

    @Schema(description = "状态。1：正常；0：禁用")
    private Integer status;

    @Schema(description = "显示排序号。越小越靠前")
    private Integer sortNo;
}

