package com.jhl.silver.union.web.data.admin;

import com.jhl.silver.union.biz.common.enums.CommonStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 查询部门信息列表请求
 *
 * @author: qingren
 * @create_time: 2025/3/25
 */
@Data
@Accessors(chain = true)
@Schema(description = "查询部门信息列表请求")
public class ListDepartmentInfoRequest {
    /**
     * 部门ID
     */
    @Schema(description = "部门ID")
    private Long id;
    /**
     * 部门名称
     */
    @Schema(description = "部门名称前缀")
    private String deptNamePrefix;
    /**
     * 部门状态 参见{@link CommonStatusEnum#status}定义
     */
    @Schema(description = "部门状态. 参见 com.jhl.silver.union.biz.common.enums.CommonStatusEnum定义")
    private Integer status;

    /**
     * 上级部门名称前缀
     */
    @Schema(description = "上级部门名称前缀")
    private String parentDeptNamePrefix;

    /**
     * 是否需要上级部门名称,修改人姓名等扩展信息 true:需要， 其它不需要。
     */
    @Schema(description = "是否需要上级部门名称,修改人姓名等扩展信息 true:需要， 其它不需要。")
    private Boolean needExtendQry;
}
