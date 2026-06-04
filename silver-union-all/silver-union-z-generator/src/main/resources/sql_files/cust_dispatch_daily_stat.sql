DROP TABLE IF EXISTS `cust_dispatch_daily_stat`;

CREATE TABLE `cust_dispatch_daily_stat` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `stat_date` date NOT NULL,
  `dept_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `auto_count` int NOT NULL DEFAULT '0',
  `manual_count` int NOT NULL DEFAULT '0',
  `gmt_create` datetime DEFAULT CURRENT_TIMESTAMP,
  `gmt_modified` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_stat_user` (`stat_date`,`user_id`),
  KEY `idx_stat_dept_date` (`dept_id`,`stat_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
