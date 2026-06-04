package com.jhl.silver.union.biz.customer.manager;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jhl.silver.union.biz.common.enums.ImportProcStatusEnum;
import com.jhl.silver.union.biz.customer.dal.entity.CustomerImportRecordDO;
import com.jhl.silver.union.biz.data.ImportFileProcInfo;

/**
 * <p>
 * 导入客户信息的操作记录 服务类
 * </p>
 *
 * @author Way
 * @since 2025-05-06 19:38:55
 */
public interface CustomerImportRecordManager extends IService<CustomerImportRecordDO> {
    /**
     * 保存导入客户文件处理信息
     *
     * @param info
     */
    void saveImportFileProcInfo(ImportFileProcInfo info);

    /**
     * 根据原始文件名查找导入客户信息处理信息
     *
     * @param oriFileName
     * @param optUserId
     * @param statusEnums
     * @return
     */
    ImportFileProcInfo findByOriFileName(String oriFileName,Long optUserId, ImportProcStatusEnum... statusEnums);

    boolean updateById(ImportFileProcInfo info);
}
