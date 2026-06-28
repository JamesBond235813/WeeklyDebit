package com.jhl.silver.union.biz.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CustomerDerivedFieldsBackfillRunner implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    public CustomerDerivedFieldsBackfillRunner(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        backfillMobileArea();
        backfillIdCardDerivedFields();
    }

    private void backfillMobileArea() {
        try {
            int rows = jdbcTemplate.update("""
                    UPDATE customer_info_item c
                    JOIN mobile_prefix_area m ON LEFT(c.mobile, 7) = m.mobile_pre
                    SET c.mobile_area = TRIM(CONCAT(COALESCE(m.province, ''), COALESCE(m.city, '')))
                    WHERE (c.mobile_area IS NULL OR c.mobile_area = '')
                      AND c.mobile REGEXP '^[0-9]{11}$'
                    """);
            log.info("backfilled customer mobile area, rows={}", rows);
        } catch (Exception e) {
            log.warn("backfill customer mobile area failed", e);
        }
    }

    private void backfillIdCardDerivedFields() {
        try {
            int rows = jdbcTemplate.update("""
                    UPDATE customer_info_item
                    SET birthday = DATE_FORMAT(STR_TO_DATE(SUBSTRING(id_card_no, 7, 8), '%Y%m%d'), '%Y-%m-%d'),
                        sex = IF(MOD(CAST(SUBSTRING(id_card_no, 17, 1) AS UNSIGNED), 2) = 1, 1, 2),
                        age = TIMESTAMPDIFF(YEAR, STR_TO_DATE(SUBSTRING(id_card_no, 7, 8), '%Y%m%d'), CURDATE())
                    WHERE id_card_no REGEXP '^[0-9]{17}[0-9Xx]$'
                      AND STR_TO_DATE(SUBSTRING(id_card_no, 7, 8), '%Y%m%d') IS NOT NULL
                      AND (birthday IS NULL OR birthday = '' OR sex IS NULL OR sex = 0 OR age IS NULL OR age = 0)
                    """);
            log.info("backfilled customer id card derived fields, rows={}", rows);
        } catch (Exception e) {
            log.warn("backfill customer id card derived fields failed", e);
        }
    }
}
