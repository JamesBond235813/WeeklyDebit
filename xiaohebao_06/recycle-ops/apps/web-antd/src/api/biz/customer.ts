import type { RequestResponse } from '@vben/request';

import type { PagedInfo } from './biz-common';

import { requestClient } from '#/api/request';

import { BizCommonApi } from './biz-common';

export interface PagedListCustomerParams {
  /* 当前页号 */
  page?: number;

  /* 单页数据条数 */
  pageSize?: number;

  /* 客户ID */
  id?: number;

  /* 姓名前缀 */
  namePrefix?: string;

  /* 手机号 */
  mobile?: string;

  /* 身份证号 */
  idCardNo?: string;

  /* 跟进状态。 见业务字典定义 */
  progress?: number;
  /* 跟进状态列表。 见业务字典定义 */
  progressList?: number[];

  /* 电话结果。见枚举定义 */
  callTips?: number;

  /* 客户分组 */
  customerGroup?: number;

  /* 最后一次跟进时间 开始时间（包含）yyyy-MM-dd HH:mm:ss 格式 */
  followTimeStart?: string;

  /* 最后一次跟进时间 截止时间（包含）yyyy-MM-dd HH:mm:ss 格式 */
  followTimeEnd?: string;

  /* 申请时间（进本平台件的时间）开始时间（包含）yyyy-MM-dd HH:mm:ss 格式 */
  applyDateStart?: string;

  /* 申请时间（进本平台件的时间）截止时间（包含）yyyy-MM-dd HH:mm:ss 格式 */
  applyDateEnd?: string;

  /* 当前数据归属人用户ID */
  ownerUserId?: number;
  /* 当前数据归属人用户ID列表 */
  userIdList?: number[];

  /* 当前数据归属企业ID */
  ownerDeptIds?: Record<string, unknown>[];

  /* 收藏标识。  */
  ownerFavorite?: number;
  /* 推广渠道ID */
  channel?: number;
  /* 推广渠道列表 */
  channelList?: number[];
  /* 未跟进天数（大于等于） */
  ignoreDays?: number;
  /* 芝麻分最小值（大于等于） */
  zhimaScoreMin?: number;
  /* 是否仅查询自己的数据 */
  selfOnly?: boolean;
  /* 是否仅查询公海数据 */
  publicPoolOnly?: boolean;
  /* 是否公海重点标识。1:☆或☆☆; 2:☆☆; 0:其他 */
  publicPoolStarFlag?: number;
  /* 地区筛选省份 */
  regionProvince?: string;
  /* 地区筛选城市 */
  regionCity?: string;
}

export interface CustomerItem {
  /* 客户ID */
  id: number;

  /* 姓名 */
  name: string;

  /* 手机号 */
  mobile: string;

  /* 手机号归属地 */
  mobileArea: string;

  /* 性别。 0:保密, 1:男, 2:女 */
  sex: number;

  /* 年龄 */
  age: number;

  /* 芝麻分 */
  zhimaScore?: number;
  hyyZhimaDesc?: string;

  /* 出生日期。 yyyy-MM-dd 格式 */
  birthday: string;

  /* 户籍所在省 */
  hukouProvince?: string;

  /* 户籍所在市 */
  hukouCity?: string;

  /* 户籍所在区县 */
  hukouDistrict?: string;

  /* 户籍详细地址 */
  hukouAddressDetail?: string;

  /* 当前所在地省 */
  currentProvince?: string;

  /* 当前所在地市 */
  currentCity?: string;

  /* 当前所在地区县 */
  currentDistrict?: string;

  /* 当前所在地街道 */
  currentStreet?: string;

  /* 当前所在地详细地址 */
  currentAddressDetail?: string;

  /* 目标贷款金额，单位元 */
  reqLoanAmount: number;
  hyyLoanAmountDesc?: string;

  /* 房产标识。0:未知; 1:有京房; 2:有房; 3:无 */
  houseFlag: number;

  /* 保单标识。0:未知; 1:有; 2:无 */
  insuranceFlag: number;

  /* 社保标识。0:未知; 1:有; 2:无 */
  socialInsuranceFlag: number;

  /* 车产标识。0:未知; 1:有; 2:无 */
  carFlag: number;

  /* 公积金标识。0:未知; 1:有; 2:无 */
  providentFlag: number;

  /* 信用卡标识。0:未知; 1:有; 2:无 */
  creditCardFlag: number;

  /* 企业主标识。0:未知; 1:是; 2:否 */
  enterpriseFlag: number;

  /* 婚姻状态. 0:未知; 1:已婚; 2:未婚; 3:离异 */
  marriageStatus: number;

  /* 公积金金额， 单位元 */
  providentAmountYuan: number;

  /* 房产价值，单位万元 */
  houseVal: number;
  hyyHouseDesc?: string;
  hyyCarDesc?: string;
  hyyProvidentDesc?: string;
  hyySocialInsuranceDesc?: string;
  hyyInsuranceDesc?: string;
  hyyOccupationDesc?: string;
  hyyOverdueDesc?: string;
  hyyIp?: string;

  /* 跟进状态。 见业务字典定义 */
  progress: number;

  /* 住址 */
  address: string;

  /* 工作单位 */
  workAddress: string;

  /* 数据来源文件名称 */
  sourceFileName: string;

  /* 推广渠道ID */
  channel: number;

  /* 上游渠道描述（channel 对应的字典名，列表展示「上游渠道」） */
  channelDesc?: string;

  /* 用户来源（上游渠道推送的 channel_id，列表展示「用户来源」） */
  userSource?: string;

  /* 电话结果。见业务字典定义 */
  callTips: number;

  /* 客户分组ID，见业务字典定义 */
  customerGroup: number;

  /* 最后一次跟进人用户ID */
  followerUserId: number;

  /* 最后一次跟进时间 */
  followTime: Record<string, unknown>;

  /* 跟进情况备注 */
  followRemark: string;

  /* 客户备注 */
  customerRemark: string;

  /* 电话备注 */
  callRemark: string;

  /* 申请时间（进本平台件的时间） */
  applyDate: Record<string, unknown>;

  /* 当前数据归属人用户ID */
  ownerUserId: number;

  /* 当前数据归属企业ID */
  ownerDeptId: number;

  /* 收藏标识。 1:收藏; 其它:未收藏 */
  ownerFavorite: number;
  /* 公海重点标识。1:显示☆; 2:显示☆☆; 0:普通客户 */
  publicPoolStarFlag?: number;

  /* 创建时间 */
  gmtCreate: Record<string, unknown>;

  /* 最后修改时间 */
  gmtModified: Record<string, unknown>;

  /* 数据版本信息. */
  version: number;

  /* 性别描述 */
  sexDesc: string;

  /* 房产标识描述 */
  houseFlagDesc: string;

  /* 保单标识描述 */
  insuranceFlagDesc: string;

  /* 社保标识描述 */
  socialInsuranceFlagDesc: string;

  /* 车产标识描述 */
  carFlagDesc: string;

  /* 公积金标识描述 */
  providentFlagDesc: string;

  /* 信用卡标识描述 */
  creditCardFlagDesc: string;

  /* 企业主标识描述 */
  enterpriseFlagDesc: string;

  /* 婚姻状态. 0:未知; 1:已婚; 2:未婚; 3:离异 */
  marriageStatusDesc: string;

  /* 跟进状态描述 */
  progressDesc: string;

  /* 电话结果描述 */
  callTipsDesc: string;

  /* 客户分组描述 */
  customerGroupDesc: string;

  /**
   * 房产有无及价值(万元)
   */
  houseFlagValDesc: string;
  /**
   * 公积金以及金额
   */
  providentFlagValDesc: string;
  /* 最后跟进人 */
  followerUserName: string;
  /* 推广渠道名称。 服务端不存在该字段*/
  channelDesc?: string;
  /* 资质描述 */
  qualification?: string;
  /* 是否命中风险地区 */
  riskRegionHit?: boolean;
  /* 是否命中黑名单地区 */
  blackRegionHit?: boolean;
  /* 身份证号 */
  idCardNo?: string;
  /* 收藏类型描述 */
  ownerFavoriteDesc?: string;
  /* 非服务端字段 ， 用于处理表格中的 checkbox*/
  key?: number;
}

export interface CustomerFieldConfigItem {
  fieldKey: string;
  label: string;
}

export interface CustomerNoticeSummary {
  newCustomerCount: number;
  assignedCustomerCount: number;
  sinceTime?: string | null;
}

export interface CustomerNoticeItem {
  id: number;
  userId: number;
  deptId: number;
  custId: number;
  custName?: string;
  custMobile?: string;
  custIdCard?: string;
  ownerUserId: number;
  ownerDeptId: number;
  ownerFavorite: number;
  noticeType: string;
  source: string;
  status: number;
  gmtCreate?: string;
}

export interface BizDictItem {
  /* 业务类型。 CALL_RESULT_TIPS：电话结果标签； CUSTOMER_STAR_GROUP：客户星级组别； PROGRESS：沟通(跟进)进度 */
  bizType: string;

  /* 业务字典值 */
  intValue: number;

  /* 字典值对应的显示名称 */
  label: string;

  /* 业务字典说明 */
  description: string;
}

/**
 * 操作记录信息
 */
export interface UpdTraceInfo {
  /* 更新记录 ID */
  id: number;

  /* 操作人姓名 */
  optUserName: string;

  /* 操作时间 */
  updTime: Record<string, unknown>;

  /* 操作描述 */
  descList: Record<string, unknown>[];

  /* 操作类型描述 */
  updTypeDesc: string;
}

export interface CustomerItemDetail extends CustomerItem {
  /**
   * 上级评价列表
   */
  leaderRemarkList: UpdTraceInfo[];
  /**
   * 跟进历史
   */
  progressList: UpdTraceInfo[];
}

export interface UpdateBizCustomerInfoParams {
  /* 客户ID */
  id: number;

  /* 手机号归属地 */
  mobileArea?: string;

  /* 身份证号 */
  idCardNo?: string;

  /* 性别。 0:保密, 1:男, 2:女 */
  sex?: number;

  /* 年龄 */
  age?: number;

  /* 出生日期。 yyyy-MM-dd 格式 */
  birthday?: string;

  /* 户籍所在省 */
  hukouProvince?: string;

  /* 户籍所在市 */
  hukouCity?: string;

  /* 户籍所在区县 */
  hukouDistrict?: string;

  /* 户籍详细地址 */
  hukouAddressDetail?: string;

  /* 当前所在地省 */
  currentProvince?: string;

  /* 当前所在地市 */
  currentCity?: string;

  /* 当前所在地区县 */
  currentDistrict?: string;

  /* 当前所在地街道 */
  currentStreet?: string;

  /* 当前所在地详细地址 */
  currentAddressDetail?: string;

  /* 目标贷款金额，单位元 */
  reqLoanAmount?: number;

  /* 芝麻分 */
  zhimaScore?: number;

  /* 房产标识。0:未知; 1:有京房; 2:有房; 3:无 */
  houseFlag?: number;

  /* 保单标识。0:未知; 1:有; 2:无 */
  insuranceFlag?: number;

  /* 社保标识。0:未知; 1:有; 2:无 */
  socialInsuranceFlag?: number;

  /* 车产标识。0:未知; 1:有; 2:无 */
  carFlag?: number;

  /* 公积金标识。0:未知; 1:有; 2:无 */
  providentFlag?: number;

  /* 信用卡标识。0:未知; 1:有; 2:无 */
  creditCardFlag?: number;

  /* 企业主标识。0:未知; 1:是; 2:否 */
  enterpriseFlag?: number;

  /* 婚姻状态. 0:未知; 1:已婚; 2:未婚; 3:离异 */
  marriageStatus?: number;

  /* 公积金金额， 单位元 */
  providentAmountYuan?: number;

  /* 房产价值，单位万元 */
  houseVal?: number;

  /* 跟进状态。 见业务字典定义 */
  progress?: number;

  /* 住址 */
  address?: string;

  /* 工作单位 */
  workAddress?: string;

  /* 电话结果。见业务字典定义 */
  callTips?: number;

  /* 客户分组，见业务字典定义 */
  customerGroup?: number;

  /* 跟进情况备注 */
  followRemark?: string;

  /* 客户备注 */
  customerRemark?: string;

  /* 电话备注 */
  callRemark?: string;

  /* 收藏标识。 1:收藏; 其它:未收藏  */
  ownerFavorite?: number;

  /* 数据版本信息.用于乐观锁 */
  version: number;
}

export interface UpdCustomerInfoParams {
  /* 客户ID */
  id?: number;
  /* 姓名 */
  name: string;

  /* 手机号 */
  mobile: string;

  /* 手机号归属地 */
  mobileArea?: string;

  /* 身份证号 */
  idCardNo?: string;

  /* 数据来源文件名称 */
  sourceFileName?: string;

  /* 推广渠道ID */
  channel?: number;

  /* 当前数据归属部门ID */
  ownerDeptId?: number;

  /* 性别。 0:保密, 1:男, 2:女 */
  sex?: number;

  /* 年龄 */
  age?: number;

  /* 芝麻分 */
  zhimaScore?: number;

  /* 出生日期。 yyyy-MM-dd 格式 */
  birthday?: string;

  /* 户籍所在省 */
  hukouProvince?: string;

  /* 户籍所在市 */
  hukouCity?: string;

  /* 户籍所在区县 */
  hukouDistrict?: string;

  /* 户籍详细地址 */
  hukouAddressDetail?: string;

  /* 当前所在地省 */
  currentProvince?: string;

  /* 当前所在地市 */
  currentCity?: string;

  /* 当前所在地区县 */
  currentDistrict?: string;

  /* 当前所在地街道 */
  currentStreet?: string;

  /* 申请时间。  yyyy-MM-dd HH:mm:ss格式 */
  applyDateStr?: any;

  /* 当前所在地详细地址 */
  currentAddressDetail?: string;
}

export interface BizDictRepo {
  CALL_RESULT_TIPS: BizDictItem[];
  CUSTOMER_STAR_GROUP: BizDictItem[];
  PROGRESS: BizDictItem[];
  DATA_CHANNEL: BizDictItem[];
}

export interface DispatchCustomerInfoParams {
  /* 客户ID 列表 */
  cids: number[];

  /* 指派对象员工的用户ID */
  ownerId?: number;

  /* 指派对象的部门ID */
  ownerDeptId?: number;
}

// Parameter interface
export interface PagedListImportRecordParams {
  /* 当前页号 */
  page?: number;

  /* 单页数据条数 */
  pageSize?: number;

  /* 是否只取当前登录用户操作的导入记录。true:是 */
  selfOnly?: boolean;
}

export interface ImportRecInfo {
  /* 操作记录的ID */
  id: number;

  /* 原始文件名 */
  oriFileName: string;

  /* 文件总数据量 */
  totalCnt: number;

  /* 已处理的数据量 */
  processedCnt: number;

  /* 入库数据量 */
  insertedCnt: number;

  /* 处理状态。 */
  procStatus: string;

  /* 处理状态说明 */
  procStatusDesc: string;

  /* 分配数据的目标部门ID */
  targetDeptId: number;

  /* 分配数据的目标部门名称 */
  targetDeptName: string;

  /* 分配数据的目标用户ID */
  targetUserId: number;

  /* 分配数据的目标用户姓名 */
  targetUserName: string;

  /* 导入完成时间 */
  finishTime: string;

  /* 操作耗费的毫秒数 */
  costMilSec: number;

  /* 操作人用户ID */
  optUserId: number;

  /* 操作人用户名称 */
  optUserName: string;

  /* 操作人部门ID */
  optDeptId: number;

  /* 操作人部门名称 */
  optDeptName: string;

  /* 是否可下载重复客户记录文件的标识。 1: 可下载。 其它不可下载 */
  downloadDupRecFlag: number;

  /* 错误信息 */
  errorMsg: string;
  /* 创建时间 */
  gmtCreate: string;
}

export const customerApi = {
  pagedListCustomerInfo: (params: PagedListCustomerParams) => {
    return requestClient
      .post<PagedInfo<CustomerItem>>('/cust/page-list-customers', params)
      .catch((error) => {
        BizCommonApi.showErrorMsg(error);
      });
  },

  getBizDictItems: async () => {
    return await requestClient.get<BizDictRepo>('/cust/list-dict-items');
  },
  getCustomerFieldConfig: () => {
    return requestClient.get<CustomerFieldConfigItem[]>('/cust/field-config');
  },
  getCustomerNoticeSummary: () => {
    return requestClient.get<CustomerNoticeSummary>('/cust/notice-summary');
  },
  getCustomerNoticeUnread: (limit = 20) => {
    return requestClient.get<CustomerNoticeItem[]>(
      `/cust/notice/unread?limit=${limit}`,
    );
  },
  markCustomerNoticeRead: (ids: number[]) => {
    return requestClient.post('/cust/notice/mark-read', { ids });
  },

  getCustomerDetail: async (id: number) => {
    return requestClient.get<CustomerItemDetail>(`/cust/detail?cid=${id}`);
  },
  updateBizCustomerInfo: async (params: UpdateBizCustomerInfoParams) => {
    return requestClient.post(`/cust/upd-biz-cust-info`, params);
  },
  optFavorite: async (cid: number, favFlag: number, version: number) => {
    await requestClient.post(`/cust/upd-biz-cust-info`, {
      id: cid,
      ownerFavorite: favFlag,
      version,
    });
  },
  /**
   * 将客户批量标记为指定收藏类型（放入对应的菜单列表）
   * @param cids
   * @param favFlag
   * @param version
   */
  batchOptFavorite: async (cids: number[], favFlag: number) => {
    await requestClient.post(`/cust/btc-swt-fav`, {
      custIdList: cids,
      favoriteType: favFlag,
    });
  },
  batchBackToOcean: async (cids: number[]) => {
    await requestClient.post(`/cust/btc-rtn-cust`, { custIdList: cids });
  },
  claimCustomer: async (cid: number) => {
    await requestClient.get(`/cust/claim-cust?cid=${cid}`);
  },
  dispatchCustomerInfo: async (params: DispatchCustomerInfoParams) => {
    // try {
    //   await requestClient.post(`/sys/cust/dispatch-cust`, params);
    // } catch (error) {
    //   BizCommonApi.showErrorMsg(error);
    // }
    await requestClient.post(`/sys/cust/dispatch-cust`, params);
  },
  addCustomerInfo: async (params: UpdCustomerInfoParams) => {
    await requestClient.post(`/sys/cust/add-cust`, params);
  },
  updCustomerInfo: async (params: UpdCustomerInfoParams) => {
    await requestClient.post(`/sys/cust/upd-cust`, params);
  },
  pagedListImportRecord: async (params: PagedListImportRecordParams) => {
    return await requestClient.post<PagedInfo<ImportRecInfo>>(
      '/sys/cust/paged-list-imp-rec',
      params,
    );
  },

  downloadExistedCustRecord: async (recId: number) => {
    const response: RequestResponse<Blob> = await requestClient.download(
      `/sys/cust/download-existed-rec?id=${recId}`,
      { responseReturn: 'raw' },
    );
    if (response.status !== 200) {
      // 不成功， 什么都不做
      return;
    }
    BizCommonApi.handleBlobResponse(response, 'download.data');
  },

  downloadCustTemplate: async () => {
    const response: RequestResponse<Blob> = await requestClient.download(
      `/sys/cust/download-tmpl`,
      { responseReturn: 'raw' },
    );
    if (response.status !== 200) {
      // 不成功， 什么都不做
      return;
    }
    BizCommonApi.handleBlobResponse(response, 'download.data');
  },
};

// 客户相关的options信息
/**
 * 婚姻状态
 */
export const marriageStatusOptions = [
  { label: '未知', value: 0 },
  { label: '已婚', value: 1 },
  { label: '未婚', value: 2 },
  { label: '离异', value: 3 },
];
/**
 * '有无'枚举
 */
export const gotNoOptions = [
  { label: '未知', value: 0 },
  { label: '有', value: 1 },
  { label: '无', value: 2 },
];
/**
 * '是否'枚举
 */
export const yesNoOptions = [
  { label: '未知', value: 0 },
  { label: '是', value: 1 },
  { label: '否', value: 2 },
];

/**
 * 房产情况
 */
export const houseFlagOptions = [
  { label: '未知', value: 0 },
  { label: '有京房', value: 1 },
  { label: '有房', value: 2 },
  { label: '无', value: 3 },
];
/**
 * 客户收藏类型
 */
export const FavoriteTypeEnum = {
  NORMAL: 0,
  MY_CUST: 1,
  KEY_CUST: 2,
  getFavTypeName: (type: number) => {
    switch (type) {
      case FavoriteTypeEnum.KEY_CUST: {
        return '重点客户';
      }
      case FavoriteTypeEnum.MY_CUST: {
        return '我的客户';
      }
      case FavoriteTypeEnum.NORMAL: {
        return '再分配客户';
      }
    }
  },
  getFavOptions: () => {
    return [
      { label: '再分配客户', value: FavoriteTypeEnum.NORMAL },
      { label: '我的客户', value: FavoriteTypeEnum.MY_CUST },
      { label: '重点客户', value: FavoriteTypeEnum.KEY_CUST },
    ];
  },
};
export const HIGHT_LIGHT_PROGRESS_VALUES = new Set([
  5, 6, 7, 8, 9, 14_078, 14_079,
]);
