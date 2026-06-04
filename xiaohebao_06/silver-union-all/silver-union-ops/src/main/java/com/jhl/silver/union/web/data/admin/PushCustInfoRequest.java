package com.jhl.silver.union.web.data.admin;

import com.jhl.silver.union.commons.CommonResultCode;
import com.jhl.silver.union.commons.exception.BizException;
import com.jhl.silver.union.commons.utils.VerifyUtils;
import com.jhl.silver.union.commons.validate.ValidateUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * 推送客户信息请求
 *
 * @author: qingren
 * @create_time: 2025/5/18
 */
@Data
@Accessors(chain = true)
@Schema(description = "推送客户信息请求")
public class PushCustInfoRequest {
    /**
     * 约定的访问令牌
     */
    @Schema(description = "约定的访问令牌")
    @NotBlank(message = "参数不正确，拒绝操作")
    private String token;

    /**
     * 客户信息列表
     */
    @Schema(description = "客户信息列表")
    private List<PushCustInfoItem> itemList;

    /**
     * 加密类型（可选）：AES 或 AES_ECB
     */
    @Schema(description = "加密类型（可选）：AES 或 AES_ECB，空表示明文")
    private String encryptType;

    /**
     * 分配的目标部门ID
     */
    @Schema(description = "分配的目标部门ID")
    private Long targetDeptId;

    /**
     * 分配的目标人员ID
     */
    @Schema(description = "分配的目标人员ID")
    private Long targetUserId;

    public void validate() {
        this.token = StringUtils.trim(this.token);
        this.encryptType = StringUtils.trimToEmpty(this.encryptType);
        ValidateUtils.validateWithDefaultValidator(this);
        VerifyUtils.notEmpty(this.itemList, "itemList", "客户信息不可为空", true);
        if (StringUtils.isNotBlank(this.encryptType) && !isAesEncrypted()) {
            throw new BizException(CommonResultCode.INVALID_PARAMS, "encryptType不支持: " + this.encryptType);
        }
    }

    public boolean isAesEncrypted() {
        return StringUtils.equalsIgnoreCase("AES", this.encryptType)
                || StringUtils.equalsIgnoreCase("AES_ECB", this.encryptType);
    }
}
