package com.jhl.silver.union.web.data.platform;

import com.jhl.silver.union.web.data.BasePagedRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "分期/租赁平台分页查询请求")
public class PlatformPageRequest extends BasePagedRequest {

    @Schema(description = "平台名称(模糊查询)")
    private String name;

    @Schema(description = "平台类型: INSTALLMENT(分期), RENTAL(租赁)")
    private String type;

    @Schema(description = "状态: 1-启用, 0-禁用")
    private Integer status;
}
