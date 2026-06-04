package com.jhl.silver.union.commons.validate;

import com.jhl.silver.union.commons.CommonConstant;
import com.jhl.silver.union.commons.CommonResultCode;
import com.jhl.silver.union.commons.exception.BizException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.groups.Default;
import org.springframework.util.CollectionUtils;

import java.util.Objects;
import java.util.Set;

/**
 * 校验工具类
 *
 * @author: qingren
 * @create_time: 2021/10/28
 */
public class ValidateUtils {
    private static ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
    public static final Validator DEFAULT_VALIDATOR = validatorFactory.getValidator();

    public static <T> void validateWithDefaultValidator(T target, Class<?>... groups) {
        validate(target, DEFAULT_VALIDATOR, groups);
    }

    public static <T> void validate(T target, Validator customValidator, Class<?>... groups) {
        if (Objects.isNull(groups) || groups.length == 0) {
            groups = new Class<?>[] { Default.class };
        }
        Validator validator = Objects.isNull(customValidator) ? DEFAULT_VALIDATOR : customValidator;
        Set<ConstraintViolation<T>> set = validator.validate(target, groups);
        if (CollectionUtils.isEmpty(set)) {
            return;
        }
        StringBuilder msgBuilder = new StringBuilder();
        boolean first = true;
        String briefMsg = CommonResultCode.INVALID_PARAMS.msg;
        for (ConstraintViolation<T> cv : set) {
            if (first) {
                msgBuilder.append("class name: ").append(cv.getRootBeanClass().getSimpleName());
                briefMsg = cv.getMessage();
                first = false;
            }
            msgBuilder.append(", ").append(cv.getPropertyPath()).append(CommonConstant.COLON).append(cv.getMessage());
        }
        throw new BizException(CommonResultCode.INVALID_PARAMS.code, briefMsg, msgBuilder.toString());
    }

    private ValidateUtils() {

    }
}
