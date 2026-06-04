package com.jhl.silver.union.web.data.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@Schema(description = "数据分配方案")
public class DispatchPlanDTO {
    @Schema(description = "方案ID")
    private Long id;

    @Schema(description = "分配模式 MANUAL/AUTO")
    private String mode;

    @Schema(description = "生效开始时间 yyyy-MM-dd HH:mm:ss")
    private String effectStart;

    @Schema(description = "生效结束时间 yyyy-MM-dd HH:mm:ss")
    private String effectEnd;

    @Schema(description = "成员列表")
    private List<DispatchPlanMemberDTO> members;
}
