package com.jhl.silver.union.web.data.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 用户极简信息
 *
 * @author: qingren
 * @create_time: 2025/4/2
 */
@Data
@Accessors(chain = true)
@Schema(description = "用户极简信息")
public class UserItemInfo {
    /**
     * 用户 ID
     */
    @Schema(description = "用户 ID")
    private Long id;
    /**
     * 用户真实姓名
     */
    @Schema(description = "用户真实姓名")
    private String name;
    /**
     * 部门 ID
     */
    private Long deptId;
    /**
     * 部门名称
     */
    @Schema(description = "部门名称")
    private String deptName;

    /**
     * 在线状态
     */
    @Schema(description = "在线状态. 1:在线")
    private Integer onlineStatus;
}
