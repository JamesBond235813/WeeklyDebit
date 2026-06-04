package com.jhl.silver.union.web.data.stats;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@Schema(description = "KPI指标")
public class StatsKpiVO {
    @Schema(description = "本月新增客户")
    private Integer newCustomers;

    @Schema(description = "今日跟进次数")
    private Integer followUps;

    @Schema(description = "待跟进客户")
    private Integer pendingFollowUps;

    @Schema(description = "转化率")
    private String conversionRate;

    @Schema(description = "累计毛利")
    private java.math.BigDecimal totalGrossProfit;

    @Schema(description = "总订单数")
    private Integer totalOrderCount;

    @Schema(description = "进行中订单")
    private Integer pendingOrders;
}
