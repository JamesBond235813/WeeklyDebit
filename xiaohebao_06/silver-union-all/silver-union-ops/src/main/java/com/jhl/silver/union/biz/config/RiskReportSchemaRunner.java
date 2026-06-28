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
public class RiskReportSchemaRunner implements ApplicationRunner {

    private static final String TABLE_NAME = "risk_control_report";
    private static final String INDEX_NAME = "idx_risk_identity_query_time";

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        try {
            Integer count = jdbcTemplate.queryForObject("""
                    SELECT COUNT(1)
                    FROM INFORMATION_SCHEMA.STATISTICS
                    WHERE TABLE_SCHEMA = DATABASE()
                      AND TABLE_NAME = ?
                      AND INDEX_NAME = ?
                    """, Integer.class, TABLE_NAME, INDEX_NAME);
            if (count != null && count > 0) {
                return;
            }
            jdbcTemplate.execute("""
                    ALTER TABLE risk_control_report
                    ADD INDEX idx_risk_identity_query_time (name, id_card, phone, query_time)
                    """);
            log.info("created risk report cache index: {}", INDEX_NAME);
        } catch (Exception e) {
            log.warn("ensure risk report cache index failed: {}", e.getMessage());
        }
    }
}
