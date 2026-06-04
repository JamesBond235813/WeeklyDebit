package com.jhl.silver.union.web.data.customer;

import com.jhl.silver.union.biz.common.enums.FavoriteTypeEnum;
import com.jhl.silver.union.web.data.IValidateRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Range;

import java.util.List;

/**
 * 将客户信息批量标记为不同的收藏类型请求
 *
 * @author: qingren
 * @create_time: 2025/4/11
 */
@Data
@Accessors(chain = true)
@Schema(description = "将客户信息批量标记为不同的收藏类型请求")
public class BatchSwitchFavoriteRequest implements IValidateRequest {
    /**
     * 客户ID列表
     */
    @Schema(description = "客户ID列表")
    @NotNull(message = "请指定需要操作的客户")
    @Size(min = 1, max = 100, message = "请指定需要操作的客户,最多不超过100个")
    private List<Long> custIdList;

    /**
     * 收藏类型
     */
    @Schema(description = "收藏类型")
    @NotNull(message = "请指定收藏类型")
    @Range(min = 0, max = 2, message = "请指定收藏类型")
    private Integer favoriteType;

    @Schema(hidden = true)
    private FavoriteTypeEnum favoriteTypeEnum;

    @Override
    public void validate() {
        IValidateRequest.super.validate();

        this.favoriteTypeEnum = FavoriteTypeEnum.findBy(this.favoriteType);
    }
}
