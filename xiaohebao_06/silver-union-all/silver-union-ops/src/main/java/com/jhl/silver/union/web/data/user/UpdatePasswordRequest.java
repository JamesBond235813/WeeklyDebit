package com.jhl.silver.union.web.data.user;

import com.jhl.silver.union.biz.common.utils.BizHelper;
import com.jhl.silver.union.commons.validate.ValidateUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

/**
 * 修改密码请求
 *
 * @author: qingren
 * @create_time: 2025/3/27
 */
@Data
@Accessors(chain = true)
@Schema(description = "修改密码请求")
public class UpdatePasswordRequest {

    /**
     * 新密码。
     */
    @Schema(description = "新密码", requiredMode = Schema.RequiredMode.REQUIRED)
    @Length(min = 8, max = 16, message = "请输入新密码,8~16位，要求包含数字和字母")
    private String newPassword;

    /**
     * 旧密码
     */
    @Schema(description = "旧密码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "请输入旧密码")
    private String oldPassword;

    public void validate() {
        ValidateUtils.validateWithDefaultValidator(this);
        //密码校验
        BizHelper.verifyPassword(this.newPassword, "请密码,8~16位，要求包含数字和字母");
    }
}
