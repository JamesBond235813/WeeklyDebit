package com.jhl.silver.union.commons;

import com.jhl.silver.union.commons.exception.BizException;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 通用接口返回值数据结构
 *
 * @author: qingren
 * @create_time: 2020/7/22
 */
@Schema(description = "通用接口返回值数据结构")
@Data
@Accessors(chain = true)
public class SuResult<T> {

    public SuResult(Integer code, String msg, T data, String tid, String bizCode, String version) {
        this.code = code;
        this.msg = msg;
        this.data = data;
        this.tid = tid;
        this.bizCode = bizCode;
        this.version = version;
    }

    public SuResult(Integer code, String msg, String bizCode) {
        this.code = code;
        this.msg = msg;
        this.bizCode = bizCode;
    }

    public SuResult(Integer code, String msg, String msgTitle, String bizCode) {
        this.code = code;
        this.msg = msg;
        this.msgTitle = msgTitle;
        this.bizCode = bizCode;
    }

    public SuResult() {
    }

    public static <T> SuResult<T> successResult() {
        return new SuResult<>(CommonResultCode.SUCCESS);
    }

    public SuResult(IResultCode resultCode) {
        this.code = resultCode.getCode();
        this.msg = resultCode.getMsg();
        this.bizCode = BizException.BIZ_CODE;
    }

    public boolean isSuccess() {
        return CommonResultCode.SUCCESS.getCode() == this.code;
    }

    /**
     * 请求返回码 0成功，其它失败<br>
     * 必填 <br>
     */
    @Schema(description = "请求返回码 0成功，其它失败 ", type = "Integer", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    private Integer code;
    /**
     * 错误消息。若code为成功，则本字段为null<br>
     * 非必填 <br>
     */
    @Schema(description = "错误消息。", type = "String", requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "SUCCESS")
    private String msg;

    /**
     * (错误)消息标题。用于前端使用错误弹窗的场景。
     */
    @Schema(description = "(错误)消息标题。用于前端使用错误弹窗的场景。", type = "String", requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "SUCCESS")
    private String msgTitle;

    /**
     * 返回的结果数据，extends Object<br>
     * 非必填 <br>
     */
    @Schema(description = "返回的结果数据", type = "T", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private T data;
    /**
     * traceId. 为请求分配trace的ID. 故障排查时使用<br>
     * 非必填 <br>
     */
    @Schema(description = "traceId. 为请求分配trace的ID. 故障排查时使用 ", type = "String", requiredMode = Schema.RequiredMode.NOT_REQUIRED, hidden = true)
    private String tid;
    /**
     * 出异常的业务编号。若code为成功，则本字段为''<br>
     * 非必填 <br>
     */
    @Schema(description = "出异常的业务编号。若code为成功，则本字段为'' ", type = "String", requiredMode = Schema.RequiredMode.NOT_REQUIRED, hidden = true)
    private String bizCode;
    /**
     * 返回值版本。 默认1.0<br>
     * 必填 <br>
     */
    @Schema(description = "返回值版本。 默认1.0 ", type = "String ", requiredMode = Schema.RequiredMode.REQUIRED, example = "1.0")
    private String version = "1.0";

    public String getMessage() {
        return this.code + " - " + this.msg;
    }

}
