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
 * 用户登录日志
 * </p>
 *
 * @author Way
 * @since 2025-03-20 01:01:17
 */
@Getter
@Setter
@ToString
@Accessors(chain = true)
@TableName("su_user_login_trace")
public class SuUserLoginTraceDO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 登录账号
     */
    private String userName;

    /**
     * 登录ip
     */
    private String loginIp;

    /**
     * 登录日期
     */
    private String loginDate;

    /**
     * 认证模式。1:用户名密码认证; 2:短信验证码认证;
     */
    private Integer grantType;

    /**
     * 客户端类型ID
     */
    private String clientId;

    /**
     * 登录结果, 参见业务枚举
     */
    private String loginResult;

    /**
     * JWT失效时间
     */
    private Date jwtExpiredAt;

    /**
     * 刷新 JWT 的令牌 TOKEN
     */
    private String refreshToken;

    /**
     * refresh_token失效时间
     */
    private Date refreshTokenExpiredAt;

    /**
     * 备注
     */
    private String loginRemark;

    /**
     * JWT 的状态，详见业务枚举
     */
    private String jwtStatus;

    /**
     * 创建时间
     */
    private Date gmtCreate;

    /**
     * 更新记录时间
     */
    private Date gmtModified;

    public static final String ID = "id";

    public static final String USER_ID = "user_id";

    public static final String USER_NAME = "user_name";

    public static final String LOGIN_IP = "login_ip";

    public static final String LOGIN_DATE = "login_date";

    public static final String GRANT_TYPE = "grant_type";

    public static final String CLIENT_ID = "client_id";

    public static final String LOGIN_RESULT = "login_result";

    public static final String JWT__EXPIRED_AT = "jwt__expired_at";

    public static final String REFRESH_TOKEN = "refresh_token";

    public static final String REFRESH_TOKEN_EXPIRED_AT = "refresh_token_expired_at";

    public static final String LOGIN_REMARK = "login_remark";

    public static final String JWT_STATUS = "jwt_status";

    public static final String GMT_CREATE = "gmt_create";

    public static final String GMT_MODIFIED = "gmt_modified";
}
