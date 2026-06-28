package com.jhl.silver.union.web.data.stats;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@Schema(description = "业务员分配统计")
public class SalesAssignmentStatsItemVO {
    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "用户姓名")
    private String userName;

    @Schema(description = "部门ID")
    private Long deptId;

    @Schema(description = "部门名称")
    private String deptName;

    @Schema(description = "自动分配数量")
    private Integer autoCount;

    @Schema(description = "公海领取数量")
    private Integer manualCount;

    @Schema(description = "公海领取中带星数量")
    private Integer starManualCount;

    @Schema(description = "合计数量")
    private Integer totalCount;
}
