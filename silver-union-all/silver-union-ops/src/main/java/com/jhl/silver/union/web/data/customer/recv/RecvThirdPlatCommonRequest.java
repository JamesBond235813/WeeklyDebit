package com.jhl.silver.union.web.data.customer.recv;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 接收三方平台数据的通用请求结构
 *
 * @author: qingren
 * @create_time: 2026/1/26
 */
@Data
@Accessors(chain = true)
@Schema(description = "接收三方平台数据的通用请求结构")
public class RecvThirdPlatCommonRequest {
    /**
     * 应用ID，由平台分配给接入方。参与签名
     */
    @Schema(description = "应用ID，由平台分配给接入方。参与签名", requiredMode = Schema.RequiredMode.REQUIRED)
    private String appId;
    /**
     * AES加密后的请求数据。参与签名
     */
    @Schema(description = "AES加密后的请求数据。参与签名", requiredMode = Schema.RequiredMode.REQUIRED)
    private String data;
    /**
     * 请求ID，全局唯一。参与签名
     */
    @Schema(description = "请求ID，全局唯一。参与签名", requiredMode = Schema.RequiredMode.REQUIRED)
    private String requestId;
    /**
     * 毫秒级时间戳。参与签名
     */
    @Schema(description = "毫秒级时间戳。参与签名", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long timestamp;
    /**
     * RSA公钥加密后的aes key。参与签名
     */
    @Schema(description = "RSA公钥加密后的aes key。参与签名", requiredMode = Schema.RequiredMode.REQUIRED)
    private String secretKey;
    /**
     * 签名数据。不参与签名
     */
    @Schema(description = "签名数据。不参与签名", requiredMode = Schema.RequiredMode.REQUIRED)
    private String sign;
}
