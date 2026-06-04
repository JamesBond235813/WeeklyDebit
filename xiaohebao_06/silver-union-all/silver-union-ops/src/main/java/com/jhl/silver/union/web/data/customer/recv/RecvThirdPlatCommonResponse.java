package com.jhl.silver.union.web.data.customer.recv;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 接收三方平台数据的通用响应结构
 *
 * @author: qingren
 * @create_time: 2026/1/26
 */
@Data
@Accessors(chain = true)
@Schema(description = "接收三方平台数据的通用响应结构")
public class RecvThirdPlatCommonResponse<T> {
    /**
     * 结果码。200表示成功，其它表示失败
     */
    @Schema(description = "结果码。200表示成功，其它表示失败", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer code;
    /**
     * 错误信息。code不为200时必填
     */
    @Schema(description = "错误信息。code不为200时必填")
    private String msg;
    /**
     * 结果数据，见各接口文档说明
     */
    @Schema(description = "结果数据，见各接口文档说明")
    private T data;
}
