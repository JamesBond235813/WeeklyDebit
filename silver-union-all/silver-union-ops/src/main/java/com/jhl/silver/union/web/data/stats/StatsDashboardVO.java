package com.jhl.silver.union.web.data.stats;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
@Schema(description = "数据看板汇总信息")
public class StatsDashboardVO {
    @Schema(description = "核心指标")
    private StatsKpiVO kpi;

    @Schema(description = "今日指标")
    private StatsPeriodKpiVO todayKpi;

    @Schema(description = "近7日指标")
    private StatsPeriodKpiVO recent7Kpi;

    @Schema(description = "近30日指标")
    private StatsPeriodKpiVO recent30Kpi;

    @Schema(description = "趋势分析")
    private StatsTrendVO trend;

    @Schema(description = "漏斗分析")
    private List<StatsItemVO> funnel;

    @Schema(description = "排行榜")
    private List<StatsRankItemVO> leaderboard;

    @Schema(description = "分布统计")
    private StatsDistributionVO distribution;
}
