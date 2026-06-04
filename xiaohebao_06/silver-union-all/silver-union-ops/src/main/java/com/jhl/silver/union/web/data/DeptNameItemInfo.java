package com.jhl.silver.union.web.data;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 部门简易信息
 * @author: qingren
 * @create_time: 2025/4/15
 */
@Data
@Accessors(chain = true)
@Schema(description = "部门简易信息")
public class DeptNameItemInfo {
    /**
     * 部门ID
     */
    @Schema(description = "部门ID")
    private Long id;
    /**
     * 部门名称
     */
    @Schema(description = "部门名称")
    private String deptName;

    public static DeptNameItemInfo of(Long id, String deptName){
        return new DeptNameItemInfo()
                .setId(id)
                .setDeptName(deptName);
    }
}
