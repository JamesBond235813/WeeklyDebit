package com.jhl.silver.union.web.data.platform;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "分期/租赁平台保存/更新请求")
public class PlatformSaveRequest {

    @Schema(description = "ID (更新时必传)")
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
}
