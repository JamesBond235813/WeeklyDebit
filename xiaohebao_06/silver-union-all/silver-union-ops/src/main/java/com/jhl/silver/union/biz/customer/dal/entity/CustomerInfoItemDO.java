package com.jhl.silver.union.biz.customer.dal.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
/**
 * <p>
 * 客户信息数据
 * </p>
 *
 * @author Way
 * @since 2025-12-05 23:50:59
 */
@Getter
@Setter
@ToString
@Accessors(chain = true)
@TableName("customer_info_item")
@Schema(name = "CustomerInfoItemDO", description = "客户信息数据")
public class CustomerInfoItemDO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 客户ID
     */
    @Schema(description = "客户ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 姓名
     */
    @Schema(description = "姓名")
    private String name;

    /**
     * 手机号
     */
    @Schema(description = "手机号")
    private String mobile;

    /**
     * 手机号MD5
     */
    @Schema(description = "手机号MD5")
    private String mobileMd5;

    /**
     * 手机号归属地
     */
    @Schema(description = "手机号归属地")
    private String mobileArea;

    /**
     * 性别。 0:保密, 1:男, 2:女
     */
    @Schema(description = "性别。 0:保密, 1:男, 2:女")
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
    private Integer zhimaScore;

    /**
     * 身份证号
     */
    @Schema(description = "身份证号")
    private String idCardNo;

    /**
     * 出生日期。 yyyy-MM-dd 格式
     */
    @Schema(description = "出生日期。 yyyy-MM-dd 格式")
    private String birthday;

    /**
     * 户籍所在省
     */
    @Schema(description = "户籍所在省")
    private String hukouProvince;

    /**
     * 户籍所在市
     */
    @Schema(description = "户籍所在市")
    private String hukouCity;

    /**
     * 户籍所在区县
     */
    @Schema(description = "户籍所在区县")
    private String hukouDistrict;

    /**
     * 户籍详细地址
     */
    @Schema(description = "户籍详细地址")
    private String hukouAddressDetail;

    /**
     * 当前所在地省
     */
    @Schema(description = "当前所在地省")
    private String currentProvince;

    /**
     * 当前所在地市
     */
    @Schema(description = "当前所在地市")
    private String currentCity;

    /**
     * 当前所在地区县
     */
    @Schema(description = "当前所在地区县")
    private String currentDistrict;

    /**
     * 当前所在地街道
     */
    @Schema(description = "当前所在地街道")
    private String currentStreet;

    /**
     * 当前所在地详细地址
     */
    @Schema(description = "当前所在地详细地址")
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
    private Integer houseFlag;

    /**
     * 保单标识。0:未知; 1:有; 2:无
     */
    @Schema(description = "保单标识。0:未知; 1:有; 2:无")
    private Integer insuranceFlag;

    /**
     * 社保标识。0:未知; 1:有; 2:无
     */
    @Schema(description = "社保标识。0:未知; 1:有; 2:无")
    private Integer socialInsuranceFlag;

    /**
     * 车产标识。0:未知; 1:有; 2:无
     */
    @Schema(description = "车产标识。0:未知; 1:有; 2:无")
    private Integer carFlag;

    /**
     * 公积金标识。0:未知; 1:有; 2:无
     */
    @Schema(description = "公积金标识。0:未知; 1:有; 2:无")
    private Integer providentFlag;

    /**
     * 信用卡标识。0:未知; 1:有; 2:无
     */
    @Schema(description = "信用卡标识。0:未知; 1:有; 2:无")
    private Integer creditCardFlag;

    /**
     * 企业主标识。0:未知; 1:是; 2:否
     */
    @Schema(description = "企业主标识。0:未知; 1:是; 2:否")
    private Integer enterpriseFlag;

    /**
     * 婚姻状态. 0:未知; 1:已婚; 2:未婚; 3:离异
     */
    @Schema(description = "婚姻状态. 0:未知; 1:已婚; 2:未婚; 3:离异")
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
     * 车牌号
     */
    @Schema(description = "车牌号")
    private String carNo;

    /**
     * 购车方式。 见业务枚举定义
     */
    @Schema(description = "购车方式。 见业务枚举定义")
    private String carPurchaseType;

    /**
     * 申请人城市ID
     */
    @Schema(description = "申请人城市ID")
    private String cityId;

    /**
     * 申请人城市名称
     */
    @Schema(description = "申请人城市名称")
    private String cityName;

    /**
     * 跟进状态。 见业务字典定义
     */
    @Schema(description = "跟进状态。 见业务字典定义")
    private Integer progress;

    /**
     * 住址
     */
    @Schema(description = "住址")
    private String address;

    /**
     * 工作单位
     */
    @Schema(description = "工作单位")
    private String workAddress;

    /**
     * 数据来源文件名称
     */
    @Schema(description = "数据来源文件名称")
    private String sourceFileName;

    /**
     * 推广渠道ID。见业务字典定义
     */
    @Schema(description = "推广渠道ID。见业务字典定义")
    private Integer channel;

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
     * 最后一次跟进人用户ID
     */
    @Schema(description = "最后一次跟进人用户ID")
    private Long followerUserId;

    /**
     * 最后一次跟进时间
     */
    @Schema(description = "最后一次跟进时间")
    private Date followTime;

    /**
     * 跟进情况备注
     */
    @Schema(description = "跟进情况备注")
    private String followRemark;

    /**
     * 跟进次数
     */
    @Schema(description = "跟进次数")
    private Integer followCnt;

    /**
     * 上级评价
     */
    @Schema(description = "上级评价")
    private String leaderRemark;

    /**
     * 客户备注
     */
    @Schema(description = "客户备注")
    private String customerRemark;

    /**
     * 电话备注
     */
    @Schema(description = "电话备注")
    private String callRemark;

    /**
     * 申请时间（进本平台件的时间）
     */
    @Schema(description = "申请时间（进本平台件的时间）")
    private Date applyDate;

    /**
     * 当前数据归属人用户ID
     */
    @Schema(description = "当前数据归属人用户ID")
    private Long ownerUserId;

    /**
     * 当前数据归属部门ID
     */
    @Schema(description = "当前数据归属部门ID")
    private Long ownerDeptId;

    /**
     * 收藏标识。 0:再分配客户; 1:我的客户; 2:重点客户
     */
    @Schema(description = "收藏标识。 0:再分配客户; 1:我的客户; 2:重点客户")
    private Integer ownerFavorite;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private Date gmtCreate;

    /**
     * 最后修改时间
     */
    @Schema(description = "最后修改时间")
    private Date gmtModified;

    /**
     * 数据版本信息.用于乐观锁
     */
    @Version
    @Schema(description = "数据版本信息.用于乐观锁")
    private Long version;

    public static final String ID = "id";

    public static final String NAME = "name";

    public static final String MOBILE = "mobile";

    public static final String MOBILE_MD5 = "mobile_md5";

    public static final String MOBILE_AREA = "mobile_area";

    public static final String SEX = "sex";

    public static final String AGE = "age";

    public static final String ZHIMA_SCORE = "zhima_score";

    public static final String ID_CARD_NO = "id_card_no";

    public static final String BIRTHDAY = "birthday";

    public static final String HUKOU_PROVINCE = "hukou_province";

    public static final String HUKOU_CITY = "hukou_city";

    public static final String HUKOU_DISTRICT = "hukou_district";

    public static final String HUKOU_ADDRESS_DETAIL = "hukou_address_detail";

    public static final String CURRENT_PROVINCE = "current_province";

    public static final String CURRENT_CITY = "current_city";

    public static final String CURRENT_DISTRICT = "current_district";

    public static final String CURRENT_STREET = "current_street";

    public static final String CURRENT_ADDRESS_DETAIL = "current_address_detail";

    public static final String REQ_LOAN_AMOUNT = "req_loan_amount";

    public static final String HOUSE_FLAG = "house_flag";

    public static final String INSURANCE_FLAG = "Insurance_flag";

    public static final String SOCIAL_INSURANCE_FLAG = "social_insurance_flag";

    public static final String CAR_FLAG = "car_flag";

    public static final String PROVIDENT_FLAG = "provident_flag";

    public static final String CREDIT_CARD_FLAG = "credit_card_flag";

    public static final String ENTERPRISE_FLAG = "enterprise_flag";

    public static final String MARRIAGE_STATUS = "marriage_status";

    public static final String PROVIDENT_AMOUNT_YUAN = "provident_amount_yuan";

    public static final String HOUSE_VAL = "house_val";

    public static final String CAR_NO = "car_no";

    public static final String CAR_PURCHASE_TYPE = "car_purchase_type";

    public static final String CITY_ID = "city_id";

    public static final String CITY_NAME = "city_name";

    public static final String PROGRESS = "progress";

    public static final String ADDRESS = "address";

    public static final String WORK_ADDRESS = "work_address";

    public static final String SOURCE_FILE_NAME = "source_file_name";

    public static final String CHANNEL = "channel";

    public static final String CALL_TIPS = "call_tips";

    public static final String CUSTOMER_GROUP = "customer_group";

    public static final String FOLLOWER_USER_ID = "follower_user_id";

    public static final String FOLLOW_TIME = "follow_time";

    public static final String FOLLOW_REMARK = "follow_remark";

    public static final String FOLLOW_CNT = "follow_cnt";

    public static final String LEADER_REMARK = "leader_remark";

    public static final String CUSTOMER_REMARK = "customer_remark";

    public static final String CALL_REMARK = "call_remark";

    public static final String APPLY_DATE = "apply_date";

    public static final String OWNER_USER_ID = "owner_user_id";

    public static final String OWNER_DEPT_ID = "owner_dept_id";

    public static final String OWNER_FAVORITE = "owner_favorite";

    public static final String GMT_CREATE = "gmt_create";

    public static final String GMT_MODIFIED = "gmt_modified";

    public static final String VERSION = "version";
}
