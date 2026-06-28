package com.jhl.silver.union.web.data.admin;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.jhl.silver.union.commons.CommonConstant;
import com.jhl.silver.union.commons.utils.SuDateUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

@Data
@Accessors(chain = true)
@Schema(description = "风险地区提醒配置")
public class RiskRegionItemDTO {
    @Schema(description = "主键ID")
    private Long id;
    @Schema(description = "地区类型 RISK/BLACK")
    private String regionType;
    @Schema(description = "省份")
    private String province;
    @Schema(description = "城市")
    private String city;
    @Schema(description = "创建时间")
    @JsonFormat(pattern = SuDateUtils.DF_YYYY_MM_DDHHMMSS, timezone = CommonConstant.DEFAULT_TIME_ZONE)
    private Date gmtCreate;
}
