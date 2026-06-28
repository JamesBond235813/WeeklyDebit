import { requestClient } from '#/api/request';

enum Api {
    DASHBOARD_SUMMARY = '/stats/dashboard/summary',
    DISPATCH_SUMMARY = '/stats/dispatch/summary',
    CHANNEL_PUSH_TREND = '/stats/dashboard/channel-push-trend',
    CHANNEL_TREND = '/stats/dashboard/channel-trend',
    DASHBOARD_CHANNELS = '/stats/dashboard/channels',
    HOURLY_ADMISSION = '/stats/dashboard/hourly-admission',
    SALES_ASSIGNMENT = '/stats/dashboard/sales-assignment',
    STAR_PUBLIC_POOL_TREND = '/stats/dashboard/star-public-pool-trend',
    STAR_PUBLIC_POOL_ENTRY_TREND = '/stats/dashboard/star-public-pool-entry-trend',
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

export interface ChannelPushTrendVO {
    dates: string[];
    channels: string[];
    series: Record<string, number[]>;
}

export interface SalesAssignmentStatsItemVO {
    userId: number;
    userName: string;
    deptId: number;
    deptName: string;
    autoCount: number;
    manualCount: number;
    starManualCount: number;
    totalCount: number;
}

export interface StarPublicPoolTrendVO {
    dates: string[];
    starEntryCounts: number[];
    currentStarCounts: number[];
}

export interface HourlyAdmissionStatsVO {
    hours: string[];
    passedCounts: number[];
    passRates: number[];
}

export const getDashboardSummary = () => {
    return requestClient.get<StatsDashboardVO>(Api.DASHBOARD_SUMMARY);
};

export const getDispatchSummary = () => {
    return requestClient.get<DispatchStatsItemVO[]>(Api.DISPATCH_SUMMARY);
};

export const getChannelPushTrend = (days: 3 | 7) => {
    return requestClient.get<ChannelPushTrendVO>(Api.CHANNEL_PUSH_TREND, { params: { days } });
};

export const getChannelTrend = (
    startDate: string,
    endDate: string,
    channel?: string,
    admissionOnly = false,
) => {
    return requestClient.get<ChannelPushTrendVO>(Api.CHANNEL_TREND, {
        params: { startDate, endDate, channel, admissionOnly },
    });
};

export const getDashboardChannels = () => {
    return requestClient.get<string[]>(Api.DASHBOARD_CHANNELS);
};

export const getSalesAssignmentStats = (startDate: string, endDate: string) => {
    return requestClient.get<SalesAssignmentStatsItemVO[]>(Api.SALES_ASSIGNMENT, {
        params: { startDate, endDate },
    });
};

export const getStarPublicPoolTrend = (days: 3 | 7) => {
    return requestClient.get<StarPublicPoolTrendVO>(Api.STAR_PUBLIC_POOL_TREND, { params: { days } });
};

export const getStarPublicPoolEntryTrend = (startDate: string, endDate: string) => {
    return requestClient.get<StarPublicPoolTrendVO>(Api.STAR_PUBLIC_POOL_ENTRY_TREND, {
        params: { startDate, endDate },
    });
};

export const getTodayHourlyAdmissionStats = (channel?: string) => {
    return requestClient.get<HourlyAdmissionStatsVO>(Api.HOURLY_ADMISSION, { params: { channel } });
};
