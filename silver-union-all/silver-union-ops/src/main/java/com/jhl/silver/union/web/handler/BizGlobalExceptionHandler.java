package com.jhl.silver.union.web.handler;

import com.jhl.silver.union.biz.common.ResultCode;
import com.jhl.silver.union.commons.CommonResultCode;
import com.jhl.silver.union.commons.SuResult;
import com.jhl.silver.union.commons.exception.BizException;
import com.jhl.silver.union.commons.utils.SuResultUtils;
import com.jhl.silver.union.commons.web.handler.GlobalExceptionHandler;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.Objects;

/**
 * Rest全局异常处理器
 *
 * @author qingren
 * @date 2019/11/17 8:52 PM
 */
@Slf4j
@Hidden // 防止 swagger 乱来，生成本类文档时还报错，
@ControllerAdvice
public class BizGlobalExceptionHandler extends GlobalExceptionHandler {

    @ExceptionHandler(value = Throwable.class)
    public ResponseEntity<SuResult<Void>> handlerAllException(HttpServletRequest request, Throwable t) {
        if (t instanceof MaxUploadSizeExceededException ex) {
            log.error("handleMaxSizeException 上传文件过大", ex);
            return ResponseEntity.status(HttpStatus.OK).body(
                    SuResultUtils.failedResult(CommonResultCode.INVALID_PARAMS.getCode(), "上传文件过大",
                            BizException.BIZ_CODE));
        }
        boolean ignoreBizExceptionLog = false;
        // 在未登录，且没有 params msg 的情况下， 不出日志， 直接给结果 即可
        if (t instanceof BizException e
                && (Objects.equals(e.getCode(), ResultCode.UNAUTHENTICATED.code)
                        || Objects.equals(e.getCode(), ResultCode.UNAUTHENTICATED_BY_ANTHER_AUTH.code))
        // && StringUtils.isBlank(e.getParamMsg())
        ) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new SuResult<Void>(e.getCode(), getReturnMessage(e.getExpMessage(), e.getParamMsg()),
                            e.getMsgTitle(), BizException.BIZ_CODE));
        }

        return super.handlerAllException(request, t, ignoreBizExceptionLog);

    }

}
