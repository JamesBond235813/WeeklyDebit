package com.jhl.silver.union.commons;

/**
 * 通用返回码
 *
 * @author qingren
 * @date 2019/9/5 5:49 PM
 */
public enum CommonResultCode implements IResultCode {
    /**
     * 请求成功
     */
    SUCCESS(0, "SUCCESS"),
    /**
     * 参数不正确
     */
    INVALID_PARAMS(101, "参数不正确"),
    /**
     * 验签失败
     */
    INVALID_SIGN(102, "验签失败"),
    /**
     * 验签失败: 时间戳为空
     */
    INVALID_SIGN_4_BLANK_TIMESTAMP(103, "验签失败"),
    /**
     * 验签失败: 时间戳 超出当前服务器时间设定的误差值
     */
    INVALID_SIGN_4_EXPIRED_TIMESTAMP(104, "验签失败"),
    /**
     * 验签失败: sign字段为空
     */
    INVALID_SIGN_4_ABSENT(105, "验签失败"),

    /**
     * 正在操作中，请稍后再试。用于获取分布式锁失败的场景
     */
    FORBID_PARALLEL_OPTIONS(9000, "正在操作中，请稍后再试"),
    /**
     * 远程调用失败
     */
    REMOTE_FAILED(9001, "远程调用失败"),
    INVALID_REQUEST(9998, "非法请求"),
    /**
     * 系统级异常。系统繁忙，请稍后再试
     */
    SYSTEM_ERROR(9999, "系统繁忙，请稍后再试");

    CommonResultCode(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    /**
     * 错误码
     */
    public final int code;
    /**
     * 错误信息
     */
    public final String msg;

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public String getMsg() {
        return msg;
    }
}
