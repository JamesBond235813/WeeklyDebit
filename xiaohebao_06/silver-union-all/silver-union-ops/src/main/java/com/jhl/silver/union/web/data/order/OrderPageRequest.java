package com.jhl.silver.union.web.data.order;

import com.jhl.silver.union.web.data.BasePagedRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "订单分页查询请求")
public class OrderPageRequest extends BasePagedRequest {

    @Schema(description = "客户姓名/手机号(模糊)")
    private String customerKeyword;

    @Schema(description = "平台名称(模糊)")
    private String platformKeyword;

    @Schema(description = "订单状态")
    private String status;

    @Schema(description = "订单状态列表")
    private java.util.List<String> statusList;

    @Schema(description = "归属人ID")
    private Long ownerUserId;

    @Schema(description = "归属部门ID")
    private Long ownerDeptId;

    @Schema(description = "订单时间起始(yyyy-MM-dd HH:mm:ss)")
    private String orderTimeStart;

    @Schema(description = "订单时间结束(yyyy-MM-dd HH:mm:ss)")
    private String orderTimeEnd;
}
