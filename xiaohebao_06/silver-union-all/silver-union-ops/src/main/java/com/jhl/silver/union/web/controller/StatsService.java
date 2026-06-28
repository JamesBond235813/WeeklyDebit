package com.jhl.silver.union.web.controller;

import com.jhl.silver.union.biz.common.enums.UserAuthRoleEnum;
import com.jhl.silver.union.web.data.stats.ChannelPushTrendVO;
import com.jhl.silver.union.web.data.stats.DispatchStatsItemVO;
import com.jhl.silver.union.web.data.stats.HourlyAdmissionStatsVO;
import com.jhl.silver.union.web.data.stats.SalesAssignmentStatsItemVO;
import com.jhl.silver.union.web.data.stats.StarPublicPoolTrendVO;
import com.jhl.silver.union.web.data.stats.StatsDashboardVO;

import java.util.Set;
import java.util.List;

public interface StatsService {
    /**
     * 获取数据看板汇总信息
     * 
     * @param optUserId     操作人ID
     * @param optUserDeptId 操作人部门ID
     * @param roles         角色列表
     * @return 汇总信息
     */
    StatsDashboardVO getDashboardSummary(Long optUserId, Long optUserDeptId, Set<UserAuthRoleEnum> roles);

    /**
     * 获取客户分配统计
     *
     * @param optUserId     操作人ID
     * @param optUserDeptId 操作人部门ID
     * @param roles         角色列表
     * @return 分配统计
     */
    List<DispatchStatsItemVO> getDispatchSummary(Long optUserId, Long optUserDeptId, Set<UserAuthRoleEnum> roles);

    ChannelPushTrendVO getChannelPushTrend(int days);

    ChannelPushTrendVO getChannelPushTrend(String startDate, String endDate, String channel, boolean admissionOnly);

    List<String> getDashboardChannels();

    List<SalesAssignmentStatsItemVO> getSalesAssignmentStats(String startDate, String endDate, Long optUserDeptId,
                                                             Set<UserAuthRoleEnum> roles);

    StarPublicPoolTrendVO getStarPublicPoolTrend(int days);

    StarPublicPoolTrendVO getStarPublicPoolTrend(String startDate, String endDate);

    HourlyAdmissionStatsVO getTodayHourlyAdmissionStats(String channel);
}
