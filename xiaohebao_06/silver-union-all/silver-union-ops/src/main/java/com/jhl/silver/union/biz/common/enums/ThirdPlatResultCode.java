package com.jhl.silver.union.biz.common.enums;

/**
 * 三方平台推送接口返回码
 *
 * @author: qingren
 * @create_time: 2026/1/26
 */
public enum ThirdPlatResultCode {
    SUCCESS(200, "成功"),
    INVALID_PARAMS(400, "参数不正确。（给出具体说明）"),
    DECRYPT_FAILED(501, "解密失败"),
    SIGN_VERIFY_FAILED(502, "验签失败"),
    APP_ID_MISMATCH(600, "appId不匹配，拒绝操作"),
    REFUSE_APPLY(700, "当前暂不接收数据"),
    SYSTEM_BUSY(9999, "系统繁忙，请稍后再试");

    ThirdPlatResultCode(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public final int code;
    public final String msg;
}
