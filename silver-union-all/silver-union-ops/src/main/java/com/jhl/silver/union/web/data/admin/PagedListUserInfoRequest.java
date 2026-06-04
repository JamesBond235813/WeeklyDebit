package com.jhl.silver.union.web.data.admin;

import com.jhl.silver.union.web.data.BasePagedRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 分页查询用户信息列表请求
 *
 * @author: qingren
 * @create_time: 2025/3/27
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(description = "分页查询用户信息列表请求")
public class PagedListUserInfoRequest extends BasePagedRequest {

    /**
     * 用户ID
     */
    @Schema(description = "用户ID")
    private Long userId;

    /**
     * 用户真实姓名前缀
     */
    @Schema(description = "用户真实姓名前缀")
    private String userRealNamePrefix;

    /**
     * 用户名（登录账号）
     */
    @Schema(description = "用户名（登录账号）")
    private String userName;

    /**
     * 用户手机号
     */
    @Schema(description = "用户手机号")
    private String phone;

    /**
     * 归属部门Id
     */
    @Schema(description = "归属部门Id")
    private Long departmentId;

    /**
     * 是否需要部门名称,创建人姓名等扩展信息 true:需要， 其它不需要。
     */
    @Schema(description = "是否需要部门名称,创建人姓名等扩展信息 true:需要， 其它不需要。")
    private Boolean needExtendQry;
}
