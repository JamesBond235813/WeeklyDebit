import { requestClient } from '#/api/request';
import type {
    ExpressTrackResult,
    OrderCreateReq,
    OrderModel,
    OrderPageReq,
    OrderStatusUpdateReq,
} from './model/orderModel';

enum Api {
    Page = '/biz/order/page',
    Create = '/biz/order/create',
    UpdateStatus = '/biz/order/update-status',
    Detail = '/biz/order/detail',
    StatusOptions = '/biz/order/status-options',
    Tracking = '/biz/order/tracking',
}

export const getOrderPage = (params: OrderPageReq) => {
    return requestClient.post<any>(Api.Page, params);
};

export const createOrder = (params: OrderCreateReq) => {
    return requestClient.post<boolean>(Api.Create, params);
};

export const updateOrderStatus = (params: OrderStatusUpdateReq) => {
    return requestClient.post<boolean>(Api.UpdateStatus, params);
};

export const getOrderDetail = (id: number) => {
    return requestClient.get<{ order: OrderModel, traces: any[] }>(`${Api.Detail}/${id}`);
};

export const getOrderStatusOptions = () => {
    return requestClient.get<{ label: string, value: string }[]>(Api.StatusOptions);
};

export const getOrderTracking = (trackingNo: string) => {
    return requestClient.get<ExpressTrackResult>(`${Api.Tracking}?trackingNo=${encodeURIComponent(trackingNo)}`);
};
