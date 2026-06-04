package com.jhl.silver.union.web.data.customer.recv;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 申请授信请求数据结构
 *
 * @author: qingren
 * @create_time: 2026/1/26
 */
@Data
@Accessors(chain = true)
@Schema(description = "申请授信请求数据结构")
public class RecvThirdPlatApplyCreditRequest {
    /**
     * 线索订单号
     */
    @Schema(description = "线索订单号", requiredMode = Schema.RequiredMode.REQUIRED)
    private String orderNo;
    /**
     * 用户姓名
     */
    @Schema(description = "用户姓名", requiredMode = Schema.RequiredMode.REQUIRED)
    private String userName;
    /**
     * 身份证号码
     */
    @Schema(description = "身份证号码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String idNo;
    /**
     * 手机号码
     */
    @Schema(description = "手机号码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String mobile;
}
