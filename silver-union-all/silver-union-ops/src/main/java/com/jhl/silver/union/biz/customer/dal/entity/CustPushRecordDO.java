package com.jhl.silver.union.biz.customer.dal.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
/**
 * <p>
 * 推送数据记录
 * </p>
 *
 * @author Way
 * @since 2025-12-05 23:50:59
 */
@Getter
@Setter
@ToString
@Accessors(chain = true)
@TableName("cust_push_record")
@Schema(name = "CustPushRecordDO", description = "推送数据记录")
public class CustPushRecordDO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @Schema(description = "ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 推送渠道ID
     */
    @Schema(description = "推送渠道ID")
    private Integer channelId;

    /**
     * 三方平台APP ID
     */
    @Schema(description = "三方平台APP ID")
    private String appId;

    /**
     * 三方平台请求ID
     */
    @Schema(description = "三方平台请求ID")
    private String requestId;

    /**
     * 渠道名称
     */
    @Schema(description = "渠道名称")
    private String channelName;

    /**
     * 数据类型。0:撞库数据,1:进件数据
     */
    @Schema(description = "数据类型。0:撞库数据,1:进件数据")
    private Integer type;

    /**
     * 客户姓名
     */
    @Schema(description = "客户姓名")
    private String custName;

    /**
     * 客户手机号
     */
    @Schema(description = "客户手机号")
    private String mobile;

    /**
     * 推送方订单号
     */
    @Schema(description = "推送方订单号")
    private String orderNo;

    /**
     * 收到的数据是否已存在。 1: 存在 其它不存在
     */
    @Schema(description = "收到的数据是否已存在。 1: 存在 其它不存在")
    private Integer existedFlag;

    /**
     * 接收的请求内容明文
     */
    @Schema(description = "接收的请求内容明文")
    private String receivedReq;

    /**
     * 备注信息
     */
    @Schema(description = "备注信息")
    private String remark;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private Date gmtCreate;

    /**
     * 最后修改时间
     */
    @Schema(description = "最后修改时间")
    private Date gmtModified;

    public static final String ID = "id";

    public static final String CHANNEL_ID = "channel_id";

    public static final String APP_ID = "app_id";

    public static final String REQUEST_ID = "request_id";

    public static final String CHANNEL_NAME = "channel_name";

    public static final String TYPE = "type";

    public static final String CUST_NAME = "cust_name";

    public static final String MOBILE = "mobile";

    public static final String ORDER_NO = "order_no";

    public static final String EXISTED_FLAG = "existed_flag";

    public static final String RECEIVED_REQ = "received_req";

    public static final String REMARK = "remark";

    public static final String GMT_CREATE = "gmt_create";

    public static final String GMT_MODIFIED = "gmt_modified";
}
