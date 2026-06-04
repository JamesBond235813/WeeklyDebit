DROP TABLE IF EXISTS su_user_info;
CREATE TABLE `su_user_info`
(
    `id`            BIGINT                             NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `user_name`     VARCHAR(64) CHARACTER SET utf8mb4  NOT NULL DEFAULT '' COMMENT '用户名,即用户登录账号，不可重复',
    `password`      VARCHAR(255) CHARACTER SET utf8mb4 NOT NULL DEFAULT '' COMMENT '用户密码。 存储散列后的密码值',
    `nick_name`     VARCHAR(32) CHARACTER SET utf8mb4  NOT NULL DEFAULT '' COMMENT '用户昵称，可重复',
    `real_name`     VARCHAR(32) CHARACTER SET utf8mb4  NOT NULL DEFAULT '' COMMENT '真实姓名',
    `phone`         VARCHAR(32) CHARACTER SET utf8mb4  NOT NULL DEFAULT '' COMMENT '用户手机号',
    `email`         VARCHAR(128) CHARACTER SET utf8mb4 NOT NULL DEFAULT '' COMMENT '用户邮箱',
    `status`        TINYINT                            NOT NULL DEFAULT '1' COMMENT '用户状态。 1：正常。 0：禁用',
    `sex`           TINYINT                            NOT NULL DEFAULT '0' COMMENT '性别。 0:保密, 1:男, 2:女',
    `head_img`      VARCHAR(255) CHARACTER SET utf8mb4 NOT NULL DEFAULT '' COMMENT '头像图片地址',
    `birthday`      VARCHAR(10) CHARACTER SET utf8mb4  NOT NULL DEFAULT '' COMMENT '生日 yyyy-MM-dd格式',
    `employee_no`   VARCHAR(32)                        NOT NULL DEFAULT '' COMMENT '工号',
    `department_id` BIGINT(20)                         NOT NULL DEFAULT '-1' COMMENT '部门id',
    `job_name`      VARCHAR(32)                        NOT NULL DEFAULT '' COMMENT '职位',
    roles           VARCHAR(1024)                      NOT NULL DEFAULT '' COMMENT '权限列表， JSON 数组格式',
    online_status   TINYINT                            NOT NULL DEFAULT 0 COMMENT '用户在线状态。 1：在线。 0：离线',
    create_by       BIGINT                             NOT NULL DEFAULT 0 COMMENT '创建人用户 ID',
    `gmt_create`    DATETIME                           NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '新增记录时间',
    `gmt_modified`  DATETIME                                    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新记录时间',
    `delete_flag`   BIGINT                             NOT NULL DEFAULT 0 COMMENT '账户状态 0-正常; 其它-已注销',
    PRIMARY KEY (`id`),
    UNIQUE KEY `unq_username_phone` (`user_name`, delete_flag),
    UNIQUE KEY unq_phone (phone, delete_flag),
    INDEX idx_emp_no (employee_no)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='用户及员工信息';

