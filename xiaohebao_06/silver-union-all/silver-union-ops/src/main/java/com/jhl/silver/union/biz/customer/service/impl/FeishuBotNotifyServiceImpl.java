package com.jhl.silver.union.biz.customer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jhl.silver.union.biz.config.BizProperty;
import com.jhl.silver.union.biz.customer.dal.entity.CustNoticeDO;
import com.jhl.silver.union.biz.customer.dal.entity.CustomerInfoItemDO;
import com.jhl.silver.union.biz.customer.manager.CustNoticeManager;
import com.jhl.silver.union.biz.customer.service.FeishuBotNotifyService;
import com.jhl.silver.union.biz.user.service.UserService;
import com.jhl.silver.union.commons.gson.GsonHelper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
@RequiredArgsConstructor
@Slf4j
public class FeishuBotNotifyServiceImpl implements FeishuBotNotifyService {

    private static final String DEFAULT_SOURCE = "上游API";
    private static final String NOTICE_SOURCE_API = "API";
    private static final String NOTICE_TYPE_NEW = "NEW";
    private static final String ROUTE_ASSIGNED = "ASSIGNED";
    private static final String ROUTE_PUBLIC_POOL = "PUBLIC_POOL";

    private final BizProperty bizProperty;
    private final CustNoticeManager custNoticeManager;
    private final UserService userService;
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    @Override
    public void notifyApiInsertedCustomers(List<CustomerInfoItemDO> insertedCustomers) {
        if (CollectionUtils.isEmpty(insertedCustomers)) {
            return;
        }
        String assignedWebhook = bizProperty.getFeishuBotWebhook();
        String publicPoolWebhook = bizProperty.getFeishuPublicPoolBotWebhook();
        if (StringUtils.isBlank(assignedWebhook) && StringUtils.isBlank(publicPoolWebhook)) {
            return;
        }
        boolean hasPublicPoolWebhook = StringUtils.isNotBlank(publicPoolWebhook);
        int publicPoolCustomerCount = hasPublicPoolWebhook ? (int) insertedCustomers.stream()
                .filter(this::isPublicPoolCustomer)
                .count() : 0;
        int publicPoolTotal = hasPublicPoolWebhook && publicPoolCustomerCount > 0 ? countTodayPublicPoolApiEntrants() : 0;
        int publicPoolSequence = Math.max(0, publicPoolTotal - publicPoolCustomerCount);
        for (CustomerInfoItemDO customer : insertedCustomers) {
            if (customer == null) {
                continue;
            }
            try {
                if (isPublicPoolCustomer(customer)) {
                    if (StringUtils.isBlank(publicPoolWebhook)) {
                        continue;
                    }
                    publicPoolSequence++;
                    sendText(publicPoolWebhook, buildPublicPoolText(customer, publicPoolSequence),
                            ROUTE_PUBLIC_POOL, customer);
                } else {
                    if (StringUtils.isBlank(assignedWebhook)) {
                        continue;
                    }
                    sendText(assignedWebhook, buildAssignedText(customer), ROUTE_ASSIGNED, customer);
                }
            } catch (Exception e) {
                log.warn("send feishu api customer notice failed, customerId={}, mobile={}, ownerUserId={}, err={}",
                        customer.getId(), customer.getMobile(), customer.getOwnerUserId(), e.getMessage());
            }
        }
    }

    @Override
    public void notifyHyyAccessCheckBlocked(String phoneCode, int candidateCount) {
        String publicPoolWebhook = bizProperty.getFeishuPublicPoolBotWebhook();
        if (StringUtils.isBlank(publicPoolWebhook)) {
            return;
        }
        String normalizedPhoneCode = StringUtils.trimToEmpty(phoneCode);
        if (!StringUtils.isNumeric(normalizedPhoneCode) || normalizedPhoneCode.length() != 8) {
            return;
        }
        String text = "=>☆ 拦截 %s*** (%d)".formatted(normalizedPhoneCode, Math.max(0, candidateCount));
        CompletableFuture.runAsync(() -> {
            try {
                sendText(publicPoolWebhook, text, ROUTE_PUBLIC_POOL, null);
            } catch (Exception e) {
                log.warn("send feishu hyy access blocked notice failed, phoneCode={}, candidateCount={}, err={}",
                        normalizedPhoneCode, candidateCount, e.getMessage());
            }
        });
    }

    private void sendText(String webhook, String text, String route, CustomerInfoItemDO customer) throws Exception {
        Map<String, Object> content = new LinkedHashMap<>();
        content.put("text", text);
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("msg_type", "text");
        payload.put("content", content);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(webhook))
                .header("Content-Type", "application/json; charset=utf-8")
                .timeout(Duration.ofSeconds(8))
                .POST(HttpRequest.BodyPublishers.ofString(GsonHelper.toJson(payload), StandardCharsets.UTF_8))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        FeishuResponseResult result = parseFeishuResponse(response.body());
        if (response.statusCode() >= 200 && response.statusCode() < 300 && result.success()) {
            log.info("send feishu api customer notice success, route={}, customerId={}, mobile={}, ownerUserId={}, httpStatus={}, feishuCode={}, feishuMsg={}",
                    route, customerId(customer), customerMobile(customer), customerOwnerUserId(customer), response.statusCode(),
                    result.code(), result.message());
            return;
        }
        log.warn("send feishu api customer notice failed, route={}, customerId={}, mobile={}, ownerUserId={}, httpStatus={}, feishuCode={}, feishuMsg={}, responseBody={}",
                route, customerId(customer), customerMobile(customer), customerOwnerUserId(customer), response.statusCode(),
                result.code(), result.message(), abbreviate(response.body()));
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IllegalStateException("HTTP " + response.statusCode());
        }
    }

    private Long customerId(CustomerInfoItemDO customer) {
        return customer == null ? null : customer.getId();
    }

    private String customerMobile(CustomerInfoItemDO customer) {
        return customer == null ? null : customer.getMobile();
    }

    private Long customerOwnerUserId(CustomerInfoItemDO customer) {
        return customer == null ? null : customer.getOwnerUserId();
    }

    @SuppressWarnings("unchecked")
    private FeishuResponseResult parseFeishuResponse(String body) {
        if (StringUtils.isBlank(body)) {
            return new FeishuResponseResult(null, "");
        }
        try {
            Map<String, Object> root = GsonHelper.fromJson(body, Map.class);
            Object code = root.get("code");
            if (code == null) {
                code = root.get("StatusCode");
            }
            Object message = root.get("msg");
            if (message == null) {
                message = root.get("StatusMessage");
            }
            return new FeishuResponseResult(normalizeCode(code), StringUtils.defaultString(
                    message == null ? null : String.valueOf(message)));
        } catch (Exception e) {
            return new FeishuResponseResult(null, "unparseable response");
        }
    }

    private Integer normalizeCode(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String abbreviate(String body) {
        return StringUtils.abbreviate(StringUtils.defaultString(body), 500);
    }

    private record FeishuResponseResult(Integer code, String message) {

        boolean success() {
            return Objects.equals(code, 0);
        }
    }

    private String buildAssignedText(CustomerInfoItemDO customer) {
        StringBuilder builder = new StringBuilder();
        builder.append("新增入库-->").append(resolveOwnerName(customer)).append('\n');
        builder.append("--------------").append('\n');
        appendLine(builder, "姓名", customer.getName());
        appendLine(builder, "手机号", customer.getMobile());
        appendLine(builder, "身份证号", customer.getIdCardNo());
        appendLine(builder, "来源", resolveSource(customer));
        appendLine(builder, "申请时间", formatDate(customer));
        builder.append("--------------");
        return builder.toString();
    }

    private String resolveOwnerName(CustomerInfoItemDO customer) {
        Long ownerUserId = customer == null ? null : customer.getOwnerUserId();
        if (ownerUserId == null || ownerUserId <= 0L) {
            return "";
        }
        String realName = userService.getUserRealName(ownerUserId);
        return StringUtils.defaultIfBlank(realName, String.valueOf(ownerUserId));
    }

    private String buildPublicPoolText(CustomerInfoItemDO customer, int publicPoolSequence) {
        return """
                -----------------
                进入公海+%d
                手机归属地：%s
                身份证归属地：%s
                -----------------""".formatted(publicPoolSequence, resolveMobileArea(customer), resolveIdCardArea(customer));
    }

    private boolean isPublicPoolCustomer(CustomerInfoItemDO customer) {
        Long ownerUserId = customer == null ? null : customer.getOwnerUserId();
        return ownerUserId == null || ownerUserId <= 0L;
    }

    private int countTodayPublicPoolApiEntrants() {
        try {
            Date start = todayStart();
            Date end = addDays(start, 1);
            QueryWrapper<CustNoticeDO> wrapper = new QueryWrapper<>();
            wrapper.select("cust_id")
                    .eq("source", NOTICE_SOURCE_API)
                    .eq("notice_type", NOTICE_TYPE_NEW)
                    .eq("owner_user_id", 0L)
                    .ge("gmt_create", start)
                    .lt("gmt_create", end);
            return (int) custNoticeManager.list(wrapper).stream()
                    .map(CustNoticeDO::getCustId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet())
                    .size();
        } catch (Exception e) {
            log.warn("count feishu public pool notice failed, err={}", e.getMessage());
            return 0;
        }
    }

    private Date todayStart() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    private Date addDays(Date date, int days) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_MONTH, days);
        return calendar.getTime();
    }

    private String resolveSource(CustomerInfoItemDO customer) {
        if (StringUtils.isNotBlank(customer.getUserSource())) {
            return customer.getUserSource();
        }
        return DEFAULT_SOURCE;
    }

    private String formatDate(CustomerInfoItemDO customer) {
        if (customer.getApplyDate() == null) {
            return "";
        }
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(customer.getApplyDate());
    }

    private String resolveMobileArea(CustomerInfoItemDO customer) {
        if (customer == null || StringUtils.isBlank(customer.getMobileArea())) {
            return "";
        }
        String normalized = customer.getMobileArea().trim().replaceAll("[,，/|、-]+", " ");
        String[] parts = normalized.split("\\s+");
        if (parts.length >= 2) {
            return parts[0] + " " + parts[1];
        }
        return normalized;
    }

    private String resolveIdCardArea(CustomerInfoItemDO customer) {
        if (customer == null) {
            return "";
        }
        return String.join(" ", List.of(
                        StringUtils.defaultString(customer.getHukouProvince()).trim(),
                        StringUtils.defaultString(customer.getHukouCity()).trim()))
                .trim();
    }

    private void appendLine(StringBuilder builder, String label, String value) {
        builder.append(label).append("：").append(StringUtils.defaultString(value)).append('\n');
    }
}
