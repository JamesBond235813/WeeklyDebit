package com.jhl.silver.union.biz.data;

import com.jhl.silver.union.commons.validate.ValidateUtils;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;

/**
 * 部门信息
 *
 * @author: qingren
 * @create_time: 2025/3/21
 */
@Data
@Accessors(chain = true)
public class DeptInfo {
    /**
     * 部门ID
     */
    private Long id;

    /**
     * 部门名称
     */
    @NotBlank(message = "请指定部门名称")
    @Size(max = 64, message = "部门名称最大64个字符")
    private String deptName;

    /**
     * 部门编号
     */
    @Size(max = 128, message = "部门编号最大128个字符")
    private String deptCode;

    /**
     * 上级部门 ID
     */
    private Long parentDeptId;

    /**
     * 部门简介
     */
    private String introduction;

    /**
     * 状态。 1：正常。 0：禁用
     */
    private Integer status;

    /**
     * 创建人用户 ID
     */
    private Long createBy;

    /**
     * 最后修改人用户 ID
     */
    private Long lastModifiedBy;

    /**
     * 新增记录时间
     */
    private Date gmtCreate;

    /**
     * 更新记录时间
     */
    private Date gmtModified;

    /**
     * 部门名称
     *
     * @param deptName
     */
    public DeptInfo setDeptName(String deptName) {
        this.deptName = StringUtils.trim(deptName);
        return this;
    }

    /**
     * 部门编号
     *
     * @param deptCode
     */
    public DeptInfo setDeptCode(String deptCode) {
        this.deptCode = StringUtils.trim(deptCode);
        return this;
    }

    public void validate(){
        ValidateUtils.validateWithDefaultValidator(this);
    }
}
