package com.jhl.silver.union.web.data.user;

import com.jhl.silver.union.web.data.IValidateRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Range;

/**
 * 更新在线状态请求
 *
 * @author: qingren
 * @create_time: 2025/4/2
 */
@Data
@Accessors(chain = true)
@Schema(description = "更新在线状态请求")
public class MarkOnlineStatusRequest implements IValidateRequest {
    /**
     * 在线状态。1:在线, 0:离线
     */
    @Schema(description = "在线状态。1:在线, 0:离线")
    @NotNull(message="参数不正确")
    @Range(min = 0, max = 1,message="参数不正确")
    private Integer onlineStatus;
}
