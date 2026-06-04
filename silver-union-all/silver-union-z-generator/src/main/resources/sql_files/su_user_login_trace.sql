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
