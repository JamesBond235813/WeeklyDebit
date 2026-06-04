package com.jhl.silver.union.web.controller.admin;

import com.jhl.silver.union.biz.common.BizConstance;
import com.jhl.silver.union.biz.data.DeptInfo;
import com.jhl.silver.union.biz.data.DeptQry;
import com.jhl.silver.union.biz.dept.service.DeptService;
import com.jhl.silver.union.biz.data.UserContext;
import com.jhl.silver.union.commons.SuResult;
import com.jhl.silver.union.commons.utils.SuResultUtils;
import com.jhl.silver.union.web.data.DeptInfoDTO;
import com.jhl.silver.union.web.data.admin.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 系统管理类接口: 组织架构管理
 *
 * @author: qingren
 * @create_time: 2025/3/21
 */
@RestController
@RequestMapping("/sys/dpt")
@Tag(name = "系统管理类接口: 组织架构管理")
public class DptMngController {
    @Resource
    private DeptService deptService;

    /**
     * 新增部门信息
     *
     * @param request
     * @return
     */
    @PostMapping(value = "/add-dpt-info")
    @Operation(summary = "增加部门信息")
    SuResult<Void> addDepartmentInfo(@RequestBody AddDepartmentInfoRequest request) {
        request.validate();
        DeptInfo deptInfo = new DeptInfo();
        BeanUtils.copyProperties(request, deptInfo);
        deptInfo.setCreateBy(UserContext.getUserId());
        deptService.addDeptInfo(deptInfo);
        return SuResultUtils.successResult();
    }

    /**
     * 更新部门信息
     *
     * @param request
     * @return
     */
    @PostMapping(value = "/upd-dpt-info")
    @Operation(summary = "更新部门信息")
    SuResult<Void> updDepartmentInfo(@RequestBody UpdDepartmentInfoRequest request) {
        request.validate();
        DeptInfo deptInfo = new DeptInfo();
        BeanUtils.copyProperties(request, deptInfo);
        deptInfo.setLastModifiedBy(UserContext.getUserId());
        deptService.updateDeptInfo(deptInfo);
        return SuResultUtils.successResult();
    }

    /**
     * 删除部门信息
     *
     * @param request
     * @return
     */
    @PostMapping(value = "/del-dpt-info")
    @Operation(summary = "删除部门信息")
    SuResult<Void> deleteDepartmentInfo(@RequestBody DeleteDepartmentInfoRequest request) {
        request.validate();
        deptService.deleteDeptInfo(request.getId(), UserContext.getUserId());
        return SuResultUtils.successResult();
    }

    /**
     * 查询部门信息列表
     *
     * @param request
     * @return
     */
    @PostMapping(value = "/list-dpt-info")
    @Operation(summary = "查询部门信息列表")
    SuResult<List<DeptInfoDTO>> listDepartmentInfo(@RequestBody ListDepartmentInfoRequest request) {

        DeptQry qry = new DeptQry()
                .setDeleteFlag(BizConstance.NOT_DELETED)
                .setId(request.getId())
                .setDeptNamePrefix(request.getDeptNamePrefix())
                .setStatus(request.getStatus())
                .setOrderByNameAsc(true)
                .setParentDeptNamePrefix(request.getParentDeptNamePrefix());
        List<DeptInfoDTO> list = deptService.listByDeptInfo(qry, Boolean.TRUE.equals(request.getNeedExtendQry()));
        return SuResultUtils.successResult(list);
    }
}
