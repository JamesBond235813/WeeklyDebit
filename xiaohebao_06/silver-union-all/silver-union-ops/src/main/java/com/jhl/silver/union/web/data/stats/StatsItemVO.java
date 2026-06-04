package com.jhl.silver.union.web.data.stats;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@Schema(description = "统计项")
public class StatsItemVO {
    @Schema(description = "名称")
    private String name;

    @Schema(description = "数值")
    private Object value;

    public StatsItemVO() {
    }

    public StatsItemVO(String name, Object value) {
        this.name = name;
        this.value = value;
    }
}
