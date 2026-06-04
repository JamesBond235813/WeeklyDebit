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
 * 导入客户信息的操作记录
 * </p>
 *
 * @author Way
 * @since 2025-05-08 19:39:53
 */
@Getter
@Setter
@ToString
@Accessors(chain = true)
@TableName("customer_import_record")
@Schema(name = "CustomerImportRecordDO", description = "导入客户信息的操作记录")
public class CustomerImportRecordDO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @Schema(description = "ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 原始文件名
     */
    @Schema(description = "原始文件名")
    private String oriFileName;

    /**
     * 服务器落盘的文件绝对路径
     */
    @Schema(description = "服务器落盘的文件绝对路径")
    private String realFileName;

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
     * 处理状态。参数业务枚举
     */
    @Schema(description = "处理状态。参数业务枚举")
    private String procStatus;

    /**
     * 分配数据的目标部门ID
     */
    @Schema(description = "分配数据的目标部门ID")
    private Long targetDeptId;

    /**
     * 分配数据的目标用户ID
     */
    @Schema(description = "分配数据的目标用户ID")
    private Long targetUserId;

    /**
     * 未导入数据的文件绝对路径
     */
    @Schema(description = "未导入数据的文件绝对路径")
    private String existedDataFileName;

    /**
     * 导入完成时间
     */
    @Schema(description = "导入完成时间")
    private Date finishTime;

    /**
     * 操作耗费的时间
     */
    @Schema(description = "操作耗费的时间")
    private Long costMilSec;

    /**
     * 错误信息
     */
    @Schema(description = "错误信息")
    private String errorMsg;

    /**
     * 操作人用户ID
     */
    @Schema(description = "操作人用户ID")
    private Long optUserId;

    /**
     * 操作人部门ID
     */
    @Schema(description = "操作人部门ID")
    private Long optDeptId;

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

    public static final String ORI_FILE_NAME = "ori_file_name";

    public static final String REAL_FILE_NAME = "real_file_name";

    public static final String TOTAL_CNT = "total_cnt";

    public static final String PROCESSED_CNT = "processed_cnt";

    public static final String INSERTED_CNT = "inserted_cnt";

    public static final String PROC_STATUS = "proc_status";

    public static final String TARGET_DEPT_ID = "target_dept_id";

    public static final String TARGET_USER_ID = "target_user_id";

    public static final String EXISTED_DATA_FILE_NAME = "existed_data_file_name";

    public static final String FINISH_TIME = "finish_time";

    public static final String COST_MIL_SEC = "cost_mil_sec";

    public static final String ERROR_MSG = "error_msg";

    public static final String OPT_USER_ID = "opt_user_id";

    public static final String OPT_DEPT_ID = "opt_dept_id";

    public static final String GMT_CREATE = "gmt_create";

    public static final String GMT_MODIFIED = "gmt_modified";
}
