package com.jhl.silver.union.biz.common.utils;

import com.jhl.silver.union.biz.data.FieldDiffItemInfo;
import com.jhl.silver.union.commons.exception.BizExceptionUtils;
import com.jhl.silver.union.commons.utils.OtherUtils;
import com.jhl.silver.union.commons.utils.SuDateUtils;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.models.media.Schema;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 差分字段工具类
 *
 * @author: qingren
 * @create_time: 2025/4/1
 */
public class FieldUtils {

    /**
     * 解析差异字段。 若需自动生成字段描述，则需要让 clazz
     * 对应的类标@{@link io.swagger.v3.oas.annotations.media.Schema}
     *
     * @param oldInstance
     * @param newInstance
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> List<FieldDiffItemInfo> getFieldDiffItemInfoListWith(T oldInstance, T newInstance,
            Class<T> clazz, Set<String> ignoreFieldNames) {
        @SuppressWarnings("rawtypes")
        Map<String, Schema> fieldSchemaMap = ModelConverters.getInstance().read(clazz);
        @SuppressWarnings("rawtypes")
        Optional<Schema> schemaInfoOpt = Optional.ofNullable(fieldSchemaMap.get(clazz.getSimpleName()));
        Field[] fields = clazz.getDeclaredFields();
        if (ArrayUtils.isEmpty(fields)) {
            return List.of();
        }
        List<FieldDiffItemInfo> fieldDiffItemInfoList = Arrays.stream(fields)
                .filter(field -> {
                    if (CollectionUtils.isEmpty(ignoreFieldNames)) {
                        return true;
                    }
                    return !ignoreFieldNames.contains(field.getName());
                })
                .map(e -> makeUpFrom(oldInstance, newInstance, schemaInfoOpt, e))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        return fieldDiffItemInfoList;
    }

    private static <T> FieldDiffItemInfo makeUpFrom(T oldInstance, T newInstance,
            @SuppressWarnings("rawtypes") Optional<Schema> schemaInfoOpt,
            Field field) {
        field.setAccessible(true);
        Object oldValue = null;
        Object newValue = null;
        try {
            oldValue = field.get(oldInstance);
            newValue = field.get(newInstance);
        } catch (IllegalAccessException e) {
            throw BizExceptionUtils.wrapThrowable(e);
        }
        // 如果新值为 null，则视为不更新该字段
        newValue = OtherUtils.defaultIfNull(newValue, oldValue);
        if (valueEquals(oldValue, newValue)) {
            return null;
        }
        String fieldDesc = schemaInfoOpt.map(e -> e.getProperties())
                .map(e -> (Schema<?>) e.get(field.getName()))
                .map(e -> e.getDescription())
                .orElse(StringUtils.EMPTY);
        String strOldValue = String.valueOf(oldValue);
        String strNewValue = String.valueOf(newValue);
        if (oldValue instanceof Date || newValue instanceof Date) {
            strOldValue = Objects.nonNull(oldValue)
                    ? SuDateUtils.format((Date) oldValue, SuDateUtils.DF_YYYY_MM_DDHHMMSS)
                    : strOldValue;
            strNewValue = Objects.nonNull(newValue)
                    ? SuDateUtils.format((Date) newValue, SuDateUtils.DF_YYYY_MM_DDHHMMSS)
                    : strNewValue;
        }
        if (StringUtils.isBlank(fieldDesc)) {
            return FieldDiffItemInfo.of(field.getName(), fieldDesc, strOldValue, strNewValue);
        }
        // 加工字段描述，去除标点符号,.，。以及空格以后的内容,只要分两组即可，省点资源
        String[] elements = fieldDesc.split("[,.:，。： ]", 2);
        return FieldDiffItemInfo.of(field.getName(), ArrayUtils.isEmpty(elements) ? fieldDesc : elements[0],
                strOldValue, strNewValue);
    }

    private static boolean valueEquals(Object oldValue, Object newValue) {
        if (Objects.isNull(oldValue) && Objects.isNull(newValue)) {
            return true;
        }
        if (Objects.isNull(oldValue) || Objects.isNull(newValue)) {
            return false;
        }

        if (oldValue instanceof String o && newValue instanceof String n) {
            return StringUtils.equals(o, n);
        }
        return oldValue.equals(newValue);
    }

}
