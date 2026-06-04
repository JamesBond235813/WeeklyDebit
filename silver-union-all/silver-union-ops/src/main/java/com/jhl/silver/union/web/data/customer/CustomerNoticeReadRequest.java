package com.jhl.silver.union.web.data.customer;

import com.jhl.silver.union.commons.utils.VerifyUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.util.CollectionUtils;

@Data
@Accessors(chain = true)
@Schema(description = "客户通知已读请求")
public class CustomerNoticeReadRequest {
    @Schema(description = "通知ID列表")
    private List<Long> ids;

    public void validate() {
        VerifyUtils.verifyTrue(!CollectionUtils.isEmpty(ids), "请指定通知ID", true);
    }
}
