package com.jhl.silver.union.web.data.stats;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@Data
@Accessors(chain = true)
@Schema(description = "周期指标汇总")
public class StatsPeriodKpiVO {
    @Schema(description = "毛收入/毛利总额")
    private BigDecimal grossProfit;

    @Schema(description = "渠道返佣总额")
    private BigDecimal channelCommission;

    @Schema(description = "成交数")
    private Integer orderCount;

    @Schema(description = "进行中订单数")
    private Integer pendingOrderCount;

    @Schema(description = "新增客户数")
    private Integer newCustomers;
}
