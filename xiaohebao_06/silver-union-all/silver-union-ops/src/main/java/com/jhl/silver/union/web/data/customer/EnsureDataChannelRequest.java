package com.jhl.silver.union.web.data.customer;

import com.jhl.silver.union.commons.validate.ValidateUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.constraints.Length;

/**
 * 查询指定的数据来源渠道，若不存在则新增数据渠道信息的请求
 *
 * @author: qingren
 * @create_time: 2025/5/18
 */
@Data
@Accessors(chain = true)
@Schema(description = "查询指定的数据来源渠道，若不存在则新增数据渠道信息的请求")
public class EnsureDataChannelRequest {
    /**
     * 约定的访问令牌
     */
    @Schema(description = "约定的访问令牌")
    @NotBlank(message = "参数不正确，拒绝操作")
    private String token;
    /**
     * 渠道名称
     */
    @Schema(description = "渠道名称")
    @NotBlank(message = "请指定渠道名称")
    @Length(min = 1, max = 16, message = "请指定渠道名称，不超过16个字符")
    private String channelName;

    public void validate() {
        this.token = StringUtils.trim(this.token);
        this.channelName = StringUtils.trim(this.channelName);
        ValidateUtils.validateWithDefaultValidator(this);
    }

}
