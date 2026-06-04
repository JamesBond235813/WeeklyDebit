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
