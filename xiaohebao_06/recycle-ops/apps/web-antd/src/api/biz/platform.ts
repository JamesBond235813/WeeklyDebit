import { requestClient } from '#/api/request';
import type { PlatformModel, PlatformPageReq, PlatformSaveReq } from './model/platformModel';

enum Api {
    Page = '/biz/platform/page',
    Save = '/biz/platform/save',
    Delete = '/biz/platform/delete',
    Options = '/biz/platform/options',
}

export const getPlatformPage = (params: PlatformPageReq) => {
    return requestClient.post<any>(Api.Page, params);
};

export const savePlatform = (params: PlatformSaveReq) => {
    return requestClient.post<boolean>(Api.Save, params);
};

export const deletePlatform = (id: number) => {
    return requestClient.post<boolean>(Api.Delete, { id });
};

export const getPlatformOptions = () => {
    return requestClient.get<PlatformModel[]>(Api.Options);
};
