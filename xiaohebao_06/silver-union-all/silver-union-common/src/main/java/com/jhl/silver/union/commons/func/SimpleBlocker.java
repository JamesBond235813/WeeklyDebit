package com.jhl.silver.union.commons.func;

/**
 * 无参数以及返回值
 * @author: qingren
 * @create_time: 2021/10/26
 */
@FunctionalInterface
public interface SimpleBlocker {
    /**
     * 处理不含参且无访问值的代码逻辑
     */
    void process();
}
