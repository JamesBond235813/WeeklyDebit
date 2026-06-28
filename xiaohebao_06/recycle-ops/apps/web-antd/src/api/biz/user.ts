import type { PagedInfo } from './biz-common';

import { requestClient } from '#/api/request';
/** 分页查询条件 */
export interface PagedListUserInfoParams {
  /** 当前页号 */
  page?: number;

  /** 单页数据条数 */
  pageSize?: number;

  /** 用户ID */
  userId?: number;

  /** 用户真实姓名前缀 */
  userRealNamePrefix?: string;

  /** 用户名（登录账号） */
  userName?: string;

  /** 用户手机号 */
  phone?: string;

  /** 归属部门Id */
  departmentId?: number;

  /** 是否需要部门名称,创建人姓名等扩展信息 true:需要， 其它不需要。 */
  needExtendQry?: boolean;
}

/** 用户列表信息条目 */
export interface UserInfoRowItem {
  /** 生日 yyyy-MM-dd格式 */
  birthday: string;

  /** 创建人用户 ID */
  createBy: number;

  /** 创建人用户名 格式:  realName(userName) */
  createByName: string;

  /** 部门id */
  departmentId: number;

  /** 部门名称 */
  departmentName: string;

  /** 用户邮箱 */
  email: string;

  /** 工号 */
  employeeNo: string;

  /** 新增记录时间 */
  gmtCreate: Record<string, unknown>;

  /** 更新记录时间 */
  gmtModified: Record<string, unknown>;

  /** 头像图片地址 */
  headImg: string;

  /** 用户ID */
  id: number;

  /** 职位 */
  jobName: string;

  /** 用户手机号 */
  phone: string;

  /** 真实姓名 */
  realName: string;

  /** 权限角色列表 */
  roles: Record<string, unknown>[];

  /** 性别。 0:保密, 1:男, 2:女 */
  sex: number;

  /** 性别。  */
  sexDispName: string;

  /** 用户状态。 1：正常。 0：禁用 */
  status: number;

  /** 在线状态。 1：在线。 0：离线 */
  onlineStatus?: number;

  /** 用户名,即用户登录账号，不可重复 */
  userName: string;
}
export interface EditUserInfoParams {
  /* 用户ID */
  id: number;
  /* 用户名,即用户登录账号，不可重复 */
  userName: string;

  /* 用户密码. 仅在新增用户时生效 */
  password?: string;

  /* 真实姓名 */
  realName: string;

  /* 用户手机号 */
  phone?: string;

  /* 用户邮箱 */
  email?: string;

  /* 性别。 0:保密, 1:男, 2:女 */
  sex?: number;

  /* 生日 yyyy-MM-dd格式 */
  birthday?: string;

  /* 工号 */
  employeeNo?: string;

  /* 部门id */
  departmentId?: number;

  /* 职位 */
  jobName?: string;

  /* 角色列表 */
  roles?: string[];

  /* 权限角色名称列表 */
  roleDispNames: Record<string, unknown>[];
}

export interface ResetPasswordParams {
  /* 用户ID */
  id: number;

  /* 用户密码 */
  password: string;
}
export interface RoleInfo {
  /* 角色名称 */
  roleName: string;

  /* 角色权限说明 */
  desc: string;

  /** 角色显示名称 */
  roleDispName: string;
}
/**
 * 团队成员信息
 */
export interface UserInfoTeamMember {
  /* 用户 ID */
  id: number;

  /* 用户真实姓名 */
  name: string;

  /* */
  deptId: number;

  /* 部门名称 */
  deptName: string;

  /* 在线状态. 1:在线 */
  onlineStatus: number;
}
export const userApi = {
  /**
   * 分页查询用户列表
   * @param params
   * @returns
   */
  pagedListUserInfo: (params: PagedListUserInfoParams) => {
    return requestClient.post<PagedInfo<UserInfoRowItem>>(
      '/sys/user/page-list-user-info',
      params,
    );
  },
  /**
   * 新增用户信息
   * @param params
   * @returns
   */
  addUserInfo: (params: EditUserInfoParams) => {
    return requestClient.post('/sys/user/add-user-info', params);
  },

  updUserInfo: (params: EditUserInfoParams) => {
    return requestClient.post('/sys/user/upd-user-info', params);
  },

  resetUserPwd: (params: ResetPasswordParams) => {
    return requestClient.post('/sys/user/reset-user-pswd', params);
  },

  delUserInfo: (id: number) => {
    return requestClient.post('/sys/user/del-user-info', { id });
  },

  listAllRoles: () => {
    return requestClient.get<RoleInfo[]>('/sys/user/list-all-roles');
  },
  /**
   * 指定用户退出登录
   * @param id
   * @returns
   */
  revokeByUid: (id: number) => {
    return requestClient.get(`/sys/user/revoke-by-uid?userId=${id}`);
  },

  countManageableOnlineUsers: async () => {
    return requestClient.get<number>('/sys/user/online-count');
  },

  listTeamUser: async (onlineOnly?: number) => {
    return requestClient.get<UserInfoTeamMember[]>(
      `/user/list-team-user${onlineOnly === 0 ? '?ol-only=0' : ''}`,
    );
  },
  markOnlineStatus: async (onlineStatus: number) => {
    return requestClient.post('/user/online-status', { onlineStatus });
  },
};
