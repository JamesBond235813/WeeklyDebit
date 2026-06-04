package com.jhl.silver.union.biz.user.dal.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
/**
 * <p>
 * 用户及员工信息
 * </p>
 *
 * @author Way
 * @since 2025-03-29 00:28:23
 */
@Getter
@Setter
@ToString
@Accessors(chain = true)
@TableName("su_user_info")
public class SuUserInfoDO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户名,即用户登录账号，不可重复
     */
    private String userName;

    /**
     * 用户密码。 存储散列后的密码值
     */
    private String password;

    /**
     * 用户昵称，可重复
     */
    private String nickName;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 用户手机号
     */
    private String phone;

    /**
     * 用户邮箱
     */
    private String email;

    /**
     * 用户状态。 1：正常。 0：禁用
     */
    private Integer status;

    /**
     * 性别。 0:保密, 1:男, 2:女
     */
    private Integer sex;

    /**
     * 头像图片地址
     */
    private String headImg;

    /**
     * 生日 yyyy-MM-dd格式
     */
    private String birthday;

    /**
     * 工号
     */
    private String employeeNo;

    /**
     * 部门id
     */
    private Long departmentId;

    /**
     * 职位
     */
    private String jobName;

    /**
     * 权限角色列表， JSON 数组格式
     */
    private String roles;

    /**
     * 用户在线状态。 1：在线。 0：离线
     */
    private Integer onlineStatus;

    /**
     * 创建人用户 ID
     */
    private Long createBy;

    /**
     * 新增记录时间
     */
    private Date gmtCreate;

    /**
     * 更新记录时间
     */
    private Date gmtModified;

    /**
     * 账户状态 0-正常; 其它-已注销
     */
    private Long deleteFlag;

    public static final String ID = "id";

    public static final String USER_NAME = "user_name";

    public static final String PASSWORD = "password";

    public static final String NICK_NAME = "nick_name";

    public static final String REAL_NAME = "real_name";

    public static final String PHONE = "phone";

    public static final String EMAIL = "email";

    public static final String STATUS = "status";

    public static final String SEX = "sex";

    public static final String HEAD_IMG = "head_img";

    public static final String BIRTHDAY = "birthday";

    public static final String EMPLOYEE_NO = "employee_no";

    public static final String DEPARTMENT_ID = "department_id";

    public static final String JOB_NAME = "job_name";

    public static final String ROLES = "roles";

    public static final String ONLINE_STATUS = "online_status";

    public static final String CREATE_BY = "create_by";

    public static final String GMT_CREATE = "gmt_create";

    public static final String GMT_MODIFIED = "gmt_modified";

    public static final String DELETE_FLAG = "delete_flag";
}
