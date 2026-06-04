package com.jhl.silver.union.web.data.customer;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.jhl.silver.union.commons.CommonConstant;
import com.jhl.silver.union.commons.utils.SuDateUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;
import java.util.List;

/**
 * 客户信息更新记录信息
 *
 * @author: qingren
 * @create_time: 2025/4/2
 */
@Data
@Accessors(chain = true)
@Schema(description = "客户信息更新记录信息")
public class CustomerUpdTraceDTO {
    /**
     * 更新记录ID
     */
    @Schema(description = "更新记录ID")
    private Long id;
    /**
     * 操作人姓名
     */
    @Schema(description = "操作人姓名")
    private String optUserName;
    /**
     * 操作时间
     */
    @Schema(description = "操作时间")
    @JsonFormat(pattern = SuDateUtils.DF_YYYY_MM_DDHHMMSS, timezone = CommonConstant.DEFAULT_TIME_ZONE)
    private Date updTime;
    /**
     * 操作描述
     */
    @Schema(description = "操作描述")
    private List<String> descList;
    /**
     * 更新类型
     */
    @Schema(description = "操作描述")
    private String updTypeDesc;
}
