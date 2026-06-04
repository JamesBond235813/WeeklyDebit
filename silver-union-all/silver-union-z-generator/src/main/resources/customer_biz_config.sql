DROP TABLE IF EXISTS biz_dict_config;
CREATE TABLE biz_dict_config
(
    id           BIGINT      NOT NULL AUTO_INCREMENT COMMENT 'ID',
    biz_type     VARCHAR(32) NOT NULL COMMENT '业务类型， 见BizDictConfigTypeEnum枚举定义',
    type_desc    VARCHAR(64) NOT NULL COMMENT '业务类型描述',
    int_value    INT         NOT NULL DEFAULT 0 COMMENT '业务字典值',
    label        VARCHAR(16) NOT NULL COMMENT '业务字典值对应的显示名称',
    DESCRIPTION  VARCHAR(64) NOT NULL DEFAULT '' COMMENT '业务字典项说明',
    status       TINYINT     NOT NULL DEFAULT '1' COMMENT '状态。 1：正常。 0：禁用',
    opt_user_id  BIGINT      NOT NULL COMMENT '操作数据变更的用户 ID',
    gmt_create   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    gmt_modified DATETIME             DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改时间',
    PRIMARY KEY (id),
    UNIQUE KEY unq_type_value (biz_type, int_value)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='业务字典配置信息。 这里配置的字典信息仅用于标记、展示，不进行业务处理';



