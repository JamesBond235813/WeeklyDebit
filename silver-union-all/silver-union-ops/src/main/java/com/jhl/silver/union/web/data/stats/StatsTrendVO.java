package com.jhl.silver.union.web.data.stats;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
@Schema(description = "趋势分析数据")
public class StatsTrendVO {
    @Schema(description = "日期列表")
    private List<String> dates;

    @Schema(description = "新增客户趋势")
    private List<Integer> newCustomers;

    @Schema(description = "跟进次数趋势")
    private List<Integer> followUps;

    @Schema(description = "订单数量趋势")
    private List<Integer> orderCounts;
}
