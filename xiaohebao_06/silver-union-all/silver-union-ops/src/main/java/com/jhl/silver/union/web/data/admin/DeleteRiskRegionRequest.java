package com.jhl.silver.union.web.data.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "删除风险地区提醒配置请求")
public class DeleteRiskRegionRequest {
    @Schema(description = "主键ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long id;
}
