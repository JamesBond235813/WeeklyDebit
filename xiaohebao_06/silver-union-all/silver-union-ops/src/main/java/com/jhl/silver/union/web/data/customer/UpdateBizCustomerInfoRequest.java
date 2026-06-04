package com.jhl.silver.union.web.data.customer;

import com.jhl.silver.union.commons.utils.SuDateUtils;
import com.jhl.silver.union.commons.utils.VerifyUtils;
import com.jhl.silver.union.commons.validate.ValidateUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;

import java.util.Objects;

/**
 * 更新客户业务信息请求。 不可更新姓名以及手机号
 *
 * @author: qingren
 * @create_time: 2025/4/1
 */
@Data
@Accessors(chain = true)
@Schema(description = "更新客户业务信息请求。 不可更新姓名等自身属性信息")
public class UpdateBizCustomerInfoRequest {

    /**
     * 客户ID
     */
    @Schema(description = "客户ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "请指定要修改信息的客户")
    private Long id;

    /**
     * 手机号归属地
     */
    @Schema(description = "手机号归属地")
    @Length(max = 64, message = "手机号归属地不超过64个字符")
    private String mobileArea;

    /**
     * 身份证号
     */
    @Schema(description = "身份证号")
    private String idCardNo;

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
     * 跟进状态。 见业务字典定义
     */
    @Schema(description = "跟进状态。 见业务字典定义")
    private Integer progress;

    /**
     * 住址
     */
    @Schema(description = "住址")
    @Length(max = 512, message = "住址不可超512个字符")
    private String address;

    /**
     * 工作单位
     */
    @Schema(description = "工作单位")
    @Length(max = 512, message = "工作单位不可超512个字符")
    private String workAddress;

    /**
     * 电话结果。见业务字典定义
     */
    @Schema(description = "电话结果。见业务字典定义")
    private Integer callTips;

    /**
     * 客户分组，见业务字典定义
     */
    @Schema(description = "客户分组，见业务字典定义")
    private Integer customerGroup;

    /**
     * 跟进情况备注
     */
    @Schema(description = "跟进情况备注")
    @Length(max = 1024, message = "跟进情况备注不可超1024个字符")
    private String followRemark;

    /**
     * 客户备注
     */
    @Schema(description = "客户备注")
    @Length(max = 2500, message = "客户备注不可超2500个字符")
    private String customerRemark;

    /**
     * 电话备注
     */
    @Schema(description = "电话备注")
    @Length(max = 1024, message = "电话备注不可超1024个字符")
    private String callRemark;

    /**
     * 收藏标识。 可见{@link com.jhl.silver.union.biz.common.enums.FavoriteTypeEnum#code }定义
     */
    @Schema(description = "收藏标识。 0:再分配客户; 1:我的客户; 2:重点客户 ")
    @Range(min = 0, max = 2, message = "请指定收藏标识")
    private Integer ownerFavorite;

    /**
     * 上级评价
     */
    @Schema(description = "上级评价", requiredMode = Schema.RequiredMode.REQUIRED)
    @Length(min = 0, max = 300, message = "请输入上级评价，不超过300个字符")
    private String leaderRemark;

    /**
     * 数据版本信息.用于乐观锁
     */
    @Schema(description = "数据版本信息.用于乐观锁", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "数据不完整，请确认后再操作")
    private Long version;

    /**
     * 去除字符串前后空格
     */
    public void trimText() {
        this.mobileArea = StringUtils.trim(this.mobileArea);
        this.idCardNo = StringUtils.defaultIfBlank(StringUtils.upperCase(StringUtils.trim(this.idCardNo)), null);
        this.birthday = StringUtils.trim(this.birthday);
        this.hukouProvince = StringUtils.trim(this.hukouProvince);
        this.hukouCity = StringUtils.trim(this.hukouCity);
        this.hukouDistrict = StringUtils.trim(this.hukouDistrict);
        this.hukouAddressDetail = StringUtils.trim(this.hukouAddressDetail);
        this.currentProvince = StringUtils.trim(this.currentProvince);
        this.currentCity = StringUtils.trim(this.currentCity);
        this.currentDistrict = StringUtils.trim(this.currentDistrict);
        this.currentStreet = StringUtils.trim(this.currentStreet);
        this.currentAddressDetail = StringUtils.trim(this.currentAddressDetail);
        this.address = StringUtils.trim(this.address);
        this.workAddress = StringUtils.trim(this.workAddress);
        this.followRemark = StringUtils.trim(this.followRemark);
        this.customerRemark = StringUtils.trim(this.customerRemark);
        this.callRemark = StringUtils.trim(this.callRemark);
        this.leaderRemark = StringUtils.trim(this.leaderRemark);
    }

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
    }
}
