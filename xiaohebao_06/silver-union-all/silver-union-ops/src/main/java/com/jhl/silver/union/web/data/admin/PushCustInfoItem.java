package com.jhl.silver.union.web.data.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 推送客户信息请求-客户信息项
 *
 * @author: qingren
 * @create_time: 2025/5/18
 */
@Data
@Accessors(chain = true)
@Schema(description = "推送客户信息请求-客户信息项")
public class PushCustInfoItem {
    /**
     * 客户姓名（必填）
     */
    @Schema(description = "客户姓名（必填）。encryptType为AES时传加密后的Base64字符串", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;
    /**
     * 手机号（必填）
     */
    @Schema(description = "手机号（必填）。encryptType为AES时传加密后的Base64字符串", requiredMode = Schema.RequiredMode.REQUIRED)
    private String mobile;
    /**
     * 身份证号
     */
    @Schema(description = "身份证号。encryptType为AES时传加密后的Base64字符串")
    private String idCardNo;
    /**
     * 房产情况
     */
    @Schema(description = "房产情况。仅可填写未知, 有京房, 有房, 无，四个值中的一个。不填写默认为未知；")
    private String houseFlagDescription;
    /**
     * 房值（万元）
     */
    @Schema(description = "房值（万元）")
    private String houseValStr;
    /**
     * 渠道信息
     */
    @Schema(description = "渠道信息")
    private String channelName;
    /**
     * 客户备注
     */
    @Schema(description = "客户备注, 超过2500个字符的部分将被截断")
    private String customerRemark;

    /** 车产情况（可选，仅限：未知/有/无，默认未知） */
    @Schema(description = "车产情况（可选，仅限：未知/有/无，默认未知）")
    private String carFlagDescription;
    /** 车牌号 */
    @Schema(description = "车牌号")
    private String carNo;
    /** 购车方式。（可选，仅限：未知/全款/贷款-已结清/贷款-未结清) */
    @Schema(description = "购车方式。（可选，仅限：未知/全款/贷款-已结清/贷款-未结清)")
    private String carPurchaseTypeDescription;
    /** 申请人城市ID */
    @Schema(description = "申请人城市ID")
    private String cityId;
    /** 申请人城市ID */
    @Schema(description = "申请人城市ID")
    private String cityName;
    /** 目标贷款金额，单位元 */
    @Schema(description = "目标贷款金额，单位元")
    private Integer reqLoanAmount;
    /** 性别。0:保密, 1:男, 2:女 */
    @Schema(description = "性别。0:保密, 1:男, 2:女")
    private Integer sex;
    /** 年龄 */
    @Schema(description = "年龄")
    private Integer age;
    /** 公积金情况（可选，仅限：未知/有/无，默认未知） */
    @Schema(description = "公积金情况（可选，仅限：未知/有/无，默认未知）")
    private String providentFlagDescription;
    /** 社保情况（可选，仅限：未知/有/无，默认未知） */
    @Schema(description = "社保情况（可选，仅限：未知/有/无，默认未知）")
    private String socialInsuranceFlagDescription;
    /** 保单情况（可选，仅限：未知/有/无，默认未知） */
    @Schema(description = "保单情况（可选，仅限：未知/有/无，默认未知）")
    private String insuranceFlagDescription;
}
