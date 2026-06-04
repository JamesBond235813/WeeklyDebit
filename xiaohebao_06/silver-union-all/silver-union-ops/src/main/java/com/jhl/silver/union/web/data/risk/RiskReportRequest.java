package com.jhl.silver.union.web.data.risk;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "风控报告查询请求")
public class RiskReportRequest {
    @Schema(description = "姓名", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(description = "身份证号", requiredMode = Schema.RequiredMode.REQUIRED)
    private String idCard;

    @Schema(description = "手机号", requiredMode = Schema.RequiredMode.REQUIRED)
    private String phone;
}
