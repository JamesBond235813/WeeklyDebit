package com.jhl.silver.union.biz.dept.manager.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jhl.silver.union.biz.dept.dal.entity.SuOrgDepartmentInfoDO;
import com.jhl.silver.union.biz.dept.dal.mapper.SuOrgDepartmentInfoMapper;
import com.jhl.silver.union.biz.data.DeptInfo;
import com.jhl.silver.union.biz.data.DeptQry;
import com.jhl.silver.union.biz.data.convert.DeptConvert;
import com.jhl.silver.union.biz.dept.manager.SuOrgDepartmentInfoManager;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * <p>
 * 组织架构部门信息 服务实现类
 * </p>
 *
 * @author Way
 * @since 2025-03-21 22:11:57
 */
@Service
public class SuOrgDepartmentInfoManagerImpl extends ServiceImpl<SuOrgDepartmentInfoMapper, SuOrgDepartmentInfoDO>
        implements SuOrgDepartmentInfoManager {
    @Resource
    private DeptConvert convert;

    @Override
    public SuOrgDepartmentInfoDO getValidDeptById(Long departmentId) {
        DeptQry qry = new DeptQry()
                .setId(departmentId);
        return this.getOne(qry.toQueryWrapper(true), false);
    }

    @Override
    public List<DeptInfo> listDeptInfo(LambdaQueryWrapper<SuOrgDepartmentInfoDO> queryWrapper) {
        if (Objects.isNull(queryWrapper)) {
            return List.of();
        }
        List<SuOrgDepartmentInfoDO> list = list(queryWrapper);
        if (CollectionUtils.isEmpty(list)) {
            return List.of();
        }
        return convert.convert2DeptInfoList(list);
    }

    @Override
    public List<DeptInfo> listDeptInfoByIds(Collection<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return List.of();
        }
        List<SuOrgDepartmentInfoDO> list = this.listByIds(ids);
        return convert.convert2DeptInfoList(list);
    }
}
