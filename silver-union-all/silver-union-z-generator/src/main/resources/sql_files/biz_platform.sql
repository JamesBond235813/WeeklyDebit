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
