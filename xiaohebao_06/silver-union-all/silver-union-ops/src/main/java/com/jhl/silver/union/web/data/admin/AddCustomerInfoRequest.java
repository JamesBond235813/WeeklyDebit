package com.jhl.silver.union.web.data.admin;

import com.jhl.silver.union.commons.utils.SuDateUtils;
import com.jhl.silver.union.commons.utils.VerifyUtils;
import com.jhl.silver.union.commons.validate.ValidateUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;

import java.text.ParseException;
import java.util.Date;
import java.util.Objects;

/**
 * 新增客户信息请求
 *
 * @author: qingren
 * @create_time: 2025/4/2
 */
@Data
@Accessors(chain = true)
@Schema(description = "新增客户信息请求")
public class AddCustomerInfoRequest {
    /**
     * 姓名
     */
    @Schema(description = "姓名", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "请输入客户姓名，不超过64个字符")
    @Length(min = 1, max = 64, message = "请输入客户姓名，不超过64个字符")
    private String name;
    /**
     * 手机号
     */
    @Schema(description = "手机号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "请输入客户户手机号,要求为11位数字,且以1打头")
    @Pattern(regexp = "1[0-9]{10}", message = "请输入客户户手机号,要求为11位数字,且以1打头")
    private String mobile;
    /**
     * 手机号归属地
     */
    @Schema(description = "手机号归属地")
    @Length(max = 64, message = "手机号归属地不超64个字符")
    private String mobileArea;
    /**
     * 身份证号
     */
    @Schema(description = "身份证号")
    private String idCardNo;

    /**
     * 数据来源文件名称
     */
    @Schema(description = "数据来源文件名称")
    @Length(max = 200, message = "数据来源文件名，不可超过200个字符")
    private String sourceFileName;

    /**
     * 推广渠道ID
     */
    @Schema(description = "推广渠道ID")
    private Integer channel;
    /**
     * 当前数据归属部门ID
     */
    @Schema(description = "当前数据归属部门ID")
    private Long ownerDeptId;

    /**
     * 性别。 0:保密, 1:男, 2:女
     */
    @Schema(description = "性别。 0:保密, 1:男, 2:女")
    @Range(min = 0, max = 2, message = "请指定性别")
    private Integer sex;

    /**
     * 年龄
     */
    @Schema(description = "年龄")
    private Integer age;
    /**
     * 芝麻分
     */
    @Schema(description = "芝麻分")
    @Range(min = 0, max = 950, message = "芝麻分范围为0-950")
    private Integer zhimaScore;
    /**
     * 出生日期。 yyyy-MM-dd 格式
     */
    @Schema(description = "出生日期。 yyyy-MM-dd 格式")
    private String birthday;

    /**
     * 户籍所在省
     */
    @Schema(description = "户籍所在省")
    @Length(max = 64, message = "户籍所在省不超过64个字符")
    private String hukouProvince;

    /**
     * 户籍所在市
     */
    @Schema(description = "户籍所在市")
    @Length(max = 64, message = "户籍所在市不超过64个字符")
    private String hukouCity;

    /**
     * 户籍所在区县
     */
    @Schema(description = "户籍所在区县")
    @Length(max = 64, message = "户籍所在区县不超过64个字符")
    private String hukouDistrict;
    /**
     * 户籍详细地址
     */
    @Schema(description = "户籍详细地址")
    @Length(max = 128, message = "户籍详细地址不超过128个字符")
    private String hukouAddressDetail;

    /**
     * 当前所在地省
     */
    @Schema(description = "当前所在地省")
    @Length(max = 64, message = "当前所在地省不超过64个字符")
    private String currentProvince;

    /**
     * 当前所在地市
     */
    @Schema(description = "当前所在地市")
    @Length(max = 64, message = "当前所在地市不超过64个字符")
    private String currentCity;

    /**
     * 当前所在地区县
     */
    @Schema(description = "当前所在地区县")
    @Length(max = 64, message = "当前所在地区县不超过64个字符")
    private String currentDistrict;

    /**
     * 当前所在地街道
     */
    @Schema(description = "当前所在地街道")
    @Length(max = 64, message = "当前所在地街道不超过64个字符")
    private String currentStreet;
    /**
     * 当前所在地详细地址
     */
    @Schema(description = "当前所在地详细地址")
    @Length(max = 128, message = "当前所在地详细地址不超过128个字符")
    private String currentAddressDetail;
    /**
     * 目标贷款金额，单位元
     */
    @Schema(description = "目标贷款金额，单位元")
    private Integer reqLoanAmount;
    /**
     * 房产标识。0:未知; 1:有京房; 2:有房; 3:无
     */
    @Schema(description = "房产标识。0:未知; 1:有京房; 2:有房; 3:无")
    @Range(min = 0, max = 3, message = "请指定房产标识")
    private Integer houseFlag;

    /**
     * 保单标识。0:未知; 1:有; 2:无
     */
    @Schema(description = "保单标识。0:未知; 1:有; 2:无")
    @Range(min = 0, max = 2, message = "请指定保单标识")
    private Integer insuranceFlag;

    /**
     * 社保标识。0:未知; 1:有; 2:无
     */
    @Schema(description = "社保标识。0:未知; 1:有; 2:无")
    @Range(min = 0, max = 2, message = "请指定社保标识")
    private Integer socialInsuranceFlag;

    /**
     * 车产标识。0:未知; 1:有; 2:无
     */
    @Schema(description = "车产标识。0:未知; 1:有; 2:无")
    @Range(min = 0, max = 2, message = "请指定车产情况")
    private Integer carFlag;

    /**
     * 公积金标识。0:未知; 1:有; 2:无
     */
    @Schema(description = "公积金标识。0:未知; 1:有; 2:无")
    @Range(min = 0, max = 2, message = "请指定公积金标识")
    private Integer providentFlag;

    /**
     * 信用卡标识。0:未知; 1:有; 2:无
     */
    @Schema(description = "信用卡标识。0:未知; 1:有; 2:无")
    @Range(min = 0, max = 2, message = "请指定信用卡标识")
    private Integer creditCardFlag;

    /**
     * 企业主标识。0:未知; 1:是; 2:否
     */
    @Schema(description = "企业主标识。0:未知; 1:是; 2:否")
    @Range(min = 0, max = 2, message = "请指定企业主标识")
    private Integer enterpriseFlag;

    /**
     * 婚姻状态. 0:未知; 1:已婚; 2:未婚; 3:离异
     */
    @Schema(description = "婚姻状态. 0:未知; 1:已婚; 2:未婚; 3:离异")
    @Range(min = 0, max = 3, message = "请指定婚姻状况标识")
    private Integer marriageStatus;

    /**
     * 公积金金额， 单位元
     */
    @Schema(description = "公积金金额， 单位元")
    private Integer providentAmountYuan;

    /**
     * 房产价值，单位万元
     */
    @Schema(description = "房产价值，单位万元")
    private Integer houseVal;

    /**
     * 客户分组，见业务字典定义
     */
    @Schema(description = "客户分组，见业务字典定义")
    private Integer customerGroup;

    /**
     * 申请时间（进本平台件的时间）
     */
    @Schema(description = "申请时间。  yyyy-MM-dd HH:mm:ss格式")
    private String applyDateStr;

    /**
     * 申请时间（进本平台件的时间）
     */
    @Schema(hidden = true)
    private Date applyDate;

    /**
     * 去除字符串前后空格
     */
    public void trimText() {
        this.mobileArea = StringUtils.trim(this.mobileArea);
        this.birthday = StringUtils.trim(this.birthday);
        this.mobile = StringUtils.trim(this.mobile);
        this.name = StringUtils.trim(this.name);
        this.idCardNo = StringUtils.defaultIfBlank(StringUtils.upperCase(StringUtils.trim(this.idCardNo)), null);
        this.hukouProvince = StringUtils.trim(this.hukouProvince);
        this.hukouCity = StringUtils.trim(this.hukouCity);
        this.hukouDistrict = StringUtils.trim(this.hukouDistrict);
        this.hukouAddressDetail = StringUtils.trim(this.hukouAddressDetail);
        this.currentProvince = StringUtils.trim(this.currentProvince);
        this.currentCity = StringUtils.trim(this.currentCity);
        this.currentDistrict = StringUtils.trim(this.currentDistrict);
        this.currentStreet = StringUtils.trim(this.currentStreet);
        this.currentAddressDetail = StringUtils.trim(this.currentAddressDetail);
        this.sourceFileName = StringUtils.trim(this.sourceFileName);
    }

    @SuppressWarnings("DuplicatedCode")
    public void validate() {
        this.trimText();
        ValidateUtils.validateWithDefaultValidator(this);
        if (StringUtils.isNotBlank(this.birthday)) {
            VerifyUtils.verifyDate(this.birthday, SuDateUtils.DF_YYYY_MM_DD, "birthday", "请输入正确的客户出生日期",
                    true);
        }
        if (Objects.nonNull(this.age)) {
            VerifyUtils.judge(this.age > 0 && this.age <= 120, "请输入正确的年龄", true, "age=" + this.age);
        }

        VerifyUtils.verifyByRegex(this.idCardNo, "([0-9]{17}([0-9]|[xX]))", "idCardNo",
                "请输入身份证号,仅支持中国大陆18位身份证号", true, true);
        if (StringUtils.isBlank(this.applyDateStr)) {
            this.applyDate = new Date();
            this.applyDateStr = SuDateUtils.format(this.applyDate, SuDateUtils.DF_YYYY_MM_DDHHMMSS);
        } else {
            try {
                this.applyDate = SuDateUtils.parse(this.applyDateStr, SuDateUtils.DF_YYYY_MM_DDHHMMSS);
            } catch (ParseException e) {
                VerifyUtils.notNull(this.applyDate, "applyDateStr", "请输入正确的申请时间", true);
            }

        }
    }
}
