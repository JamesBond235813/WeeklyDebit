package com.jhl.silver.union.web.controller;

import com.jhl.silver.union.biz.common.enums.UserAuthRoleEnum;
import com.jhl.silver.union.biz.data.UserContext;
import com.jhl.silver.union.commons.SuResult;
import com.jhl.silver.union.web.data.stats.StatsDashboardVO;
import com.jhl.silver.union.web.data.stats.DispatchStatsItemVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;
import java.util.List;

@RestController
@RequestMapping("/stats")
@Tag(name = "数据看板接口")
public class StatsController {

    @Resource
    private StatsService statsService;

    @GetMapping("/dashboard/summary")
    @Operation(summary = "获取数据看板汇总信息")
    public SuResult<StatsDashboardVO> getDashboardSummary() {
        Long userId = UserContext.getUserId();
        Long deptId = UserContext.getDeptId();
        Set<UserAuthRoleEnum> roles = UserContext.getRoles();

        StatsDashboardVO vo = statsService.getDashboardSummary(userId, deptId, roles);
        return new SuResult<StatsDashboardVO>().setData(vo).setCode(0);
    }

    @GetMapping("/dispatch/summary")
    @Operation(summary = "获取客户分配统计")
    public SuResult<List<DispatchStatsItemVO>> getDispatchSummary() {
        Long userId = UserContext.getUserId();
        Long deptId = UserContext.getDeptId();
        Set<UserAuthRoleEnum> roles = UserContext.getRoles();
        List<DispatchStatsItemVO> list = statsService.getDispatchSummary(userId, deptId, roles);
        return new SuResult<List<DispatchStatsItemVO>>().setData(list).setCode(0);
    }
}
