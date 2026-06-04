package com.jhl.silver.union.web.data.customer.hyy;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@Schema(description = "花易用通用加密请求")
public class HyyCommonRequest {
    @Schema(description = "AES-128-ECB 加密后的请求 JSON", requiredMode = Schema.RequiredMode.REQUIRED)
    private String data;
}
