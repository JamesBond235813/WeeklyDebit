package com.jhl.silver.union.web.data.stats;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
@Schema(description = "星标公海趋势")
public class StarPublicPoolTrendVO {
    @Schema(description = "日期列表")
    private List<String> dates;

    @Schema(description = "每日进入公海的星标客户数")
    private List<Integer> starEntryCounts;

    @Schema(description = "查询时点仍在公海的星标客户数")
    private List<Integer> currentStarCounts;
}
