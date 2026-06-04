package com.jhl.silver.union.biz.data;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;

/**
 * Panorama 风控报告配置信息
 *
 * @author: qingren
 * @create_time: 2026/1/26
 */
@Data
public class PanoramaConfig {
    /**
     * 是否启用或允许使用风控报告 true: 启用
     */
    private boolean enabled;

    /**
     * 请求地址
     */
    private String apiUrl;

    /**
     * 平台的商户号
     */
    private String merchantNo;

    /**
     * 访问令牌
     */
    private String accessKey;

    /**
     * 令牌密钥
     */
    private String secretKey;
}
