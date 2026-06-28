package com.jhl.silver.union.web.controller;

import com.jhl.silver.union.biz.common.ResultCode;
import com.jhl.silver.union.biz.common.enums.UserAuthRoleEnum;
import com.jhl.silver.union.biz.data.UserContext;
import com.jhl.silver.union.commons.SuResult;
import com.jhl.silver.union.commons.exception.BizException;
import com.jhl.silver.union.web.data.stats.ChannelPushTrendVO;
import com.jhl.silver.union.web.data.stats.StatsDashboardVO;
import com.jhl.silver.union.web.data.stats.DispatchStatsItemVO;
import com.jhl.silver.union.web.data.stats.HourlyAdmissionStatsVO;
import com.jhl.silver.union.web.data.stats.SalesAssignmentStatsItemVO;
import com.jhl.silver.union.web.data.stats.StarPublicPoolTrendVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

    @GetMapping("/dashboard/channel-push-trend")
    @Operation(summary = "获取各渠道推送趋势")
    public SuResult<ChannelPushTrendVO> getChannelPushTrend(@RequestParam(defaultValue = "7") Integer days) {
        assertDashboardAdmin();
        return new SuResult<ChannelPushTrendVO>()
                .setData(statsService.getChannelPushTrend(days == null ? 7 : days))
                .setCode(0);
    }

    @GetMapping("/dashboard/channel-trend")
    @Operation(summary = "获取渠道每日入库或准入趋势")
    public SuResult<ChannelPushTrendVO> getChannelTrend(@RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam(required = false) String channel,
            @RequestParam(defaultValue = "false") Boolean admissionOnly) {
        assertDashboardAdmin();
        return new SuResult<ChannelPushTrendVO>()
                .setData(statsService.getChannelPushTrend(startDate, endDate, channel,
                        Boolean.TRUE.equals(admissionOnly)))
                .setCode(0);
    }

    @GetMapping("/dashboard/channels")
    @Operation(summary = "获取看板渠道筛选项")
    public SuResult<List<String>> getDashboardChannels() {
        assertDashboardAdmin();
        return new SuResult<List<String>>().setData(statsService.getDashboardChannels()).setCode(0);
    }

    @GetMapping("/dashboard/sales-assignment")
    @Operation(summary = "获取业务员分配统计")
    public SuResult<List<SalesAssignmentStatsItemVO>> getSalesAssignmentStats(@RequestParam String startDate,
            @RequestParam String endDate) {
        assertDashboardAdmin();
        List<SalesAssignmentStatsItemVO> list = statsService.getSalesAssignmentStats(startDate, endDate,
                UserContext.getDeptId(), UserContext.getRoles());
        return new SuResult<List<SalesAssignmentStatsItemVO>>().setData(list).setCode(0);
    }

    @GetMapping("/dashboard/star-public-pool-trend")
    @Operation(summary = "获取星标公海趋势")
    public SuResult<StarPublicPoolTrendVO> getStarPublicPoolTrend(@RequestParam(defaultValue = "7") Integer days) {
        assertDashboardAdmin();
        return new SuResult<StarPublicPoolTrendVO>()
                .setData(statsService.getStarPublicPoolTrend(days == null ? 7 : days))
                .setCode(0);
    }

    @GetMapping("/dashboard/star-public-pool-entry-trend")
    @Operation(summary = "获取星标客户每日进入公海趋势")
    public SuResult<StarPublicPoolTrendVO> getStarPublicPoolEntryTrend(@RequestParam String startDate,
            @RequestParam String endDate) {
        assertDashboardAdmin();
        return new SuResult<StarPublicPoolTrendVO>()
                .setData(statsService.getStarPublicPoolTrend(startDate, endDate))
                .setCode(0);
    }

    @GetMapping("/dashboard/hourly-admission")
    @Operation(summary = "获取当天每小时准入统计")
    public SuResult<HourlyAdmissionStatsVO> getTodayHourlyAdmissionStats(@RequestParam(required = false) String channel) {
        assertDashboardAdmin();
        return new SuResult<HourlyAdmissionStatsVO>()
                .setData(statsService.getTodayHourlyAdmissionStats(channel))
                .setCode(0);
    }

    private void assertDashboardAdmin() {
        if (!UserContext.hasAnyRole(UserAuthRoleEnum.ROLE_SUPPER, UserAuthRoleEnum.ROLE_DEPT_DATA_ADMIN)) {
            throw new BizException(ResultCode.SYS_NO_AUTH, "dashboard stats");
        }
    }
}
