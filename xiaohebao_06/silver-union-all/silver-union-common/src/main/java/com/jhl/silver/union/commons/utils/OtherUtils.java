package com.jhl.silver.union.commons.utils;

import com.jhl.silver.union.commons.func.SimpleBlocker;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * 未分组的工具方法
 *
 * @author: qingren
 * @create_time: 2021/8/13
 */
public class OtherUtils {

    /**
     * 指定默认值
     *
     * @param value
     * @param defaultValue
     * @param <T>
     * @return
     */
    public static <T> T defaultIfNull(T value, T defaultValue) {
        if (Objects.isNull(value)) {
            return defaultValue;
        }
        return value;
    }

    /**
     * 当obj 非时， 执行processor
     *
     * @param obj
     * @param processor
     */
    public static void processIfNotNull(Object obj, SimpleBlocker processor) {
        processIf(Objects.nonNull(obj), processor);
    }

    /**
     * Optional.ofNullable(obj).ifPresent(e -> consumer.accept(e)) 的缩写
     *
     * @param obj
     * @param consumer
     * @param <T>
     */
    public static <T> void processIfNotNull(T obj, Consumer<T> consumer) {
        Optional.ofNullable(obj).ifPresent(e -> consumer.accept(e));
    }

    /**
     * 当content 非空白值时， 执行processor
     *
     * @param content
     * @param processor
     */
    public static void processIfNotBlank(String content, SimpleBlocker processor) {
        processIf(StringUtils.isNotBlank(content), processor);
    }

    /**
     * 当content 空白值时， 执行processor
     *
     * @param content
     * @param processor
     */
    public static void processIfBlank(String content, SimpleBlocker processor) {
        processIf(StringUtils.isBlank(content), processor);
    }

    /**
     * 当content 非空白值时， 执行consumer
     *
     * @param content
     * @param consumer
     */
    public static void processIfNotBlank(String content, Consumer<String> consumer) {
        if (StringUtils.isBlank(content)) {
            return;
        }
        consumer.accept(content);
    }

    /**
     * 若condition 为 true, 则执行 processor<br>
     * 扁平化 if 语句。
     *
     * @param condition
     * @param processor
     */
    public static void processIf(Boolean condition, SimpleBlocker processor) {
        condition = defaultIfNull(condition, Boolean.FALSE);
        if (!condition) {
            return;
        }
        if (Objects.isNull(processor)) {
            return;
        }
        processor.process();
    }
}
