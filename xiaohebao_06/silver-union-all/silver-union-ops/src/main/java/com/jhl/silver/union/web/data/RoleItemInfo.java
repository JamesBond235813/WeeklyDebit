package com.jhl.silver.union.web.data;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 权限角色信息
 *
 * @author: qingren
 * @create_time: 2025/3/27
 */
@Data
@Accessors(chain = true)
@Schema(description = "权限角色信息")
public class RoleItemInfo {
    /**
     * 角色名称
     */
    @Schema(description = "角色名称")
    private String roleName;

    /**
     * 用于展示的角色名称
     */
    @Schema(description = "用于展示的角色名称")
    private String roleDispName;
    /**
     * 角色权限说明
     */
    @Schema(description = "角色权限说明")
    private String desc;


    public static  RoleItemInfo of(String roleName,String dispName, String desc) {
        RoleItemInfo roleItemInfo = new RoleItemInfo()
                .setRoleName(roleName)
                .setDesc(desc)
                .setRoleDispName(dispName);
        return roleItemInfo;
    }
}
