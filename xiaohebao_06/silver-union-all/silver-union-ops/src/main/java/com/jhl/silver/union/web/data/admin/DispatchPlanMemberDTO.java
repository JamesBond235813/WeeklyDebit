package com.jhl.silver.union.web.data.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@Schema(description = "数据分配方案成员信息")
public class DispatchPlanMemberDTO {
    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "用户姓名")
    private String userName;

    @Schema(description = "部门ID")
    private Long deptId;

    @Schema(description = "部门名称")
    private String deptName;

    @Schema(description = "每日上限")
    private Integer dailyLimit;

    @Schema(description = "排序号")
    private Integer sortNo;

    @Schema(description = "状态 1:启用 0:停用")
    private Integer status;
}
