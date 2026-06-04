package com.jhl.silver.union.web.controller.risk;

import com.jhl.silver.union.biz.risk.dal.entity.RiskControlReportDO;
import com.jhl.silver.union.biz.risk.service.RiskReportService;
import com.jhl.silver.union.biz.data.UserContext;
import com.jhl.silver.union.commons.CommonResultCode;
import com.jhl.silver.union.commons.SuResult;
import com.jhl.silver.union.commons.exception.BizException;
import com.jhl.silver.union.commons.utils.SuResultUtils;
import com.jhl.silver.union.web.data.risk.RiskCustomerItemDTO;
import com.jhl.silver.union.web.data.risk.RiskReportRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/risk")
@Tag(name = "风控报告")
@Slf4j
public class RiskController {

    private final RiskReportService riskReportService;

    public RiskController(RiskReportService riskReportService) {
        this.riskReportService = riskReportService;
    }

    @GetMapping("/search")
    @Operation(summary = "搜索客户(风控报告)")
    public SuResult<List<RiskCustomerItemDTO>> searchCustomers(@RequestParam("keyword") String keyword) {
        List<RiskCustomerItemDTO> result = riskReportService.searchCustomers(
                keyword, UserContext.getUserId(), UserContext.getDeptId(), UserContext.getRoles());
        return SuResultUtils.successResult(result);
    }

    @PostMapping("/report")
    @Operation(summary = "获取风控报告")
    public SuResult<RiskControlReportDO> getReport(@RequestBody RiskReportRequest request) {
        if (request == null
                || StringUtils.isBlank(request.getName())
                || StringUtils.isBlank(request.getIdCard())
                || StringUtils.isBlank(request.getPhone())) {
            return SuResultUtils.failedResult(
                    CommonResultCode.INVALID_PARAMS.getCode(),
                    "Missing parameters",
                    BizException.BIZ_CODE);
        }
        try {
            RiskControlReportDO report = riskReportService.getReport(
                    request.getName(),
                    request.getIdCard(),
                    request.getPhone(),
                    UserContext.getUserId(),
                    UserContext.getDeptId(),
                    UserContext.getRoles());
            return SuResultUtils.successResult(report);
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error fetching risk report", e);
            return SuResultUtils.failedResult(
                    CommonResultCode.REMOTE_FAILED.getCode(),
                    e.getMessage(),
                    BizException.BIZ_CODE);
        }
    }
}
