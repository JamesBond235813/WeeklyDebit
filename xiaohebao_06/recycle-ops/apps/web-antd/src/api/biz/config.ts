import { requestClient } from '#/api/request';

export type BizDictType =
  | 'PROGRESS'
  | 'CUSTOMER_STAR_GROUP'
  | 'CALL_RESULT_TIPS'
  | 'DATA_CHANNEL'
  | 'CUSTOMER_LIST_FIELD'
  | 'CUSTOMER_BLACK_REGION'
  | 'CUSTOMER_RISK_REGION'
  | 'ZHIMA_SCORE_THRESHOLD';

export interface BizDictItemDto {
  id: number;
  bizType: BizDictType;
  intValue: number;
  label: string;
  description?: string;
  status: number;
  sortNo?: number;
}

export interface EnableRecvConfigDto {
  enable: boolean;
}

export type RiskRegionType = 'BLACK' | 'RISK';

export interface RiskRegionItemDto {
  id: number;
  regionType: RiskRegionType;
  province: string;
  city: string;
  gmtCreate?: string;
}

export interface RegionItemDto {
  id: number;
  name: string;
  level: number;
  leaf: boolean;
}

export const bizConfigApi = {
  pageList: async (bizType: BizDictType, validOnly = false) => {
    return requestClient.post<BizDictItemDto[]>('/sys/biz-cnf/page-list', {
      bizType,
      validOnly,
    });
  },
  add: async (item: Partial<BizDictItemDto>) => {
    return requestClient.post('/sys/biz-cnf/add-biz-config', item);
  },
  update: async (item: Partial<BizDictItemDto>) => {
    return requestClient.post('/sys/biz-cnf/upd-biz-config', item);
  },
  getEnableRecv: async () => {
    return requestClient.post<EnableRecvConfigDto>('/sys/biz-cnf/get-enable-recv');
  },
  updateEnableRecv: async (config: EnableRecvConfigDto) => {
    return requestClient.post('/sys/biz-cnf/upd-enable-recv', config);
  },
  listRiskRegions: async (regionType: RiskRegionType) => {
    return requestClient.get<RiskRegionItemDto[]>(
      `/sys/risk-region/list?regionType=${regionType}`,
    );
  },
  addRiskRegion: async (item: {
    city: string;
    province: string;
    regionType: RiskRegionType;
  }) => {
    return requestClient.post('/sys/risk-region/add', item);
  },
  deleteRiskRegion: async (id: number) => {
    return requestClient.post('/sys/risk-region/delete', { id });
  },
  listRegionChildren: async (parentId = 0) => {
    return requestClient.get<RegionItemDto[]>(
      `/region/children?parentId=${parentId}`,
    );
  },
};
