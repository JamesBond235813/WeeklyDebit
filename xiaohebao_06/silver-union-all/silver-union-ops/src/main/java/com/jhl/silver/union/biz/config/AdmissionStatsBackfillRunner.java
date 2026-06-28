package com.jhl.silver.union.biz.config;

import com.jhl.silver.union.biz.risk.service.RiskAdmissionRuleEvaluator;
import com.jhl.silver.union.commons.gson.GsonHelper;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdmissionStatsBackfillRunner implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        try {
            Integer existing = jdbcTemplate.queryForObject("SELECT COUNT(1) FROM cust_api_admission_stat_event",
                    Integer.class);
            if (existing != null && existing > 0) {
                return;
            }
            List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                    SELECT c.id AS cust_id,
                           c.mobile,
                           c.hyy_zhima_desc,
                           c.zhima_score,
                           c.user_source,
                           c.owner_user_id,
                           c.public_pool_star_flag,
                           COALESCE(NULLIF(r.channel_name, ''), '未知渠道') AS channel_name,
                           r.received_req,
                           r.gmt_create AS stat_time,
                           rr.report_json
                    FROM cust_push_record r
                    JOIN customer_info_item c ON c.mobile = r.mobile
                    LEFT JOIN risk_control_report rr ON rr.id = (
                        SELECT rr2.id
                        FROM risk_control_report rr2
                        WHERE rr2.phone = c.mobile
                          AND rr2.report_json IS NOT NULL
                          AND rr2.report_json <> ''
                        ORDER BY rr2.query_time DESC, rr2.id DESC
                        LIMIT 1
                    )
                    WHERE r.type = 1
                      AND COALESCE(r.existed_flag, 0) <> 1
                      AND r.mobile IS NOT NULL
                      AND r.mobile <> ''
                    ORDER BY r.gmt_create ASC, r.id ASC
                    """);
            int inserted = 0;
            int passed = 0;
            for (Map<String, Object> row : rows) {
                Integer zhimaCode = extractZhimaCode(Objects.toString(row.get("received_req"), ""));
                boolean zhimaQualified = zhimaCode != null
                        ? RiskAdmissionRuleEvaluator.isZhima600Plus(zhimaCode)
                        : RiskAdmissionRuleEvaluator.isZhima600Plus(Objects.toString(row.get("hyy_zhima_desc"), ""),
                                toInteger(row.get("zhima_score")));
                boolean admissionPassed = zhimaQualified
                        && RiskAdmissionRuleEvaluator.isPanoramaQualified(Objects.toString(row.get("report_json"), ""));
                jdbcTemplate.update("""
                        INSERT INTO cust_api_admission_stat_event
                            (cust_id, mobile, channel_name, user_source, admission_passed, dispatch_result,
                             stat_time, stat_date, stat_hour, gmt_create)
                        VALUES (?, ?, ?, ?, ?, ?, ?, DATE(?), HOUR(?), NOW())
                        """,
                        toLong(row.get("cust_id")),
                        Objects.toString(row.get("mobile"), ""),
                        StringUtils.defaultIfBlank(Objects.toString(row.get("channel_name"), ""), "未知渠道"),
                        StringUtils.defaultString(Objects.toString(row.get("user_source"), "")),
                        admissionPassed ? 1 : 0,
                        resolveDispatchResult(row, admissionPassed),
                        row.get("stat_time"),
                        row.get("stat_time"),
                        row.get("stat_time"));
                inserted++;
                if (admissionPassed) {
                    passed++;
                }
            }
            log.info("backfilled admission stat events, inserted={}, passed={}", inserted, passed);
        } catch (Exception e) {
            log.warn("backfill admission stat events failed: {}", e.getMessage());
        }
    }

    private String resolveDispatchResult(Map<String, Object> row, boolean admissionPassed) {
        Long ownerUserId = toLong(row.get("owner_user_id"));
        Integer starFlag = toInteger(row.get("public_pool_star_flag"));
        if (admissionPassed && ownerUserId != null && ownerUserId > 0L) {
            return "AUTO_ASSIGNED";
        }
        if (admissionPassed && Objects.equals(starFlag, 1)) {
            return "STAR_PUBLIC_POOL";
        }
        return "PUBLIC_POOL";
    }

    @SuppressWarnings("unchecked")
    private Integer extractZhimaCode(String requestBody) {
        if (StringUtils.isBlank(requestBody)) {
            return null;
        }
        try {
            Map<String, Object> map = GsonHelper.fromJson(requestBody, LinkedHashMap.class);
            return toInteger(map.get("zhima"));
        } catch (Exception e) {
            return null;
        }
    }

    private Integer toInteger(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value == null) {
            return null;
        }
        try {
            return Integer.parseInt(value.toString());
        } catch (Exception e) {
            return null;
        }
    }

    private Long toLong(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value == null) {
            return null;
        }
        try {
            return Long.parseLong(value.toString());
        } catch (Exception e) {
            return null;
        }
    }
}
