package com.jhl.silver.union.web.data.risk;

import com.jhl.silver.union.web.data.BasePagedRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "风控报告查询历史分页请求")
public class RiskHistoryPageRequest extends BasePagedRequest {

    @Schema(description = "姓名/手机号/身份证号")
    private String keyword;
}
