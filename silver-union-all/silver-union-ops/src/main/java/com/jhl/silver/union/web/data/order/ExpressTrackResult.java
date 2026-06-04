package com.jhl.silver.union.web.data.order;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@Schema(description = "快递轨迹查询结果")
public class ExpressTrackResult {

    @Schema(description = "物流单号")
    private String trackingNo;

    @Schema(description = "快递公司编码")
    private String companyCode;

    @Schema(description = "快递公司名称")
    private String companyName;

    @Schema(description = "查询状态码")
    private String status;

    @Schema(description = "运输状态码")
    private String state;

    @Schema(description = "返回消息")
    private String message;

    @Schema(description = "轨迹列表")
    private List<ExpressTraceItem> traces;
}
