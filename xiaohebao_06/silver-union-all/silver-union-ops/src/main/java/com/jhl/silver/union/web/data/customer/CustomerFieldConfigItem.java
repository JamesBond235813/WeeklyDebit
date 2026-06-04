package com.jhl.silver.union.web.data.customer;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@Schema(description = "客户信息列表字段配置")
public class CustomerFieldConfigItem {

    @Schema(description = "字段Key")
    private String fieldKey;

    @Schema(description = "字段名称")
    private String label;
}
