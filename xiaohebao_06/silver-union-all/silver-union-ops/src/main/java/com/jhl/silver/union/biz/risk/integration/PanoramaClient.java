package com.jhl.silver.union.biz.risk.integration;

import com.jhl.silver.union.biz.common.ResultCode;
import com.jhl.silver.union.biz.data.PanoramaConfig;
import com.jhl.silver.union.biz.sys.service.SysConfigService;
import com.jhl.silver.union.commons.exception.BizException;
import com.jhl.silver.union.commons.gson.GsonHelper;
import com.jhl.silver.union.commons.utils.VerifyUtils;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 全景雷达API客户端 - 与 V20 同步的请求签名与结构
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class PanoramaClient {

    public static final String PANORAMA_PRODUCT_NO = "JX1000020";
    public static final String PROBE_C_PRODUCT_NO = "JX1000021";

    private final SysConfigService sysConfigService;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public boolean isEnabled() {
        PanoramaConfig config = sysConfigService.getPanoramaConfig();
        return Objects.nonNull(config) && config.isEnabled();
    }

    public String fetchReport(String name, String idCard, String phone) throws Exception {
        return fetchProductReport(name, idCard, phone, PANORAMA_PRODUCT_NO);
    }

    public String fetchProductReport(String name, String idCard, String phone, String productNo) throws Exception {
        PanoramaConfig config = sysConfigService.getPanoramaConfig();
        if (Objects.isNull(config) || !config.isEnabled()) {
            throw new BizException(ResultCode.RISK_REPORT_SEARCH_UNABLED, "phone:" + phone);
        }
        VerifyUtils.notBlank(config.getApiUrl(), "panorama.apiUrl", "Panorama API 地址未配置", true);
        VerifyUtils.notBlank(config.getMerchantNo(), "panorama.merchantNo", "Panorama 商户号未配置", true);
        VerifyUtils.notBlank(config.getAccessKey(), "panorama.accessKey", "Panorama accessKey 未配置", true);
        VerifyUtils.notBlank(config.getSecretKey(), "panorama.secretKey", "Panorama secretKey 未配置", true);

        long timestamp = System.currentTimeMillis();
        String requestId = UUID.randomUUID().toString().replace("-", "") + timestamp;
        String sign = generateSign(timestamp, config.getAccessKey(), config.getSecretKey());

        Map<String, String> bodyParams = new HashMap<>();
        bodyParams.put("name", name);
        bodyParams.put("phone", phone);
        bodyParams.put("idNumber", idCard);
        bodyParams.put("productNo", productNo);
        String bodyJson = GsonHelper.toJson(bodyParams);

        Map<String, Object> payload = new HashMap<>();
        payload.put("timestamp", timestamp);
        payload.put("requestId", requestId);
        payload.put("sign", sign);
        payload.put("version", "1");
        payload.put("accesssKey", config.getAccessKey());
        payload.put("merchantNo", config.getMerchantNo());
        payload.put("body", bodyJson);

        String requestJson = GsonHelper.toJson(payload);
        log.info("Panorama API Request: requestId={}, productNo={}, merchantNo={}",
                requestId, productNo, mask(config.getMerchantNo()));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(config.getApiUrl()))
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(30))
                .POST(HttpRequest.BodyPublishers.ofString(requestJson))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            log.info("Panorama API Response: requestId={}, productNo={}, bodySize={}",
                    requestId, productNo, response.body() == null ? 0 : response.body().length());
            return response.body();
        }

        throw new RuntimeException("API Call Failed: HTTP " + response.statusCode() + " " + response.body());
    }

    private String generateSign(long timestamp, String accessKey, String secret) {
        String raw = timestamp + accessKey + secret;
        return md5(raw);
    }

    private String md5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : messageDigest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("MD5 Error", e);
        }
    }

    private String mask(String value) {
        if (value == null || value.length() <= 6) {
            return "***";
        }
        return value.substring(0, 3) + "***" + value.substring(value.length() - 3);
    }
}
