package com.jhl.silver.union.biz.risk.dal.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Getter
@Setter
@ToString
@Accessors(chain = true)
@TableName("risk_control_report")
@Schema(name = "RiskControlReportDO", description = "风控报告")
public class RiskControlReportDO implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "姓名")
    private String name;

    @Schema(description = "身份证号")
    @TableField("id_card")
    private String idCard;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "报告JSON")
    @TableField("report_json")
    private String reportJson;

    @Schema(description = "查询时间")
    @TableField("query_time")
    private Date queryTime;

    @Schema(description = "创建时间")
    @TableField("gmt_create")
    private Date gmtCreate;

    @Schema(description = "修改时间")
    @TableField("gmt_modified")
    private Date gmtModified;
}
