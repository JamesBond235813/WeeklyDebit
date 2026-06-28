package com.jhl.silver.union.biz.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomerHyySchemaRunner implements ApplicationRunner {

    private static final String TABLE_NAME = "customer_info_item";

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        try {
            ensureColumn("hyy_house_desc", "VARCHAR(32) NULL COMMENT 'HYY房产语义'");
            ensureColumn("hyy_car_desc", "VARCHAR(64) NULL COMMENT 'HYY车辆语义'");
            ensureColumn("hyy_provident_desc", "VARCHAR(32) NULL COMMENT 'HYY公积金语义'");
            ensureColumn("hyy_social_insurance_desc", "VARCHAR(32) NULL COMMENT 'HYY社保语义'");
            ensureColumn("hyy_insurance_desc", "VARCHAR(32) NULL COMMENT 'HYY投保语义'");
            ensureColumn("hyy_occupation_desc", "VARCHAR(32) NULL COMMENT 'HYY职业语义'");
            ensureColumn("hyy_overdue_desc", "VARCHAR(32) NULL COMMENT 'HYY逾期情况语义'");
            ensureColumn("hyy_zhima_desc", "VARCHAR(32) NULL COMMENT 'HYY芝麻分档语义'");
            ensureColumn("hyy_loan_amount_desc", "VARCHAR(32) NULL COMMENT 'HYY贷款额度语义'");
            ensureColumn("hyy_ip", "VARCHAR(64) NULL COMMENT 'HYY推送IP'");
            backfillHyyFields();
        } catch (Exception e) {
            log.warn("ensure HYY customer schema failed: {}", e.getMessage());
        }
    }

    private void ensureColumn(String columnName, String columnDefinition) {
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(1)
                FROM INFORMATION_SCHEMA.COLUMNS
                WHERE TABLE_SCHEMA = DATABASE()
                  AND TABLE_NAME = ?
                  AND COLUMN_NAME = ?
                """, Integer.class, TABLE_NAME, columnName);
        if (count != null && count > 0) {
            return;
        }
        jdbcTemplate.execute("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + columnName + " " + columnDefinition);
        log.info("added HYY customer column: {}", columnName);
    }

    private void backfillHyyFields() {
        int affected = jdbcTemplate.update("""
                UPDATE customer_info_item c
                JOIN cust_push_record r ON r.mobile = c.mobile
                    AND r.type = 1
                    AND r.channel_name = '花易用'
                SET c.hyy_house_desc = CASE CAST(JSON_UNQUOTE(JSON_EXTRACT(r.received_req, '$.house')) AS UNSIGNED)
                        WHEN 1 THEN '有' WHEN 2 THEN '无' ELSE c.hyy_house_desc END,
                    c.hyy_car_desc = CASE CAST(JSON_UNQUOTE(JSON_EXTRACT(r.received_req, '$.car')) AS UNSIGNED)
                        WHEN 3 THEN '无'
                        WHEN 1 THEN CONCAT_WS('|', '有',
                            CASE CAST(JSON_UNQUOTE(JSON_EXTRACT(r.received_req, '$.car_price')) AS UNSIGNED)
                                WHEN 1 THEN '10万以下' WHEN 2 THEN '10-20万'
                                WHEN 3 THEN '20-50万' WHEN 4 THEN '50万以上' ELSE NULL END,
                            CASE CAST(JSON_UNQUOTE(JSON_EXTRACT(r.received_req, '$.car_status')) AS UNSIGNED)
                                WHEN 1 THEN '全款车' WHEN 2 THEN '按揭车' ELSE NULL END)
                        WHEN 2 THEN CONCAT_WS('|', '有',
                            CASE CAST(JSON_UNQUOTE(JSON_EXTRACT(r.received_req, '$.car_price')) AS UNSIGNED)
                                WHEN 1 THEN '10万以下' WHEN 2 THEN '10-20万'
                                WHEN 3 THEN '20-50万' WHEN 4 THEN '50万以上' ELSE NULL END,
                            CASE CAST(JSON_UNQUOTE(JSON_EXTRACT(r.received_req, '$.car_status')) AS UNSIGNED)
                                WHEN 1 THEN '全款车' WHEN 2 THEN '按揭车' ELSE NULL END)
                        ELSE c.hyy_car_desc END,
                    c.hyy_provident_desc = CASE CAST(JSON_UNQUOTE(JSON_EXTRACT(r.received_req, '$.gjj')) AS UNSIGNED)
                        WHEN 1 THEN '6个月以上' WHEN 2 THEN '6个月以下' WHEN 3 THEN '无' ELSE c.hyy_provident_desc END,
                    c.hyy_social_insurance_desc = CASE CAST(JSON_UNQUOTE(JSON_EXTRACT(r.received_req, '$.shebao')) AS UNSIGNED)
                        WHEN 1 THEN '6个月以上' WHEN 2 THEN '6个月以下' WHEN 3 THEN '无' ELSE c.hyy_social_insurance_desc END,
                    c.hyy_insurance_desc = CASE CAST(JSON_UNQUOTE(JSON_EXTRACT(r.received_req, '$.insurance')) AS UNSIGNED)
                        WHEN 1 THEN '一年以上' WHEN 2 THEN '一年以下' WHEN 3 THEN '无' ELSE c.hyy_insurance_desc END,
                    c.hyy_occupation_desc = CASE CAST(JSON_UNQUOTE(JSON_EXTRACT(r.received_req, '$.occupation')) AS UNSIGNED)
                        WHEN 1 THEN '上班族' WHEN 2 THEN '个体户' WHEN 3 THEN '企业主' WHEN 4 THEN '自由职业' ELSE c.hyy_occupation_desc END,
                    c.hyy_overdue_desc = CASE CAST(JSON_UNQUOTE(JSON_EXTRACT(r.received_req, '$.overdue')) AS UNSIGNED)
                        WHEN 1 THEN '有' WHEN 2 THEN '无' ELSE c.hyy_overdue_desc END,
                    c.hyy_zhima_desc = CASE CAST(JSON_UNQUOTE(JSON_EXTRACT(r.received_req, '$.zhima')) AS UNSIGNED)
                        WHEN 1 THEN '600以下' WHEN 2 THEN '600-650' WHEN 3 THEN '650-700' WHEN 4 THEN '700以上' ELSE c.hyy_zhima_desc END,
                    c.hyy_loan_amount_desc = CASE CAST(JSON_UNQUOTE(JSON_EXTRACT(r.received_req, '$.loan_amount')) AS UNSIGNED)
                        WHEN 1 THEN '1-3万' WHEN 2 THEN '3-5万' WHEN 3 THEN '5-10万' WHEN 4 THEN '10万以上' ELSE c.hyy_loan_amount_desc END,
                    c.hyy_ip = COALESCE(JSON_UNQUOTE(JSON_EXTRACT(r.received_req, '$.ip')), c.hyy_ip),
                    c.customer_remark = CASE
                        WHEN c.customer_remark LIKE '花易用request_id:%' THEN ''
                        ELSE c.customer_remark
                    END
                WHERE r.received_req IS NOT NULL
                """);
        if (affected > 0) {
            log.info("backfilled HYY customer fields, affected={}", affected);
        }
    }
}
