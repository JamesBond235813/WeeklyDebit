package com.jhl.silver.union.commons.exception;

import com.jhl.silver.union.commons.CommonResultCode;
import com.jhl.silver.union.commons.IResultCode;
import com.jhl.silver.union.commons.utils.OtherUtils;
import jakarta.validation.ValidationException;

/**
 * 异常工具类
 *
 * @author: qingren
 * @create_time: 2021/3/15
 */
public class BizExceptionUtils {
    /**
     * 将非BizException 包装成 BizException
     *
     * @param exception
     * @return
     */
    public static final BizException wrapException(Exception exception) {
        return wrapThrowable(exception);
    }

    /**
     * 将非BizException 包装成 BizException
     *
     * @param exception
     * @param resultCode 包装成 BizException使用的错误编码
     * @return
     */
    public static final BizException wrapException(Exception exception, IResultCode resultCode) {
        return wrapThrowable(exception, resultCode);
    }

    /**
     * 将非BizException 包装成 BizException
     *
     * @param throwable
     * @return
     */
    public static final BizException wrapThrowable(Throwable throwable) {
        return wrapThrowable(throwable, CommonResultCode.SYSTEM_ERROR);
    }

    /**
     * 将非BizException 包装成 BizException
     *
     * @param throwable
     * @param resultCode 包装成 BizException 实例时，使用的错误编码
     * @return
     */
    public static final BizException wrapThrowable(Throwable throwable, IResultCode resultCode) {
        if (throwable instanceof BizException) {
            return (BizException) throwable;
        }
        if (throwable instanceof ValidationException) {
            return new BizException(CommonResultCode.INVALID_PARAMS, throwable.getMessage(), throwable);
        }
        resultCode = OtherUtils.defaultIfNull(resultCode, CommonResultCode.SYSTEM_ERROR);
        return new BizException(resultCode, null, throwable);
    }

}
