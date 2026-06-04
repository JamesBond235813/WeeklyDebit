package com.jhl.silver.union.biz.customer.dal.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * <p>
 * 业务字典配置信息。 这里配置的字典信息仅用于标记、展示，不进行业务处理
 * </p>
 *
 * @author Way
 * @since 2025-03-29 23:00:33
 */
@Getter
@Setter
@ToString
@Accessors(chain = true)
@TableName("biz_dict_config")
public class BizDictConfigDO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 业务类型， 见BizDictConfigTypeEnum枚举定义
     */
    private String bizType;

    /**
     * 业务类型描述
     */
    private String typeDesc;

    /**
     * 业务字典值
     */
    private Integer intValue;

    /**
     * 业务字典值对应的显示名称
     */
    private String label;

    /**
     * 业务字典项说明
     */
    private String description;

    /**
     * 状态。 1：正常。 0：禁用
     */
    private Integer status;

    /**
     * 操作数据变更的用户 ID
     */
    private Long optUserId;

    /**
     * 创建时间
     */
    private Date gmtCreate;

    /**
     * 最后修改时间
     */
    private Date gmtModified;

    public static final String ID = "id";

    public static final String BIZ_TYPE = "biz_type";

    public static final String TYPE_DESC = "type_desc";

    public static final String INT_VALUE = "int_value";

    public static final String LABEL = "label";

    public static final String DESCRIPTION = "DESCRIPTION";

    public static final String GMT_MODIFIED = "gmt_modified";
}
