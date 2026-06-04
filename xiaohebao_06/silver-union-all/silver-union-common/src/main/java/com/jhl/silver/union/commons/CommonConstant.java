package com.jhl.silver.union.commons;

import java.util.Locale;

/**
 * 公共常量
 *
 * @author qingren
 * @date 2019/11/20 4:24 PM
 */
public class CommonConstant {
    /**
     * Local 对象的字符串
     */
    public static final String LOCAL_STR = Locale.CHINA.toString();
    public static final String DEFAULT_TIME_ZONE = "GMT+8";
    /**
     * 数字常量：是
     */
    public static final int YES = 1;
    /**
     * 数字常量：否
     */
    public static final int NO = 0;
    /**
     * 字符串常量：是
     */
    public static final String YES_STR = "1";
    /**
     * 字符串常量：否
     */
    public static final String NO_STR = "0";
    /**
     * 表达式ANY : *
     */
    public static final String ANY = "*";

    /**
     * 默认字符集：UTF-8
     */
    public static final String DEFAULT_CHARSET = "UTF-8";

    /**
     * 英文逗号
     */
    public static final String COMMA = ",";

    /**
     * 英文横杠
     */
    public static final String HYPHEN = "-";
    /**
     * 英文冒号
     */
    public static final String COLON = ":";
    /**
     * 下划线
     */
    public static final String UNDERLINE = "_";
    /**
     * 英文分号
     */
    public static final String SEMICOLON = ";";
    /**
     * 英文点号
     */
    public static final String DOT = ".";

    /**
     * 100分（1元）
     */
    public static final long HUNDRED_CENT = 100L;

    /**
     * 100分（1元）整型
     */
    public static final int INT_HUNDRED_CENT = 100;

    /**
     * 等号
     */
    public static final String EQUALS = "=";
    /**
     * 问号
     */
    public static final String QUESTION_MARK = "?";
    /**
     * &符号
     */
    public static final String AND = "&";
    /**
     * #符号
     */
    public static final String SHARP = "#";

    /**
     * / 符号
     */
    public static final String SLASH = "/";

    /**
     * 透传给其它服务的的header信息汇总字段
     */
    public static final String HEADER_JING = "jing-header";

    /**
     * 客户端IP 经 gateway 服务提取的请求方IP地址
     */
    public static final String HEADER_CLIENT_IP = "jing-client-ip";

    public static final String HEADER_AUTHORIZATION = "Authorization";
    public static final String AUTHORIZATION_PREFIX = "Bearer ";
    public static final String HEADER_CLIENT_ID = "ClientId";
    public static final String IP_UNKNOWN = "unknown";
}
