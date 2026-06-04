package com.jhl.silver.union.web.data.stats;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
@Schema(description = "分布统计数据")
public class StatsDistributionVO {
    @Schema(description = "地域分布")
    private List<StatsItemVO> region;

    @Schema(description = "性别分布")
    private List<StatsItemVO> sex;

    @Schema(description = "年龄分布")
    private List<StatsItemVO> age;
}
