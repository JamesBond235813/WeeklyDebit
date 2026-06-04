package com.jhl.silver.union.web.data.admin;

import com.jhl.silver.union.biz.common.enums.DispatchModeEnum;
import com.jhl.silver.union.commons.utils.VerifyUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

@Data
@Accessors(chain = true)
@Schema(description = "保存数据分配方案")
public class SaveDispatchPlanRequest {
    @Schema(description = "分配模式 MANUAL/AUTO")
    private String mode;

    @Schema(description = "生效开始时间 yyyy-MM-dd HH:mm:ss")
    private String effectStart;

    @Schema(description = "生效结束时间 yyyy-MM-dd HH:mm:ss")
    private String effectEnd;

    @Schema(description = "成员配置")
    private List<DispatchPlanMemberRequest> members;

    public void validate() {
        VerifyUtils.notBlank(mode, "mode", "请指定分配模式", true);
        VerifyUtils.verifyTrue(DispatchModeEnum.fromCode(mode) != null, "分配模式不合法", true);
        VerifyUtils.notBlank(effectStart, "effectStart", "请指定生效开始时间", true);
        VerifyUtils.notBlank(effectEnd, "effectEnd", "请指定生效结束时间", true);
        if (DispatchModeEnum.isAuto(mode)) {
            VerifyUtils.verifyTrue(!CollectionUtils.isEmpty(members), "自动分配需要配置成员", true);
            for (DispatchPlanMemberRequest member : members) {
                if (member == null) {
                    continue;
                }
                member.validate();
            }
        } else {
            if (!CollectionUtils.isEmpty(members)) {
                for (DispatchPlanMemberRequest member : members) {
                    if (member == null) {
                        continue;
                    }
                    member.validate();
                }
            }
        }
        VerifyUtils.verifyTrue(!StringUtils.equals(effectStart, effectEnd), "生效时间区间不合法", true);
    }
}
