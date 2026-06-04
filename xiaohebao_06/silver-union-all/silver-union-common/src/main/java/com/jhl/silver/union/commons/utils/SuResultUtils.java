package com.jhl.silver.union.commons.utils;

import com.jhl.silver.union.commons.CommonResultCode;
import com.jhl.silver.union.commons.IResultCode;
import com.jhl.silver.union.commons.SuResult;
import com.jhl.silver.union.commons.exception.BizException;
import com.jhl.silver.union.commons.gson.GsonHelper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * REST 响应信息工具类
 *
 * @author qingren
 * @date 18/3/8 上午11:47
 */
public class SuResultUtils {

    public static <T> SuResult<T> successResult(T data) {
        return successResult(data, null);
    }

    public static <T> SuResult<T> successResult(T data, String traceId) {
        return new SuResult<T>()
                .setCode(CommonResultCode.SUCCESS.code)
                .setMsg(CommonResultCode.SUCCESS.msg)
                .setTid(traceId)
                .setData(data);
    }

    public static <T> SuResult<T> successResult() {
        return successResult(null);
    }

    /**
     * 构造请求失败的结果信息
     *
     * @param resultCode 错误编码枚举
     * @param <T>
     * @return
     */
    public static <T> SuResult<T> failedResult(IResultCode resultCode) {
        return failedResult(resultCode, BizException.BIZ_CODE);
    }

    /**
     * 构造请求失败的结果信息
     *
     * @param code    错误码
     * @param msg     错误消息
     * @param bizCode 发生错误服务的业务编码
     * @param traceId 请求的traceId
     * @param <T>
     * @return
     */
    public static <T> SuResult<T> failedResult(int code, String msg, String bizCode, String traceId) {

        return new SuResult<T>()
                .setBizCode(StringUtils.defaultIfBlank(bizCode, StringUtils.EMPTY))
                .setCode(code)
                .setMsg(msg)
                .setTid(traceId);

    }

    public static <T> SuResult<T> failedResult(int code, String msg, String bizCode) {
        return failedResult(code, msg, bizCode, StringUtils.EMPTY);
    }

    public static <T> SuResult<T> failedResult(IResultCode resultCode, String bizCode, String traceId) {
        return failedResult(resultCode.getCode(), resultCode.getMsg(), bizCode, traceId);
    }

    /**
     * 构造请求失败的结果信息
     *
     * @param resultCode
     * @param bizCode    微服务业务编码。优先级高于 ResultUtils.BIZ_CODE
     * @param <T>
     * @return
     */
    public static <T> SuResult<T> failedResult(IResultCode resultCode, String bizCode) {
        return failedResult(resultCode, bizCode, null);
    }

    public static <T> boolean isSuccess(SuResult<T> result) {
        return result != null && Objects.equals(CommonResultCode.SUCCESS.code, result.getCode());
    }

    /**
     * 校验 JingResult， 若校验不通过， 则抛异常
     *
     * @param result
     * @param code
     * @param errMsg
     * @param addMsgMaker 仅在校验失败的时候执行。
     * @param <T>
     */
    public static <T> void validateResult(SuResult<T> result, Integer code, String errMsg,
            Supplier<String> addMsgMaker) {
        if (isSuccess(result)) {
            return;
        }
        code = OtherUtils.defaultIfNull(code, CommonResultCode.SYSTEM_ERROR.code);
        errMsg = OtherUtils.defaultIfNull(errMsg, CommonResultCode.SYSTEM_ERROR.msg);
        String addMsg = null;
        if (Objects.nonNull(addMsgMaker)) {
            addMsg = addMsgMaker.get();
        }
        throw new BizException(code, errMsg, addMsg);
    }

    /**
     * 校验 JingResult， 若校验不通过， 则抛异常
     *
     * @param result
     * @param <T>
     */
    public static <T> void validateResult(SuResult<T> result) {
        validateResult(result, result.getCode(), result.getMsg(), () -> result.getBizCode());
    }

    /**
     * 校验 JingResult 并返回结果数据。若校验不通过， 则抛异常
     *
     * @param result
     * @param code
     * @param errMsg
     * @param addMsgMaker 仅在校验失败的时候执行。
     * @param <T>
     * @return
     */
    public static <T> T validateAndGetData(SuResult<T> result, int code, String errMsg,
            Supplier<String> addMsgMaker) {
        validateResult(result, code, errMsg, addMsgMaker);
        return result.getData();
    }

    /**
     * 校验 JingResult 并返回结果数据。若校验不通过， 则抛异常
     *
     * @param result
     * @param <T>
     * @return
     */
    public static <T> T validateAndGetData(SuResult<T> result, Supplier<String> addMsgMaker) {
        validateResult(result, result.getCode(), result.getMsg(), addMsgMaker);
        return result.getData();
    }

    /**
     * 构造远程调用失败的JingResult，并输出日志
     *
     * @param log
     * @param cause
     * @param req
     * @param <T>
     * @return
     */
    public static <T> SuResult<T> makeFallbackResult(Logger log, Throwable cause, String className, String req) {
        if (Objects.isNull(cause) || Objects.isNull(log)) {
            return SuResultUtils.failedResult(CommonResultCode.REMOTE_FAILED, BizException.BIZ_CODE);
        }
        StackTraceElement[] stackTrace = cause.getStackTrace();
        String methodName = StringUtils.EMPTY;
        String innerClassName = StringUtils.EMPTY;
        if (Objects.nonNull(stackTrace) && stackTrace.length > 0) {
            methodName = stackTrace[0].getMethodName();
            innerClassName = stackTrace[0].getClassName();
        }
        if (StringUtils.isNotBlank(className)) {
            innerClassName = className;
        }
        log.error("{}.{} req:{}, error:", innerClassName, methodName, req, cause);
        return SuResultUtils.failedResult(CommonResultCode.REMOTE_FAILED, BizException.BIZ_CODE);
    }

    /**
     * 处理function 逻辑 ， 并打印日志的信息
     *
     * @param request
     * @param function
     * @param funcDesc 对 function的描述
     * @param <T>
     * @param <R>
     * @return
     */
    public static <T, R> R processWithLog(T request, Function<T, SuResult<R>> function, String funcDesc, Logger log) {
        StackTraceElement[] stack = new Throwable().getStackTrace();
        String tmpMethodName = StringUtils.EMPTY;
        if (Objects.nonNull(stack) && stack.length > 1) {
            tmpMethodName = stack[1].getMethodName();
        }
        String methodName = tmpMethodName;
        String desc = StringUtils.defaultIfBlank(funcDesc, StringUtils.EMPTY);
        SuResult<R> result = processAndReturnResult(request, function, funcDesc, log, tmpMethodName);
        return SuResultUtils.validateAndGetData(result, () -> "调用 " + methodName + " " + desc + " 失败");
    }

    /**
     * 处理function 逻辑 ， 并打印日志的信息
     *
     * @param request
     * @param function
     * @param funcDesc 对 function的描述
     * @param <T>
     * @param <R>
     * @return
     */
    public static <T, R> SuResult<R> processAndReturnResult(T request, Function<T, SuResult<R>> function,
            String funcDesc, Logger log, String methodName) {
        if (StringUtils.isBlank(methodName)) {
            StackTraceElement[] stack = new Throwable().getStackTrace();
            if (Objects.nonNull(stack) && stack.length > 1) {
                methodName = stack[1].getMethodName();
            }
        }

        String desc = StringUtils.defaultIfBlank(funcDesc, StringUtils.EMPTY);
        log.info("{} {} request: {}", methodName, desc, GsonHelper.GSON_NHE.toJson(request));
        SuResult<R> result = function.apply(request);
        log.info("{} {} response: {}", methodName, desc, GsonHelper.GSON_NHE.toJson(result));
        return result;
    }

    /**
     * 处理function 逻辑 ， 并打印日志的信息
     *
     * @param request
     * @param function
     * @param funcDesc 对 function的描述
     * @param log
     * @param <T>
     * @param <R>
     * @return
     */
    public static <T, R> SuResult<R> processAndReturnResult(T request, Function<T, SuResult<R>> function,
            String funcDesc, Logger log) {
        return processAndReturnResult(request, function, funcDesc, log, null);
    }

}
