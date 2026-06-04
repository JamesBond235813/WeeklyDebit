DROP TABLE IF EXISTS `id_card_area`;

CREATE TABLE `id_card_area` (
  `code` varchar(6) NOT NULL,
  `name` varchar(64) NOT NULL,
  PRIMARY KEY (`code`),
  KEY `idx_id_card_area_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
