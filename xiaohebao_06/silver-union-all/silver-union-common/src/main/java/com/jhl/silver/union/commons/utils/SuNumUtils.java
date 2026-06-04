package com.jhl.silver.union.commons.utils;

import com.jhl.silver.union.commons.CommonConstant;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * 数字相关工具类
 * 
 * @author: liwei
 * @date 2022-10-27 6:02:49 PM
 */
public class SuNumUtils {

    /**
     * 分转元， 保留2位小数
     *
     * @param fenStr
     * @return
     */
    public static BigDecimal fen2Yuan(String fenStr) {
        if (!StringUtils.isNumeric(fenStr)) {
            return null;
        }
        BigDecimal yuan = new BigDecimal(fenStr)
                .divide(BigDecimal.valueOf(CommonConstant.HUNDRED_CENT), 2, RoundingMode.HALF_EVEN);
        return yuan;
    }

    /**
     * 分转元字符串， 保留2位小数
     *
     * @param fenStr
     * @return
     */
    public static String fen2YuanStr(String fenStr) {
        BigDecimal yuan = fen2Yuan(fenStr);
        if (Objects.isNull(yuan)) {
            return StringUtils.EMPTY;
        }
        return yuan.toString();
    }

    /**
     * 分转元字符串， 保留2位小数
     * 
     * @param fen
     * @return
     */
    public static String fen2YuanStr(Long fen) {
        return fen2YuanStr(fen + StringUtils.EMPTY);
    }

}
