
export interface OrderModel {
    id: number;
    customerId: number;
    platformId: number;
    deviceModel: string;
    deviceQuantity?: number;
    status: string;
    downPaymentAmount?: number;
    isDownPaymentAdvanced?: boolean;
    recyclePrice?: number;
    agreedRecyclePrice?: number;
    channelCommission?: number;
    resalePrice?: number;
    grossProfit?: number;
    trackingNoPlatform?: string;
    trackingNoCustomer?: string;
    platformRecvProvince?: string;
    platformRecvCity?: string;
    platformRecvDistrict?: string;
    platformRecvStreet?: string;
    platformRecvDetail?: string;
    selfRecvProvince?: string;
    selfRecvCity?: string;
    selfRecvDistrict?: string;
    selfRecvStreet?: string;
    selfRecvDetail?: string;
    ownerUserId: number;
    ownerDeptId: number;
    remark?: string;
    gmtCreate: string;
    gmtModified: string;

    // Joins (if backend returns)
    customerName?: string;
    customerMobile?: string;
    platformName?: string;
    ownerUserName?: string;
}

export interface OrderPageReq {
    page: number;
    pageSize: number;
    status?: string;
    statusList?: string[];
    customerKeyword?: string;
    platformKeyword?: string;
    ownerUserId?: number;
    ownerDeptId?: number;
    orderTimeStart?: string;
    orderTimeEnd?: string;
}

export interface OrderCreateReq {
    customerId: number;
    platformId: number;
    deviceModel: string;
    deviceQuantity?: number;
    downPaymentAmount?: number;
    isDownPaymentAdvanced?: boolean;
    remark?: string;
    agreedRecyclePrice?: number;
    channelCommission?: number;
    platformRecvProvince?: string;
    platformRecvCity?: string;
    platformRecvDistrict?: string;
    platformRecvStreet?: string;
    platformRecvDetail?: string;
    selfRecvProvince?: string;
    selfRecvCity?: string;
    selfRecvDistrict?: string;
    selfRecvStreet?: string;
    selfRecvDetail?: string;
}

export interface OrderStatusUpdateReq {
    id: number;
    targetStatus: string;
    deviceModel?: string;
    deviceQuantity?: number;
    downPaymentAmount?: number;
    isDownPaymentAdvanced?: boolean;
    agreedRecyclePrice?: number;
    channelCommission?: number;
    trackingNoPlatform?: string;
    trackingNoCustomer?: string;
    platformRecvProvince?: string;
    platformRecvCity?: string;
    platformRecvDistrict?: string;
    platformRecvStreet?: string;
    platformRecvDetail?: string;
    selfRecvProvince?: string;
    selfRecvCity?: string;
    selfRecvDistrict?: string;
    selfRecvStreet?: string;
    selfRecvDetail?: string;
    recyclePrice?: number;
    resalePrice?: number;
    remark?: string;
}

export interface ExpressTraceItem {
    time?: string;
    context?: string;
}

export interface ExpressTrackResult {
    trackingNo?: string;
    companyCode?: string;
    companyName?: string;
    status?: string;
    state?: string;
    message?: string;
    traces?: ExpressTraceItem[];
}

export const OrderStatusMap: Record<string, string> = {
    APPLIED: '申请中',
    APPROVED: '已通过/待发货',
    SHIPPED: '平台已发货',
    FORWARDED: '客户已转寄',
    RECEIVED: '我方已收货',
    PAID: '已付尾款/结清',
    RESOLD: '已出售/变现',
    CANCELLED: '已取消',
    REJECTED: '平台拒绝'
};

export const OrderStatusColorMap: Record<string, string> = {
    APPLIED: 'processing',     // Blue
    APPROVED: 'cyan',
    SHIPPED: 'purple',
    FORWARDED: 'geekblue',
    RECEIVED: 'orange',
    PAID: 'lime',
    RESOLD: 'success',         // Green
    CANCELLED: 'default',
    REJECTED: 'error'
};
