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
public class StatsDashboardSchemaRunner implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        try {
            ensureDispatchStarColumn();
            ensureStarDailyTable();
            ensureAdmissionEventTable();
            ensureIndex("cust_push_record", "idx_push_stats_date_mobile", "gmt_create, type, mobile");
            ensureIndex("customer_info_item", "idx_customer_mobile", "mobile");
            ensureIndex("customer_info_item", "idx_customer_public_star", "owner_user_id, public_pool_star_flag");
            ensureIndex("cust_dispatch_daily_stat", "idx_dispatch_stat_date_dept_user", "stat_date, dept_id, user_id");
            ensureIndex("cust_api_admission_stat_event", "idx_admission_stat_date_channel",
                    "stat_date, channel_name, admission_passed");
            ensureIndex("cust_api_admission_stat_event", "idx_admission_stat_time", "stat_time");
        } catch (Exception e) {
            log.warn("ensure stats dashboard schema failed: {}", e.getMessage());
        }
    }

    private void ensureDispatchStarColumn() {
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(1)
                FROM INFORMATION_SCHEMA.COLUMNS
                WHERE TABLE_SCHEMA = DATABASE()
                  AND TABLE_NAME = 'cust_dispatch_daily_stat'
                  AND COLUMN_NAME = 'star_manual_count'
                """, Integer.class);
        if (count != null && count > 0) {
            return;
        }
        jdbcTemplate.execute("""
                ALTER TABLE cust_dispatch_daily_stat
                ADD COLUMN star_manual_count INT NOT NULL DEFAULT 0 COMMENT '领取公海星标客户数量'
                """);
        log.info("added dispatch daily stat column: star_manual_count");
    }

    private void ensureStarDailyTable() {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS cust_public_pool_star_daily_stat (
                    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                    stat_date DATE NOT NULL COMMENT '统计日期',
                    entry_count INT NOT NULL DEFAULT 0 COMMENT '进入公海星标客户数量',
                    gmt_create DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                    gmt_modified DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                    PRIMARY KEY (id),
                    UNIQUE KEY uk_star_daily_stat_date (stat_date)
                ) COMMENT='公海星标客户每日统计'
                """);
    }

    private void ensureAdmissionEventTable() {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS cust_api_admission_stat_event (
                    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                    cust_id BIGINT NULL COMMENT '客户ID',
                    mobile VARCHAR(32) NOT NULL COMMENT '手机号',
                    channel_name VARCHAR(64) NOT NULL DEFAULT '未知渠道' COMMENT '渠道名称',
                    user_source VARCHAR(128) NOT NULL DEFAULT '' COMMENT '用户来源',
                    admission_passed TINYINT NOT NULL DEFAULT 0 COMMENT '是否符合准入条件',
                    dispatch_result VARCHAR(32) NOT NULL DEFAULT '' COMMENT '分配结果',
                    stat_time DATETIME NOT NULL COMMENT '统计时间',
                    stat_date DATE NOT NULL COMMENT '统计日期',
                    stat_hour TINYINT NOT NULL DEFAULT 0 COMMENT '统计小时',
                    gmt_create DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                    PRIMARY KEY (id)
                ) COMMENT='上游API客户准入统计事件'
                """);
    }

    private void ensureIndex(String tableName, String indexName, String columns) {
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(1)
                FROM INFORMATION_SCHEMA.STATISTICS
                WHERE TABLE_SCHEMA = DATABASE()
                  AND TABLE_NAME = ?
                  AND INDEX_NAME = ?
                """, Integer.class, tableName, indexName);
        if (count != null && count > 0) {
            return;
        }
        jdbcTemplate.execute("CREATE INDEX " + indexName + " ON " + tableName + " (" + columns + ")");
        log.info("added stats dashboard index: {}.{}", tableName, indexName);
    }
}
