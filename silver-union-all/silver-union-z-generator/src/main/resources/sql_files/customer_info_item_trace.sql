DROP TABLE IF EXISTS `customer_info_item_trace`;

CREATE TABLE `customer_info_item_trace` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '更新历史记录ID',
  `source_id` bigint NOT NULL COMMENT '被更新的原始客户信息ID',
  `upd_type` varchar(16) NOT NULL COMMENT '更新记录类型。见业务枚举',
  `upd_field_json` text NOT NULL COMMENT '更新的字段及更新后的内容信息，json 格式',
  `upd_disp_content_json` text NOT NULL COMMENT '更新的字段及更新后的内容信息，json 格式',
  `opt_user_id` bigint NOT NULL COMMENT '操作数据变更的用户 ID',
  `opt_user_real_name` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '操作人姓名',
  `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `gmt_modified` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改时间',
  PRIMARY KEY (`id`),
  KEY `idx_source_id` (`source_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='客户信息数据更新历史';
