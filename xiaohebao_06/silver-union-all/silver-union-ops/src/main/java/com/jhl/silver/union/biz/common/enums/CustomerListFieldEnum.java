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
    SEX_DESC("sexDesc", "性别", 4),
    CUSTOMER_GROUP_DESC("customerGroupDesc", "客户星级", 5),
    PROGRESS_DESC("progressDesc", "跟进状态", 6),
    CUSTOMER_REMARK("customerRemark", "客户备注", 7),
    FOLLOW_TIME("followTime", "最后跟进时间", 8),
    FOLLOW_CNT("followCnt", "跟进次数", 9),
    AGE("age", "年龄", 10),
    REQ_LOAN_AMOUNT("reqLoanAmount", "需要金额", 11),
    OWNER_USER_NAME("ownerUserName", "数据归属人", 12),
    APPLY_DATE("applyDate", "申请时间", 13),
    FOLLOWER_USER_NAME("followerUserName", "最后跟进人", 14),
    CALL_TIPS_DESC("callTipsDesc", "沟通结果", 15),
    FOLLOW_REMARK("followRemark", "跟进情况备注", 16),
    OWNER_DEPT_NAME("ownerDeptName", "数据归属部门", 17);

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
