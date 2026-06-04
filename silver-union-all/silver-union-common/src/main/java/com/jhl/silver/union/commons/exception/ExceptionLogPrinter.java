package com.jhl.silver.union.commons.exception;

import com.jhl.silver.union.commons.CommonResultCode;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import java.util.Objects;

/**
 * 统一异常日志输出工作
 *
 * @author: qingren
 * @create_time: 2020/9/21
 */
@Slf4j
public class ExceptionLogPrinter {

    /**
     * 打印异常信息
     *
     * @param t
     * @param logger
     */
    public static void printExceptionLog(Throwable t, Logger logger) {
        printExceptionLog(t, logger, false);
    }

    /**
     * 打印异常信息
     *
     * @param t
     * @param logger
     * @param forceBrief true: 不打堆栈
     */
    public static void printExceptionLog(Throwable t, Logger logger, boolean forceBrief) {
        if (Objects.isNull(logger)) {
            logger = log;
        }
        if (t instanceof IExceptionInfo) {
            //自定义Exception 打印错误码以及错误信息
            IExceptionInfo he = (IExceptionInfo) t;
            if (Objects.equals(he.getCode(), CommonResultCode.INVALID_PARAMS.code)) {
                logger.warn("{} - Parameter is invalid:{} - {}", he.getCode(),
                        he.getExpMessage(), he.getParamMsg());
            } else if (StringUtils.isNotBlank(he.getParamMsg())) {
                logger.error("{} - Exception occurred: {} - {}", he.getCode(),
                        he.getExpMessage(), he.getParamMsg());
            } else {
                if (forceBrief) {
                    logger.error("Unhandled IExceptionInfo:[{}] - {}", he.getCode(), he.getExpMessage());
                } else {
                    logger.error("Unhandled IExceptionInfo:[" + he.getCode() + "]" + he.getExpMessage(), he);
                }
            }

            return;
        }

        if (t instanceof ValidationException) {
            logger.warn("Parameter is invalid:[{}]{} - {}",
                    CommonResultCode.INVALID_PARAMS.getCode(),
                    CommonResultCode.INVALID_PARAMS.getMsg(), t.getMessage());
            return;
        }

        if (t instanceof IllegalArgumentException) {
            IllegalArgumentException he = (IllegalArgumentException) t;
            logger.warn("Parameter is invalid:[{}]{} - {}",
                    CommonResultCode.INVALID_PARAMS.getCode(),
                    CommonResultCode.INVALID_PARAMS.getMsg(), he.getMessage());
            return;
        }
        //未捕获的异常
        if (forceBrief) {
            logger.error("Unhandled Exception: {}: {}", t.getClass().getSimpleName(), t.getMessage());
        } else {
            logger.error("Unhandled Exception.", t);
        }
    }
}
