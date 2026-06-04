package com.jhl.silver.union.web.data;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.jhl.silver.union.commons.CommonConstant;
import com.jhl.silver.union.commons.utils.SuDateUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * 导入客户信息操作记录
 *
 * @author: qingren
 * @create_time: 2025/5/5
 */
@Data
@Accessors(chain = true)
@Schema(description = "导入客户信息操作记录")
public class ImportFileRecordDTO {
    /**
     * 操作记录的ID
     */
    @Schema(description = "操作记录的ID")
    private Long id;
    /**
     * 原始文件名
     */
    @Schema(description = "原始文件名")
    private String oriFileName;
    /**
     * 文件总数据量
     */
    @Schema(description = "文件总数据量")
    private Long totalCnt;
    /**
     * 已处理的数据量
     */
    @Schema(description = "已处理的数据量")
    private Long processedCnt;
    /**
     * 入库数据量
     */
    @Schema(description = "入库数据量")
    private Long insertedCnt;
    /**
     * 处理状态。
     */
    @Schema(description = "处理状态。")
    private String procStatus;
    /**
     * 处理状态说明
     */
    @Schema(description = "处理状态说明")
    private String procStatusDesc;
    /**
     * 分配数据的目标部门ID
     */
    @Schema(description = "分配数据的目标部门ID")
    private Long targetDeptId;
    /**
     * 分配数据的目标部门名称
     */
    @Schema(description = "分配数据的目标部门名称")
    private String targetDeptName;
    /**
     * 分配数据的目标用户ID
     */
    @Schema(description = "分配数据的目标用户ID")
    private Long targetUserId;
    /**
     * 分配数据的目标用户姓名
     */
    @Schema(description = "分配数据的目标用户姓名")
    private String targetUserName;
    /**
     * 导入完成时间
     */
    @Schema(description = "导入完成时间")
    @JsonFormat(pattern = SuDateUtils.DF_YYYY_MM_DDHHMMSS, timezone = CommonConstant.DEFAULT_TIME_ZONE)
    private Date finishTime;
    /**
     * 操作耗费的毫秒数
     */
    @Schema(description = "操作耗费的毫秒数")
    private Long costMilSec;
    /**
     * 操作人用户ID
     */
    @Schema(description = "操作人用户ID")
    private Long optUserId;
    /**
     * 操作人用户名称
     */
    @Schema(description = "操作人用户名称")
    private String optUserName;
    /**
     * 操作人部门ID
     */
    @Schema(description = "操作人部门ID")
    private Long optDeptId;
    /**
     * 操作人部门名称
     */
    @Schema(description = "操作人部门名称")
    private String optDeptName;
    /**
     * 是否可下载重复客户记录文件的标识。 1: 可下载。 其它不可下载
     */
    @Schema(description = "是否可下载重复客户记录文件的标识。 1: 可下载。 其它不可下载")
    private Integer downloadDupRecFlag;
    /**
     * 错误信息
     */
    @Schema(description = "错误信息")
    private String errorMsg;
    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    @JsonFormat(pattern = SuDateUtils.DF_YYYY_MM_DDHHMMSS, timezone = CommonConstant.DEFAULT_TIME_ZONE)
    private Date gmtCreate;
}
