package com.jhl.silver.union.web.data.customer;

import com.jhl.silver.union.web.data.CustomerItemDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 客户业务信息详情
 *
 * @author: qingren
 * @create_time: 2025/4/2
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(description = "客户业务信息详情")
public class CustomerItemDetailDTO extends CustomerItemDTO {

    /**
     * 跟进历史
     */
    @Schema(description = "跟进历史")
    private List<CustomerUpdTraceDTO> progressList;
    /**
     * 上级评价列表
     */
    @Schema(description = "上级评价列表")
    private List<CustomerUpdTraceDTO> leaderRemarkList;

}
