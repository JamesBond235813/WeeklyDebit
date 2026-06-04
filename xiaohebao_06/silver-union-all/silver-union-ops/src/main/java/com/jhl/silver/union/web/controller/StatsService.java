package com.jhl.silver.union.web.controller;

import com.jhl.silver.union.biz.common.enums.UserAuthRoleEnum;
import com.jhl.silver.union.web.data.stats.StatsDashboardVO;
import com.jhl.silver.union.web.data.stats.DispatchStatsItemVO;

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
}
