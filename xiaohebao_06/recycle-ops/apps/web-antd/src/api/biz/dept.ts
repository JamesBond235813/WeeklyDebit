import { requestClient } from '#/api/request';

export interface EditDepartmentInfoParams {
  /** 部门 ID */
  id: number;

  /** 部门名称 */
  deptName: string;

  /** 部门编号 */
  deptCode?: string;

  /** 上级部门ID。 0:表示无上级部门 */
  parentDeptId?: number;

  /** 部门简介 */
  introduction?: string;
}
export interface ListDepartmentInfoParams {
  /** 部门ID */
  id?: number;

  /** 部门名称 */
  deptNamePrefix?: string;

  /** 部门状态. 参见 com.jhl.silver.union.biz.common.enums.CommonStatusEnum定义 */
  status?: number;
  /** 上级部门名称前缀 */
  parentDeptNamePrefix?: string;

  /** 是否需要上级部门名称,修改人姓名等扩展信息 true:需要， 其它不需要。 */
  needExtendQry?: boolean;
}
/**
 * 部门信息
 */
export interface DeptInfo {
  /** 部门ID */
  id: number;

  /** 部门名称 */
  deptName: string;

  /** 部门编号 */
  deptCode: string;

  /** 上级部门 ID */
  parentDeptId: number;

  /** 上级部门名称前缀 */
  parentDeptNamePrefix: string;

  /** 部门简介 */
  introduction: string;

  /** 状态。 1：正常。 0：禁用 */
  status: number;

  /** 状态名称 */
  statusDesc: string;

  /** 创建人用户名称（格式为realName(username)） */
  createByUserName: string;

  /** 最后修改人用户名称（格式为realName(username)） */
  lastModifiedByUserName: string;

  /** 新增记录时间, yyyy-MM-dd HH:mm:ss */
  gmtCreateStr: string;

  /** 更新记录时间, yyyy-MM-dd HH:mm:ss */
  gmtModifiedStr: string;
}
/**
 * 部门简要信息
 */
export interface DeptItem {
  /* 部门ID */
  id: number;

  /* 部门名称 */
  deptName: string;
}

const DeptApi = () => {
  /**
   * Adds department information.
   *
   * @param params - The department data to be added.
   */

  function addDeptInfo(params: EditDepartmentInfoParams) {
    return requestClient.post<string>('/sys/dpt/add-dpt-info', params);
  }

  function editDeptInfo(params: EditDepartmentInfoParams) {
    return requestClient.post<string>('/sys/dpt/upd-dpt-info', params);
  }

  function delDeptInfo(id: number) {
    return requestClient.post<string>('/sys/dpt/del-dpt-info', { id });
  }

  /**
   * Retrieves department information based on the provided parameters.
   *
   * @param params - The parameters used to filter and list department information.
   * @returns A promise that resolves to the department information.
   */
  function listDeptInfo(params: ListDepartmentInfoParams) {
    return requestClient.post<DeptInfo[]>('/sys/dpt/list-dpt-info', params);
  }

  async function getDeptItems() {
    return await requestClient.get<DeptItem[]>('/user/get-dept');
  }

  return {
    addDeptInfo,
    listDeptInfo,
    editDeptInfo,
    delDeptInfo,
    getDeptItems,
  };
};
export const deptApi = DeptApi();
