package com.jhl.silver.union.web.data.admin;

import com.jhl.silver.union.commons.validate.ValidateUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;

import java.util.List;

/**
 * 更新用户信息请求
 *
 * @author: qingren
 * @create_time: 2025/3/27
 */
@Data
@Accessors(chain = true)
@Schema(description = "更新用户信息请求")
public class UpdateUserInfoRequest {
    /**
     * 用户ID
     */
    @Schema(description = "用户ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "请指定需要更新信息的用户")
    private Long id;

    /**
     * 真实姓名
     */
    @Schema(description = "真实姓名", requiredMode = Schema.RequiredMode.REQUIRED)
    @Length(min = 1, max = 32, message = "请输入真实姓名,不超过32个字符")
    private String realName;

    /**
     * 用户手机号
     */
    @Schema(description = "用户手机号")
    @Pattern(regexp = "1[0-9]{10}", message = "请输入用户手机号,要求为11位数字,且以1打头")
    private String phone;

    /**
     * 用户邮箱
     */
    @Schema(description = "用户邮箱")
    private String email;

    /**
     * 性别。 0:保密, 1:男, 2:女
     */
    @Schema(description = "性别。 0:保密, 1:男, 2:女")
    @Range(min = 0, max = 2, message = "请指定性别")
    private Integer sex;

    /**
     * 生日 yyyy-MM-dd格式
     */
    @Schema(description = "生日 yyyy-MM-dd格式")
    private String birthday;

    /**
     * 工号
     */
    @Schema(description = "工号")
    @Length(max = 32, message = "工号不超过32个字符")
    private String employeeNo;

    /**
     * 部门id
     */
    @Schema(description = "部门id")
    private Long departmentId;

    /**
     * 职位
     */
    @Schema(description = "职位")
    @Length(max = 32, message = "职位信息不超过32个字符")
    private String jobName;

    /**
     * 权限角色列表
     */
    @Schema(description = "权限角色列表")
    private List<String> roles;

    public void validate() {
        ValidateUtils.validateWithDefaultValidator(this);
        // 去除前后空格
        this.realName = StringUtils.trim(this.realName);
        this.phone = StringUtils.trim(this.phone);
        this.email = StringUtils.trim(this.email);
        this.birthday = StringUtils.trim(this.birthday);
        this.employeeNo = StringUtils.trim(this.employeeNo);
        this.jobName = StringUtils.trim(this.jobName);
    }

}
