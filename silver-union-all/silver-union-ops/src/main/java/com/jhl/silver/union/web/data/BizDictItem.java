package com.jhl.silver.union.web.data;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 业务字典信息
 *
 * @author: qingren
 * @create_time: 2025/3/29
 */
@Data
@Accessors(chain = true)
@Schema(description = "业务字典信息")
public class BizDictItem {

    /**
     * 业务类型， 见BizDictConfigTypeEnum枚举定义
     */
    @Schema(description = "业务类型。 CALL_RESULT_TIPS：电话结果标签； CUSTOMER_STAR_GROUP：客户星级组别； PROGRESS：沟通(跟进)进度；DATA_CHANNEL：客户信息来源的渠道")
    private String bizType;

    /**
     * 业务字典值
     */
    @Schema(description = "业务字典值")
    private Integer intValue;

    /**
     * 字典值对应的显示名称
     */
    @Schema(description = "字典值对应的显示名称")
    private String label;

    /**
     * 业务字典说明
     */
    @Schema(description = "业务字典说明")
    private String description;
}
