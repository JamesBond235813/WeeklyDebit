DROP TABLE IF EXISTS customer_info_item_trace;
CREATE TABLE customer_info_item_trace
(
    id                    BIGINT                            NOT NULL AUTO_INCREMENT COMMENT '更新历史记录ID',
    source_id             BIGINT                            NOT NULL COMMENT '被更新的原始客户信息ID',
    upd_type              VARCHAR(16)                       NOT NULL COMMENT '更新记录类型。见业务枚举',
    upd_field_json        TEXT                              NOT NULL COMMENT '更新的字段及更新后的内容信息，json 格式',
    upd_disp_content_json TEXT                              NOT NULL COMMENT '更新的字段及更新后的内容信息，json 格式',
    opt_user_id           BIGINT                            NOT NULL COMMENT '操作数据变更的用户 ID',
    opt_user_real_name    VARCHAR(32) CHARACTER SET utf8mb4 NOT NULL DEFAULT '' COMMENT '操作人姓名',
    gmt_create            DATETIME                          NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    gmt_modified          DATETIME                                   DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改时间',
    PRIMARY KEY (id),
    INDEX idx_source_id (source_id)

) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='客户信息数据更新历史';


