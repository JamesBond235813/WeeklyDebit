package com.jhl.silver.union.web.controller.risk;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jhl.silver.union.biz.common.enums.UserAuthRoleEnum;
import com.jhl.silver.union.biz.customer.service.CustomerFlowQueryService;
import com.jhl.silver.union.biz.risk.dal.entity.RiskControlReportDO;
import com.jhl.silver.union.biz.risk.service.RiskReportService;
import com.jhl.silver.union.biz.data.UserContext;
import com.jhl.silver.union.commons.CommonResultCode;
import com.jhl.silver.union.commons.SuResult;
import com.jhl.silver.union.commons.exception.BizException;
import com.jhl.silver.union.commons.utils.SuResultUtils;
import com.jhl.silver.union.web.data.customer.CustomerFlowQueryRequest;
import com.jhl.silver.union.web.data.customer.CustomerFlowQueryResult;
import com.jhl.silver.union.web.data.risk.RiskCustomerItemDTO;
import com.jhl.silver.union.web.data.risk.RiskHistoryPageRequest;
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
    private final CustomerFlowQueryService customerFlowQueryService;

    public RiskController(RiskReportService riskReportService, CustomerFlowQueryService customerFlowQueryService) {
        this.riskReportService = riskReportService;
        this.customerFlowQueryService = customerFlowQueryService;
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
        return getReportInternal(request, false);
    }

    @PostMapping("/radar-report")
    @Operation(summary = "获取全景雷达报告")
    public SuResult<RiskControlReportDO> getRadarReport(@RequestBody RiskReportRequest request) {
        return getReportInternal(request, true);
    }

    private SuResult<RiskControlReportDO> getReportInternal(RiskReportRequest request, boolean radarOnly) {
        if (request == null
                || (StringUtils.isBlank(request.getName())
                && StringUtils.isBlank(request.getIdCard())
                && StringUtils.isBlank(request.getPhone()))) {
            return SuResultUtils.failedResult(
                    CommonResultCode.INVALID_PARAMS.getCode(),
                    "请至少填写姓名、身份证号、手机号中的一项",
                    BizException.BIZ_CODE);
        }
        try {
            RiskControlReportDO report = radarOnly
                    ? riskReportService.getRadarReport(
                            request.getName(),
                            request.getIdCard(),
                            request.getPhone(),
                            UserContext.getUserId(),
                            UserContext.getDeptId(),
                            UserContext.getRoles())
                    : riskReportService.getReport(
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
            log.error("Error fetching {}", radarOnly ? "radar report" : "risk report", e);
            return SuResultUtils.failedResult(
                    CommonResultCode.REMOTE_FAILED.getCode(),
                    e.getMessage(),
                    BizException.BIZ_CODE);
        }
    }

    @PostMapping("/history")
    @Operation(summary = "分页查询风控报告历史")
    public SuResult<IPage<RiskControlReportDO>> pageHistory(@RequestBody RiskHistoryPageRequest request) {
        IPage<RiskControlReportDO> result = riskReportService.pageHistory(
                request, UserContext.getUserId(), UserContext.getDeptId(), UserContext.getRoles());
        return SuResultUtils.successResult(result);
    }

    @PostMapping("/customer-flow")
    @Operation(summary = "查询客户流转")
    public SuResult<CustomerFlowQueryResult> customerFlow(@RequestBody CustomerFlowQueryRequest request) {
        if (UserContext.getRoles().contains(UserAuthRoleEnum.ROLE_OBSERVER)) {
            return SuResultUtils.failedResult(
                    CommonResultCode.INVALID_REQUEST.getCode(),
                    "观察员无权查询客户流转",
                    BizException.BIZ_CODE);
        }
        CustomerFlowQueryResult result = customerFlowQueryService.query(request);
        return SuResultUtils.successResult(result);
    }
}
