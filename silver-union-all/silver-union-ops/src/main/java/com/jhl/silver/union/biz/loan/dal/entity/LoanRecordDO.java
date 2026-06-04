package com.jhl.silver.union.biz.loan.dal.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Getter
@Setter
@ToString
@Accessors(chain = true)
@TableName("loan_record")
@Schema(name = "LoanRecordDO", description = "授信放款记录")
public class LoanRecordDO implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private Long customerId;
    private Long ownerUserId;
    private Long ownerDeptId;
    private BigDecimal loanAmount;
    private Integer loanTerm;
    private BigDecimal serviceFeeRate;
    private BigDecimal receivableAmount;
    private BigDecimal repaymentAmount;
    private String loanStatus;
    private String remark;
    private Date gmtCreate;
    private Date gmtModified;
}
