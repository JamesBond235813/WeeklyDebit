package com.jhl.silver.union.web.data.customer;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@Schema(description = "客户流转查询请求")
public class CustomerFlowQueryRequest {

    @Schema(description = "客户姓名")
    private String name;

    @Schema(description = "手机号")
    private String mobile;
}
