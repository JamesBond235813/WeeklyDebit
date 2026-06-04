package com.jhl.silver.union.commons.web;

import com.jhl.silver.union.commons.CommonConstant;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ip地址相关处理工具类
 *
 * @author: qingren
 * @create_time: 2020/9/24
 */
public class IpUtils {
    private static final Pattern IPV4_PATTERN =
            Pattern.compile("^((25[0-5]|2[0-4]\\d|[01]?\\d\\d?)\\.){3}(25[0-5]|2[0-4]\\d|[01]?\\d\\d?)$");
    private static final Pattern IPV6_PATTERN = Pattern.compile("^([\\da-fA-F]{1,4}:){7}[\\da-fA-F]{1,4}$");

    /**
     * 获取客户端IP地址
     *
     * @param request
     * @return
     */
    public static String getIpFromRequest(HttpServletRequest request) {
        //从x-forwarded-for中取
        String ip = getIpFromHeader(request, "x-forwarded-for");
        if (StringUtils.isNotBlank(ip)) {
            return ip;
        }
        //从 Proxy-Client-IP 中取
        ip = getIpFromHeader(request, "Proxy-Client-IP");
        if (StringUtils.isNotBlank(ip)) {
            return ip;
        }

        // WL-Proxy-Client-IP
        ip = getIpFromHeader(request, "WL-Proxy-Client-IP");
        if (StringUtils.isNotBlank(ip)) {
            return ip;
        }

        // HTTP_CLIENT_IP
        ip = getIpFromHeader(request, "HTTP_CLIENT_IP");
        if (StringUtils.isNotBlank(ip)) {
            return ip;
        }
        // X-Real-IP 中取
        ip = getIpFromHeader(request, "X-Real-IP");
        if (StringUtils.isNotBlank(ip)) {
            return ip;
        }
        // HTTP_X_FORWARDED_FOR 最后一次尝试
        return getIpFromHeader(request, "HTTP_X_FORWARDED_FOR");

    }

    private static String getIpFromHeader(HttpServletRequest request, String headerName) {
        if (StringUtils.isBlank(headerName)) {
            return StringUtils.EMPTY;
        }
        String ip = request.getHeader(headerName);
        if (isUnknownIp(ip)) {
            return StringUtils.EMPTY;
        }
        if (!StringUtils.contains(ip, CommonConstant.COMMA)) {
            return ip;
        }
        String[] ips = ip.split(CommonConstant.COMMA);
        //取第一个不为127.0.0.1的 IP
        for (String s : ips) {
            if (StringUtils.isBlank(s)) {
                continue;
            }

            if (isIPV4(s)) {
                if (!StringUtils.startsWithAny(s, "127.0.0.1", "localhost", "LOCALHOST")) {
                    return s;
                }
                continue;
            }
            if (!StringUtils.startsWithAny(s, "::1", "localhost", "LOCALHOST")) {
                return s;
            }
        }
        return StringUtils.EMPTY;
    }

    private static boolean isUnknownIp(String ip) {
        return StringUtils.isBlank(ip) || StringUtils.equalsIgnoreCase(CommonConstant.IP_UNKNOWN, ip);
    }

    public static boolean isIPV4(String addr) {
        return isMatch(addr, IPV4_PATTERN);
    }

    public static boolean isIPV6(String addr) {
        return isMatch(addr, IPV6_PATTERN);
    }

    private static boolean isMatch(String data, Pattern pattern) {
        if (StringUtils.isBlank(data)) {
            return false;
        }
        Matcher mat = pattern.matcher(data);
        return mat.find();
    }
}
