package com.jhl.silver.union.commons.web.handler;

import com.jhl.silver.union.commons.CommonConstant;
import com.jhl.silver.union.commons.CommonResultCode;
import com.jhl.silver.union.commons.SuResult;
import com.jhl.silver.union.commons.exception.BizException;
import com.jhl.silver.union.commons.exception.IExceptionInfo;
import com.jhl.silver.union.commons.gson.GsonHelper;
import com.jhl.silver.union.commons.utils.SuResultUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.List;
import java.util.Objects;

/**
 * 全局异常处理器
 *
 * @author qingren
 * @date 18/3/7 上午12:15
 */
@Slf4j
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    /**
     * 返回附加异常信息
     */
    public static boolean RETURN_ADDITIONAL_INFO = false;

    /**
     * 所有异常全部走此方法
     *
     * @param request
     * @param t
     * @return
     */
    // @ExceptionHandler(value = Throwable.class)
    public ResponseEntity<SuResult<Void>> handlerAllException(HttpServletRequest request, Throwable t,
            boolean ignoreBizExceptionLog) {
        // 处理参数校验类异常
        t = handlerValidateException(t);

        if (t instanceof IExceptionInfo he) {
            if (!ignoreBizExceptionLog) {

                //自定义Exception 打印错误码以及错误信息
                if (Objects.equals(he.getCode(), CommonResultCode.INVALID_PARAMS.code)) {
                    log.warn("{} - Parameter is invald:[{}]{} - {}", request.getRequestURL(), he.getCode(),
                            he.getExpMessage(), he.getParamMsg());
                } else if (StringUtils.isNotBlank(he.getParamMsg())) {
                    log.error("{} - Exception occurred:[{}]{} - {}", request.getRequestURL(), he.getCode(),
                            he.getExpMessage(), he.getParamMsg());
                } else {
                    log.error("{} - Unhandled JingException:[{}] - {}", request.getRequestURL(), he.getCode(),
                            he.getExpMessage(), he);
                }
            }

            return ResponseEntity.status(HttpStatus.OK)
                    .body(new SuResult<Void>(he.getCode(), getReturnMessage(he.getExpMessage(), he.getParamMsg()),
                            he.getMsgTitle(), BizException.BIZ_CODE));
        }

        if (t instanceof IllegalArgumentException) {
            IllegalArgumentException he = (IllegalArgumentException) t;
            log.warn("{} - Parameter is invalid:[{}]{} - {}", request.getRequestURL(),
                    CommonResultCode.INVALID_PARAMS.getCode(),
                    CommonResultCode.INVALID_PARAMS.getMsg(), he.getMessage());
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new SuResult<Void>(CommonResultCode.INVALID_PARAMS.getCode(),
                            getReturnMessage(CommonResultCode.INVALID_PARAMS.getMsg(), he.getMessage()),
                            BizException.BIZ_CODE));
        }

        //未捕获的异常
        log.error("{} - Unhandled Exception.", request.getRequestURL(), t);
        return ResponseEntity.status(HttpStatus.OK)
                .body(SuResultUtils.failedResult(CommonResultCode.SYSTEM_ERROR, BizException.BIZ_CODE));
    }

    protected String getReturnMessage(String errMsg, String additionalMsg) {
        if (RETURN_ADDITIONAL_INFO) {
            if (StringUtils.isBlank(additionalMsg)) {
                return errMsg;
            }
            return errMsg + " - " + additionalMsg;
        }
        return errMsg;
    }

    /**
     * 参数校验类异常则将t 转换为一个BizException
     */
    private Throwable handlerValidateException(Throwable t) {
        if (t instanceof BindException) {
            List<ObjectError> list = ((BindException) t).getBindingResult().getAllErrors();
            if (!CollectionUtils.isEmpty(list)) {
                String msg = list.get(0).getDefaultMessage();
                return new BizException(CommonResultCode.INVALID_PARAMS.code,
                        getReturnMessage(msg, GsonHelper.GSON.toJson(list)), BizException.BIZ_CODE);
            }
        }

        if (t instanceof ValidationException) {
            String msg = t.getMessage();
            int msgStart = StringUtils.defaultIfBlank(msg, StringUtils.EMPTY).lastIndexOf(CommonConstant.COLON);
            // 取:号后面的内容做为异常信息
            if (msgStart > -1) {
                msgStart += 1;
            } else {
                msgStart = 0;
            }
            return new BizException(CommonResultCode.INVALID_PARAMS.code,
                    getReturnMessage(msg.substring(msgStart).trim(), msg), BizException.BIZ_CODE);
        }

        if (t instanceof MethodArgumentNotValidException) {
            BindingResult bindingResult = ((MethodArgumentNotValidException) t).getBindingResult();
            FieldError fieldError = bindingResult.getFieldError();
            if (Objects.nonNull(fieldError)) {
                return new BizException(CommonResultCode.INVALID_PARAMS.code,
                        GsonHelper.GSON.toJson(fieldError), BizException.BIZ_CODE);
            }
        }

        return t;
    }

}
