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
@TableName("id_card_area")
@Schema(name = "IdCardAreaDO", description = "ID card area mapping")
public class IdCardAreaDO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "Area code")
    @TableId(value = "code", type = IdType.INPUT)
    private String code;

    @Schema(description = "Area name")
    private String name;

    public static final String CODE = "code";
    public static final String NAME = "name";
}
