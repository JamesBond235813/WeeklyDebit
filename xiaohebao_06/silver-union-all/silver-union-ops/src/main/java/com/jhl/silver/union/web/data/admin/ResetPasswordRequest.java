package com.jhl.silver.union.web.data.admin;

import com.jhl.silver.union.biz.common.utils.BizHelper;
import com.jhl.silver.union.commons.validate.ValidateUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

/**
 * 重置密码请求
 *
 * @author: qingren
 * @create_time: 2025/3/27
 */
@Data
@Accessors(chain = true)
@Schema(description = "重置密码请求")
public class ResetPasswordRequest {
    /**
     * 用户ID
     */
    @Schema(description = "用户ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "请指定需要重置密码的用户")
    private Long id;
    /**
     * 用户密码。
     */
    @Schema(description = "用户密码", requiredMode = Schema.RequiredMode.REQUIRED)
    @Length(min = 8, max = 16, message = "请指定用户初始密码,8~16位，要求包含数字和字母")
    private String password;

    public void validate() {
        ValidateUtils.validateWithDefaultValidator(this);
        //密码校验
        BizHelper.verifyPassword(this.password, "请指定用户初始密码,8~16位，要求包含数字和字母");
    }
}
