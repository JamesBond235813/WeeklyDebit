package com.jhl.silver.union.biz.data;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jhl.silver.union.biz.common.BizConstance;
import com.jhl.silver.union.biz.common.enums.CommonStatusEnum;
import com.jhl.silver.union.biz.dept.dal.entity.SuOrgDepartmentInfoDO;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.Objects;

/**
 * 部门信息查询类
 *
 * @author: qingren
 * @create_time: 2025/3/21
 */
@Data
@Accessors(chain = true)
public class DeptQry {
    private Long id;

    /**
     * 部门名称
     */
    private String deptName;
    /**
     * 部门名称前缀
     */
    private String deptNamePrefix;

    /**
     * 部门编号
     */
    private String deptCode;

    /**
     * 上级部门 ID集合
     */
    private Collection<Long> parentDeptIds;

    /**
     * 用户状态。 1：正常。 0：禁用
     */
    private Integer status;

    /**
     * 删除标识 0-正常; 其它-已删除
     */
    private Long deleteFlag;

    /**
     * 部门 ID 集合
     */
    private Collection<Long> ids;

    /**
     * 根据ID升序标识。 true:根据ID升序, false: 根据ID降序, null: 不排序
     */
    private Boolean orderByIdAsc;
    /**
     * 根据部门名称升序标识。 true:升序, false: 降序, null: 不排序
     */
    private Boolean orderByNameAsc;

    /**
     * 上级部门名称前缀
     */
    private String parentDeptNamePrefix;

    public LambdaQueryWrapper<SuOrgDepartmentInfoDO> toQueryWrapper(boolean forceValid) {
        LambdaQueryWrapper<SuOrgDepartmentInfoDO> qw = new LambdaQueryWrapper<>();
        qw.eq(StringUtils.isNotBlank(this.getDeptCode()), SuOrgDepartmentInfoDO::getDeptCode, this.getDeptCode())
                .eq(StringUtils.isNotBlank(this.getDeptName()), SuOrgDepartmentInfoDO::getDeptName, this.getDeptName())
                .eq(Objects.nonNull(id), SuOrgDepartmentInfoDO::getId, id)
                .in(!CollectionUtils.isEmpty(this.parentDeptIds), SuOrgDepartmentInfoDO::getParentDeptId,
                        this.parentDeptIds)
                .in(!CollectionUtils.isEmpty(ids), SuOrgDepartmentInfoDO::getId, ids)
                .like(StringUtils.isNotBlank(this.deptNamePrefix), SuOrgDepartmentInfoDO::getDeptName,
                        this.deptNamePrefix)
        ;
        if (forceValid) {
            qw.eq(SuOrgDepartmentInfoDO::getDeleteFlag, BizConstance.NOT_DELETED)
                    .eq(SuOrgDepartmentInfoDO::getStatus, CommonStatusEnum.OK.status);
        } else {
            qw.eq(Objects.nonNull(this.deleteFlag), SuOrgDepartmentInfoDO::getDeleteFlag, this.deleteFlag)
                    .eq(Objects.nonNull(this.status), SuOrgDepartmentInfoDO::getStatus, this.status);
        }
        if (Boolean.TRUE.equals(this.orderByIdAsc)) {
            qw.orderByAsc(SuOrgDepartmentInfoDO::getId);
        } else if (Boolean.FALSE.equals(this.orderByIdAsc)) {
            qw.orderByDesc(SuOrgDepartmentInfoDO::getId);
        }
        if (Boolean.TRUE.equals(this.orderByNameAsc)) {
            qw.orderByAsc(SuOrgDepartmentInfoDO::getDeptName);
        } else if (Boolean.FALSE.equals(this.orderByNameAsc)) {
            qw.orderByDesc(SuOrgDepartmentInfoDO::getDeptName);
        }
        return qw;
    }

    /**
     * 取所有有效部门信息的查询条件
     *
     * @return
     */
    public static LambdaQueryWrapper<SuOrgDepartmentInfoDO> allValidDeptQuery() {
        return new DeptQry().toQueryWrapper(true);
    }
}
