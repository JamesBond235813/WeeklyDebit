package com.jhl.silver.union.commons.exception;

/**
 * 异常信息内容接口
 *
 * @author: qingren
 * @create_time: 2020/7/24
 */
public interface IExceptionInfo {
    /**
     * 获取错误码
     *
     * @return
     */
    int getCode();

    /**
     * 获取额外信息
     *
     * @return
     */
    String getParamMsg();

    /**
     * 获取本服务业务编号
     *
     * @return
     */
    String getBizCode();

    /**
     * 获取异常信息中message的值
     *
     * @return
     */
    String getExpMessage();

    /**
     * 获取错误消息的标题
     *
     * @return
     */
    String getMsgTitle();
}
