package com.jhl.silver.union.biz.config;

import com.jhl.silver.union.biz.common.enums.BizDictConfigTypeEnum;
import com.jhl.silver.union.biz.customer.service.BizConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CustomerListFieldDisplayRunner implements ApplicationRunner {
    private static final String MARKER_TYPE = "CUSTOMER_LIST_FIELD";
    private static final String MARKER_KEY = "DEFAULT_VISIBLE_V20260605";
    private static final String VISIBLE_KEYS =
            "'name','mobile','idCardNo','userSource','channelDesc','applyDate','radarReport','riskReport','customerRemark'";

    private final JdbcTemplate jdbcTemplate;
    private final BizConfigService bizConfigService;

    public CustomerListFieldDisplayRunner(JdbcTemplate jdbcTemplate, BizConfigService bizConfigService) {
        this.jdbcTemplate = jdbcTemplate;
        this.bizConfigService = bizConfigService;
    }

    @Override
    public void run(ApplicationArguments args) {
        Integer applied = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM sys_config
                WHERE cnf_type = ? AND cnf_key = ? AND delete_flag = 0
                """, Integer.class, MARKER_TYPE, MARKER_KEY);
        if (applied != null && applied > 0) {
            return;
        }
        bizConfigService.getBizDictItems(BizDictConfigTypeEnum.CUSTOMER_LIST_FIELD, false);
        int rows = jdbcTemplate.update("""
                UPDATE biz_dict_config
                SET status = CASE
                    WHEN description IN (%s) THEN 1
                    ELSE 0
                END,
                opt_user_id = 0
                WHERE biz_type = 'CUSTOMER_LIST_FIELD'
                """.formatted(VISIBLE_KEYS));
        jdbcTemplate.update("""
                INSERT INTO sys_config
                    (cnf_type, cnf_key, cnf_desc, cnf_value, delete_flag, creator_uid, modifier_uid, gmt_create, gmt_modified)
                VALUES (?, ?, ?, ?, 0, 0, 0, NOW(), NOW())
                """, MARKER_TYPE, MARKER_KEY, "客户列表默认展示字段初始化", VISIBLE_KEYS);
        log.info("initialized customer list visible fields, rows={}", rows);
    }
}
