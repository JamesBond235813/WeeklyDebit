package com.jhl.silver.union.web.data.loan;

import com.jhl.silver.union.web.data.BasePagedRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class LoanRecordPageRequest extends BasePagedRequest {
    @Schema(description = "客户ID")
    private Long customerId;
    @Schema(description = "归属人ID")
    private Long ownerUserId;
    @Schema(description = "归属部门ID")
    private Long ownerDeptId;
    @Schema(description = "放款状态")
    private String loanStatus;
}
