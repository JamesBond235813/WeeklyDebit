-- cust_push_record
DROP TABLE IF EXISTS cust_push_record;
CREATE TABLE cust_push_record
(
    id           BIGINT      NOT NULL AUTO_INCREMENT COMMENT 'ID',
    channel_id   INT         NOT NULL DEFAULT 0 COMMENT '推送渠道ID',
    app_id       VARCHAR(64)          DEFAULT NULL COMMENT '三方平台APP ID',
    request_id   VARCHAR(64)          DEFAULT NULL COMMENT '三方平台请求ID',
    channel_name VARCHAR(16) NOT NULL COMMENT '渠道名称',
    type         TINYINT     NOT NULL COMMENT '数据类型。0:撞库数据,1:进件数据',
    cust_name    VARCHAR(64) NOT NULL DEFAULT '' COMMENT '客户姓名',
    mobile       VARCHAR(32) NOT NULL DEFAULT '' COMMENT '客户手机号',
    order_no     VARCHAR(32) NOT NULL DEFAULT '' COMMENT '推送方订单号',
    existed_flag TINYINT     NOT NULL DEFAULT 0 COMMENT '收到的数据是否已存在。 1: 存在 其它不存在',
    received_req TEXT COMMENT '接收的请求内容明文',
    remark       TEXT COMMENT '备注信息',
    gmt_create   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    gmt_modified DATETIME             DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_app_request (app_id, request_id),
    INDEX idx_channel (channel_id),
    INDEX idx_mobile_name (mobile, cust_name)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='推送数据记录';

-- 生产环境已执行 20251207
-- ALTER TABLE cust_push_record
--     ADD COLUMN order_no     VARCHAR(32) NOT NULL DEFAULT '' COMMENT '推送方订单号' after mobile;

-- 生产环境待执行
-- ALTER TABLE cust_push_record
--     ADD COLUMN app_id VARCHAR(64) DEFAULT NULL COMMENT '三方平台APP ID' AFTER channel_id,
--     ADD COLUMN request_id VARCHAR(64) DEFAULT NULL COMMENT '三方平台请求ID' AFTER app_id,
--     ADD UNIQUE KEY uk_app_request (app_id, request_id);
