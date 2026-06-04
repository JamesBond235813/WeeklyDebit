package com.jhl.silver.union.biz.customer.manager.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jhl.silver.union.biz.customer.dal.entity.CustomerInfoItemDO;
import com.jhl.silver.union.biz.customer.dal.mapper.CustomerInfoItemMapper;
import com.jhl.silver.union.biz.customer.manager.CustomerInfoItemManager;
import com.jhl.silver.union.biz.data.CustomerInfoQry;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.List;

/**
 * <p>
 * 客户信息数据 服务实现类
 * </p>
 *
 * @author Way
 * @since 2025-03-29 00:47:28
 */
@Service
public class CustomerInfoItemManagerImpl extends ServiceImpl<CustomerInfoItemMapper, CustomerInfoItemDO>
        implements CustomerInfoItemManager {
    private void setupMobileMd5(CustomerInfoItemDO item) {
        if (StringUtils.isNotBlank(item.getMobile())) {
            item.setMobileMd5(DigestUtils.md5Hex(item.getMobile()));
        }
    }

    @Override
    public boolean saveBatch(Collection<CustomerInfoItemDO> entityList) {
        entityList.forEach(this::setupMobileMd5);
        return super.saveBatch(entityList);
    }

    @Override
    public boolean save(CustomerInfoItemDO entity) {
        this.setupMobileMd5(entity);
        return super.save(entity);
    }

    @Override
    public boolean updateById(CustomerInfoItemDO entity) {
        this.setupMobileMd5(entity);
        return super.updateById(entity);
    }

    @Override
    public List<CustomerInfoItemDO> getByIds(Collection<Long> ids, Collection<Long> deptIds) {
        if (CollectionUtils.isEmpty(ids)) {

            return List.of();
        }
        CustomerInfoQry qry = new CustomerInfoQry()
                .setIds(ids)
                .setOwnerDeptIds(deptIds);
        return this.list(qry.toQueryWrapper());
    }

}
