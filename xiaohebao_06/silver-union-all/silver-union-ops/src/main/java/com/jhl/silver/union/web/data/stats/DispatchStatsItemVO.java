package com.jhl.silver.union.web.data.stats;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@Schema(description = "客户分配统计")
public class DispatchStatsItemVO {
    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "用户姓名")
    private String userName;

    @Schema(description = "部门ID")
    private Long deptId;

    @Schema(description = "部门名称")
    private String deptName;

    @Schema(description = "今日分配数量")
    private Integer todayCount;

    @Schema(description = "昨日分配数量")
    private Integer yesterdayCount;

    @Schema(description = "近7日分配数量")
    private Integer recent7Count;
}
