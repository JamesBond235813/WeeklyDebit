package com.jhl.silver.union.biz.common.enums;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 客户信息列表字段配置
 */
public enum CustomerListFieldEnum {
    NAME("name", "姓名", 1),
    MOBILE("mobile", "手机号", 2),
    ID_CARD_NO("idCardNo", "身份证号码", 3),
    USER_SOURCE("userSource", "用户来源", 4),
    CHANNEL_DESC("channelDesc", "上游渠道", 5),
    APPLY_DATE("applyDate", "申请时间", 6),
    RADAR_REPORT("radarReport", "雷达查询", 7),
    RISK_REPORT("riskReport", "风控报告", 8),
    CUSTOMER_REMARK("customerRemark", "客户备注", 9),
    HYY_OVERDUE_DESC("hyyOverdueDesc", "逾期情况", 10),
    REQ_LOAN_AMOUNT("reqLoanAmount", "需要金额", 11),
    PROGRESS_DESC("progressDesc", "跟进状态", 12),
    HYY_SOCIAL_INSURANCE_DESC("hyySocialInsuranceDesc", "社保", 13),
    ZHIMA_SCORE("zhimaScore", "芝麻分", 14),
    HYY_PROVIDENT_DESC("hyyProvidentDesc", "公积金", 15),
    HYY_HOUSE_DESC("hyyHouseDesc", "房", 16),
    HYY_CAR_DESC("hyyCarDesc", "车", 17),
    HYY_OCCUPATION_DESC("hyyOccupationDesc", "职业", 18),
    SEX_DESC("sexDesc", "性别", 19),
    FOLLOW_TIME("followTime", "最后跟进时间", 20),
    FOLLOW_CNT("followCnt", "跟进次数", 21),
    AGE("age", "年龄", 22),
    HYY_INSURANCE_DESC("hyyInsuranceDesc", "投保", 23),
    HYY_IP("hyyIp", "IP地址", 24),
    OWNER_USER_NAME("ownerUserName", "数据归属人", 25),
    FOLLOWER_USER_NAME("followerUserName", "最后跟进人", 26),
    CALL_TIPS_DESC("callTipsDesc", "沟通结果", 27);

    private final String fieldKey;
    private final String label;
    private final int sortNo;

    CustomerListFieldEnum(String fieldKey, String label, int sortNo) {
        this.fieldKey = fieldKey;
        this.label = label;
        this.sortNo = sortNo;
    }

    public String getFieldKey() {
        return fieldKey;
    }

    public String getLabel() {
        return label;
    }

    public int getSortNo() {
        return sortNo;
    }

    public static List<String> fieldKeys() {
        return Arrays.stream(values())
                .map(CustomerListFieldEnum::getFieldKey)
                .collect(Collectors.toList());
    }
}
