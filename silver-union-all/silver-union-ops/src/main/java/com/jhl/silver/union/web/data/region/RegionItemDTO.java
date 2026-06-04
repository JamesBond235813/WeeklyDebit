package com.jhl.silver.union.web.data.region;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@Schema(description = "Region area item")
public class RegionItemDTO {
    @Schema(description = "ID")
    private Long id;
    @Schema(description = "Name")
    private String name;
    @Schema(description = "Level")
    private Integer level;
    @Schema(description = "Leaf flag")
    private Boolean leaf;
}
