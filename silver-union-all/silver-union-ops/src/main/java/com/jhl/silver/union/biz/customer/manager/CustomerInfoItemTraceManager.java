package com.jhl.silver.union.biz.customer.manager;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jhl.silver.union.biz.common.enums.UpdTypeEnum;
import com.jhl.silver.union.biz.customer.dal.entity.CustomerInfoItemTraceDO;

import java.util.List;

/**
 * <p>
 * 客户信息数据更新历史 服务类
 * </p>
 *
 * @author Way
 * @since 2025-03-29 00:47:29
 */
public interface CustomerInfoItemTraceManager extends IService<CustomerInfoItemTraceDO> {
    /**
     * 根据客户ID取更新记录信息
     *
     * @param customerId
     * @param updTypes
     * @return
     */
    List<CustomerInfoItemTraceDO> listByCustomerId(Long customerId, UpdTypeEnum... updTypes);
}
