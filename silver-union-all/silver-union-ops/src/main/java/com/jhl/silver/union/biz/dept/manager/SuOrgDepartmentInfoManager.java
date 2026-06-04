package com.jhl.silver.union.biz.dept.manager;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jhl.silver.union.biz.dept.dal.entity.SuOrgDepartmentInfoDO;
import com.jhl.silver.union.biz.data.DeptInfo;

import java.util.Collection;
import java.util.List;

/**
 * <p>
 * 组织架构部门信息 服务类
 * </p>
 *
 * @author Way
 * @since 2025-03-21 22:11:57
 */
public interface SuOrgDepartmentInfoManager extends IService<SuOrgDepartmentInfoDO> {

    /**
     * 根据 ID 查询有效的部门信息
     *
     * @param departmentId
     * @return
     */
    SuOrgDepartmentInfoDO getValidDeptById(Long departmentId);

    List<DeptInfo> listDeptInfo(LambdaQueryWrapper<SuOrgDepartmentInfoDO> queryWrapper);


    List<DeptInfo> listDeptInfoByIds(Collection<Long> ids);
}
