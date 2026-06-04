package com.jhl.silver.union.biz.sys.dal.entity;

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
 * 平台配置信息
 *
 * @author: qingren
 * @create_time: 2026/1/26
 */
@Getter
@Setter
@ToString
@Accessors(chain = true)
@TableName("sys_config")
public class SysConfigDO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 平台配置信息ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 配置类型
     */
    private String cnfType;

    /**
     * 配置信息 key
     */
    private String cnfKey;

    /**
     * 配置信息描述
     */
    private String cnfDesc;

    /**
     * 配置信息内容(json格式)
     */
    private String cnfValue;

    /**
     * 删除标识. 0:表示未删除; 其它表示未删除
     */
    private Long deleteFlag;

    /**
     * 创建人用户ID
     */
    private Long creatorUid;

    /**
     * 最后修改人用户ID
     */
    private Long modifierUid;

    /**
     * 创建时间
     */
    private Date gmtCreate;

    /**
     * 最后修改时间
     */
    private Date gmtModified;

    public static final String ID = "id";
    public static final String CNF_TYPE = "cnf_type";
    public static final String CNF_KEY = "cnf_key";
    public static final String CNF_DESC = "cnf_desc";
    public static final String CNF_VALUE = "cnf_value";
    public static final String DELETE_FLAG = "delete_flag";
    public static final String CREATOR_UID = "creator_uid";
    public static final String MODIFIER_UID = "modifier_uid";
    public static final String GMT_CREATE = "gmt_create";
    public static final String GMT_MODIFIED = "gmt_modified";
}
