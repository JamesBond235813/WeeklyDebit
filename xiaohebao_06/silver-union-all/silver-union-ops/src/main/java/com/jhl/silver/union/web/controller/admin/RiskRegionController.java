package com.jhl.silver.union.web.controller.admin;

import com.jhl.silver.union.biz.common.ResultCode;
import com.jhl.silver.union.biz.common.enums.UserAuthRoleEnum;
import com.jhl.silver.union.biz.customer.service.CustRiskRegionService;
import com.jhl.silver.union.biz.data.UserContext;
import com.jhl.silver.union.commons.SuResult;
import com.jhl.silver.union.commons.exception.BizException;
import com.jhl.silver.union.commons.utils.SuResultUtils;
import com.jhl.silver.union.web.data.admin.AddRiskRegionRequest;
import com.jhl.silver.union.web.data.admin.DeleteRiskRegionRequest;
import com.jhl.silver.union.web.data.admin.RiskRegionItemDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/sys/risk-region")
@Tag(name = "系统管理类接口: 风险地区提醒")
public class RiskRegionController {

    @Resource
    private CustRiskRegionService custRiskRegionService;

    @GetMapping("/list")
    @Operation(summary = "查询风险/黑名单地区配置")
    public SuResult<List<RiskRegionItemDTO>> list(@RequestParam("regionType") String regionType) {
        verifyAuth();
        return SuResultUtils.successResult(custRiskRegionService.list(regionType));
    }

    @PostMapping("/add")
    @Operation(summary = "新增风险/黑名单地区配置")
    public SuResult<Void> add(@RequestBody AddRiskRegionRequest request) {
        verifyAuth();
        custRiskRegionService.add(request, UserContext.getUserId());
        return SuResultUtils.successResult();
    }

    @PostMapping("/delete")
    @Operation(summary = "删除风险/黑名单地区配置")
    public SuResult<Void> delete(@RequestBody DeleteRiskRegionRequest request) {
        verifyAuth();
        custRiskRegionService.delete(request.getId());
        return SuResultUtils.successResult();
    }

    private void verifyAuth() {
        if (!UserContext.hasAnyRole(UserAuthRoleEnum.ROLE_SUPPER)) {
            throw new BizException(ResultCode.SYS_NO_AUTH, "risk region config");
        }
    }
}
