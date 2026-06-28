package com.jhl.silver.union.web.data.stats;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
@Schema(description = "小时准入统计")
public class HourlyAdmissionStatsVO {
    @Schema(description = "小时标签")
    private List<String> hours;

    @Schema(description = "每小时符合准入条件的客户量")
    private List<Integer> passedCounts;

    @Schema(description = "每小时准入通过率，单位百分比")
    private List<Double> passRates;
}
