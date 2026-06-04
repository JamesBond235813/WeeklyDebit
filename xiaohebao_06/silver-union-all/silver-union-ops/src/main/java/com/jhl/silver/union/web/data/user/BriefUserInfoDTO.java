package com.jhl.silver.union.web.data.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 用户简要信息
 *
 * @author: qingren
 * @create_time: 2025/3/24
 */
@Data
@Accessors(chain = true)
@Schema(description = "用户简要信息")
public class BriefUserInfoDTO {
    /**
     * 头像
     */
    @Schema(description = "用户头像")
    private String avatar;
    /**
     * 用户 ID
     */
    @Schema(description = "用户 ID")
    private String userId;

    /**
     * 用户真实姓名
     */
    @Schema(description = "用户真实姓名")
    private String realName;

    /**
     * 用户账号（登录用户名）
     */
    @Schema(description = "用户账号（登录用户名）")
    private String username;

    /**
     * 用户角色列表
     */
    @Schema(description = "用户角色列表")
    private List<String> roles;

    /**
     * 部门id
     */
    @Schema(description = "部门ID")
    private Long departmentId;

    /**
     * 用户在线状态。 1：在线。 0：离线
     */
    @Schema(description = "用户在线状态。 1：在线。 0：离线")
    private Integer onlineStatus;
}
