package com.jhl.silver.union.biz.region.dal.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Getter
@Setter
@ToString
@Accessors(chain = true)
@TableName("region_area")
@Schema(name = "RegionAreaDO", description = "Region area data")
public class RegionAreaDO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "Parent ID")
    private Long parentId;

    @Schema(description = "Name")
    private String name;

    @Schema(description = "Level (1:province, 2:city, 3:district, 4:street)")
    private Integer level;

    @Schema(description = "Code")
    private String code;

    public static final String ID = "id";
    public static final String PARENT_ID = "parent_id";
    public static final String NAME = "name";
    public static final String LEVEL = "level";
    public static final String CODE = "code";
}
