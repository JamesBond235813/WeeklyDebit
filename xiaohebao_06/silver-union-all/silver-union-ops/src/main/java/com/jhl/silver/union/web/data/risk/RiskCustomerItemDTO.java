package com.jhl.silver.union.web.data.risk;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "风控查询客户信息")
public class RiskCustomerItemDTO {
    @Schema(description = "客户ID")
    private Long id;

    @Schema(description = "姓名")
    private String name;

    @Schema(description = "身份证号")
    private String idCard;

    @Schema(description = "手机号")
    private String phone;
}
