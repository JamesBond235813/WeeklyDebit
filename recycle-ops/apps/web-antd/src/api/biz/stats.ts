import { requestClient } from '#/api/request';

enum Api {
    DASHBOARD_SUMMARY = '/stats/dashboard/summary',
    DISPATCH_SUMMARY = '/stats/dispatch/summary',
}

export interface StatsDashboardVO {
    kpi: StatsKpiVO;
    todayKpi: StatsPeriodKpiVO;
    recent7Kpi: StatsPeriodKpiVO;
    recent30Kpi: StatsPeriodKpiVO;
    trend: StatsTrendVO;
    funnel: StatsItemVO[];
    leaderboard: StatsRankItemVO[];
    distribution: StatsDistributionVO;
}

export interface StatsKpiVO {
    newCustomers: number;
    followUps: number;
    pendingFollowUps: number;
    conversionRate: string;
    totalGrossProfit: number;
    totalOrderCount: number;
    pendingOrders: number;
}

export interface StatsPeriodKpiVO {
    grossProfit: number;
    channelCommission: number;
    orderCount: number;
    pendingOrderCount: number;
    newCustomers: number;
}

export interface StatsTrendVO {
    dates: string[];
    newCustomers: number[];
    followUps: number[];
    orderCounts: number[];
}

export interface StatsItemVO {
    name: string;
    value: number;
}

export interface StatsRankItemVO {
    rank: number;
    name: string;
    department: string;
    newCustomers: number;
    followUps: number;
}

export interface StatsDistributionVO {
    region: StatsItemVO[];
    sex: StatsItemVO[];
    age: StatsItemVO[];
}

export interface DispatchStatsItemVO {
    userId: number;
    userName: string;
    deptId: number;
    deptName: string;
    todayCount: number;
    yesterdayCount: number;
    recent7Count: number;
}

export const getDashboardSummary = () => {
    return requestClient.get<StatsDashboardVO>(Api.DASHBOARD_SUMMARY);
};

export const getDispatchSummary = () => {
    return requestClient.get<DispatchStatsItemVO[]>(Api.DISPATCH_SUMMARY);
};
