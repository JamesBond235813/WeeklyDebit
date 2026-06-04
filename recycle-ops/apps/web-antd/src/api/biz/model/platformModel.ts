
export interface PlatformModel {
    id: number;
    name: string;
    type: string; // 'INSTALLMENT' | 'RENTAL'
    link?: string;
    contactInfo?: string;
    status: number; // 1: Enable, 0: Disable
    remark?: string;
    gmtCreate?: string;
    gmtModified?: string;
}

export interface PlatformPageReq {
    page: number;
    pageSize: number;
    name?: string;
    type?: string;
    status?: number;
}

export interface PlatformSaveReq {
    id?: number;
    name: string;
    type: string;
    link?: string;
    contactInfo?: string;
    status: number;
    remark?: string;
}
