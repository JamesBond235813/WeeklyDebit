package com.jhl.silver.union.web.data.customer.recv;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 撞库准入请求数据结构
 *
 * @author: qingren
 * @create_time: 2026/1/26
 */
@Data
@Accessors(chain = true)
@Schema(description = "撞库准入请求数据结构")
public class RecvThirdPlatAccessCheckRequest {
    /**
     * 11位手机号MD5字符串
     */
    @Schema(description = "11位手机号MD5字符串", requiredMode = Schema.RequiredMode.REQUIRED)
    private String phoneMd5;
}
