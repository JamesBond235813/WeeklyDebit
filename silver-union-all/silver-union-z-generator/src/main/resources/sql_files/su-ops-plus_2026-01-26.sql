# ************************************************************
# Sequel Ace SQL dump
# 版本号： 20095
#
# https://sequel-ace.com/
# https://github.com/Sequel-Ace/Sequel-Ace
#
# 主机: 122.9.2.214 (MySQL 8.0.43)
# 数据库: su-ops-plus
# 生成时间: 2026-01-26 09:26:13 +0000
# ************************************************************


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
SET NAMES utf8mb4;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE='NO_AUTO_VALUE_ON_ZERO', SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;


# 转储表 biz_dict_config
# ------------------------------------------------------------

DROP TABLE IF EXISTS `biz_dict_config`;

CREATE TABLE `biz_dict_config` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `biz_type` varchar(32) NOT NULL COMMENT '业务类型， 见BizDictConfigTypeEnum枚举定义',
  `type_desc` varchar(64) NOT NULL COMMENT '业务类型描述',
  `int_value` int NOT NULL DEFAULT '0' COMMENT '业务字典值',
  `label` varchar(16) NOT NULL COMMENT '业务字典值对应的显示名称',
  `DESCRIPTION` varchar(64) NOT NULL DEFAULT '' COMMENT '业务字典项说明',
  `sort_no` int NOT NULL DEFAULT '0',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态。 1：正常。 0：禁用',
  `opt_user_id` bigint NOT NULL COMMENT '操作数据变更的用户 ID',
  `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `gmt_modified` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `unq_type_value` (`biz_type`,`int_value`),
  KEY `idx_biz_sort` (`biz_type`,`sort_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='业务字典配置信息。 这里配置的字典信息仅用于标记、展示，不进行业务处理';



# 转储表 biz_order
# ------------------------------------------------------------

DROP TABLE IF EXISTS `biz_order`;

CREATE TABLE `biz_order` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `customer_id` bigint NOT NULL COMMENT '客户ID',
  `platform_id` bigint NOT NULL COMMENT '平台ID',
  `device_model` varchar(128) NOT NULL COMMENT '设备型号',
  `status` varchar(32) NOT NULL COMMENT '订单状态',
  `down_payment_amount` decimal(10,2) DEFAULT '0.00' COMMENT '首付款金额',
  `is_down_payment_advanced` tinyint(1) DEFAULT '0' COMMENT '是否垫付首付',
  `recycle_price` decimal(10,2) DEFAULT '0.00' COMMENT '回收价/尾款支出',
  `resale_price` decimal(10,2) DEFAULT '0.00' COMMENT '出售价/变现收入',
  `gross_profit` decimal(10,2) DEFAULT '0.00' COMMENT '毛利润',
  `tracking_no_platform` varchar(64) DEFAULT NULL COMMENT '平台发货物流单号',
  `tracking_no_customer` varchar(64) DEFAULT NULL COMMENT '客户转寄物流单号',
  `owner_user_id` bigint DEFAULT NULL COMMENT '归属人ID',
  `owner_dept_id` bigint DEFAULT NULL COMMENT '归属部门ID',
  `remark` varchar(512) DEFAULT NULL COMMENT '备注',
  `gmt_create` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `gmt_modified` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `platform_recv_province` varchar(64) DEFAULT NULL COMMENT 'å¹³å°æ”¶ä»¶çœ',
  `platform_recv_city` varchar(64) DEFAULT NULL COMMENT 'å¹³å°æ”¶ä»¶å¸‚',
  `platform_recv_district` varchar(64) DEFAULT NULL COMMENT 'å¹³å°æ”¶ä»¶åŒºåŽ¿',
  `platform_recv_street` varchar(64) DEFAULT NULL COMMENT 'å¹³å°æ”¶ä»¶è¡—é“',
  `platform_recv_detail` varchar(255) DEFAULT NULL COMMENT 'å¹³å°æ”¶ä»¶è¯¦ç»†åœ°å€',
  `self_recv_province` varchar(64) DEFAULT NULL COMMENT 'æˆ‘æ–¹æ”¶ä»¶çœ',
  `self_recv_city` varchar(64) DEFAULT NULL COMMENT 'æˆ‘æ–¹æ”¶ä»¶å¸‚',
  `self_recv_district` varchar(64) DEFAULT NULL COMMENT 'æˆ‘æ–¹æ”¶ä»¶åŒºåŽ¿',
  `self_recv_street` varchar(64) DEFAULT NULL COMMENT 'æˆ‘æ–¹æ”¶ä»¶è¡—é“',
  `self_recv_detail` varchar(255) DEFAULT NULL COMMENT 'æˆ‘æ–¹æ”¶ä»¶è¯¦ç»†åœ°å€',
  `device_quantity` int NOT NULL DEFAULT '1' COMMENT 'æ•°é‡',
  `agreed_recycle_price` decimal(10,2) DEFAULT '0.00' COMMENT 'çº¦å®šå›žæ”¶ä»·',
  `channel_commission` decimal(10,2) DEFAULT '0.00' COMMENT '渠道返佣',
  PRIMARY KEY (`id`),
  KEY `idx_customer_id` (`customer_id`),
  KEY `idx_platform_id` (`platform_id`),
  KEY `idx_owner_user_id` (`owner_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='业务订单表';



# 转储表 biz_order_trace
# ------------------------------------------------------------

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



# 转储表 biz_platform
# ------------------------------------------------------------

DROP TABLE IF EXISTS `biz_platform`;

CREATE TABLE `biz_platform` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(64) NOT NULL COMMENT '平台名称',
  `type` varchar(32) NOT NULL COMMENT '平台类型: INSTALLMENT(分期), RENTAL(租赁)',
  `link` varchar(255) DEFAULT NULL COMMENT '平台链接',
  `remark` varchar(512) DEFAULT NULL COMMENT '备注',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态: 1-启用, 0-禁用',
  `gmt_create` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `gmt_modified` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='分期/租赁平台表';



# 转储表 cust_dispatch_cursor
# ------------------------------------------------------------

DROP TABLE IF EXISTS `cust_dispatch_cursor`;

CREATE TABLE `cust_dispatch_cursor` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `plan_id` bigint NOT NULL,
  `dept_id` bigint NOT NULL,
  `last_user_id` bigint NOT NULL DEFAULT '0',
  `gmt_create` datetime DEFAULT CURRENT_TIMESTAMP,
  `gmt_modified` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_plan_dept` (`plan_id`,`dept_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;



# 转储表 cust_dispatch_daily_stat
# ------------------------------------------------------------

DROP TABLE IF EXISTS `cust_dispatch_daily_stat`;

CREATE TABLE `cust_dispatch_daily_stat` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `stat_date` date NOT NULL,
  `dept_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `auto_count` int NOT NULL DEFAULT '0',
  `manual_count` int NOT NULL DEFAULT '0',
  `gmt_create` datetime DEFAULT CURRENT_TIMESTAMP,
  `gmt_modified` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_stat_user` (`stat_date`,`user_id`),
  KEY `idx_stat_dept_date` (`dept_id`,`stat_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;



# 转储表 cust_dispatch_plan
# ------------------------------------------------------------

DROP TABLE IF EXISTS `cust_dispatch_plan`;

CREATE TABLE `cust_dispatch_plan` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `mode` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'MANUAL',
  `effect_start` datetime NOT NULL,
  `effect_end` datetime NOT NULL,
  `status` tinyint NOT NULL DEFAULT '1',
  `opt_user_id` bigint NOT NULL DEFAULT '0',
  `gmt_create` datetime DEFAULT CURRENT_TIMESTAMP,
  `gmt_modified` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_status_time` (`status`,`effect_start`,`effect_end`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;



# 转储表 cust_dispatch_plan_user
# ------------------------------------------------------------

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



# 转储表 cust_info_repo
# ------------------------------------------------------------

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



# 转储表 cust_notice
# ------------------------------------------------------------

DROP TABLE IF EXISTS `cust_notice`;

CREATE TABLE `cust_notice` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `dept_id` bigint NOT NULL DEFAULT '0',
  `cust_id` bigint NOT NULL,
  `cust_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `cust_mobile` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `cust_id_card` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `owner_user_id` bigint NOT NULL DEFAULT '0',
  `owner_dept_id` bigint NOT NULL DEFAULT '0',
  `owner_favorite` int NOT NULL DEFAULT '0',
  `notice_type` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'PUSH',
  `source` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'API',
  `status` tinyint NOT NULL DEFAULT '0',
  `gmt_create` datetime DEFAULT CURRENT_TIMESTAMP,
  `gmt_modified` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_status` (`user_id`,`status`,`gmt_create`),
  KEY `idx_cust` (`cust_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;



# 转储表 cust_push_record
# ------------------------------------------------------------

DROP TABLE IF EXISTS `cust_push_record`;

CREATE TABLE `cust_push_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `channel_id` int NOT NULL DEFAULT '0' COMMENT '推送渠道ID',
  `app_id` varchar(64) DEFAULT NULL COMMENT '三方平台APP ID',
  `request_id` varchar(64) DEFAULT NULL COMMENT '三方平台请求ID',
  `channel_name` varchar(16) NOT NULL COMMENT '渠道名称',
  `type` tinyint NOT NULL COMMENT '数据类型。0:撞库数据,1:进件数据',
  `cust_name` varchar(64) NOT NULL DEFAULT '' COMMENT '客户姓名',
  `mobile` varchar(16) NOT NULL DEFAULT '' COMMENT '客户手机号',
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



# 转储表 customer_import_record
# ------------------------------------------------------------

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



# 转储表 customer_info_item
# ------------------------------------------------------------

DROP TABLE IF EXISTS `customer_info_item`;

CREATE TABLE `customer_info_item` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '客户ID',
  `name` varchar(64) NOT NULL DEFAULT '' COMMENT '姓名',
  `mobile` varchar(16) NOT NULL DEFAULT '' COMMENT '手机号',
  `mobile_md5` varchar(32) NOT NULL DEFAULT '' COMMENT '手机号MD5',
  `mobile_area` varchar(64) NOT NULL DEFAULT '' COMMENT '手机号归属地',
  `sex` tinyint NOT NULL DEFAULT '0' COMMENT '性别。 0:保密, 1:男, 2:女',
  `age` tinyint DEFAULT NULL COMMENT '年龄',
  `zhima_score` int DEFAULT NULL COMMENT 'Zhima score',
  `id_card_no` varchar(32) DEFAULT '' COMMENT '身份证号',
  `birthday` varchar(10) DEFAULT '' COMMENT '出生日期。 yyyy-MM-dd 格式',
  `req_loan_amount` int DEFAULT NULL COMMENT '目标贷款金额，单位元',
  `house_flag` tinyint DEFAULT '0' COMMENT '房产标识。0:未知; 1:有京房; 2:有房; 3:无',
  `Insurance_flag` tinyint DEFAULT '0' COMMENT '保单标识。0:未知; 1:有; 2:无',
  `social_insurance_flag` tinyint DEFAULT '0' COMMENT '社保标识。0:未知; 1:有; 2:无',
  `car_flag` tinyint DEFAULT '0' COMMENT '车产标识。0:未知; 1:有; 2:无',
  `provident_flag` tinyint DEFAULT '0' COMMENT '公积金标识。0:未知; 1:有; 2:无',
  `credit_card_flag` tinyint DEFAULT '0' COMMENT '信用卡标识。0:未知; 1:有; 2:无',
  `enterprise_flag` tinyint DEFAULT '0' COMMENT '企业主标识。0:未知; 1:是; 2:否',
  `marriage_status` tinyint DEFAULT '0' COMMENT '婚姻状态. 0:未知; 1:已婚; 2:未婚; 3:离异',
  `provident_amount_yuan` int DEFAULT NULL COMMENT '公积金金额， 单位元',
  `house_val` int DEFAULT NULL COMMENT '房产价值，单位万元',
  `car_no` varchar(16) NOT NULL DEFAULT '' COMMENT '车牌号',
  `car_purchase_type` varchar(16) NOT NULL DEFAULT '' COMMENT '购车方式。 见业务枚举定义',
  `city_id` varchar(8) NOT NULL DEFAULT '' COMMENT '申请人城市ID',
  `city_name` varchar(32) NOT NULL DEFAULT '' COMMENT '申请人城市名称',
  `progress` int NOT NULL DEFAULT '1' COMMENT '跟进状态。 见业务字典定义',
  `address` text COMMENT '住址',
  `work_address` text COMMENT '工作单位',
  `source_file_name` varchar(200) DEFAULT '' COMMENT '数据来源文件名称',
  `channel` int DEFAULT '0' COMMENT '推广渠道ID。见业务字典定义',
  `call_tips` tinyint NOT NULL DEFAULT '0' COMMENT '电话结果。见业务字典定义',
  `customer_group` int NOT NULL DEFAULT '0' COMMENT '客户分组，见业务字典定义',
  `follower_user_id` bigint NOT NULL DEFAULT '0' COMMENT '最后一次跟进人用户ID',
  `follow_time` datetime DEFAULT NULL COMMENT '最后一次跟进时间',
  `follow_remark` text COMMENT '跟进情况备注',
  `follow_cnt` int NOT NULL DEFAULT '0' COMMENT '跟进次数',
  `leader_remark` varchar(512) NOT NULL DEFAULT '' COMMENT '上级评价',
  `customer_remark` varchar(255) NOT NULL DEFAULT '' COMMENT '客户备注',
  `call_remark` varchar(255) NOT NULL DEFAULT '' COMMENT '电话备注',
  `apply_date` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间（进本平台件的时间）',
  `owner_user_id` bigint NOT NULL DEFAULT '0' COMMENT '当前数据归属人用户ID',
  `owner_dept_id` bigint NOT NULL DEFAULT '0' COMMENT '当前数据归属部门ID',
  `owner_favorite` tinyint NOT NULL DEFAULT '0' COMMENT '收藏标识。 0:再分配客户; 1:我的客户; 2:重点客户 ',
  `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `gmt_modified` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改时间',
  `version` bigint NOT NULL DEFAULT '0' COMMENT '数据版本信息.用于乐观锁',
  `hukou_province` varchar(64) DEFAULT NULL COMMENT 'Hukou province',
  `hukou_city` varchar(64) DEFAULT NULL COMMENT 'Hukou city',
  `hukou_district` varchar(64) DEFAULT NULL COMMENT 'Hukou district',
  `hukou_address_detail` varchar(128) DEFAULT NULL COMMENT 'Hukou address detail',
  `current_province` varchar(64) DEFAULT NULL COMMENT 'Current province',
  `current_city` varchar(64) DEFAULT NULL COMMENT 'Current city',
  `current_district` varchar(64) DEFAULT NULL COMMENT 'Current district',
  `current_street` varchar(64) DEFAULT NULL COMMENT 'Current street',
  `current_address_detail` varchar(128) DEFAULT NULL COMMENT 'Current address detail',
  PRIMARY KEY (`id`),
  UNIQUE KEY `unq_mobile_name` (`mobile`,`name`),
  KEY `IDX_FOLLOW_TIME` (`follow_time`),
  KEY `IDX_APPLY_DATE` (`apply_date`),
  KEY `IDX_NAME` (`name`),
  KEY `IDX_OWNER_USER_ID` (`owner_user_id`),
  KEY `IDX_OWNER_DEPT_ID` (`owner_dept_id`),
  KEY `IDX_CUSTOMER_GROUP_ID` (`customer_group`),
  KEY `IDX_ID_CARD_NO` (`id_card_no`),
  KEY `IDX_PROGRESS` (`progress`),
  KEY `IDX_MOBILE_MD5` (`mobile_md5`),
  KEY `IDX_CHANNEL` (`channel`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='客户信息数据';



# 转储表 customer_info_item_trace
# ------------------------------------------------------------

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



# 转储表 id_card_area
# ------------------------------------------------------------

DROP TABLE IF EXISTS `id_card_area`;

CREATE TABLE `id_card_area` (
  `code` varchar(6) NOT NULL,
  `name` varchar(64) NOT NULL,
  PRIMARY KEY (`code`),
  KEY `idx_id_card_area_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;



# 转储表 mobile_prefix_area
# ------------------------------------------------------------

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



# 转储表 region_area
# ------------------------------------------------------------

DROP TABLE IF EXISTS `region_area`;

CREATE TABLE `region_area` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `parent_id` bigint NOT NULL DEFAULT '0',
  `name` varchar(64) NOT NULL,
  `level` tinyint NOT NULL,
  `code` varchar(32) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_region_code` (`code`),
  KEY `idx_region_parent` (`parent_id`),
  KEY `idx_region_level` (`level`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;



# 转储表 risk_control_report
# ------------------------------------------------------------

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



# 转储表 su_org_department_info
# ------------------------------------------------------------

DROP TABLE IF EXISTS `su_org_department_info`;

CREATE TABLE `su_org_department_info` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `dept_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '部门名称',
  `dept_code` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '部门编号',
  `parent_dept_id` bigint NOT NULL DEFAULT '0' COMMENT '上级部门 ID. 0表示无上级部门',
  `introduction` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '部门简介',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态。 1：正常。 0：禁用',
  `create_by` bigint NOT NULL DEFAULT '0' COMMENT '创建人用户 ID',
  `last_modified_by` bigint NOT NULL DEFAULT '0' COMMENT '最后修改人用户 ID',
  `delete_flag` bigint NOT NULL DEFAULT '0' COMMENT '删除标识 0-正常; 其它-已删除',
  `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '新增记录时间',
  `gmt_modified` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新记录时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `unq_name` (`dept_name`,`delete_flag`),
  KEY `idx_parent_id` (`parent_dept_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='组织架构部门信息';



# 转储表 su_user_info
# ------------------------------------------------------------

DROP TABLE IF EXISTS `su_user_info`;

CREATE TABLE `su_user_info` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `user_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '用户名,即用户登录账号，不可重复',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '用户密码。 存储散列后的密码值',
  `nick_name` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '用户昵称，可重复',
  `real_name` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '真实姓名',
  `phone` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '用户手机号',
  `email` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '用户邮箱',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '用户状态。 1：正常。 0：禁用',
  `sex` tinyint NOT NULL DEFAULT '0' COMMENT '性别。 0:保密, 1:男, 2:女',
  `head_img` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '头像图片地址',
  `birthday` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '生日 yyyy-MM-dd格式',
  `employee_no` varchar(32) NOT NULL DEFAULT '' COMMENT '工号',
  `department_id` bigint NOT NULL DEFAULT '-1' COMMENT '部门id',
  `job_name` varchar(32) NOT NULL DEFAULT '' COMMENT '职位',
  `roles` varchar(1024) NOT NULL DEFAULT '' COMMENT '权限列表， JSON 数组格式',
  `online_status` tinyint NOT NULL DEFAULT '0' COMMENT '用户在线状态。 1：在线。 0：离线',
  `create_by` bigint NOT NULL DEFAULT '0' COMMENT '创建人用户 ID',
  `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '新增记录时间',
  `gmt_modified` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新记录时间',
  `delete_flag` bigint NOT NULL DEFAULT '0' COMMENT '账户状态 0-正常; 其它-已注销',
  PRIMARY KEY (`id`),
  UNIQUE KEY `unq_username_phone` (`user_name`,`delete_flag`),
  UNIQUE KEY `unq_phone` (`phone`,`delete_flag`),
  KEY `idx_emp_no` (`employee_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户及员工信息';



# 转储表 su_user_login_trace
# ------------------------------------------------------------

DROP TABLE IF EXISTS `su_user_login_trace`;

CREATE TABLE `su_user_login_trace` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `user_id` bigint NOT NULL DEFAULT '-1' COMMENT '用户id',
  `user_name` varchar(64) NOT NULL DEFAULT '' COMMENT '登录账号',
  `login_ip` varchar(20) NOT NULL DEFAULT '' COMMENT '登录ip',
  `login_date` char(10) NOT NULL DEFAULT '' COMMENT '登录日期',
  `grant_type` int NOT NULL DEFAULT '1' COMMENT '认证模式。1:用户名密码认证; 2:短信验证码认证;',
  `client_id` varchar(64) NOT NULL DEFAULT '' COMMENT '客户端类型ID',
  `login_result` varchar(32) NOT NULL DEFAULT '' COMMENT '登录结果, 参见业务枚举',
  `jwt_expired_at` datetime DEFAULT NULL COMMENT 'JWT失效时间',
  `refresh_token` varchar(255) NOT NULL DEFAULT '' COMMENT '刷新 JWT 的令牌 TOKEN',
  `refresh_token_expired_at` datetime DEFAULT NULL COMMENT 'refresh_token失效时间',
  `login_remark` varchar(255) NOT NULL DEFAULT '' COMMENT '备注',
  `jwt_status` varchar(16) NOT NULL DEFAULT 'OK' COMMENT 'JWT 的状态，详见业务枚举',
  `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `gmt_modified` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新记录时间',
  PRIMARY KEY (`id`),
  KEY `idx_create_time` (`gmt_create`),
  KEY `idx_user_name` (`user_name`),
  KEY `idx_jwt_expire` (`jwt_expired_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户登录日志';



# 转储表 sys_config
# ------------------------------------------------------------

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




/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
