package com.jhl.silver.union.biz.customer.service;

import com.jhl.silver.union.biz.config.BizProperty;
import com.jhl.silver.union.commons.gson.GsonHelper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class HyyCollisionRemoteClient {
    private final BizProperty bizProperty;
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofMillis(500))
            .build();

    public boolean enabled() {
        return StringUtils.isNotBlank(bizProperty.getHyyCollisionServiceUrl());
    }

    public CollisionCheckResult check(String phoneCode, String nameMd5, String idnoMd5) {
        if (!enabled()) {
            return CollisionCheckResult.empty();
        }
        String baseUrl = StringUtils.removeEnd(StringUtils.trim(bizProperty.getHyyCollisionServiceUrl()), "/");
        int timeoutMs = bizProperty.getHyyCollisionServiceTimeoutMs() == null
                ? 1000 : Math.max(200, bizProperty.getHyyCollisionServiceTimeoutMs());
        CollisionCheckRequest payload = new CollisionCheckRequest()
                .setPhoneCode(StringUtils.trimToEmpty(phoneCode))
                .setNameMd5(StringUtils.lowerCase(StringUtils.trimToEmpty(nameMd5)))
                .setIdnoMd5(StringUtils.lowerCase(StringUtils.trimToEmpty(idnoMd5)));
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/hyy-collision/access-check"))
                .version(HttpClient.Version.HTTP_1_1)
                .timeout(Duration.ofMillis(timeoutMs))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(GsonHelper.toJson(payload), StandardCharsets.UTF_8));
        if (StringUtils.isNotBlank(bizProperty.getHyyCollisionServiceToken())) {
            builder.header("X-Collision-Token", StringUtils.trim(bizProperty.getHyyCollisionServiceToken()));
        }
        try {
            HttpResponse<String> response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException("hyy collision service http status=" + response.statusCode());
            }
            CollisionCheckResult result = GsonHelper.fromJson(response.body(), CollisionCheckResult.class);
            if (result == null || !result.isSuccess()) {
                throw new IllegalStateException("hyy collision service response failed");
            }
            return result;
        } catch (Exception e) {
            log.error("hyy collision remote check failed, phoneCode={}", phoneCode, e);
            throw new IllegalStateException("hyy collision service unavailable", e);
        }
    }

    @Data
    @Accessors(chain = true)
    public static class CollisionCheckRequest {
        private String phoneCode;
        private String nameMd5;
        private String idnoMd5;
    }

    @Data
    @Accessors(chain = true)
    public static class CollisionCheckResult {
        private boolean success;
        private boolean duplicated;
        private boolean nameHit;
        private boolean idnoHit;
        private int md5Count;
        private List<String> md5List;

        public static CollisionCheckResult empty() {
            return new CollisionCheckResult()
                    .setSuccess(true)
                    .setDuplicated(false)
                    .setMd5Count(0)
                    .setMd5List(List.of());
        }
    }
}
