package com.jhl.silver.union.web.data.order;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@Schema(description = "快递轨迹项")
public class ExpressTraceItem {

    @Schema(description = "时间")
    private String time;

    @Schema(description = "轨迹描述")
    private String context;
}
