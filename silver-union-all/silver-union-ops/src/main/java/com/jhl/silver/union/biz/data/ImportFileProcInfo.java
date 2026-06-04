package com.jhl.silver.union.biz.data;

import com.jhl.silver.union.biz.common.enums.ImportProcStatusEnum;
import com.jhl.silver.union.biz.data.excel.CustomerInfoExcelRowInfo;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * 导入文件信息
 *
 * @author: qingren
 * @create_time: 2025/5/5
 */
@Data
@Accessors(chain = true)
public class ImportFileProcInfo {
    /**
     * ID
     */
    private Long id;

    /**
     * 原始文件名
     */
    private String oriFileName;

    /**
     * 服务器落盘的文件名(绝对路径)
     */

    private String realFileName;

    /**
     * 文件总数据量
     */

    private Long totalCnt;
    /**
     * 已处理的数据量
     */
    private Long processedCnt;
    /**
     * 入库数据量
     */
    private Long insertedCnt;

    /**
     * 处理状态。参数业务枚举
     */

    private ImportProcStatusEnum procStatusEnum;

    /**
     * 分配数据的目标部门ID
     */

    private Long targetDeptId;

    /**
     * 分配数据的目标用户ID
     */

    private Long targetUserId;

    /**
     * 导入完成时间
     */

    private Date finishTime;

    /**
     * 操作耗费的时间
     */

    private Long costMilSec;

    /**
     * 操作人用户ID
     */

    private Long optUserId;

    /**
     * 操作人部门ID
     */

    private Long optDeptId;
    /**
     * 未导入数据的文件绝对路径
     */
    private String existedDataFileName;
    /**
     * 错误信息
     */
    private String errorMsg;

    /**
     * 合法性校验失败的错误信息
     */
    private List<String> validateErrorMsgs;

    private transient List<CustomerInfoExcelRowInfo> rowInfoList;

    public boolean hasError() {
        return !CollectionUtils.isEmpty(validateErrorMsgs);
    }

    public synchronized void addInsertedCnt(int insertedCnt) {
        if (Objects.isNull(this.insertedCnt)) {
            this.insertedCnt = 0L;
        }
        this.insertedCnt += insertedCnt;
    }

    public synchronized void addProcessedCnt(int processedCnt) {
        if (Objects.isNull(this.processedCnt)) {
            this.processedCnt = 0L;
        }
        this.processedCnt += processedCnt;
    }

    public static ImportFileProcInfo of(String oriFileName, String realFileName) {
        ImportFileProcInfo importFileProcInfo = new ImportFileProcInfo()
                .setOriFileName(oriFileName)
                .setRealFileName(realFileName)
                .setProcStatusEnum(ImportProcStatusEnum.INIT);
        return importFileProcInfo;
    }

    public ImportFileProcInfo setErrorMsg(String errorMsg) {
        if (StringUtils.length(errorMsg) > 512) {
            errorMsg = StringUtils.substring(errorMsg, 0, 512);
        }
        this.errorMsg = errorMsg;
        return this;
    }
}
