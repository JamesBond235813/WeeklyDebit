package com.jhl.silver.union.web.data.admin;

import com.jhl.silver.union.web.data.IValidateRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 分配客户信息请求
 *
 * @author: qingren
 * @create_time: 2025/4/2
 */
@Data
@Accessors(chain = true)
@Schema(description = "分配客户信息请求")
public class DispatchCustomerInfoRequest implements IValidateRequest {

    /**
     * 客户ID
     */
    @Schema(description = "客户ID 列表")
    @NotNull(message = "请指定需要分配的客户信息")
    @Size(min = 1, max = 100, message = "请指定需要分配的客户信息,一次最多分配100个客户信息")
    private List<Long> cids;

    /**
     * 指派对象员工的用户ID
     */
    @Schema(description = "指派对象员工的用户ID")
    private Long ownerId;
    /**
     * 指派对象的部门ID
     */
    @Schema(description = "指派对象的部门ID")
    @NotNull(message = "请指定分配的部门")
    private Long ownerDeptId;

    @Override
    public void validate() {
        IValidateRequest.super.validate();
        // 去重
        this.cids = this.cids.stream()
                .distinct()
                .collect(Collectors.toList());
    }
}
