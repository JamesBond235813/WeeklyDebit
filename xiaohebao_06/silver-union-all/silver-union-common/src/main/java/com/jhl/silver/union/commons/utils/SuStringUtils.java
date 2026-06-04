package com.jhl.silver.union.commons.utils;

import org.apache.commons.lang3.StringUtils;

import java.text.DecimalFormat;
import java.util.Objects;

/**
 * 字符串相关工具类
 *
 * @author qingren
 */
public class SuStringUtils {
    /**
     * 保证字符串不超过指定的最大长度。 若超出则按最大长度截取字符串并返回
     * 
     * @param content
     * @param maxLength
     * @return
     */
    public static String ensureStringMaxLength(String content, int maxLength) {
        if (Objects.isNull(content) || maxLength < 1) {
            return content;
        }
        if (content.length() < maxLength) {
            return content;
        }
        content = content.substring(0, maxLength);
        return content;
    }

    /**
     * 为StringBuilder 追加非空字符串内容。 若content 为blank，则直接返回
     *
     * @param builder
     * @param prefix  若为 null， 则被忽略
     * @param content 若为 blank ，则prefix , content, suffix 均被忽略
     * @param suffix  若为 null， 则被忽略
     */
    public static void append(final StringBuilder builder, final String prefix, final String content,
            final String suffix) {
        if (Objects.isNull(builder)) {
            return;
        }
        if (StringUtils.isBlank(content)) {
            return;
        }
        OtherUtils.processIfNotNull(prefix, () -> builder.append(prefix));
        OtherUtils.processIfNotBlank(content, () -> builder.append(content));
        OtherUtils.processIfNotNull(suffix, () -> builder.append(suffix));
    }

    /**
     * 字符串脱敏
     *
     * @param s     待处理字符串
     * @param left  左边保留位数
     * @param right 右边保留位数
     * @return
     */
    public static String desensitization(String s, int left, int right) {
        if (StringUtils.isBlank(s)) {
            return "";
        }
        if (s.length() <= left + right) {
            return s;
        }
        StringBuilder result = new StringBuilder(StringUtils.left(s, left));
        for (int i = 0; i < s.length() - left - right; i++) {
            result.append("*");
        }
        result.append(StringUtils.right(s, right));
        return result.toString();
    }

    /**
     * 生成redis KEY
     *
     * @param objs
     * @return
     */
    public static String buildKeyString(Object... objs) {
        return buildString("_", objs);
    }

    /**
     * 拼接字符串
     *
     * @param objs
     * @return
     */
    public static String buildString(String splitter, Object... objs) {
        if (objs == null || objs.length == 0) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (Object obj : objs) {
            if (obj instanceof Class) {
                builder.append(((Class<?>) obj).getSimpleName());
            } else {
                builder.append(obj);
            }
            builder.append(splitter);
        }
        return builder.deleteCharAt(builder.length() - 1).toString();
    }

    /**
     * 将浮点数格式化成2位小数
     * <p>
     *
     * @param number
     * @return 返回number 格式化后的字符串：
     *         <p>
     *         1.若number 为整数 ，返回的字符串将不带小数点
     *         <p>
     *         2.number在保留两位小数的过程中，在计算最后一位小数时，会进行四舍五入操作，若四舍五入结果为xx.00,则按整数进行处理
     */
    public static String formatFloatNumberWith2FactionDigits(double number) {
        DecimalFormat decimalFormat = (DecimalFormat) DecimalFormat.getInstance();
        decimalFormat.setGroupingUsed(false);
        decimalFormat.setMaximumFractionDigits(2);
        return decimalFormat.format(number);
    }

    /**
     * 将字符串转成unicode
     *
     * @param str 待转字符串
     * @return unicode字符串
     */
    public static String strToUnicode(String str) {
        char[] chars = str.toCharArray();
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < chars.length; i++) {
            builder.append("\\u").append(Integer.toString(chars[i], 16));
        }
        return builder.toString();
    }

    /**
     * 将unicode字符串转成普通字符串
     *
     * @param unicode
     * @return
     */
    public static String unicodeToStr(String unicode) {
        // 以 \ u 分割，因为java注释也能识别unicode，因此中间加了一个空格
        String[] strs = unicode.split("\\\\u");
        StringBuilder builder = new StringBuilder();
        // 由于unicode字符串以 \ u 开头，因此分割出的第一个字符是""。
        for (int i = 1; i < strs.length; i++) {
            builder.append((char) Integer.valueOf(strs[i], 16).intValue());
        }
        return builder.toString();
    }

}
