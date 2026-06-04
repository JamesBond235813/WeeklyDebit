package com.jhl.silver.union.web.data;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 分页获取上传文件记录请求
 *
 * @author: qingren
 * @create_time: 2025/5/9
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(description = "分页获取上传文件记录请求")
public class PagedListImportRecordRequest extends BasePagedRequest {

    /**
     * 是否只取当前登录用户操作的导入记录
     */
    @Schema(description = "是否只取当前登录用户操作的导入记录。true:是")
    private boolean selfOnly;
}
