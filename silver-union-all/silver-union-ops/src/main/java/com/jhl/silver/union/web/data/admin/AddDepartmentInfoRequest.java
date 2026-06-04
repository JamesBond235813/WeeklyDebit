package com.jhl.silver.union.web.data.admin;

import com.jhl.silver.union.web.data.IValidateRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 增加部门信息请求
 *
 * @author: qingren
 * @create_time: 2025/3/23
 */
@Data
@Accessors(chain = true)
@Schema(description = "增加部门信息请求")
public class AddDepartmentInfoRequest implements IValidateRequest {
    /**
     * 部门名称
     */
    @Schema(description = "部门名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "请指定部门名称,不超过64个字符")
    @Size(max = 64, message = "请指定部门名称,不超过64个字符")
    private String deptName;

    /**
     * 部门编号
     */
    @Schema(description = "部门编号")
    @Size(max = 64, message = "请输入部门编号,不超过128个字符")
    private String deptCode;

    /**
     * 上级部门ID
     */
    @Schema(description = "上级部门ID。 0:表示无上级部门")
    private Long parentDeptId;

    /**
     * 部门简介
     */
    @Schema(description = "部门简介")
    @Size(max = 255, message = "部门简介不超过255个字符")
    private String introduction;

}
