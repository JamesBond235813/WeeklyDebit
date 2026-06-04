package com.jhl.silver.union.biz.dept.service;

import com.jhl.silver.union.biz.common.enums.UserAuthRoleEnum;
import com.jhl.silver.union.biz.data.DeptInfo;
import com.jhl.silver.union.biz.data.DeptNodeInfo;
import com.jhl.silver.union.biz.data.DeptQry;
import com.jhl.silver.union.web.data.DeptInfoDTO;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 部门相关服务
 *
 * @author: qingren
 * @create_time: 2025/3/21
 */
public interface DeptService {

    /**
     * 保存部门信息
     *
     * @param deptInfo
     */
    void addDeptInfo(DeptInfo deptInfo);

    /**
     * 更新部门信息
     *
     * @param deptInfo
     */
    void updateDeptInfo(DeptInfo deptInfo);

    /**
     * 根据 ID 删除部门信息
     *
     * @param id
     * @param optUserId 操作人用户 ID
     */
    void deleteDeptInfo(Long id, Long optUserId);

    /**
     * 根据部门 ID 获取 部门信息
     *
     * @param id
     * @param validOnly true: 仅取有效的部门信息
     * @return
     */
    DeptInfo getDeptInfoById(Long id, boolean validOnly);

    /**
     * 取部门树
     *
     * @return
     */
    DeptNodeInfo getDeptTree();

    /**
     * 取给定父节点的所有子节点ID
     *
     * @param parentDeptId
     * @param includeSelf  true: 返回的部门ID列表 包含parentDeptId, false: 不包含
     * @return
     */
    List<Long> getAllChildrenIdByParentDeptId(Long parentDeptId, boolean includeSelf);

    /**
     * 根据权限取给定父节点的所有子节点ID。 要求有超管或部门数据管理员权限
     *
     * @param parentDeptId
     * @param roleSet      若权限列表为空，则返回当前部门 ID
     * @return
     */
    List<Long> getAllChildrenIdByParentDeptId(Long parentDeptId, Set<UserAuthRoleEnum> roleSet);

    /**
     * 判断 childDeptId 是否为 parentDeptId 的下级部门. parentDeptId 为 0 时， 直接返回 false
     *
     * @param parentDeptId
     * @param childDeptId
     * @param allowSameDeptBelonging true: 若parentDeptId == childDeptId 则返回 true
     * @return
     */
    boolean doesBelongParentDeptId(Long parentDeptId, Long childDeptId, boolean allowSameDeptBelonging);

    /**
     * 根据部门名称查询部门列表
     *
     * @param deptName
     * @return
     */
    List<DeptInfo> queryDeptListByName(String deptName);

    /**
     * 查询部门信息列表
     *
     * @param deptQry
     * @param needExtendQry true: 需要查询上级部门名称以及用户姓名等信息
     * @return
     */
    List<DeptInfoDTO> listByDeptInfo(DeptQry deptQry, boolean needExtendQry);

    /**
     * 根据 ID 取部门名称
     *
     * @param ids
     * @return dept id - deptName
     */
    Map<Long, String> getDeptNameByIds(Collection<Long> ids);

    /**
     * 根据 ID 取部门名称
     *
     * @param id
     * @return
     */
    String getDeptNameById(Long id);

}
