package com.jhl.silver.union.web.data.stats;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Map;

@Data
@Accessors(chain = true)
@Schema(description = "渠道推送趋势")
public class ChannelPushTrendVO {
    @Schema(description = "日期列表")
    private List<String> dates;

    @Schema(description = "渠道列表")
    private List<String> channels;

    @Schema(description = "按渠道分组的每日推送量")
    private Map<String, List<Integer>> series;
}
