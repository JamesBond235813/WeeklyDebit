package com.jhl.silver.union.web.data.admin;

import com.jhl.silver.union.commons.validate.ValidateUtils;
import com.jhl.silver.union.web.data.IValidateRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 删除部门信息请求
 *
 * @author: qingren
 * @create_time: 2025/3/23
 */
@Data
@Accessors(chain = true)
@Schema(description = "删除部门信息请求")
public class DeleteDepartmentInfoRequest implements IValidateRequest {

    /**
     * 部门ID
     */
    @Schema(description = "部门 ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "请指定待删除的部门")
    private Long id;
    public void validate(){
        ValidateUtils.validateWithDefaultValidator(this);
    }
}
