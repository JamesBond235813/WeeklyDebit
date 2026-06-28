-- =============================================================
-- WeeklyDebit 迁移：客户表新增「用户来源」字段 (HYY channel_id)
-- 日期：2026-06-09
-- 适用库：本地 weeklydebit_su_ops_plus_20260126 / 云端 prod (weeklydebit)
-- 说明：
--   * user_source 存放上游渠道 HYY 推送数据中的 channel_id（上游的上游来源），字符串原样存储。
--   * 业务列表字段配置(biz_dict_config: userSource / channelDesc)由应用启动/读取时自动 seed
--     (BizConfigServiceImpl.ensureCustomerListFieldConfigsIfNeeded)，本脚本不手动插入，避免与
--     应用内 intValue 防冲突分配冲突。
--   * 幂等：列已存在则跳过，可重复执行。
-- =============================================================

SET @col_exists := (
  SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'customer_info_item'
    AND COLUMN_NAME = 'user_source'
);

SET @ddl := IF(@col_exists = 0,
  'ALTER TABLE `customer_info_item` ADD COLUMN `user_source` varchar(64) NOT NULL DEFAULT '''' COMMENT ''用户来源（上游渠道推送的 channel_id）'' AFTER `channel`',
  'SELECT ''column user_source already exists, skip'' AS msg'
);

PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
