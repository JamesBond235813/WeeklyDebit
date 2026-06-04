package com.jhl.silver.union.web.data.customer;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@Schema(description = "客户提醒汇总")
public class CustomerNoticeSummaryDTO {

    @Schema(description = "新入库客户数量")
    private Integer newCustomerCount;

    @Schema(description = "新分配客户数量")
    private Integer assignedCustomerCount;

    @Schema(description = "统计起始时间")
    private String sinceTime;
}
