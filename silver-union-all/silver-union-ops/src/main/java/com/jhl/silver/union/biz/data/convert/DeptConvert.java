package com.jhl.silver.union.biz.data.convert;

import com.jhl.silver.union.biz.dept.dal.entity.SuOrgDepartmentInfoDO;
import com.jhl.silver.union.biz.data.DeptInfo;
import com.jhl.silver.union.biz.data.DeptNodeInfo;
import com.jhl.silver.union.web.data.DeptInfoDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * @author: qingren
 * @create_time: 2025/3/21
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DeptConvert {

    /**
     * convert2DeptInfo
     *
     * @param suOrgDepartmentInfoDO
     * @return
     */
    DeptInfo convert2DeptInfo(SuOrgDepartmentInfoDO suOrgDepartmentInfoDO);

    /**
     * convert2SuOrgDepartmentInfoDO
     *
     * @param deptInfo
     * @return
     */
    @Named("convert2SuOrgDepartmentInfoDO")
    SuOrgDepartmentInfoDO convert2SuOrgDepartmentInfoDO(DeptInfo deptInfo);

    /**
     * convert2DeptInfoList
     *
     * @param list
     * @return
     */
    List<DeptInfo> convert2DeptInfoList(List<SuOrgDepartmentInfoDO> list);

    /**
     * convert2DeptNodeInfo
     *
     * @param suOrgDepartmentInfoDO
     * @return
     */
    @Named("convert2DeptNodeInfo")
    DeptNodeInfo convert2DeptNodeInfo(SuOrgDepartmentInfoDO suOrgDepartmentInfoDO);

    /**
     * convert2DeptNodeInfoList
     *
     * @param list
     * @return
     */
    List<DeptNodeInfo> convert2DeptNodeInfoList(List<SuOrgDepartmentInfoDO> list);

    /**
     * convert2DeptInfoDTO
     *
     * @param suOrgDepartmentInfoDO
     * @return
     */
    DeptInfoDTO convert2DeptInfoDTO(SuOrgDepartmentInfoDO suOrgDepartmentInfoDO);

}
