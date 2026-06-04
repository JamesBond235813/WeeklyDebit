DROP TABLE IF EXISTS `customer_import_record`;

CREATE TABLE `customer_import_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `ori_file_name` varchar(512) NOT NULL COMMENT '原始文件名',
  `real_file_name` varchar(512) NOT NULL COMMENT '服务器落盘的文件绝对路径',
  `total_cnt` bigint NOT NULL DEFAULT '0' COMMENT '文件总数据量',
  `processed_cnt` bigint NOT NULL DEFAULT '0' COMMENT '已处理的数据量',
  `inserted_cnt` bigint NOT NULL DEFAULT '0' COMMENT '入库数据量',
  `proc_status` varchar(32) NOT NULL DEFAULT 'INIT' COMMENT '处理状态。参数业务枚举',
  `target_dept_id` bigint NOT NULL DEFAULT '0' COMMENT '分配数据的目标部门ID',
  `target_user_id` bigint NOT NULL DEFAULT '0' COMMENT '分配数据的目标用户ID',
  `existed_data_file_name` varchar(512) NOT NULL DEFAULT '' COMMENT '未导入数据的文件绝对路径',
  `finish_time` datetime DEFAULT NULL COMMENT '导入完成时间',
  `cost_mil_sec` bigint DEFAULT NULL COMMENT '操作耗费的时间',
  `error_msg` varchar(512) NOT NULL DEFAULT '' COMMENT '错误信息',
  `opt_user_id` bigint NOT NULL COMMENT '操作人用户ID',
  `opt_dept_id` bigint NOT NULL DEFAULT '0' COMMENT '操作人部门ID',
  `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `gmt_modified` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改时间',
  PRIMARY KEY (`id`),
  KEY `file_name` (`ori_file_name`),
  KEY `uid` (`opt_user_id`),
  KEY `opt_dept` (`opt_dept_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='导入客户信息的操作记录';
