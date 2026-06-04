DROP TABLE IF EXISTS `biz_order_trace`;

CREATE TABLE `biz_order_trace` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_id` bigint NOT NULL COMMENT '订单ID',
  `pre_status` varchar(32) DEFAULT NULL COMMENT '变更前状态',
  `curr_status` varchar(32) NOT NULL COMMENT '变更后状态',
  `opt_user_id` bigint DEFAULT NULL COMMENT '操作人ID',
  `opt_user_name` varchar(64) DEFAULT NULL COMMENT '操作人姓名',
  `remark` varchar(512) DEFAULT NULL COMMENT '备注',
  `gmt_create` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='订单状态流转记录表';
