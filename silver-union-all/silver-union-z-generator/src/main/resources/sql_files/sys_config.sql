DROP TABLE IF EXISTS `sys_config`;

CREATE TABLE `sys_config` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '平台配置信息ID',
  `cnf_type` varchar(64) NOT NULL DEFAULT '' COMMENT '配置类型',
  `cnf_key` varchar(64) NOT NULL DEFAULT '' COMMENT '配置信息 key',
  `cnf_desc` varchar(64) NOT NULL DEFAULT '' COMMENT '配置信息描述',
  `cnf_value` text NOT NULL COMMENT '配置信息内容(json格式)',
  `delete_flag` bigint NOT NULL DEFAULT '0' COMMENT '删除标识. 0:表示未删除; 其它表示未删除',
  `creator_uid` bigint NOT NULL DEFAULT '0' COMMENT '创建人用户ID',
  `modifier_uid` bigint NOT NULL DEFAULT '0' COMMENT '最后修改人用户ID',
  `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `gmt_modified` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `unq_biz_code` (`cnf_key`,`cnf_type`,`delete_flag`),
  KEY `idx_cnf_type` (`cnf_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='平台配置信息';
