package com.jhl.silver.union.commons.utils;

import org.apache.commons.lang3.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * 日期相关工具类
 *
 * @author qingren
 */
public class SuDateUtils {
    public static final String DF_YYYY = "yyyy";
    public static final String DF_YYYYMM = "yyyyMM";
    public static final String DF_YYYYMMDD = "yyyyMMdd";
    public static final String DF_YYYY_MM_DD = "yyyy-MM-dd";
    public static final String DF_YYYYMMDDHHMMSS = "yyyyMMddHHmmss";
    public static final String DF_YYMMDDHHMMSS = "yyMMddHHmmss";
    public static final String DF_YYYY_MM_DDHHMMSS = "yyyy-MM-dd HH:mm:ss";
    public static final String DF_HH_MM_SS = "HH:mm:ss";
    /**
     * 用于计算定时任务下次运行时间的专用格式
     */
    public static final String DF_YYYYMMDD_TIME_8 = "yyyyMMddHH:mm:ss";

    /**
     * 获取yyyy-MM-dd的0点
     * @param dateStr
     * @return
     * @throws ParseException
     */
    public static Date getDate0(String dateStr) throws ParseException {
        return parse(dateStr, DF_YYYY_MM_DD);
    }

    /**
     * 获取yyyy-MM-dd的23:59:59
     * @param dateStr
     * @return
     * @throws ParseException
     */
    public static Date getDate24(String dateStr) throws ParseException {
        dateStr = dateStr + " 23:59:59";
        return parse(dateStr, DF_YYYY_MM_DDHHMMSS);
    }
    /**
     * 将日期转换成字符串
     *
     * @param date
     * @param format
     * @return
     */
    public static String format(Date date, String format) {
        if (date == null) {
            throw new IllegalArgumentException("Param date is null!");
        }
        if (StringUtils.isBlank(format)) {
            throw new IllegalArgumentException("Param format is blank!");
        }
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(date);
    }

    /**
     * 将日期字符串转换成日期
     *
     * @param dateStr
     * @param format
     * @return
     * @throws ParseException
     */
    public static Date parse(String dateStr, String format) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.parse(dateStr);
    }

    /**
     * 将日期转换成yyyyMMddHHmmss字符串
     *
     * @param date
     * @return
     */
    public static String format14(Date date) {
        return format(date, DF_YYYYMMDDHHMMSS);
    }

    /**
     * 将日期转换成yyyy-MM-dd字符串
     *
     * @param date
     * @return
     */
    public static String format10(Date date) {
        return format(date, DF_YYYY_MM_DD);
    }

    /**
     * 将日期转换成yyyyMMdd字符串
     *
     * @param date
     * @return
     */
    public static String format8(Date date) {
        return format(date, DF_YYYYMMDD);
    }

    /**
     * 将yyyyMMddHHmmss字符串转换成日期
     *
     * @param dateStr
     * @return
     * @throws ParseException
     */
    public static Date parse14(String dateStr) throws ParseException {
        return parse(dateStr, DF_YYYYMMDDHHMMSS);
    }

    /**
     * 将yyyy-MM-dd HH:mm:ss字符串转换成日期
     *
     * @param dateStr
     * @return
     * @throws ParseException
     */
    public static Date parse15(String dateStr) throws ParseException {
        return parse(dateStr, DF_YYYY_MM_DDHHMMSS);
    }

    /**
     * 将yyyy-MM-dd字符串转换成日期
     *
     * @param dateStr
     * @return
     * @throws ParseException
     */
    public static Date parse10(String dateStr) throws ParseException {
        return parse(dateStr, DF_YYYY_MM_DD);
    }

    /**
     * 将yyyyMMdd字符串转换成日期
     *
     * @param dateStr
     * @return
     * @throws ParseException
     */
    public static Date parse8(String dateStr) throws ParseException {
        return parse(dateStr, DF_YYYYMMDD);
    }

    /**
     * 返回当前日期 yyyyMMddHHmmss格式 字符串
     *
     * @return
     */
    public static String getNow14() {
        return format14(new Date());
    }

    /**
     * 返回当前日期 yyyyMMdd格式 字符串
     *
     * @return
     */
    public static String getNow10() {
        return format10(new Date());
    }

    /**
     * 返回当前日期 yyyyMMdd格式 字符串
     *
     * @return
     */
    public static String getNow8() {
        return format8(new Date());
    }

    /**
     * 获取当天0点 date对象
     *
     * @return
     */
    public static Date getDay(Date date) {
        if (date == null) {
            date = new Date();
        }
        try {
            return parse(format(date, DF_YYYYMMDD), DF_YYYYMMDD);
        } catch (ParseException e) {
            //unreachable
            throw new RuntimeException("Parsing / Getting day failed.", e);
        }

    }

    // 获得本周一0点时间
    public static Date getTimesWeekmorning() {
        Calendar cal = Calendar.getInstance();
        cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONDAY), cal.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        return cal.getTime();
    }

    // 获得本周日24点时间
    public static Date getTimesWeeknight() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(getTimesWeekmorning());
        cal.add(Calendar.DAY_OF_WEEK, 7);
        return cal.getTime();
    }

    // 获得本月第一天0点时间
    public static Date getTimesMonthmorning() {
        Calendar cal = Calendar.getInstance();
        cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONDAY), cal.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMinimum(Calendar.DAY_OF_MONTH));
        return cal.getTime();
    }

    // 获得本月最后一天24点时间
    public static Date getTimesMonthnight() {
        Calendar cal = Calendar.getInstance();
        cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONDAY), cal.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
        cal.set(Calendar.HOUR_OF_DAY, 24);
        return cal.getTime();
    }

    /**
     * 获取当月1日0点 date对象
     *
     * @return
     */
    public static Date getMonth(Date date) {
        if (date == null) {
            date = new Date();
        }
        try {
            return parse(format(date, DF_YYYYMM), DF_YYYYMM);
        } catch (ParseException e) {
            //unreachable
            throw new RuntimeException("Parsing / Getting month failed.", e);
        }

    }

    /**
     * 获取当年1月1日0点 date对象
     *
     * @return
     */
    public static Date getYear(Date date) {
        if (date == null) {
            date = new Date();
        }
        try {
            return parse(format(date, DF_YYYY), DF_YYYY);
        } catch (ParseException e) {
            //unreachable
            throw new RuntimeException("Parsing / Getting year failed.", e);
        }
    }

    /**
     * 时间加days天(支持负数)
     *
     * @param date
     * @param days
     * @return
     */
    public static Date addDay(Date date, int days) {
        return add(date, Calendar.DATE, days);
    }

    /**
     * 时间加days分钟(支持负数)
     *
     * @param date
     * @param minutes
     * @return
     */
    public static Date addMinute(Date date, int minutes) {
        return add(date, Calendar.MINUTE, minutes);
    }

    /**
     * 时间加(支持负数)
     *
     * @param date   待处理Date
     * @param period 类型。参照java.util.Calendar 定义
     * @param num    数值
     * @return
     */
    public static Date add(Date date, int period, int num) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(period, num);
        return calendar.getTime();
    }

    /**
     * 获取两个时间的中间时间
     *
     * @param startDate
     * @param endDate
     * @return
     */
    public static Date getMiddleDate(Date startDate, Date endDate) {
        long middle = (endDate.getTime() + startDate.getTime()) / 2;
        return new Date(middle);
    }

    /**
     * 比较两个时间是否相等
     *
     * @param date1
     * @param date2
     * @return
     */
    public static boolean equals(Date date1, Date date2) {
        if (date1 == null || date2 == null) {
            return false;
        }
        return date1.compareTo(date2) == 0;
    }

    /**
     * 计算到明日0点为止的剩余秒数
     *
     * @return
     */
    public static long getLeftSecondsToNextDay() {
        long now = System.currentTimeMillis() / 1000;
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        return calendar.getTimeInMillis() / 1000 - now;
    }

}
