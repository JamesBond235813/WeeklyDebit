package com.jhl.silver.union.web.data;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 上级评价信息
 *
 * @author: qingren
 * @create_time: 2025/4/8
 */
@Data
@Accessors(chain = true)
@Schema(description = "上级评价信息")
public class LeaderRemarkDTO {
    /**
     * 上级姓名
     */
    @Schema(description = "上级姓名")
    private String name;
    /**
     * 评价时间 yyyy-MM-dd HH:mm:ss
     */
    @Schema(description = "评价时间 yyyy-MM-dd HH:mm:ss")
    private String commentTimeStr;
    /**
     * 评价内容
     */
    @Schema(description = "评价内容")
    private String comment;

    public static LeaderRemarkDTO of(String name, String comment, String commentTimeStr) {
        return new LeaderRemarkDTO()
                .setName(name)
                .setComment(comment)
                .setCommentTimeStr(commentTimeStr);
    }

}
