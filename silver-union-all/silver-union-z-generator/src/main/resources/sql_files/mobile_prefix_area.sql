DROP TABLE IF EXISTS `mobile_prefix_area`;

CREATE TABLE `mobile_prefix_area` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `mobile_pre` varchar(7) NOT NULL,
  `province` varchar(32) NOT NULL,
  `city` varchar(32) NOT NULL,
  `carrier` varchar(32) DEFAULT NULL,
  `district_code` varchar(16) DEFAULT NULL,
  `post_code` varchar(16) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_mobile_pre` (`mobile_pre`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
