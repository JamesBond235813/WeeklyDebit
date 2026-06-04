import { requestClient } from '#/api/request';

export interface LoanRecord {
  id?: number;
  customerId?: number;
  ownerUserId?: number;
  ownerDeptId?: number;
  loanAmount?: number;
  loanTerm?: number;
  serviceFeeRate?: number;
  receivableAmount?: number;
  repaymentAmount?: number;
  loanStatus?: string;
  remark?: string;
  gmtCreate?: string;
  gmtModified?: string;
}

export interface LoanRecordPageParams {
  page?: number;
  pageSize?: number;
  customerId?: number;
  ownerUserId?: number;
  ownerDeptId?: number;
  loanStatus?: string;
}

export const loanApi = {
  page: (params: LoanRecordPageParams) => {
    return requestClient.post<any>('/biz/loan-record/page', params);
  },
  save: (params: LoanRecord) => {
    return requestClient.post<boolean>('/biz/loan-record/save', params);
  },
  detail: (id: number) => {
    return requestClient.get<LoanRecord>(`/biz/loan-record/detail/${id}`);
  },
  statusOptions: () => {
    return requestClient.get<{ label: string; value: string }[]>(
      '/biz/loan-record/status-options',
    );
  },
};
