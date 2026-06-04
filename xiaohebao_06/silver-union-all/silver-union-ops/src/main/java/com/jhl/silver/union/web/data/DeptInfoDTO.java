package com.jhl.silver.union.web.data;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 部门信息
 *
 * @author: qingren
 * @create_time: 2025/3/25
 */
@Data
@Accessors(chain = true)
@Schema(description = "部门信息")
public class DeptInfoDTO {

    /**
     * 部门ID
     */
    @Schema(description = "部门ID")
    private Long id;

    /**
     * 部门名称
     */
    @Schema(description = "部门名称")
    private String deptName;

    /**
     * 部门编号
     */
    @Schema(description = "部门编号")
    private String deptCode;

    /**
     * 上级部门 ID
     */
    @Schema(description = "上级部门 ID")
    private Long parentDeptId;

    /**
     * 上级部门 名称
     */
    @Schema(description = "上级部门 名称")
    private String parentDeptName;

    /**
     * 部门简介
     */
    @Schema(description = "部门简介")
    private String introduction;

    /**
     * 状态。 1：正常。 0：禁用
     */
    @Schema(description = "状态。 1：正常。 0：禁用")
    private Integer status;

    /**
     * 状态名称
     */
    @Schema(description = "状态名称")
    private String statusDesc;

    /**
     * 创建人用户 ID
     */
    @Schema(description = "创建人用户 ID")
    private Long createBy;

    /**
     * 创建人用户名称（格式为realName(username)）
     */
    @Schema(description = "创建人用户名称（格式为realName(username)）")
    private String createByUserName;

    /**
     * 最后修改人用户 ID
     */
    @Schema(description = "最后修改人用户 ID")
    private Long lastModifiedBy;

    /**
     * 最后修改人用户名称（格式为realName(username)）
     */
    @Schema(description = "最后修改人用户名称（格式为realName(username)）")
    private String lastModifiedByUserName;

    /**
     * 新增记录时间
     */
    @Schema(description = "新增记录时间, yyyy-MM-dd HH:mm:ss")
    private String gmtCreateStr;

    /**
     * 更新记录时间
     */
    @Schema(description = "更新记录时间, yyyy-MM-dd HH:mm:ss")
    private String gmtModifiedStr;

}
