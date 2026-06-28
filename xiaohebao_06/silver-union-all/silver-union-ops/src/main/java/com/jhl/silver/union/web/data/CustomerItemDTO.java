package com.jhl.silver.union.web.data;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.jhl.silver.union.commons.CommonConstant;
import com.jhl.silver.union.commons.utils.SuDateUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * 客户信息
 *
 * @author: qingren
 * @create_time: 2025/3/30
 */
@Data
@Accessors(chain = true)
@Schema(description = "客户信息")
public class CustomerItemDTO {
    /**
     * 客户ID
     */
    @Schema(description = "客户ID")
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

    @Schema(description = "HYY房产语义")
    private String hyyHouseDesc;

    @Schema(description = "HYY车辆语义")
    private String hyyCarDesc;

    @Schema(description = "HYY公积金语义")
    private String hyyProvidentDesc;

    @Schema(description = "HYY社保语义")
    private String hyySocialInsuranceDesc;

    @Schema(description = "HYY投保语义")
    private String hyyInsuranceDesc;

    @Schema(description = "HYY职业语义")
    private String hyyOccupationDesc;

    @Schema(description = "HYY逾期情况语义")
    private String hyyOverdueDesc;

    @Schema(description = "HYY芝麻分档语义")
    private String hyyZhimaDesc;

    @Schema(description = "HYY贷款额度语义")
    private String hyyLoanAmountDesc;

    @Schema(description = "HYY推送IP")
    private String hyyIp;
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
     * 推广渠道ID
     */
    @Schema(description = "推广渠道ID。见业务字典定义")
    private Integer channel;
    /**
     * 用户来源（上游渠道推送的 channel_id）
     */
    @Schema(description = "用户来源（上游渠道推送的 channel_id）")
    private String userSource;
    /**
     * 电话结果。见业务字典定义
     */
    @Schema(description = "电话结果。见业务字典定义")
    private Integer callTips;
    /**
     * 客户分组ID，见业务字典定义
     */
    @Schema(description = "客户分组ID，见业务字典定义")
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
    @JsonFormat(pattern = SuDateUtils.DF_YYYY_MM_DDHHMMSS, timezone = CommonConstant.DEFAULT_TIME_ZONE)
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
     * 身份证号
     */
    @Schema(description = "身份证号")
    private String idCardNo;
    /**
     * 申请时间（进本平台件的时间）
     */
    @Schema(description = "申请时间（进本平台件的时间）")
    @JsonFormat(pattern = SuDateUtils.DF_YYYY_MM_DDHHMMSS, timezone = CommonConstant.DEFAULT_TIME_ZONE)
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
     * 收藏标识。 1:收藏; 其它:未收藏
     */
    @Schema(description = "收藏标识。 1:收藏; 其它:未收藏")
    private Integer ownerFavorite;
    /**
     * 公海重点标识。1:显示☆; 2:显示☆☆; 0:普通客户
     */
    @Schema(description = "公海重点标识。1:显示☆; 2:显示☆☆; 0:普通客户")
    private Integer publicPoolStarFlag;
    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    @JsonFormat(pattern = SuDateUtils.DF_YYYY_MM_DDHHMMSS, timezone = CommonConstant.DEFAULT_TIME_ZONE)
    private Date gmtCreate;
    /**
     * 最后修改时间
     */
    @Schema(description = "最后修改时间")
    @JsonFormat(pattern = SuDateUtils.DF_YYYY_MM_DDHHMMSS, timezone = CommonConstant.DEFAULT_TIME_ZONE)
    private Date gmtModified;
    /**
     * 数据版本信息.
     */
    @Schema(description = "数据版本信息.")
    private Long version;
    /**
     * 性别描述
     */
    @Schema(description = "性别描述")
    private String sexDesc;
    /**
     * 房产标识描述
     */
    @Schema(description = "房产标识描述")
    private String houseFlagDesc;
    /**
     * 保单标识描述
     */
    @Schema(description = "保单标识描述")
    private String insuranceFlagDesc;
    /**
     * 社保标识描述
     */
    @Schema(description = "社保标识描述")
    private String socialInsuranceFlagDesc;
    /**
     * 车产标识描述
     */
    @Schema(description = "车产标识描述")
    private String carFlagDesc;
    /**
     * 公积金标识描述
     */
    @Schema(description = "公积金标识描述")
    private String providentFlagDesc;
    /**
     * 信用卡标识描述
     */
    @Schema(description = "信用卡标识描述")
    private String creditCardFlagDesc;
    /**
     * 企业主标识描述
     */
    @Schema(description = "企业主标识描述")
    private String enterpriseFlagDesc;
    /**
     * 婚姻状态. 0:未知; 1:已婚; 2:未婚; 3:离异
     */
    @Schema(description = "婚姻状态. 0:未知; 1:已婚; 2:未婚; 3:离异")
    private String marriageStatusDesc;
    /**
     * 跟进状态描述
     */
    @Schema(description = "跟进状态描述")
    private String progressDesc;
    /**
     * 电话结果描述
     */
    @Schema(description = "电话结果描述")
    private String callTipsDesc;
    /**
     * 客户分组描述
     */
    @Schema(description = "客户分组描述")
    private String customerGroupDesc;

    /**
     * 推广渠道ID
     */
    @Schema(description = "推广渠道描述")
    private String channelDesc;

    /**
     * 当前数据归属人用户ID
     */
    @Schema(description = "当前数据归属人姓名")
    private String ownerUserName;
    /**
     * 最后一次跟进人用户姓名
     */
    @Schema(description = "最后一次跟进人姓名")
    private String followerUserName;
    /**
     * 当前数据归属部门ID
     */
    @Schema(description = "当前数据归属部门名称")
    private String ownerDeptName;

    @Schema(description = "是否命中风险地区")
    private Boolean riskRegionHit;

    @Schema(description = "是否命中黑名单地区")
    private Boolean blackRegionHit;

    /**
     * 资质信息描述（如，是否有房等）
     */
    @Schema(description = "资质信息描述。如，是否有房等")
    private String qualification;
}
