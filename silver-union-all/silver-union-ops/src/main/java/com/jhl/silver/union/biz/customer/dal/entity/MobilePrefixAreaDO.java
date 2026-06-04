package com.jhl.silver.union.biz.customer.dal.entity;

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
@TableName("mobile_prefix_area")
@Schema(name = "MobilePrefixAreaDO", description = "Mobile prefix area mapping")
public class MobilePrefixAreaDO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "Mobile prefix (7 digits)")
    private String mobilePre;

    @Schema(description = "Province")
    private String province;

    @Schema(description = "City")
    private String city;

    @Schema(description = "Carrier")
    private String carrier;

    @Schema(description = "District code")
    private String districtCode;

    @Schema(description = "Post code")
    private String postCode;

    public static final String ID = "id";
    public static final String MOBILE_PRE = "mobile_pre";
    public static final String PROVINCE = "province";
    public static final String CITY = "city";
    public static final String CARRIER = "carrier";
    public static final String DISTRICT_CODE = "district_code";
    public static final String POST_CODE = "post_code";
}
