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

export interface CustomerFlowQueryRequest {
  name?: string;
  mobile?: string;
}

export interface CustomerFlowNode {
  time?: string;
  title?: string;
  description?: string;
  operator?: string;
  type?: string;
}

export interface CustomerFlowQueryResult {
  found?: boolean;
  summary?: string;
  customerId?: number;
  customerName?: string;
  mobile?: string;
  currentOwner?: string;
  currentDept?: string;
  currentStatus?: string;
  nodes?: CustomerFlowNode[];
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
 * 获取全景雷达报告
 */
export function getRadarReport(data: RiskReportRequest) {
  return requestClient.post<any>('/risk/radar-report', data);
}

/**
 * 查询风控报告历史
 */
export function getRiskHistory(data: RiskHistoryRequest) {
  return requestClient.post<any>('/risk/history', data);
}

/**
 * 查询客户流转
 */
export function queryCustomerFlow(data: CustomerFlowQueryRequest) {
  return requestClient.post<CustomerFlowQueryResult>('/risk/customer-flow', data);
}
