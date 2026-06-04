package com.jhl.silver.union.biz.customer.service;

import com.google.common.collect.Maps;
import com.jhl.silver.union.biz.common.enums.BizDictConfigTypeEnum;
import com.jhl.silver.union.biz.common.enums.UpdTypeEnum;
import com.jhl.silver.union.biz.data.FieldDiffItemInfo;
import com.jhl.silver.union.web.data.BizDictItem;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * 【客户信息修改历史】描述的处理策略
 *
 * @author: qingren
 * @create_time: 2025/4/1
 */
public abstract class CustomerUpdTraceStrategyContext {

    public static final String FIELD_PROGRESS = "progress";
    public static final String FIELD_FOLLOW_REMARK = "followRemark";
    public static final String FIELD_CUSTOMER_REMARK = "customerRemark";
    public static final String FIELD_CALL_TIPS = "callTips";
    public static final String FIELD_CALL_REMARK = "callRemark";
    public static final String FIELD_LEADER_REMARK = "leaderRemark";

    private final static Map<String/*fieldName*/, UpdTypeEnum> UPD_TYPE_ENUM_MAP = Maps.newHashMap();

    static {
        UPD_TYPE_ENUM_MAP.put(FIELD_PROGRESS, UpdTypeEnum.PROGRESS);
        UPD_TYPE_ENUM_MAP.put(FIELD_FOLLOW_REMARK, UpdTypeEnum.PROGRESS);
        UPD_TYPE_ENUM_MAP.put(FIELD_CUSTOMER_REMARK, UpdTypeEnum.PROGRESS);
        UPD_TYPE_ENUM_MAP.put(FIELD_CALL_TIPS, UpdTypeEnum.PROGRESS);
        UPD_TYPE_ENUM_MAP.put(FIELD_CALL_REMARK, UpdTypeEnum.PROGRESS);
        UPD_TYPE_ENUM_MAP.put(FIELD_LEADER_REMARK, UpdTypeEnum.LEAD_REMARK);
    }

    /**
     * 根据字段名取更新类型
     *
     * @param fieldName
     * @return
     */
    public static UpdTypeEnum getFieldUpdTypeEnum(String fieldName) {
        return UPD_TYPE_ENUM_MAP.getOrDefault(fieldName, UpdTypeEnum.OTHER);
    }

    /**
     * 判断是否有更新有关于【跟进信息】的字段
     *
     * @param items
     * @return
     */
    public static boolean containProgressUpdating(Collection<FieldDiffItemInfo> items, UpdTypeEnum updTypeEnum) {
        if (CollectionUtils.isEmpty(items)) {
            return false;
        }
        return items.stream()
                .anyMatch(e -> Objects.equals(updTypeEnum, UPD_TYPE_ENUM_MAP.get(e.getFieldName())));
    }

    /**
     * 描述字段更新信息的文案
     *
     * @param fieldDiffItemInfo
     * @return
     */
    public static String describeFiledChange(FieldDiffItemInfo fieldDiffItemInfo, BizConfigService bizConfigService) {
        switch (fieldDiffItemInfo.getFieldName()) {
            case FIELD_PROGRESS: {
                Map<Integer, BizDictItem> map =
                        bizConfigService.getBizDictItemMap(BizDictConfigTypeEnum.PROGRESS, true);
                if (map != null && NumberUtils.isParsable(fieldDiffItemInfo.getNewValue())) {
                    String fieldValueDesc =
                            Optional.ofNullable(map.get(Integer.parseInt(fieldDiffItemInfo.getNewValue())))
                                    .map(e -> e.getLabel())
                                    .orElse(null);
                    fieldDiffItemInfo.setNewValueDesc(fieldValueDesc);
                }
                return "更新 [" + StringUtils.defaultIfBlank(fieldDiffItemInfo.getFieldDescription(),
                        fieldDiffItemInfo.getFieldName()) + "] 为: " + StringUtils.defaultIfBlank(
                        fieldDiffItemInfo.getNewValueDesc(), fieldDiffItemInfo.getNewValue());
            }
            case FIELD_CALL_TIPS: {
                Map<Integer, BizDictItem> map =
                        bizConfigService.getBizDictItemMap(BizDictConfigTypeEnum.CALL_RESULT_TIPS, true);
                if (map != null && NumberUtils.isParsable(fieldDiffItemInfo.getNewValue())) {
                    String fieldValueDesc =
                            Optional.ofNullable(map.get(Integer.parseInt(fieldDiffItemInfo.getNewValue())))
                                    .map(BizDictItem::getLabel)
                                    .orElse(null);
                    fieldDiffItemInfo.setNewValueDesc(fieldValueDesc);
                }
                return "更新 [沟通结果] 为: " + StringUtils.defaultIfBlank(
                        fieldDiffItemInfo.getNewValueDesc(), fieldDiffItemInfo.getNewValue());
            }
            case FIELD_FOLLOW_REMARK:
            case FIELD_CUSTOMER_REMARK:
            case FIELD_CALL_REMARK:
                return "更新 [" + StringUtils.defaultIfBlank(fieldDiffItemInfo.getFieldDescription(),
                        fieldDiffItemInfo.getFieldName()) + "] 为: " + StringUtils.defaultIfBlank(
                        fieldDiffItemInfo.getNewValueDesc(), fieldDiffItemInfo.getNewValue());
            case FIELD_LEADER_REMARK:
                return "[" + StringUtils.defaultIfBlank(fieldDiffItemInfo.getFieldDescription(),
                        fieldDiffItemInfo.getFieldName()) + "]: " + StringUtils.defaultIfBlank(
                        fieldDiffItemInfo.getNewValueDesc(), fieldDiffItemInfo.getNewValue());
            default:
                return StringUtils.EMPTY;
        }

    }
}
