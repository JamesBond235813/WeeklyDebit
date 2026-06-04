DROP TABLE IF EXISTS su_user_login_trace;
CREATE TABLE su_user_login_trace
(
    id                       BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键id',
    user_id                  BIGINT(20)          NOT NULL DEFAULT '-1' COMMENT '用户id',
    user_name                VARCHAR(64)         NOT NULL DEFAULT '' COMMENT '登录账号',
    login_ip                 VARCHAR(20)         NOT NULL DEFAULT '' COMMENT '登录ip',
    login_date               CHAR(10)            NOT NULL DEFAULT '' COMMENT '登录日期',
    grant_type               INT(11)             NOT NULL DEFAULT 1 COMMENT '认证模式。1:用户名密码认证; 2:短信验证码认证;', -- 预留字段
    client_id                VARCHAR(64)         NOT NULL DEFAULT '' COMMENT '客户端类型ID',
    login_result             VARCHAR(32)         NOT NULL DEFAULT '' COMMENT '登录结果, 参见业务枚举',
    jwt_expired_at          DATETIME COMMENT 'JWT失效时间',
    refresh_token            VARCHAR(255)        NOT NULL DEFAULT '' COMMENT '刷新 JWT 的令牌 TOKEN',
    refresh_token_expired_at DATETIME COMMENT 'refresh_token失效时间',
    login_remark             VARCHAR(255)        NOT NULL DEFAULT '' COMMENT '备注',
    jwt_status               VARCHAR(16)         NOT NULL DEFAULT 'OK' COMMENT 'JWT 的状态，详见业务枚举',
    gmt_create               DATETIME            NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    gmt_modified             DATETIME                     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新记录时间',
    PRIMARY KEY (id),
    INDEX idx_create_time (gmt_create),
    INDEX idx_user_name (user_name),
    INDEX idx_jwt_expire (jwt_expired_at)

) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='用户登录日志';