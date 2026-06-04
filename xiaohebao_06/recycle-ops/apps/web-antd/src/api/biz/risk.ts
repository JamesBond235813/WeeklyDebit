import { requestClient } from '#/api/request';

export interface CustomerSearchModel {
  keyword: string;
}

export interface RiskReportRequest {
  name?: string;
  idCard?: string;
  phone?: string;
}

export interface RiskHistoryRequest {
  keyword?: string;
  page?: number;
  pageSize?: number;
}

/**
 * 搜索客户
 */
export function searchCustomers(keyword: string) {
  return requestClient.get<any[]>('/risk/search', { params: { keyword } });
}

/**
 * 获取风控报告
 */
export function getRiskReport(data: RiskReportRequest) {
  return requestClient.post<any>('/risk/report', data);
}

/**
 * 查询风控报告历史
 */
export function getRiskHistory(data: RiskHistoryRequest) {
  return requestClient.post<any>('/risk/history', data);
}
