package com.jhl.silver.union.commons.utils;

import com.jhl.silver.union.commons.CommonResultCode;
import com.jhl.silver.union.commons.IResultCode;
import com.jhl.silver.union.commons.exception.BizException;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 参数格式校验工具类
 *
 * @author qingren
 *         Created by qingren
 */
public class VerifyUtils {

    /**
     * 正则表达式 数字+英文字母
     */
    public static final String REGX_ALPHABETA_NUM = "^[a-zA-Z0-9]$";

    /**
     * 中国大陆手机号
     */
    public static final String REGX_CN_MOBILE = "^((86)|(0086)|([+]86))?1\\d{10}";

    /**
     * 邮编
     */
    public static final String REGEX_POSTCODE = "^[0-9]{6}$";

    /**
     * 用于校验 0, 1
     */
    public static final String PEGEX_YES_OR_NO = "^[0-1]{1}$";

    /**
     * 正整数校验 正则表达式
     */
    public static final String REGEX_POSITIVE_INT_NUM = "^[0-9]+$";

    /**
     * 整数校验 正则表达式
     */
    public static final String REGEX_INT_NUM = "^-?[0-9]+$";
    public static final String REGEX_DIGTAL = "^-?[0-9]+(\\.\\d+)?";
    public static final String REGEX_IPV4 = "^(?:(?:2[0-4][0-9]\\.)|(?:25[0-5]\\.)|(?:1[0-9][0-9]\\.)|(?:[1-9][0-9]\\.)|(?:[0-9]\\.)){3}(?:(?:2[0-5][0-5])|(?:25[0-5])|(?:1[0-9][0-9])|(?:[1-9][0-9])|(?:[0-9]))$";
    public static final String REGEX_IPV6 = "^((([0-9A-Fa-f]{1,4}:){7}([0-9A-Fa-f]{1,4}|:))|(([0-9A-Fa-f]{1,4}:){6}(:[0-9A-Fa-f]{1,4}|((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3})|:))|(([0-9A-Fa-f]{1,4}:){5}(((:[0-9A-Fa-f]{1,4}){1,2})|:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3})|:))|(([0-9A-Fa-f]{1,4}:){4}(((:[0-9A-Fa-f]{1,4}){1,3})|((:[0-9A-Fa-f]{1,4})?:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){3}(((:[0-9A-Fa-f]{1,4}){1,4})|((:[0-9A-Fa-f]{1,4}){0,2}:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){2}(((:[0-9A-Fa-f]{1,4}){1,5})|((:[0-9A-Fa-f]{1,4}){0,3}:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){1}(((:[0-9A-Fa-f]{1,4}){1,6})|((:[0-9A-Fa-f]{1,4}){0,4}:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}))|:))|(:(((:[0-9A-Fa-f]{1,4}){1,7})|((:[0-9A-Fa-f]{1,4}){0,5}:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}))|:)))(%.+)?$";
    public static final String REGEX_SIMPLE_MOBILE_NO = "^1[0-9]{10}";

    public static boolean verifyPhoneNo(String phoneNo, boolean throwException, String fieldName) throws BizException {

        return verifyPhoneNo(phoneNo, CommonResultCode.INVALID_PARAMS, throwException, fieldName);
    }

    public static boolean verifyPhoneNo(String phoneNo, IResultCode resultCode, boolean throwException,
            String fieldName) throws BizException {

        boolean result = notNull(phoneNo, fieldName, resultCode, throwException);
        if (!result) {
            // 走到这里说明 throwException 为false
            return false;
        }
        return judge(phoneNo.matches(REGEX_SIMPLE_MOBILE_NO), resultCode, throwException,
                fieldName + "= " + phoneNo + " is invalid phone number.");
    }

    public static boolean notBlank(String value, boolean throwException) throws BizException {
        return notBlank(value, "", throwException);
    }

    /**
     * 非空白判断
     *
     * @param value
     * @param paramName
     * @param throwException
     * @return
     * @throws BizException
     */
    public static boolean notBlank(String value, String paramName, boolean throwException) throws BizException {
        return notBlank(value, paramName, CommonResultCode.INVALID_PARAMS, throwException);
    }

    /**
     * 非空白判断
     *
     * @param value
     * @param paramName
     * @param resultCode
     * @param throwException
     * @return
     * @throws BizException
     */
    public static boolean notBlank(String value, String paramName, IResultCode resultCode, boolean throwException)
            throws BizException {
        boolean result = StringUtils.isNotBlank(value);
        return judge(result, resultCode, throwException, paramName + " is blank.");
    }

    public static boolean notBlank(String value, String paramName, String errMsg, boolean throwException)
            throws BizException {
        return judge(StringUtils.isNotBlank(value), errMsg, throwException, paramName + " is blank.");
    }

    public static boolean greaterThan(int value, int compareValue, String paramName, boolean throwException)
            throws BizException {
        return greaterThan(value, compareValue, paramName, CommonResultCode.INVALID_PARAMS, throwException);
    }

    public static boolean greaterThan(int value, int compareValue, String paramName, IResultCode resultCode,
            boolean throwException) throws BizException {
        boolean result = value > compareValue;
        return judge(result, resultCode, throwException, paramName + "=" + value);
    }

    public static boolean greaterThan(int value, int compareValue, String paramName, String errMsg,
            boolean throwException) throws BizException {
        boolean result = value > compareValue;
        return judge(result, errMsg, throwException, paramName + "=" + value);
    }

    public static boolean isPositiveNumber(String value, String paramName, boolean allowBlank, boolean throwException)
            throws BizException {
        return verifyByRegex(value, REGEX_POSITIVE_INT_NUM, paramName, allowBlank, throwException);
    }

    public static boolean isPositiveNumber(String value, String paramName, IResultCode resultCode, boolean allowBlank,
            boolean throwException)
            throws BizException {
        return verifyByRegex(value, REGEX_POSITIVE_INT_NUM, paramName, resultCode, allowBlank, throwException);
    }

    public static boolean isPositiveNumber(String value, String paramName, String errMsg, boolean allowBlank,
            boolean throwException)
            throws BizException {
        return verifyByRegex(value, REGEX_POSITIVE_INT_NUM, paramName, errMsg, allowBlank, throwException);
    }

    public static boolean isNumber(String value, String paramName, boolean allowBlank, boolean throwException)
            throws BizException {
        return verifyByRegex(value, REGEX_INT_NUM, paramName, allowBlank, throwException);

    }

    public static boolean isNumber(String value, String paramName, IResultCode resultCode, boolean allowBlank,
            boolean throwException) throws BizException {
        return verifyByRegex(value, REGEX_INT_NUM, paramName, resultCode, allowBlank, throwException);
    }

    public static boolean isNumber(String value, String paramName, String errMsg, boolean allowBlank,
            boolean throwException) throws BizException {
        return verifyByRegex(value, REGEX_INT_NUM, paramName, errMsg, allowBlank, throwException);
    }

    public static boolean isDigital(String value, String paramName, boolean allowBlank, boolean throwException)
            throws BizException {
        return verifyByRegex(value, REGEX_DIGTAL, paramName, allowBlank, throwException);
    }

    public static boolean isDigital(String value, String paramName, IResultCode resultCode, boolean allowBlank,
            boolean throwException) throws BizException {
        return verifyByRegex(value, REGEX_DIGTAL, paramName, resultCode, allowBlank, throwException);
    }

    public static boolean isDigital(String value, String paramName, String errMsg, boolean allowBlank,
            boolean throwException) throws BizException {
        return verifyByRegex(value, REGEX_DIGTAL, paramName, errMsg, allowBlank, throwException);
    }

    public static boolean greaterThan(long value, long compareValue, String paramName, boolean throwException)
            throws BizException {
        return greaterThan(value, compareValue, paramName, CommonResultCode.INVALID_PARAMS, throwException);
    }

    public static boolean greaterThan(long value, long compareValue, String paramName, IResultCode resultCode,
            boolean throwException) throws BizException {
        boolean result = value > compareValue;
        return judge(result, resultCode, throwException, paramName + "=" + value);
    }

    public static boolean greaterThan(long value, long compareValue, String paramName, String errMsg,
            boolean throwException) throws BizException {
        boolean result = value > compareValue;
        return judge(result, errMsg, throwException, paramName + "=" + value);
    }

    public static boolean greaterThan(double value, double compareValue, String paramName, boolean throwException)
            throws BizException {
        return greaterThan(value, compareValue, paramName, CommonResultCode.INVALID_PARAMS, throwException);
    }

    public static boolean greaterThan(double value, double compareValue, String paramName, IResultCode resultCode,
            boolean throwException) throws BizException {
        boolean result = value > compareValue;
        return judge(result, resultCode, throwException, paramName + "=" + value);
    }

    public static boolean greaterThan(double value, double compareValue, String paramName, String errMsg,
            boolean throwException) throws BizException {
        boolean result = value > compareValue;
        return judge(result, errMsg, throwException, paramName + "=" + value);
    }

    public static boolean equalsOrGreaterThan(double value, double compareValue, String paramName,
            boolean throwException)
            throws BizException {
        return equalsOrGreaterThan(value, compareValue, paramName, CommonResultCode.INVALID_PARAMS, throwException);
    }

    public static boolean equalsOrGreaterThan(double value, double compareValue, String paramName,
            IResultCode resultCode, boolean throwException) throws BizException {
        boolean result = value >= compareValue;
        return judge(result, resultCode, throwException, paramName + "=" + value);
    }

    public static boolean equalOrLessThan(int value, int compareValue, String paramName, boolean throwException)
            throws BizException {
        return equalOrLessThan(value, compareValue, paramName, CommonResultCode.INVALID_PARAMS, throwException);
    }

    public static boolean equalOrLessThan(int value, int compareValue, String paramName, IResultCode resultCode,
            boolean throwException)
            throws BizException {
        boolean result = value <= compareValue;
        return judge(result, resultCode, throwException, paramName + "=" + value);
    }

    public static boolean equalOrLessThan(long value, long compareValue, String paramName, boolean throwException)
            throws BizException {
        return equalOrLessThan(value, compareValue, paramName, CommonResultCode.INVALID_PARAMS, throwException);
    }

    public static boolean equalOrLessThan(long value, long compareValue, String paramName, IResultCode resultCode,
            boolean throwException)
            throws BizException {
        boolean result = value <= compareValue;
        return judge(result, resultCode, throwException, paramName + "=" + value);
    }

    public static boolean maxStringLength(String value, int maxLength, String paramName, boolean allowBlank,
            boolean throwException) throws BizException {
        return maxStringLength(value, maxLength, paramName, CommonResultCode.INVALID_PARAMS, allowBlank,
                throwException);
    }

    public static boolean maxStringLength(String value, int maxLength, String paramName, IResultCode resultCode,
            boolean allowBlank, boolean throwException) throws BizException {
        // 非空校验
        boolean isNotBlank = notBlank(value, false);
        if (!isNotBlank) {
            if (allowBlank) {
                return true;
            }
            return judge(false, resultCode, throwException, paramName + " is blank");
        }

        // 字符串长度校验
        return judge(value.length() <= maxLength, resultCode, throwException,
                paramName + "=" + value + " is beyond of maxLength " + maxLength);
    }

    public static boolean maxStringLength(String value, int maxLength, String paramName, String errMsg,
            boolean allowBlank, boolean throwException) throws BizException {
        // 非空校验
        boolean isNotBlank = notBlank(value, paramName, errMsg, false);
        if (!isNotBlank) {
            if (allowBlank) {
                return true;
            }
            return judge(false, errMsg, throwException, paramName + " is blank");
        }

        // 字符串长度校验
        return judge(value.length() <= maxLength, errMsg, throwException,
                paramName + "=" + value + " is beyond of maxLength " + maxLength);
    }

    /**
     * 检查数据最小字符串长度
     *
     * @param value
     * @param minLength
     * @param paramName
     * @param allowBlank
     * @param throwException
     * @return
     * @throws BizException
     */
    public static boolean minStringLength(String value, int minLength, String paramName, boolean allowBlank,
            boolean throwException) throws BizException {
        return minStringLength(value, minLength, paramName, CommonResultCode.INVALID_PARAMS, allowBlank,
                throwException);
    }

    /**
     * 检查数据最小字符串长度
     *
     * @param value
     * @param minLength
     * @param paramName
     * @param allowBlank
     * @param throwException
     * @return
     * @throws BizException
     */
    public static boolean minStringLength(String value, int minLength, String paramName, IResultCode resultCode,
            boolean allowBlank,
            boolean throwException) throws BizException {
        // 非空校验
        if (!notBlank(value, false)) {
            if (allowBlank) {
                return true;
            }
            return judge(false, resultCode, throwException, paramName + " is blank.");
        }

        // 字符串长度校验
        return judge(value.length() >= minLength, resultCode, throwException,
                paramName + "=" + value + " is less than min length " + minLength);
    }

    public static boolean minStringLength(String value, int minLength, String paramName, String errMsg,
            boolean allowBlank,
            boolean throwException) throws BizException {
        // 非空校验
        if (!notBlank(value, false)) {
            if (allowBlank) {
                return true;
            }
            return judge(false, errMsg, throwException, paramName + " is blank.");
        }

        // 字符串长度校验
        return judge(value.length() >= minLength, errMsg, throwException,
                paramName + "=" + value + " is less than min length " + minLength);
    }

    public static boolean contains(int value, int[] container, String paramName, boolean throwException)
            throws BizException {
        return contains(value, container, paramName, CommonResultCode.INVALID_PARAMS, throwException);
    }

    public static boolean contains(int value, int[] container, String paramName, IResultCode resultCode,
            boolean throwException)
            throws BizException {
        if (container == null) {
            throw new BizException(CommonResultCode.INVALID_PARAMS, "container");
        }

        boolean result = ArrayUtils.contains(container, value);
        return judge(result, resultCode, throwException, paramName + "=" + value);
    }

    public static boolean contains(int value, int[] container, String paramName, String errMsg,
            boolean throwException)
            throws BizException {
        if (container == null) {
            throw new BizException(CommonResultCode.INVALID_PARAMS, "container");
        }

        boolean result = ArrayUtils.contains(container, value);
        return judge(result, errMsg, throwException, paramName + "=" + value);
    }

    public static boolean judge(boolean valid, IResultCode resultCode, boolean throwException, String paramsMsg) {
        if (valid) {
            return true;
        }
        if (throwException) {
            throw new BizException(Objects.isNull(resultCode) ? CommonResultCode.INVALID_PARAMS : resultCode,
                    paramsMsg);
        }
        return false;
    }

    public static boolean judge(boolean valid, int errCode, String errMsg, boolean throwException, String paramsMsg) {
        if (valid) {
            return true;
        }
        if (throwException) {
            throw new BizException(errCode, errMsg, paramsMsg);
        }
        return false;
    }

    public static boolean judge(boolean valid, String errMsg, boolean throwException, String paramsMsg) {
        if (valid) {
            return true;
        }
        if (throwException) {
            throw new BizException(CommonResultCode.INVALID_PARAMS.code,
                    StringUtils.defaultIfBlank(errMsg, CommonResultCode.INVALID_PARAMS.msg), paramsMsg);
        }
        return false;
    }

    /**
     * 非空判断
     *
     * @param obj
     * @param throwException
     * @return
     * @throws BizException
     */
    public static boolean notNull(Object obj, String paramName, boolean throwException) throws BizException {
        return notNull(obj, paramName, CommonResultCode.INVALID_PARAMS, throwException);
    }

    /**
     * 非空判断
     *
     * @param obj
     * @param throwException
     * @return
     * @throws BizException
     */
    public static boolean notNull(Object obj, String paramName, IResultCode resultCode, boolean throwException)
            throws BizException {
        return judge(Objects.nonNull(obj), resultCode, throwException, paramName + " is null");
    }

    public static boolean notNull(Object obj, String paramName, String msg, boolean throwException)
            throws BizException {
        return judge(Objects.nonNull(obj), msg, throwException, paramName + " is null");
    }

    /**
     * 集合非空判断
     *
     * @param collection
     * @param paramName
     * @param throwException
     * @return
     * @throws BizException
     */
    public static boolean notEmpty(Collection<?> collection, String paramName, boolean throwException)
            throws BizException {
        return notEmpty(collection, paramName, CommonResultCode.INVALID_PARAMS, throwException);
    }

    /**
     * 集合非空判断
     *
     * @param collection
     * @param paramName
     * @param throwException
     * @return
     * @throws BizException
     */
    public static boolean notEmpty(Collection<?> collection, String paramName, IResultCode resultCode,
            boolean throwException) throws BizException {
        boolean result = !(collection == null || collection.isEmpty());
        return judge(result, resultCode, throwException, paramName + " is empty");
    }

    /**
     * 集合非空判断
     *
     * @param collection
     * @param paramName
     * @param throwException
     * @return
     * @throws BizException
     */
    public static boolean notEmpty(Collection<?> collection, String paramName, String errMsg,
            boolean throwException) throws BizException {
        boolean result = !(collection == null || collection.isEmpty());
        return judge(result, errMsg, throwException, paramName + " is empty");
    }

    /**
     * Map非空判断
     *
     * @param collection
     * @param paramName
     * @param throwException
     * @return
     * @throws BizException
     */
    public static boolean notEmpty(Map<?, ?> collection, String paramName, boolean throwException)
            throws BizException {
        return notEmpty(collection, paramName, CommonResultCode.INVALID_PARAMS, throwException);
    }

    /**
     * Map非空判断
     *
     * @param collection
     * @param paramName
     * @param throwException
     * @return
     * @throws BizException
     */
    public static boolean notEmpty(Map<?, ?> collection, String paramName, IResultCode resultCode,
            boolean throwException)
            throws BizException {
        boolean result = !(collection == null || collection.isEmpty());
        return judge(result, resultCode, throwException, paramName + " is empty");
    }

    public static boolean notEmpty(Map<?, ?> collection, String paramName, String errMsg, boolean throwException)
            throws BizException {
        boolean result = !(collection == null || collection.isEmpty());
        return judge(result, errMsg, throwException, paramName + " is empty");
    }

    /**
     * 根据正则表达式判断参数合法性
     *
     * @param value
     * @param regex
     * @param fieldName
     * @param allowBlank
     * @param throwException
     * @return
     * @throws BizException
     */
    public static boolean verifyByRegex(String value, String regex, String fieldName, boolean allowBlank,
            boolean throwException) throws BizException {
        return verifyByRegex(value, regex, fieldName, CommonResultCode.INVALID_PARAMS, allowBlank, throwException);
    }

    /**
     * 根据正则表达式判断参数合法性
     *
     * @param value
     * @param regex
     * @param fieldName
     * @param allowBlank
     * @param throwException
     * @return
     * @throws BizException
     */
    public static boolean verifyByRegex(String value, String regex, String fieldName, IResultCode resultCode,
            boolean allowBlank,
            boolean throwException)
            throws BizException {
        Pattern pattern = Pattern.compile(regex);
        return verifyByPattern(value, pattern, fieldName, resultCode, allowBlank, throwException);
    }

    public static boolean verifyByRegex(String value, String regex, String fieldName, String errMsg,
            boolean allowBlank,
            boolean throwException)
            throws BizException {

        Pattern pattern = Pattern.compile(regex);
        return verifyByPattern(value, pattern, fieldName, errMsg, allowBlank, throwException);
    }

    @SuppressWarnings("DuplicatedCode")
    public static boolean verifyByPattern(String value, Pattern pattern, String fieldName, String errMsg,
            boolean allowBlank,
            boolean throwException)
            throws BizException {
        // 非空校验
        if (!notBlank(value, false)) {
            if (allowBlank) {
                return true;
            }
            return judge(false, errMsg, throwException, fieldName + " is blank!");
        }
        // 正则校验
        Matcher matcher = pattern.matcher(value);
        return judge(matcher.matches(), errMsg, throwException,
                fieldName + "=" + value + " is not match with pattern:" + pattern.pattern());
    }

    @SuppressWarnings("DuplicatedCode")
    public static boolean verifyByPattern(String value, Pattern pattern, String fieldName, IResultCode resultCode,
            boolean allowBlank,
            boolean throwException)
            throws BizException {
        // 非空校验
        if (!notBlank(value, false)) {
            if (allowBlank) {
                return true;
            }
            return judge(false, resultCode, throwException, fieldName + " is blank!");
        }

        // 正则校验
        Matcher matcher = pattern.matcher(value);
        return judge(matcher.matches(), resultCode, throwException,
                fieldName + "=" + value + " is not match with pattern:" + pattern.pattern());
    }

    /**
     * 日期字符串校验
     *
     * @param value
     * @param format
     * @param fieldName
     * @param throwException
     * @return
     * @throws BizException
     */
    public static boolean verifyDate(String value, String format, String fieldName, boolean throwException)
            throws BizException {
        return verifyDate(value, format, fieldName, CommonResultCode.INVALID_PARAMS, throwException);
    }

    /**
     * 日期字符串校验
     *
     * @param value
     * @param format
     * @param fieldName
     * @param throwException
     * @return
     * @throws BizException
     */
    public static boolean verifyDate(String value, String format, String fieldName, IResultCode resultCode,
            boolean throwException) throws BizException {
        boolean result = notNull(value, fieldName, resultCode, throwException);
        if (!result) {
            return result;
        }
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        Date date = null;
        try {
            date = sdf.parse(value);
        } catch (ParseException e) {
            return judge(false, resultCode, throwException, fieldName + "=" + value + " can not be parsed to date.");
        }
        String actValue = sdf.format(date);
        return judge(StringUtils.equals(value, actValue), resultCode, throwException, fieldName + "=" + value);
    }

    public static boolean verifyDate(String value, String format, String fieldName, String errMsg,
            boolean throwException) throws BizException {
        boolean result = notNull(value, fieldName, errMsg, throwException);
        if (!result) {
            return result;
        }
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        Date date = null;
        try {
            date = sdf.parse(value);
        } catch (ParseException e) {
            return judge(false, errMsg, throwException, fieldName + "=" + value + " can not be parsed to date.");
        }
        String actValue = sdf.format(date);
        return judge(StringUtils.equals(value, actValue), errMsg, throwException, fieldName + "=" + value);
    }

    /**
     * 校验IP地址格式，支持IPV4 ，IPV6
     *
     * @param value
     * @param fieldName
     * @param throwException
     * @return
     * @throws BizException
     */
    public static boolean verifyIp(String value, String fieldName, boolean allowBlank, boolean throwException)
            throws BizException {
        return verifyIp(value, fieldName, CommonResultCode.INVALID_PARAMS, allowBlank, throwException);
    }

    /**
     * 校验IP地址格式，支持IPV4 ，IPV6
     *
     * @param value
     * @param fieldName
     * @param throwException
     * @return
     * @throws BizException
     */
    public static boolean verifyIp(String value, String fieldName, IResultCode resultCode, boolean allowBlank,
            boolean throwException) throws BizException {

        String regx = StringUtils.contains(value, ":") ? REGEX_IPV6 : REGEX_IPV4;
        return verifyByRegex(value, regx, fieldName, resultCode, allowBlank, true);
    }

    /**
     * 校验 express
     *
     * @param express
     * @param errMsg
     * @param throwException
     * @return
     */
    public static boolean verifyTrue(Boolean express, String errMsg, boolean throwException) {
        return judge(Boolean.TRUE.equals(express), errMsg, throwException, null);
    }
}
