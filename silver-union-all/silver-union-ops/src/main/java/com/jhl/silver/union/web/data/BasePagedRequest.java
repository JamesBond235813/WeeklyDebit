package com.jhl.silver.union.web.data;

import com.jhl.silver.union.commons.utils.VerifyUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Objects;

/**
 * 分页查询请求基类
 *
 * @author: qingren
 * @create_time: 2025/3/27
 */
@Data
@Accessors(chain = true)
@Schema(description = "分页查询请求基类")
public abstract class BasePagedRequest {
    public static final Integer DEFAULT_PAGE_SIZE = 20;
    public static final Integer DEFAULT_MAX_PAGE_SIZE = 100;

    /**
     * 当前页号
     */
    @Schema(description = "当前页号")
    private Integer page;
    /**
     * 单页数据条数
     */
    @Schema(description = "单页数据条数")
    private Integer pageSize;

    public void autoFix() {
        if (Objects.isNull(page) || page < 1) {
            page = 1;
        }
        if (Objects.isNull(pageSize) || pageSize < 1) {
            pageSize = DEFAULT_PAGE_SIZE;
        }
        VerifyUtils.verifyTrue(pageSize <= DEFAULT_MAX_PAGE_SIZE, "超过最大页数量,拒绝操作", true);
    }

}
