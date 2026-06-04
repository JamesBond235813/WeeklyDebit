import { requestClient } from '#/api/request';

export interface DispatchPlanMember {
  userId: number;
  deptId: number;
  userName?: string;
  deptName?: string;
  dailyLimit: number;
  sortNo?: number;
  status?: number;
}

export interface DispatchPlan {
  id?: number;
  mode: string;
  effectStart?: string;
  effectEnd?: string;
  members?: DispatchPlanMember[];
}

export const dispatchApi = {
  getPlan: () => {
    return requestClient.get<DispatchPlan>('/sys/cust/dispatch-plan');
  },
  savePlan: (params: DispatchPlan) => {
    return requestClient.post('/sys/cust/dispatch-plan', params);
  },
};
