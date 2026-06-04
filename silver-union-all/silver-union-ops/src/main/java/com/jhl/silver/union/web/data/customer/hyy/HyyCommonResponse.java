package com.jhl.silver.union.web.data.customer.hyy;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@Schema(description = "花易用通用响应")
public class HyyCommonResponse<T> {
    @Schema(description = "状态码。0 成功，其它失败", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer code;

    @Schema(description = "描述")
    private String message;

    @Schema(description = "响应数据")
    private T data;
}
