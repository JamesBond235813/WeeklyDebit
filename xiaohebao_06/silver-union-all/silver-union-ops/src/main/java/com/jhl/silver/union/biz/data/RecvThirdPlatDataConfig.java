package com.jhl.silver.union.biz.data;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 接收三方平台数据的配置信息
 *
 * @author: qingren
 * @create_time: 2026/1/26
 */
@Data
@Accessors(chain = true)
public class RecvThirdPlatDataConfig {
    /**
     * 三方平台的APP ID
     */
    private String appId;
    /**
     * 本平台的RSA私钥字符串(base64)
     */
    private String privateKey;

    /**
     * 本平台的RSA公钥字符串(base64)
     */
    private String publicKey;

    /**
     * 三方平台的公钥
     */
    private String thirdPlatPublicKey;

    /**
     * 渠道名称
     */
    private String channelName;

    /**
     * 渠道ID
     */
    private Long channelId;
}
