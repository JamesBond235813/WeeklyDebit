package com.jhl.silver.union.commons.exception;

import com.jhl.silver.union.commons.IResultCode;

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 运行时业务异常类
 *
 * @author qingren
 * @date 2020/07/21
 */
public class BizException extends RuntimeException implements IExceptionInfo {
    /**
     * 业务服务代码。默认 空字符串
     */
    public static String BIZ_CODE = "";
    /**
     * 错误码
     */
    protected int code;
    /**
     * 异常时的附加信息
     */
    protected String paramMsg;

    /**
     * 错误消息的标题
     */
    protected String msgTitle;

    public BizException(int code, String message, String paramMsg, Throwable cause) {
        this(code, message, null, paramMsg, cause);
    }

    public BizException(int code, String message, String msgTitle, String paramMsg, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.msgTitle = msgTitle;
        this.paramMsg = paramMsg;

    }

    /**
     * 通用业务异常类构造方法
     *
     * @param code     错误编码
     * @param message  错误消息
     * @param paramMsg 异常时的附加信息
     */
    public BizException(int code, String message, String paramMsg) {
        this(code, message, null, paramMsg, null);
    }

    /**
     * 通用业务异常类构造方法
     *
     * @param code    错误编码
     * @param message 错误消息
     */
    public BizException(int code, String message) {
        this(code, message, null, null, null);
    }

    /**
     * 通用业务异常类构造方法
     *
     * @param resultCode 返回码信息
     * @param paramMsg   参数名
     * @param cause      上一层异常
     */
    public BizException(IResultCode resultCode, String paramMsg, Throwable cause) {
        this(resultCode.getCode(), resultCode.getMsg(), resultCode.getMsgTitle(), paramMsg, cause);
    }

    /**
     * 通用业务异常类构造方法
     *
     * @param resultCode 返回码信息
     * @param paramMsg   参数名
     */
    public BizException(IResultCode resultCode, String paramMsg) {
        this(resultCode.getCode(), resultCode.getMsg(), resultCode.getMsgTitle(), paramMsg, null);
    }

    /**
     * 通用业务异常类构造方法
     *
     * @param resultCode 返回码信息
     */
    public BizException(IResultCode resultCode) {
        this(resultCode.getCode(), resultCode.getMsg(), resultCode.getMsgTitle(), null, null);
    }

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public String getParamMsg() {
        return paramMsg;
    }

    /**
     * 获取本异常所属的业务编号
     *
     * @return
     */
    @Override
    public String getBizCode() {
        return BIZ_CODE;
    }

    @Override
    public String getExpMessage() {
        return this.getMessage();
    }

    @Override
    public String getMsgTitle() {
        return this.msgTitle;
    }

    /**
     * 根据信息创建 BizException 对象<br>
     * 通过<br>
     * <code>MessageFormat.format(resultCode.getMsg(), placeHolders.toArray(new String[placeHolders.size()])) </code>
     * <br>
     * 的方式替换文本占位符
     *
     * @param resultCode
     * @param placeHolders resultCode中的占位符的替换值。
     * @param paramMsg
     * @param cause
     * @return
     */
    public static BizException makeupBy(IResultCode resultCode, List<String> placeHolders, String paramMsg,
            Throwable cause) {
        String msg = resultCode.getMsg();
        if (Objects.nonNull(placeHolders) && !placeHolders.isEmpty()) {
            msg = MessageFormat.format(msg, placeHolders.toArray(new Object[placeHolders.size()]));
        }
        return new BizException(resultCode.getCode(), msg, resultCode.getMsgTitle(), paramMsg, cause);
    }

    /**
     * 根据信息创建 BizException 对象<br>
     * 通过<br>
     * <code>MessageFormat.format(resultCode.getMsg(), placeHolders.toArray(new String[placeHolders.size()])) </code>
     * <br>
     * 的方式替换文本占位符
     *
     * @param resultCode
     * @param placeHolders resultCode中的占位符的替换值。
     * @param paramMsg
     * @return
     */
    public static BizException makeupBy(IResultCode resultCode, List<String> placeHolders, String paramMsg) {
        return makeupBy(resultCode, placeHolders, paramMsg, null);
    }

    /**
     * 根据信息创建 BizException 对象<br>
     * 通过<br>
     * <code>MessageFormat.format(resultCode.getMsg(), placeHolders.toArray(new String[placeHolders.size()])) </code>
     * <br>
     * 的方式替换文本占位符
     *
     * @param resultCode
     * @param placeHolders resultCode中的占位符的替换值。
     * @return
     */
    public static BizException makeupBy(IResultCode resultCode, List<String> placeHolders) {
        return makeupBy(resultCode, placeHolders, null, null);
    }

    /**
     * 根据信息创建 BizException 对象。
     *
     * @param resultCode
     * @param placeHolderMap "变量名"-"替换字符串值"的形式。 用于resultCode中的占位符的替换值， 且要求resultCode#msg中的占位符以 "{变量名}" 的形式存在;
     * @param paramMsg
     * @param cause
     * @return
     */
    public static BizException makeupBy(IResultCode resultCode, Map<String, String> placeHolderMap,
            String paramMsg, Throwable cause) {
        String msg = resultCode.getMsg();
        if (Objects.nonNull(placeHolderMap) && !placeHolderMap.isEmpty()) {
            for (Map.Entry<String, String> entry : placeHolderMap.entrySet()) {
                msg = msg.replace("{" + entry.getKey() + "}", entry.getValue());
            }
        }
        return new BizException(resultCode.getCode(), msg, resultCode.getMsgTitle(), paramMsg, cause);
    }

    /**
     * 根据信息创建 BizException 对象。
     *
     * @param resultCode
     * @param placeHolderMap "变量名"-"替换字符串值"的形式。 用于resultCode中的占位符的替换值， 且要求resultCode#msg中的占位符以 "{变量名}" 的形式存在;
     * @param paramMsg
     * @return
     */
    public static BizException makeupBy(IResultCode resultCode, Map<String, String> placeHolderMap,
            String paramMsg) {
        return makeupBy(resultCode, placeHolderMap, paramMsg, null);
    }

    /**
     * 根据信息创建 BizException 对象。
     *
     * @param resultCode
     * @param placeHolderMap "变量名"-"替换字符串值"的形式。 用于resultCode中的占位符的替换值， 且要求resultCode#msg中的占位符以 "{变量名}" 的形式存在;
     * @return
     */
    public static BizException makeupBy(IResultCode resultCode, Map<String, String> placeHolderMap) {
        return makeupBy(resultCode, placeHolderMap, null, null);
    }

}
