package com.jhl.silver.union.web.data.admin;

import com.jhl.silver.union.web.data.IValidateRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.constraints.Length;

/**
 * 追加上级评价信息请求
 *
 * @author: qingren
 * @create_time: 2025/4/8
 */
@Data
@Accessors(chain = true)
@Schema(description = "追加上级评价信息请求")
public class AddLeaderRemarkRequest implements IValidateRequest {
    /**
     * 客户ID
     */
    @Schema(description = "客户ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "请指定客户信息")
    private Long id;

    /**
     * 上级评价
     */
    @Schema(description = "上级评价", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "请输入上级评价，不超过300个字符")
    @Length(min = 1, max = 300, message = "请输入上级评价，不超过300个字符")
    private String leaderRemark;

    @Override
    public void validate() {
        this.leaderRemark = StringUtils.trim(this.leaderRemark);
        IValidateRequest.super.validate();
    }
}
