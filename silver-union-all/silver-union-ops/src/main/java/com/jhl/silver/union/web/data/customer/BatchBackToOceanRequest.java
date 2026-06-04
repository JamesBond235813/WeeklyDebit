package com.jhl.silver.union.web.data.customer;

import com.jhl.silver.union.web.data.IValidateRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 将客户信息批量退回至公海请求
 *
 * @author: qingren
 * @create_time: 2025/4/11
 */
@Data
@Accessors(chain = true)
@Schema(description = "将客户信息批量退回至公海请求")
public class BatchBackToOceanRequest implements IValidateRequest {
    /**
     * 客户ID列表
     */
    @Schema(description = "客户ID列表")
    @NotNull(message = "请指定需要退回公海的客户")
    @Size(min = 1, max = 100, message = "请指定需要退回公海的客户,最多不超过100个")
    private List<Long> custIdList;

}
