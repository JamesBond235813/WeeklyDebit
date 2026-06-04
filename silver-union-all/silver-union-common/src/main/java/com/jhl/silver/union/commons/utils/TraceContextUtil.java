package com.jhl.silver.union.commons.utils;

import com.jhl.silver.union.commons.CommonConstant;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;

import java.util.UUID;

/**
 * trace id 上下文工具
 * 
 * @author: liwei
 * @date 2022-11-02 3:39:20 PM
 */
public class TraceContextUtil {
    public static final String LOG_TRACE_ID_KEY = "X-B3-TraceId";
    public static final String LOG_TRACE_ID_KEY_SMP = "traceId";
    public static final String LOG_TID_KEY = "tid";

    public static String getTraceId() {
        String tid = OtherUtils.defaultIfNull(MDC.get(LOG_TRACE_ID_KEY), MDC.get(LOG_TRACE_ID_KEY_SMP));
        return OtherUtils.defaultIfNull(tid, (MDC.get(LOG_TID_KEY)));
    }

    public static String makeupTraceId() {
        return UUID.randomUUID().toString().replace(CommonConstant.HYPHEN, StringUtils.EMPTY);
    }

    public static void setupTraceId2MDC(String traceId) {
        traceId = StringUtils.defaultIfBlank(traceId, makeupTraceId());
        MDC.put(LOG_TRACE_ID_KEY, traceId);
        MDC.put(LOG_TRACE_ID_KEY_SMP, traceId);
        MDC.put(LOG_TID_KEY, traceId);
    }

    public static void setupTraceId2MDC() {
        setupTraceId2MDC(null);
    }

}
