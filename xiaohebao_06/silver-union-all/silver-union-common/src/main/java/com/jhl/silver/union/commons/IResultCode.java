package com.jhl.silver.union.commons;

/**
 * 响应返回码
 *
 * @author: qingren
 * @create_time: 2020/7/21
 */
public interface IResultCode {
    String DEFAULT_MSG_TITLE = "错误信息";

    /**
     * 获取返回码
     *
     * @return
     */
    int getCode();

    /**
     * 获取错误信息
     *
     * @return
     */
    String getMsg();

    /**
     * 获取(错误)消息标题
     *
     * @return
     */
    default String getMsgTitle() {
        return null;
    }
}
