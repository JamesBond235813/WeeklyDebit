package com.jhl.silver.union.web.data.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 更新客户信息请求
 *
 * @author: qingren
 * @create_time: 2025/4/2
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(description = "更新客户信息请求")
public class UpdCustomerInfoRequest extends AddCustomerInfoRequest {

    /**
     * 客户ID
     */
    @Schema(description = "客户ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "请指定要修改信息的客户")
    private Long id;

}
