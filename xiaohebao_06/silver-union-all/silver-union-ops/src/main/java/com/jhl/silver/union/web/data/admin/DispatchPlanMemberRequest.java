package com.jhl.silver.union.web.data.admin;

import com.jhl.silver.union.commons.utils.VerifyUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@Schema(description = "数据分配方案成员配置")
public class DispatchPlanMemberRequest {
    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "部门ID")
    private Long deptId;

    @Schema(description = "每日上限")
    private Integer dailyLimit;

    @Schema(description = "排序号")
    private Integer sortNo;

    public void validate() {
        VerifyUtils.notNull(userId, "userId", "请指定用户", true);
        VerifyUtils.notNull(deptId, "deptId", "请指定部门", true);
        VerifyUtils.notNull(dailyLimit, "dailyLimit", "请填写每日上限", true);
        VerifyUtils.verifyTrue(dailyLimit >= 0, "每日上限不可小于0", true);
    }
}
