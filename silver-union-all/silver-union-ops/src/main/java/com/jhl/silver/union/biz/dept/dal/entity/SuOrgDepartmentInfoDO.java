package com.jhl.silver.union.biz.dept.dal.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
/**
 * <p>
 * 组织架构部门信息
 * </p>
 *
 * @author Way
 * @since 2025-03-21 22:11:57
 */
@Getter
@Setter
@ToString
@Accessors(chain = true)
@TableName("su_org_department_info")
public class SuOrgDepartmentInfoDO implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 部门名称
     */
    private String deptName;

    /**
     * 部门编号
     */
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
     * 删除标识 0-正常; 其它-已删除
     */
    private Long deleteFlag;

    /**
     * 新增记录时间
     */
    private Date gmtCreate;

    /**
     * 更新记录时间
     */
    private Date gmtModified;

    public static final String ID = "id";

    public static final String DEPT_NAME = "dept_name";

    public static final String DEPT_CODE = "dept_code";

    public static final String PARENT_DEPT_ID = "parent_dept_id";

    public static final String INTRODUCTION = "introduction";

    public static final String STATUS = "status";

    public static final String CREATE_BY = "create_by";

    public static final String LAST_MODIFIED_BY = "last_modified_by";

    public static final String DELETE_FLAG = "delete_flag";

    public static final String GMT_CREATE = "gmt_create";

    public static final String GMT_MODIFIED = "gmt_modified";
}
