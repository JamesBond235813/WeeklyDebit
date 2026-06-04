package com.jhl.silver.union.web.data.customer.recv;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 撞库准入响应业务数据结构
 *
 * @author: qingren
 * @create_time: 2026/1/26
 */
@Data
@Accessors(chain = true)
@Schema(description = "撞库准入响应业务数据结构")
public class RecvThirdPlatAccessCheckResult {
    /**
     * 准入结果。0：准入失败；1：准入
     */
    @Schema(description = "准入结果。0：准入失败；1：准入", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer result;
    /**
     * 准入失败原因。result=0时必传
     */
    @Schema(description = "准入失败原因。result=0时必传")
    private String reason;
}
