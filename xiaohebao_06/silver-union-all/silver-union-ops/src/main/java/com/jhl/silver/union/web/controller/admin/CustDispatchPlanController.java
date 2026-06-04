package com.jhl.silver.union.web.controller.admin;

import com.jhl.silver.union.biz.customer.service.CustDispatchService;
import com.jhl.silver.union.biz.data.UserContext;
import com.jhl.silver.union.commons.SuResult;
import com.jhl.silver.union.commons.utils.SuResultUtils;
import com.jhl.silver.union.web.data.admin.DispatchPlanDTO;
import com.jhl.silver.union.web.data.admin.SaveDispatchPlanRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sys/cust/dispatch-plan")
@Tag(name = "数据分配方案管理")
public class CustDispatchPlanController {
    @Resource
    private CustDispatchService custDispatchService;

    @GetMapping
    @Operation(summary = "获取当前数据分配方案")
    public SuResult<DispatchPlanDTO> getDispatchPlan() {
        return SuResultUtils.successResult(custDispatchService.getActivePlan());
    }

    @PostMapping
    @Operation(summary = "保存数据分配方案")
    public SuResult<Void> saveDispatchPlan(@RequestBody SaveDispatchPlanRequest request) {
        custDispatchService.saveDispatchPlan(request, UserContext.getUserId());
        return SuResultUtils.successResult();
    }
}
