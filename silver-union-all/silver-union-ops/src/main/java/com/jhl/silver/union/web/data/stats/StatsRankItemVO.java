package com.jhl.silver.union.web.data.stats;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@Schema(description = "排行榜项")
public class StatsRankItemVO {
    @Schema(description = "排名")
    private Integer rank;

    @Schema(description = "姓名")
    private String name;

    @Schema(description = "部门名称")
    private String department;

    @Schema(description = "新增客户数")
    private Integer newCustomers;

    @Schema(description = "跟进次数")
    private Integer followUps;
}
