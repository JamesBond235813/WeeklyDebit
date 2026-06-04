package com.jhl.silver.union.biz.data.convert;

import com.jhl.silver.union.biz.common.enums.ImportProcStatusEnum;
import com.jhl.silver.union.biz.customer.dal.entity.CustomerImportRecordDO;
import com.jhl.silver.union.biz.data.ImportFileProcInfo;
import com.jhl.silver.union.biz.data.excel.CustomerInfoExcelRowInfo;
import com.jhl.silver.union.web.data.ImportFileRecordDTO;
import com.jhl.silver.union.web.data.admin.PushCustInfoItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

/**
 * @author: qingren
 * @create_time: 2025/3/30
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE,
        imports = { ImportProcStatusEnum.class })
public interface CustImportRecordConvert {

    /**
     * convert2CustomerImportRecordDO
     *
     * @param info
     * @return
     */
    @Mapping(target = "procStatus",
             expression = "java(info.getProcStatusEnum() != null ? info.getProcStatusEnum().name() : null)")
    CustomerImportRecordDO convert2CustomerImportRecordDO(ImportFileProcInfo info);

    /**
     * convert2ImportFileProcInfo
     *
     * @param rec
     * @return
     */
    @Mapping(target = "procStatusEnum", expression = "java(ImportProcStatusEnum.findByName(rec.getProcStatus()))")
    ImportFileProcInfo convert2ImportFileProcInfo(CustomerImportRecordDO rec);

    /**
     * convert2ImportFileRecordDTO
     *
     * @param rec
     * @return
     */
    ImportFileRecordDTO convert2ImportFileRecordDTO(CustomerImportRecordDO rec);

    /**
     * convert2CustomerInfoExcelRowInfo
     *
     * @param item
     * @return
     */
    CustomerInfoExcelRowInfo convert2CustomerInfoExcelRowInfo(PushCustInfoItem item);
}
