package com.jhl.silver.union.biz.data;

import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;

/**
 * 字段变更差异区分信息
 *
 * @author: qingren
 * @create_time: 2025/4/1
 */
@Data
@Accessors(chain = true)
public class FieldDiffItemInfo {
    /**
     * 字段名称
     */
    private String fieldName;
    /**
     * 旧值
     */
    private String oldValue;

    /**
     * 旧值描述
     */
    private String oldValueDesc;

    /**
     * 新值
     */
    private String newValue;
    /**
     * 新值描述
     */
    private String newValueDesc;
    /**
     * 字段说明
     */
    private String fieldDescription;

    public static FieldDiffItemInfo of(String fieldName, String fieldDescription, String oldValue, String newValue) {
        return new FieldDiffItemInfo()
                .setFieldName(fieldName)
                .setFieldDescription(fieldDescription)
                .setOldValue(StringUtils.equals(oldValue, "null") ? null : oldValue)
                .setNewValue(StringUtils.equals(newValue, "null") ? null : newValue)
                ;
    }

}
