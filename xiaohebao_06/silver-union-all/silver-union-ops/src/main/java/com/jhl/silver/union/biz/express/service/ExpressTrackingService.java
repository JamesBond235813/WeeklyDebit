package com.jhl.silver.union.biz.express.service;

import com.jhl.silver.union.commons.gson.GsonHelper;
import com.jhl.silver.union.web.data.order.ExpressTraceItem;
import com.jhl.silver.union.web.data.order.ExpressTrackResult;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ExpressTrackingService {

    private static final String UA =
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120 Safari/537.36";
    private static final String AUTO_DETECT_URL = "https://www.kuaidi100.com/autonumber/autoComNum?text=";
    private static final String QUERY_URL = "https://www.kuaidi100.com/query?type=%s&postid=%s";

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public ExpressTrackResult query(String trackingNo) {
        String normalized = StringUtils.trimToEmpty(trackingNo);
        if (StringUtils.isBlank(normalized)) {
            return new ExpressTrackResult()
                    .setTrackingNo(trackingNo)
                    .setMessage("物流单号为空");
        }
        try {
            CompanyInfo company = detectCompany(normalized);
            if (company == null || StringUtils.isBlank(company.getCode())) {
                return new ExpressTrackResult()
                        .setTrackingNo(normalized)
                        .setMessage("未识别快递公司");
            }
            ExpressTrackResult result = queryTraces(normalized, company);
            if (result != null) {
                return result;
            }
        } catch (Exception ex) {
            log.warn("Query express tracking failed: {}", ex.getMessage());
        }
        return new ExpressTrackResult()
                .setTrackingNo(normalized)
                .setMessage("查询失败，请稍后重试");
    }

    private CompanyInfo detectCompany(String trackingNo) throws Exception {
        String url = AUTO_DETECT_URL + URLEncoder.encode(trackingNo, StandardCharsets.UTF_8);
        String body = httpGet(url);
        if (StringUtils.isBlank(body)) {
            return null;
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> json = GsonHelper.fromJson(body, Map.class);
        if (json == null || !json.containsKey("auto")) {
            return null;
        }
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> auto = (List<Map<String, Object>>) json.get("auto");
        if (auto == null || auto.isEmpty()) {
            return null;
        }
        Map<String, Object> first = auto.get(0);
        String code = first.get("comCode") != null ? first.get("comCode").toString() : "";
        String name = first.get("comName") != null ? first.get("comName").toString() : "";
        return new CompanyInfo(code, name);
    }

    private ExpressTrackResult queryTraces(String trackingNo, CompanyInfo company) throws Exception {
        String url = String.format(QUERY_URL, company.getCode(),
                URLEncoder.encode(trackingNo, StandardCharsets.UTF_8));
        String body = httpGet(url);
        if (StringUtils.isBlank(body)) {
            return null;
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> json = GsonHelper.fromJson(body, Map.class);
        if (json == null) {
            return null;
        }
        String status = json.get("status") != null ? json.get("status").toString() : "";
        String state = json.get("state") != null ? json.get("state").toString() : "";
        String message = json.get("message") != null ? json.get("message").toString() : "";
        List<ExpressTraceItem> traces = new ArrayList<>();
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> data = (List<Map<String, Object>>) json.getOrDefault("data",
                Collections.emptyList());
        if (data != null) {
            for (Map<String, Object> item : data) {
                String time = item.get("time") != null ? item.get("time").toString() : "";
                String context = item.get("context") != null ? item.get("context").toString() : "";
                traces.add(new ExpressTraceItem().setTime(time).setContext(context));
            }
        }
        return new ExpressTrackResult()
                .setTrackingNo(trackingNo)
                .setCompanyCode(company.getCode())
                .setCompanyName(company.getName())
                .setStatus(status)
                .setState(state)
                .setMessage(message)
                .setTraces(traces);
    }

    private String httpGet(String url) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("User-Agent", UA)
                .header("Accept", "application/json")
                .timeout(Duration.ofSeconds(15))
                .GET()
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            return response.body();
        }
        return null;
    }

    private static class CompanyInfo {
        private final String code;
        private final String name;

        CompanyInfo(String code, String name) {
            this.code = code;
            this.name = name;
        }

        String getCode() {
            return code;
        }

        String getName() {
            return name;
        }
    }
}
