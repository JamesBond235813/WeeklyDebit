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
 * 客户信息数据更新历史
 * </p>
 *
 * @author Way
 * @since 2025-04-02 01:01:26
 */
@Getter
@Setter
@ToString
@Accessors(chain = true)
@TableName("customer_info_item_trace")
@Schema(name = "CustomerInfoItemTraceDO", description = "客户信息数据更新历史")
public class CustomerInfoItemTraceDO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 更新历史记录ID
     */
    @Schema(description = "更新历史记录ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 被更新的原始客户信息ID
     */
    @Schema(description = "被更新的原始客户信息ID")
    private Long sourceId;

    /**
     * 更新记录类型。见业务枚举
     */
    @Schema(description = "更新记录类型。见业务枚举")
    private String updType;

    /**
     * 更新的字段及更新后的内容信息，json 格式
     */
    @Schema(description = "更新的字段及更新后的内容信息，json 格式")
    private String updFieldJson;

    /**
     * 更新的字段及更新后的内容信息，json 格式
     */
    @Schema(description = "更新的字段及更新后的内容信息，json 格式")
    private String updDispContentJson;

    /**
     * 操作数据变更的用户 ID
     */
    @Schema(description = "操作数据变更的用户 ID")
    private Long optUserId;

    /**
     * 操作人姓名
     */
    @Schema(description = "操作人姓名")
    private String optUserRealName;

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

    public static final String SOURCE_ID = "source_id";

    public static final String UPD_TYPE = "upd_type";

    public static final String UPD_FIELD_JSON = "upd_field_json";

    public static final String UPD_DISP_CONTENT_JSON = "upd_disp_content_json";

    public static final String OPT_USER_ID = "opt_user_id";

    public static final String OPT_USER_REAL_NAME = "opt_user_real_name";

    public static final String GMT_CREATE = "gmt_create";

    public static final String GMT_MODIFIED = "gmt_modified";
}
