DROP TABLE IF EXISTS `risk_control_report`;

CREATE TABLE `risk_control_report` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL COMMENT 'Name',
  `id_card` varchar(255) DEFAULT NULL COMMENT 'ID Card',
  `phone` varchar(255) DEFAULT NULL COMMENT 'Phone',
  `report_json` json DEFAULT NULL COMMENT 'JSON Data',
  `query_time` datetime DEFAULT NULL COMMENT 'Query Time',
  `gmt_create` datetime DEFAULT CURRENT_TIMESTAMP,
  `gmt_modified` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_name` (`name`),
  KEY `idx_id_card` (`id_card`),
  KEY `idx_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Risk Control Reports';
