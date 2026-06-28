package com.jhl.silver.union.biz.risk.service;

import com.jhl.silver.union.commons.gson.GsonHelper;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;

public final class RiskAdmissionRuleEvaluator {

    private static final int MIN_ZHIMA_CODE_600_PLUS = 2;
    private static final int MAX_ZHIMA_CODE = 4;
    private static final int ZHIMA_CODE_ABSENT = 5;

    private RiskAdmissionRuleEvaluator() {
    }

    public static boolean isZhima600Plus(Integer code) {
        return code != null
                && ((code >= MIN_ZHIMA_CODE_600_PLUS && code <= MAX_ZHIMA_CODE)
                || code == ZHIMA_CODE_ABSENT);
    }

    public static boolean isZhima600Plus(String hyyZhimaDesc, Integer zhimaScore) {
        if (zhimaScore != null && zhimaScore >= 600) {
            return true;
        }
        String text = StringUtils.trimToEmpty(hyyZhimaDesc);
        if (StringUtils.isBlank(text)) {
            return false;
        }
        return StringUtils.containsAny(text, "600", "650", "700")
                || StringUtils.containsAny(text, "无芝麻", "没有芝麻");
    }

    public static boolean isPanoramaQualified(String reportJson) {
        Map<String, Object> root = parseJson(reportJson);
        Map<String, Object> panorama = nestedMap(root, "panorama");
        Map<String, Object> payload = nestedMap(panorama, "payload");
        Map<String, Object> data = nestedMap(payload, "data");
        Map<String, Object> raw = nestedMap(data, "raw_radar_data");
        Map<String, Object> applyDetail = nestedMap(data, "apply_report_detail");
        Map<String, Object> behaviorDetail = nestedMap(data, "behavior_report_detail");
        Map<String, Object> currentDetail = nestedMap(data, "current_report_detail");
        Map<String, Object> metrics = new LinkedHashMap<>();
        metrics.putAll(data);
        metrics.putAll(raw);
        metrics.putAll(applyDetail);
        metrics.putAll(behaviorDetail);
        metrics.putAll(currentDetail);

        BigDecimal recent1MonthAmount = firstNumber(metrics, "近1个月_履约贷款总金额", "B22170040");
        BigDecimal recent3MonthLoans = firstNumber(metrics, "近3个月_贷款笔数", "B22170003");
        BigDecimal overdue6Month = firstNumber(metrics, "近6个月M0+逾期贷款笔数", "近6个月MO+逾期贷款笔数",
                "B22170025");
        BigDecimal normalPayRatio = firstNumber(metrics, "正常还款订单占贷款总订单数比例", "B22170034");
        BigDecimal closedOrders = firstNumber(metrics, "贷款已结清订单数", "B22170052");
        BigDecimal accessScore = firstNumber(metrics, "申请准入分", "A22160001");

        return ge(recent1MonthAmount, 500)
                && ge(recent3MonthLoans, 1)
                && eq(overdue6Month, 0)
                && ge(normalPayRatio, 75)
                && ge(closedOrders, 3)
                && ge(accessScore, 510);
    }

    public static boolean hasPanorama(String reportJson) {
        Map<String, Object> root = parseJson(reportJson);
        Map<String, Object> panorama = nestedMap(root, "panorama");
        return !panorama.isEmpty();
    }

    private static BigDecimal firstNumber(Map<String, Object> source, String... keys) {
        for (String key : keys) {
            BigDecimal value = number(source, key);
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    private static BigDecimal number(Map<String, Object> source, String key) {
        Object value = source.get(key);
        if (value == null) {
            return null;
        }
        String text = Objects.toString(value, "").trim()
                .replace("%", "");
        if (StringUtils.isBlank(text)) {
            return null;
        }
        BigDecimal rangeLowerBound = parseRangeLowerBound(text);
        if (rangeLowerBound != null) {
            return rangeLowerBound;
        }
        text = text.replace(",", "");
        try {
            return new BigDecimal(text);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static BigDecimal parseRangeLowerBound(String text) {
        if (StringUtils.length(text) < 3 || !(text.startsWith("[") || text.startsWith("("))) {
            return null;
        }
        int commaIdx = text.indexOf(',');
        if (commaIdx <= 1) {
            return null;
        }
        String lowerBound = text.substring(1, commaIdx).trim();
        if (StringUtils.isBlank(lowerBound)) {
            return null;
        }
        try {
            return new BigDecimal(lowerBound);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static boolean ge(BigDecimal value, int threshold) {
        return value != null && value.compareTo(BigDecimal.valueOf(threshold)) >= 0;
    }

    private static boolean eq(BigDecimal value, int expected) {
        return value != null && value.compareTo(BigDecimal.valueOf(expected)) == 0;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> parseJson(String json) {
        if (StringUtils.isBlank(json)) {
            return new LinkedHashMap<>();
        }
        try {
            return GsonHelper.fromJson(json, Map.class);
        } catch (Exception e) {
            return new LinkedHashMap<>();
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> nestedMap(Map<String, Object> source, String key) {
        Object value = source.get(key);
        return value instanceof Map ? (Map<String, Object>) value : new LinkedHashMap<>();
    }
}
