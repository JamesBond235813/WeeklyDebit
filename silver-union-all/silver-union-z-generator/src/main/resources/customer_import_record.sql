# customer_import_record
DROP TABLE IF EXISTS customer_import_record;
CREATE TABLE customer_import_record
(
    id             BIGINT       NOT NULL AUTO_INCREMENT COMMENT 'ID',
    ori_file_name  VARCHAR(512) NOT NULL COMMENT '原始文件名',
    real_file_name VARCHAR(512) NOT NULL COMMENT '服务器落盘的文件绝对路径',
    total_cnt      BIGINT       NOT NULL DEFAULT 0 COMMENT '文件总数据量',
    processed_cnt  BIGINT       NOT NULL DEFAULT 0 COMMENT '已处理的数据量',
    inserted_cnt   BIGINT       NOT NULL DEFAULT 0 COMMENT '入库数据量',
    proc_status    VARCHAR(32)  NOT NULL DEFAULT 'INIT' COMMENT '处理状态。参数业务枚举',
    target_dept_id BIGINT       NOT NULL DEFAULT 0 COMMENT '分配数据的目标部门ID',
    target_user_id BIGINT       NOT NULL DEFAULT 0 COMMENT '分配数据的目标用户ID',
    existed_data_file_name VARCHAR(512) NOT NULL DEFAULT '' COMMENT '未导入数据的文件绝对路径',
    finish_time    DATETIME COMMENT '导入完成时间',
    cost_mil_sec   BIGINT COMMENT '操作耗费的时间',
    error_msg      VARCHAR(512) NOT NULL DEFAULT '' COMMENT '错误信息',
    opt_user_id    BIGINT       NOT NULL COMMENT '操作人用户ID',
    opt_dept_id        BIGINT       NOT NULL DEFAULT 0 COMMENT '操作人部门ID',
    gmt_create     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    gmt_modified   DATETIME              DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改时间',
    PRIMARY KEY (id),
    INDEX file_name (ori_file_name),
    INDEX uid (opt_user_id),
    index opt_dept(opt_dept_id)


) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='导入客户信息的操作记录';



