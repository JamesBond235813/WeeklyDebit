package com.jhl.silver.union.biz.customer.manager;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jhl.silver.union.biz.customer.dal.entity.CustomerInfoItemDO;

import java.util.Collection;
import java.util.List;

/**
 * <p>
 * 客户信息数据 服务类
 * </p>
 *
 * @author Way
 * @since 2025-03-29 00:47:28
 */
public interface CustomerInfoItemManager extends IService<CustomerInfoItemDO> {
    /**
     * 根据 客户ID取客户信息
     *
     * @param ids
     * @param deptIds 部门范围
     * @return
     */
    List<CustomerInfoItemDO> getByIds(Collection<Long> ids, Collection<Long> deptIds);

}
