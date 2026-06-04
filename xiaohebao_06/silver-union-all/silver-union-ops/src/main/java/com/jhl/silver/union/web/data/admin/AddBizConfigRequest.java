package com.jhl.silver.union.web.data.admin;

import com.jhl.silver.union.biz.common.enums.BizDictConfigTypeEnum;
import com.jhl.silver.union.biz.common.enums.CommonStatusEnum;
import com.jhl.silver.union.commons.utils.OtherUtils;
import com.jhl.silver.union.commons.utils.VerifyUtils;
import com.jhl.silver.union.commons.validate.ValidateUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

/**
 * 新增业务字典信息请求
 *
 * @author: qingren
 * @create_time: 2025/3/29
 */
@Data
@Accessors(chain = true)
@Schema(description = "新增业务字典信息请求")
public class AddBizConfigRequest {

    /**
     * 业务类型， 见BizDictConfigTypeEnum枚举定义
     */
    @Schema(description = "业务类型， 见BizDictConfigTypeEnum枚举定义", requiredMode = Schema.RequiredMode.REQUIRED)
    private String bizType;

    /**
     * 业务字典值
     */
    @Schema(description = "业务字典值", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "请输入业务字典值")
    private Integer intValue;
    /**
     * 字典值对应的显示名称
     */
    @Schema(description = "字典值对应的显示名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @Length(max = 16, min = 1, message = "请输入字典值对应的显示名称,且不超过16个字符")
    private String label;

    /**
     * 业务字典说明
     */
    @Schema(description = "业务字典说明")
    @Length(max = 64, message = "业务字典说明不可超过64个字符")
    private String description;

    /**
     * 业务字典可用状态。 若为null, 默认为正常状态
     */
    @Schema(description = "业务字典可用状态。 1:正常；0:禁用。 默认为正常状态")
    private Integer status;

    /**
     * 显示排序号。越小越靠前
     */
    @Schema(description = "显示排序号。越小越靠前")
    private Integer sortNo;

    /**
     * 业务字典类型枚举
     */
    @Schema(hidden = true)
    private BizDictConfigTypeEnum bizDictConfigTypeEnum;

    public void validate() {
        ValidateUtils.validateWithDefaultValidator(this);
        this.bizDictConfigTypeEnum = BizDictConfigTypeEnum.findByName(this.bizType);
        VerifyUtils.notNull(this.bizDictConfigTypeEnum, "bizType", "请指定正确的业务类型", true);
        this.status = OtherUtils.defaultIfNull(this.status, CommonStatusEnum.OK.status);
        VerifyUtils.contains(this.status,
                new int[] { CommonStatusEnum.OK.status, CommonStatusEnum.FORBIDDEN.status }, "请指定正确的可用状态",
                true);
    }
}
