DROP TABLE IF EXISTS `cust_push_record`;

CREATE TABLE `cust_push_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `channel_id` int NOT NULL DEFAULT '0' COMMENT '推送渠道ID',
  `app_id` varchar(64) DEFAULT NULL COMMENT '三方平台APP ID',
  `request_id` varchar(64) DEFAULT NULL COMMENT '三方平台请求ID',
  `channel_name` varchar(16) NOT NULL COMMENT '渠道名称',
  `type` tinyint NOT NULL COMMENT '数据类型。0:撞库数据,1:进件数据',
  `cust_name` varchar(64) NOT NULL DEFAULT '' COMMENT '客户姓名',
  `mobile` varchar(32) NOT NULL DEFAULT '' COMMENT '客户手机号',
  `order_no` varchar(32) NOT NULL DEFAULT '' COMMENT '推送方订单号',
  `existed_flag` tinyint NOT NULL DEFAULT '0' COMMENT '收到的数据是否已存在。 1: 存在 其它不存在',
  `received_req` text COMMENT '接收的请求内容明文',
  `remark` text COMMENT '备注信息',
  `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `gmt_modified` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_app_request` (`app_id`,`request_id`),
  KEY `idx_channel` (`channel_id`),
  KEY `idx_mobile_name` (`mobile`,`cust_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='推送数据记录';
