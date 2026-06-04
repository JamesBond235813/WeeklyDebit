DROP TABLE IF EXISTS su_org_department_info;
CREATE TABLE su_org_department_info
(
    id               BIGINT                             NOT NULL AUTO_INCREMENT COMMENT '',
    dept_name        VARCHAR(64) CHARACTER SET utf8mb4  NOT NULL DEFAULT '' COMMENT '部门名称',
    dept_code        VARCHAR(128) CHARACTER SET utf8mb4 NOT NULL DEFAULT '' COMMENT '部门编号',
    parent_dept_id   BIGINT                             NOT NULL DEFAULT 0 COMMENT '上级部门 ID. 0表示无上级部门',
    introduction     VARCHAR(255) CHARACTER SET utf8mb4 NOT NULL DEFAULT '' COMMENT '部门简介',
    status           TINYINT                            NOT NULL DEFAULT '1' COMMENT '状态。 1：正常。 0：禁用',
    create_by        BIGINT                             NOT NULL DEFAULT 0 COMMENT '创建人用户 ID',
    last_modified_by BIGINT                             NOT NULL DEFAULT 0 COMMENT '最后修改人用户 ID',
    delete_flag      BIGINT                             NOT NULL DEFAULT 0 COMMENT '删除标识 0-正常; 其它-已删除',
    gmt_create       DATETIME                           NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '新增记录时间',
    gmt_modified     DATETIME                                    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新记录时间',
    PRIMARY KEY (id),
    UNIQUE KEY unq_name (dept_name, delete_flag),
    INDEX idx_parent_id (parent_dept_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='组织架构部门信息';
