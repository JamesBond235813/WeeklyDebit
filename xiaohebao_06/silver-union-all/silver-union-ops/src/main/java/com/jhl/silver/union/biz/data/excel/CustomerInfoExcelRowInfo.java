package com.jhl.silver.union.biz.data.excel;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.jhl.silver.union.biz.common.enums.BizFlagEnum;
import com.jhl.silver.union.biz.common.enums.HouseFlagEnum;
import com.jhl.silver.union.biz.common.enums.PurchaseTypeEnum;
import com.jhl.silver.union.commons.utils.VerifyUtils;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * 客户导入信息数据
 *
 * @author: qingren
 * @create_time: 2025/5/7
 */
@Data
@ExcelIgnoreUnannotated
public class CustomerInfoExcelRowInfo {
    /**
     * Excel模板版本号
     */
    public static final String VERSION = "IMP-CST-1.0";
    public static final String TEMPLATE_VERSION_KEYWORD = "版本标识";
    public static final int VERSION_KEY_COLUMN = 0;
    public static final int VERSION_VALUE_COLUMN = 2;

    /**
     * 最大数据量
     */
    public static final int MAX_RECORD_CNT = 5000;
    /**
     * 模板头信息行数
     */
    public static final int HEADER_LINES = 6;

    private static final Pattern ID_CARD_PATTERN = Pattern.compile("^\\d{17}[0-9xX]$");
    private static final Pattern NUM_PATTERN = Pattern.compile("^[0-9]+$");
    private static final int maxRemarkLength = 2500;

    /**
     * 行号
     */
    private Integer lineNo;
    /**
     * 客户姓名（必填）
     */
    @ExcelProperty(index = 0)
    private String name;
    /**
     * 手机号（必填）
     */
    @ExcelProperty(index = 1)
    private String mobile;
    /**
     * 身份证号
     */
    @ExcelProperty(index = 2)
    private String idCardNo;
    /**
     * 房产情况
     */
    @ExcelProperty(index = 3)
    private String houseFlagDescription;
    private HouseFlagEnum houseFlagEnum;
    /**
     * 房值（万元）
     */
    @ExcelProperty(index = 4)
    private String houseValStr;
    private Integer houseVal;
    /**
     * 渠道信息
     */
    @ExcelProperty(index = 5)
    private String channelName;
    /**
     * 客户备注
     */
    @ExcelProperty(index = 6)
    private String customerRemark;

    /**
     * 车产情况（可选，仅限：未知/有/无，默认未知）
     */
    private String carFlagDescription;
    private BizFlagEnum carFlagEnum;
    /**
     * 车牌号
     */
    private String carNo;
    /**
     * 购车方式。（可选，仅限：未知/全款/贷款-已结清/贷款-未结清)
     */
    private String carPurchaseTypeDescription;
    private PurchaseTypeEnum carPurchaseTypeEnum;
    /**
     * 申请人城市ID
     */
    private String cityId;
    /**
     * 申请人城市名称
     */
    private String cityName;
    private Integer reqLoanAmount;
    private Integer sex;
    private Integer age;
    private String providentFlagDescription;
    private BizFlagEnum providentFlagEnum;
    private String socialInsuranceFlagDescription;
    private BizFlagEnum socialInsuranceFlagEnum;
    private String insuranceFlagDescription;
    private BizFlagEnum insuranceFlagEnum;

    public boolean isEmpty() {
        boolean empty = true;
        empty &= StringUtils.isBlank(name);
        empty &= StringUtils.isBlank(mobile);
        empty &= StringUtils.isBlank(idCardNo);
        empty &= StringUtils.isBlank(houseFlagDescription);
        empty &= StringUtils.isBlank(houseValStr);
        empty &= StringUtils.isBlank(channelName);
        empty &= StringUtils.isBlank(customerRemark);
        return empty;
    }

    /**
     * 确保备注内容长度不超限
     */
    public void ensureRemarkLengthSafe() {
        if (StringUtils.isBlank(this.customerRemark) || StringUtils.length(this.customerRemark) <= maxRemarkLength) {
            return;
        }
        this.customerRemark = this.customerRemark.substring(0, maxRemarkLength);
    }

    public String validateAndReturnErrMsg() {
        StringBuilder errMsgBuilder = new StringBuilder();
        if (!VerifyUtils.maxStringLength(this.name, 64, "name", false, false)) {
            errMsgBuilder.append("请填写客户姓名，且不超过64个字符；");
        }
        if (!VerifyUtils.verifyPhoneNo(this.mobile, false, "mobile")) {
            errMsgBuilder.append("请填写正确的客户手机号，以1开头的11位数字；");
        }
        if (!VerifyUtils.verifyByPattern(this.idCardNo, ID_CARD_PATTERN, "idCardNo", "", true, false)) {
            errMsgBuilder.append("请填写正确的18位身份证号，非必填；");
        }

        this.houseFlagEnum = StringUtils.isBlank(this.houseFlagDescription)
                ? HouseFlagEnum.UNKNOWN
                : HouseFlagEnum.findByDesc(this.houseFlagDescription);
        if (!VerifyUtils.notNull(this.houseFlagEnum, "", false)) {
            errMsgBuilder.append(
                    "请填写正确的房产情况，仅可填写未知, 有京房, 有房, 无，四个值中的一个。不填写默认为未知；");
        }
        if (!VerifyUtils.verifyByPattern(this.houseValStr, NUM_PATTERN, "houseValStr", "", true, false)) {
            errMsgBuilder.append("请填写正确的房值，要求填写正整数，非必填；");
        } else {
            this.houseVal = StringUtils.isNotBlank(houseValStr) ? Integer.parseInt(houseValStr) : null;
        }
        if (!VerifyUtils.maxStringLength(this.channelName, 16, "channelName", true, false)) {
            errMsgBuilder.append("渠道名称，不可超过16个字符，非必填；");
        }
        if (!VerifyUtils.maxStringLength(this.customerRemark, maxRemarkLength, "customerRemark", true, false)) {
            errMsgBuilder.append("客户备注，不可超过2500个字符，非必填；");
        }
        if (errMsgBuilder.length() > 0) {
            if (Objects.nonNull(this.lineNo)) {
                errMsgBuilder.insert(0, "第" + this.lineNo + "行：");
            }
            return errMsgBuilder.toString();
        }
        if (StringUtils.isNotBlank(this.carFlagDescription)) {
            this.carFlagEnum = BizFlagEnum.findByDesc(this.carFlagDescription);
            if (!VerifyUtils.notNull(this.carFlagEnum, "", false)) {
                errMsgBuilder.append("请填写正确的车产情况，仅可填写未知/有/无，默认未知；");
            }
        }

        if (StringUtils.isNotBlank(this.carPurchaseTypeDescription)) {
            this.carPurchaseTypeEnum = PurchaseTypeEnum.findByDesc(this.carPurchaseTypeDescription);
            if (!VerifyUtils.notNull(this.carPurchaseTypeEnum, "", false)) {
                errMsgBuilder.append("请填写正确的购车方式，仅可填写未知/全款/贷款-已结清/贷款-未结清，默认未知；");
            }
        }
        this.providentFlagEnum = parseBizFlag(this.providentFlagDescription);
        this.socialInsuranceFlagEnum = parseBizFlag(this.socialInsuranceFlagDescription);
        this.insuranceFlagEnum = parseBizFlag(this.insuranceFlagDescription);

        return StringUtils.EMPTY;
    }

    private BizFlagEnum parseBizFlag(String desc) {
        if (StringUtils.isBlank(desc)) {
            return BizFlagEnum.UNKNOWN;
        }
        BizFlagEnum flagEnum = BizFlagEnum.findByDesc(desc);
        return Objects.nonNull(flagEnum) ? flagEnum : BizFlagEnum.UNKNOWN;
    }

    public void setName(String name) {
        this.name = StringUtils.trim(name);
    }

    public void setMobile(String mobile) {
        this.mobile = StringUtils.trim(mobile);
    }

    public void setIdCardNo(String idCardNo) {
        this.idCardNo = StringUtils.trim(idCardNo);
        this.idCardNo = StringUtils.upperCase(this.idCardNo);
    }

    public void setHouseFlagDescription(String houseFlagDescription) {
        this.houseFlagDescription = StringUtils.trim(houseFlagDescription);
    }

    public void setHouseValStr(String houseValStr) {
        this.houseValStr = StringUtils.trim(houseValStr);
    }

    public void setChannelName(String channelName) {
        this.channelName = StringUtils.trim(channelName);
    }

    public void setCustomerRemark(String customerRemark) {
        this.customerRemark = StringUtils.trim(customerRemark);
    }
}
