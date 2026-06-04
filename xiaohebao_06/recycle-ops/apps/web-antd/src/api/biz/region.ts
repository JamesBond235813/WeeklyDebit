import { requestClient } from '#/api/request';

export interface RegionItem {
  id: number;
  name: string;
  level: number;
  leaf: boolean;
}

export interface IdCardAreaInfo {
  province?: string;
  city?: string;
  district?: string;
}

export const regionApi = {
  listChildren: async (parentId?: number) => {
    const pid = parentId ?? 0;
    return requestClient.get<RegionItem[]>(`/region/children?parentId=${pid}`);
  },
  getIdCardArea: async (idCardNo: string) => {
    return requestClient.get<IdCardAreaInfo>(
      `/region/id-card-area?idCardNo=${encodeURIComponent(idCardNo)}`,
    );
  },
};
