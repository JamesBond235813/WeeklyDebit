package com.jhl.silver.union.biz.data.convert;

import com.jhl.silver.union.biz.customer.dal.entity.BizDictConfigDO;
import com.jhl.silver.union.web.data.BizDictItem;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * @author: qingren
 * @create_time: 2025/3/29
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BizDictConfigConvert {
    /**
     * convert2BizDictItem
     *
     * @param bizDictConfigDO
     * @return
     */
    BizDictItem convert2BizDictItem(BizDictConfigDO bizDictConfigDO);

    /**
     * convert2BizDictItemList
     *
     * @param bizDictConfigDOList
     * @return
     */
    List<BizDictItem> convert2BizDictItemList(List<BizDictConfigDO> bizDictConfigDOList);

}
