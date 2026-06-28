package com.jhl.silver.union.web.data;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.jhl.silver.union.commons.CommonConstant;
import com.jhl.silver.union.commons.utils.SuDateUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;
import java.util.List;

/**
 * 用户信息
 *
 * @author: qingren
 * @create_time: 2025/3/27
 */
@Data
@Accessors(chain = true)
@Schema(description = "用户信息")
public class UserInfoDTO {
    /**
     * 用户ID
     */
    @Schema(description = "用户ID")
    private Long id;

    /**
     * 用户名,即用户登录账号，不可重复
     */
    @Schema(description = "用户名,即用户登录账号，不可重复")
    private String userName;

    /**
     * 真实姓名
     */
    @Schema(description = "真实姓名")
    private String realName;

    /**
     * 用户手机号
     */
    @Schema(description = "用户手机号")
    private String phone;

    /**
     * 用户邮箱
     */
    @Schema(description = "用户邮箱")
    private String email;

    /**
     * 用户状态。 1：正常。 0：禁用
     */
    @Schema(description = "用户状态。 1：正常。 0：禁用")
    private Integer status;

    /**
     * 用户在线状态。 1：在线。 0：离线
     */
    @Schema(description = "用户在线状态。 1：在线。 0：离线")
    private Integer onlineStatus;

    /**
     * 性别。 0:保密, 1:男, 2:女
     */
    @Schema(description = "性别。 0:保密, 1:男, 2:女")
    private Integer sex;

    /**
     * 头像图片地址
     */
    @Schema(description = "头像图片地址")
    private String headImg;

    /**
     * 生日 yyyy-MM-dd格式
     */
    @Schema(description = "生日 yyyy-MM-dd格式")
    private String birthday;

    /**
     * 工号
     */
    @Schema(description = "工号")
    private String employeeNo;

    /**
     * 部门id
     */
    @Schema(description = "部门id")
    private Long departmentId;

    /**
     * 职位
     */
    @Schema(description = "职位")
    private String jobName;

    /**
     * 创建人用户 ID
     */
    @Schema(description = "创建人用户 ID")
    private Long createBy;

    /**
     * 新增记录时间
     */
    @Schema(description = "新增记录时间")
    @JsonFormat(pattern = SuDateUtils.DF_YYYY_MM_DDHHMMSS, timezone = CommonConstant.DEFAULT_TIME_ZONE)
    private Date gmtCreate;

    /**
     * 更新记录时间
     */
    @Schema(description = "更新记录时间")
    @JsonFormat(pattern = SuDateUtils.DF_YYYY_MM_DDHHMMSS, timezone = CommonConstant.DEFAULT_TIME_ZONE)
    private Date gmtModified;
    /**
     * 权限角色列表
     */
    @Schema(description = "权限角色列表")
    private List<String> roles;
    /**
     * 创建人用户名 格式:  realName(userName)
     */
    @Schema(description = "创建人用户名 格式:  realName(userName)")
    private String createByName;
    /**
     * 部门名称
     */
    @Schema(description = "部门名称")
    private String departmentName;

    /**
     * 权限角色名称列表
     */
    @Schema(description = "权限角色名称列表")
    private List<String> roleDispNames;
}
