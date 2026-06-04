DROP TABLE IF EXISTS `cust_info_repo`;

CREATE TABLE `cust_info_repo` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '客户ID',
  `name` varchar(64) NOT NULL DEFAULT '' COMMENT '姓名',
  `mobile` varchar(16) NOT NULL DEFAULT '' COMMENT '手机号',
  `id_card_no` varchar(32) DEFAULT '' COMMENT '身份证号',
  `house_flag` tinyint DEFAULT '0' COMMENT '房产标识。0:未知; 1:有京房; 2:有房; 3:无',
  `house_val` int DEFAULT NULL COMMENT '房产价值，单位万元',
  `source_file_name` varchar(200) DEFAULT '' COMMENT '数据来源文件名称',
  `channel_name` varchar(16) NOT NULL DEFAULT '' COMMENT '推广渠道',
  `remark` text COMMENT '客户备注文案',
  `json_remark` text COMMENT '客户信息备注json 数据结构',
  `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `gmt_modified` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `unq_mobile_name` (`mobile`,`name`),
  KEY `idx_name` (`name`),
  KEY `idx_id_card_no` (`id_card_no`),
  KEY `idx_channel` (`channel_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='客户信息数据仓库';
