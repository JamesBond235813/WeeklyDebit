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
public class CustomerPublicPoolStarSchemaRunner implements ApplicationRunner {

    private static final String TABLE_NAME = "customer_info_item";
    private static final String COLUMN_NAME = "public_pool_star_flag";

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        try {
            Integer count = jdbcTemplate.queryForObject("""
                    SELECT COUNT(1)
                    FROM INFORMATION_SCHEMA.COLUMNS
                    WHERE TABLE_SCHEMA = DATABASE()
                      AND TABLE_NAME = ?
                      AND COLUMN_NAME = ?
                    """, Integer.class, TABLE_NAME, COLUMN_NAME);
            if (count != null && count > 0) {
                return;
            }
            jdbcTemplate.execute("""
                    ALTER TABLE customer_info_item
                    ADD COLUMN public_pool_star_flag TINYINT NOT NULL DEFAULT 0
                    COMMENT '公海重点标识。1:符合准入但无人在线导致进入公海; 0:普通客户'
                    """);
            log.info("added customer public pool star column: {}", COLUMN_NAME);
        } catch (Exception e) {
            log.warn("ensure customer public pool star schema failed: {}", e.getMessage());
        }
    }
}
