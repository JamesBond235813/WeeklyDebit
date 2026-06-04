DROP TABLE IF EXISTS `cust_dispatch_plan_user`;

CREATE TABLE `cust_dispatch_plan_user` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `plan_id` bigint NOT NULL,
  `dept_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `daily_limit` int NOT NULL DEFAULT '0',
  `sort_no` int NOT NULL DEFAULT '0',
  `status` tinyint NOT NULL DEFAULT '1',
  `gmt_create` datetime DEFAULT CURRENT_TIMESTAMP,
  `gmt_modified` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_plan_user` (`plan_id`,`user_id`),
  KEY `idx_plan_dept` (`plan_id`,`dept_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
