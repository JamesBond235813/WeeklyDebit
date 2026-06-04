package com.jhl.silver.union.web.data.admin;

import com.jhl.silver.union.biz.common.utils.BizHelper;
import com.jhl.silver.union.commons.utils.SuDateUtils;
import com.jhl.silver.union.commons.utils.VerifyUtils;
import com.jhl.silver.union.commons.validate.ValidateUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;

import java.util.List;

/**
 * 新增用户信息请求
 *
 * @author: qingren
 * @create_time: 2025/3/27
 */
@Data
@Accessors(chain = true)
@Schema(description = "新增用户信息请求")
public class AddUserInfoRequest {
    /**
     * 用户名,即用户登录账号，不可重复
     */
    @Schema(description = "用户名,即用户登录账号，不可重复", requiredMode = Schema.RequiredMode.REQUIRED)
    @Length(min = 1, max = 64, message = "请输入用户名,且不超过64个字符")
    @NotBlank(message = "请输入用户名,且不超过64个字符")
    private String userName;

    /**
     * 用户密码。
     */
    @Schema(description = "用户密码")
    @Length(min = 8, max = 16, message = "请指定用户初始密码,8~16位，要求包含数字和字母")
    @NotBlank(message = "请指定用户初始密码,8~16位，要求包含数字和字母")
    private String password;

    /**
     * 真实姓名
     */
    @Schema(description = "真实姓名", requiredMode = Schema.RequiredMode.REQUIRED)
    @Length(min = 1, max = 32, message = "请输入真实姓名,不超过32个字符")
    @NotBlank(message = "请输入真实姓名,不超过32个字符")
    private String realName;

    /**
     * 用户手机号
     */
    @Schema(description = "用户手机号")
    @Pattern(regexp = "1[0-9]{10}", message = "请输入用户手机号,要求为11位数字,且以1打头")
    @NotBlank(message = "请输入用户手机号,要求为11位数字,且以1打头")
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
    @Range(min = 0,max = 2,message = "请指定性别")
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
    private List<String> roles;

    public void validate() {
        //去除前后空格
        this.userName = StringUtils.trim(this.userName);
        this.password = StringUtils.trim(this.password);
        this.realName = StringUtils.trim(this.realName);
        this.phone = StringUtils.trim(this.phone);
        this.email = StringUtils.trim(this.email);
        this.birthday = StringUtils.trim(this.birthday);
        this.employeeNo = StringUtils.trim(this.employeeNo);
        this.jobName = StringUtils.trim(this.jobName);

        ValidateUtils.validateWithDefaultValidator(this);
        //密码校验
        BizHelper.verifyPassword(this.password, "请指定用户初始密码,8~16位，要求包含数字和字母");
        if (StringUtils.isNotBlank(this.birthday)) {
            VerifyUtils.verifyDate(this.birthday, SuDateUtils.DF_YYYY_MM_DD, "birthday", "请输入正确的用户生日信息",
                    true);
        }
        VerifyUtils.verifyByRegex(this.email, "[a-zA-Z0-9.]+@[a-zA-Z0-9.]+", "email", "请输入正确的 email", true,
                true);

    }

}
