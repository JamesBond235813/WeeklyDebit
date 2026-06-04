package com.jhl.silver.union.web.data.loan;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class LoanRecordSaveRequest {
    private Long id;
    @Schema(description = "客户ID")
    private Long customerId;
    @Schema(description = "放款金额")
    private BigDecimal loanAmount;
    @Schema(description = "期限")
    private Integer loanTerm;
    @Schema(description = "服务费费率")
    private BigDecimal serviceFeeRate;
    @Schema(description = "应回款金额")
    private BigDecimal receivableAmount;
    @Schema(description = "已回款金额")
    private BigDecimal repaymentAmount;
    @Schema(description = "状态")
    private String loanStatus;
    @Schema(description = "备注")
    private String remark;
}
