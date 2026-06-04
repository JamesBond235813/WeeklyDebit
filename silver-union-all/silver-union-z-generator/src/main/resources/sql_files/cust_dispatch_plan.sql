DROP TABLE IF EXISTS `cust_dispatch_plan`;

CREATE TABLE `cust_dispatch_plan` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `mode` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'MANUAL',
  `effect_start` datetime NOT NULL,
  `effect_end` datetime NOT NULL,
  `status` tinyint NOT NULL DEFAULT '1',
  `opt_user_id` bigint NOT NULL DEFAULT '0',
  `gmt_create` datetime DEFAULT CURRENT_TIMESTAMP,
  `gmt_modified` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_status_time` (`status`,`effect_start`,`effect_end`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
