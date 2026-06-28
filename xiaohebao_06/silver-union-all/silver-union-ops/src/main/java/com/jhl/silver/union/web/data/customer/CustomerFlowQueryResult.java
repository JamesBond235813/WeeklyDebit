package com.jhl.silver.union.web.data.customer;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@Schema(description = "客户流转查询结果")
public class CustomerFlowQueryResult {

    @Schema(description = "是否查到客户")
    private Boolean found;

    @Schema(description = "自然语言摘要")
    private String summary;

    @Schema(description = "客户ID")
    private Long customerId;

    @Schema(description = "客户姓名")
    private String customerName;

    @Schema(description = "手机号")
    private String mobile;

    @Schema(description = "当前归属人")
    private String currentOwner;

    @Schema(description = "当前归属部门")
    private String currentDept;

    @Schema(description = "当前状态")
    private String currentStatus;

    @Schema(description = "流转节点")
    private List<CustomerFlowNode> nodes = new ArrayList<>();

    @Data
    @Accessors(chain = true)
    @Schema(description = "客户流转节点")
    public static class CustomerFlowNode {

        @Schema(description = "节点时间")
        private String time;

        @Schema(description = "节点标题")
        private String title;

        @Schema(description = "节点说明")
        private String description;

        @Schema(description = "操作人")
        private String operator;

        @Schema(description = "节点类型")
        private String type;
    }
}
