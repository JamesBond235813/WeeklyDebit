package com.jhl.silver.union.web.data.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

@Data
@Schema(description = "新增风险地区提醒配置请求")
public class AddRiskRegionRequest {
    @Schema(description = "地区类型 RISK/BLACK", requiredMode = Schema.RequiredMode.REQUIRED)
    private String regionType;
    @Schema(description = "省份", requiredMode = Schema.RequiredMode.REQUIRED)
    private String province;
    @Schema(description = "城市", requiredMode = Schema.RequiredMode.REQUIRED)
    private String city;

    public void normalize() {
        this.regionType = StringUtils.upperCase(StringUtils.trim(this.regionType));
        this.province = StringUtils.trim(this.province);
        this.city = StringUtils.trim(this.city);
    }
}
