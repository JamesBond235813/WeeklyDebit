package com.jhl.silver.union.biz.customer.manager.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jhl.silver.union.biz.common.enums.ImportProcStatusEnum;
import com.jhl.silver.union.biz.customer.dal.entity.CustomerImportRecordDO;
import com.jhl.silver.union.biz.customer.dal.mapper.CustomerImportRecordMapper;
import com.jhl.silver.union.biz.customer.manager.CustomerImportRecordManager;
import com.jhl.silver.union.biz.data.CustImpRecQry;
import com.jhl.silver.union.biz.data.ImportFileProcInfo;
import com.jhl.silver.union.biz.data.convert.CustImportRecordConvert;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * <p>
 * 导入客户信息的操作记录 服务实现类
 * </p>
 *
 * @author Way
 * @since 2025-05-06 19:38:55
 */
@Service
public class CustomerImportRecordManagerImpl extends ServiceImpl<CustomerImportRecordMapper, CustomerImportRecordDO>
        implements CustomerImportRecordManager {

    @Resource
    private CustImportRecordConvert customerConvert;

    @Override
    public void saveImportFileProcInfo(ImportFileProcInfo info) {
        if (Objects.isNull(info)) {
            return;
        }
        CustomerImportRecordDO rec = customerConvert.convert2CustomerImportRecordDO(info);
        this.save(rec);
        info.setId(rec.getId());
    }

    @Override
    public ImportFileProcInfo findByOriFileName(String oriFileName, Long optUserId,
            ImportProcStatusEnum... statusEnums) {
        if (StringUtils.isBlank(oriFileName)) {
            return null;
        }
        CustImpRecQry qry = new CustImpRecQry()
                .setOriFileName(oriFileName)
                .setOptUserId(optUserId);
        if (statusEnums != null && statusEnums.length > 0) {
            qry.setProcStatusEnumList(List.of(statusEnums));
        }
        LambdaQueryWrapper<CustomerImportRecordDO> qw =qry.toQueryWrapper();
        qw.orderByAsc(CustomerImportRecordDO::getId);
        CustomerImportRecordDO rec = this.getOne(qw, false);
        if (Objects.isNull(rec)) {
            return null;
        }
        return customerConvert.convert2ImportFileProcInfo(rec);
    }

    @Override
    public boolean updateById(ImportFileProcInfo info) {
        if (Objects.isNull(info) || Objects.isNull(info.getId())) {
            return false;
        }
        CustomerImportRecordDO toUpdate = customerConvert.convert2CustomerImportRecordDO(info);
        return this.updateById(toUpdate);
    }
}
