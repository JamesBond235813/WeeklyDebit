package com.jhl.silver.union.web.data;

import com.jhl.silver.union.commons.validate.ValidateUtils;

/**
 * Request 自检接口
 *
 * @author: qingren
 * @create_time: 2025/3/23
 */
public interface IValidateRequest {
    /**
     * 检查当前实现类的参数有效性
     */
    default void validate() {
        ValidateUtils.validateWithDefaultValidator(this);
    }
}
