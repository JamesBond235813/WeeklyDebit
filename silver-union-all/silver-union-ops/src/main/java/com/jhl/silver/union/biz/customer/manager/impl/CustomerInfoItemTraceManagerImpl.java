package com.jhl.silver.union.biz.customer.manager.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jhl.silver.union.biz.common.enums.UpdTypeEnum;
import com.jhl.silver.union.biz.customer.dal.entity.CustomerInfoItemTraceDO;
import com.jhl.silver.union.biz.customer.dal.mapper.CustomerInfoItemTraceMapper;
import com.jhl.silver.union.biz.customer.manager.CustomerInfoItemTraceManager;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <p>
 * 客户信息数据更新历史 服务实现类
 * </p>
 *
 * @author Way
 * @since 2025-03-29 00:47:29
 */
@Service
public class CustomerInfoItemTraceManagerImpl extends ServiceImpl<CustomerInfoItemTraceMapper, CustomerInfoItemTraceDO>
        implements CustomerInfoItemTraceManager {

    @Override
    public List<CustomerInfoItemTraceDO> listByCustomerId(Long customerId, UpdTypeEnum... updTypes) {
        if (Objects.isNull(customerId)) {
            return List.of();
        }
        LambdaQueryWrapper<CustomerInfoItemTraceDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CustomerInfoItemTraceDO::getSourceId, customerId)
                .orderByDesc(CustomerInfoItemTraceDO::getId);
        if (Objects.nonNull(updTypes) && updTypes.length > 0) {
            Set<String> types = Arrays.stream(updTypes)
                    .map(e -> e.name())
                    .collect(Collectors.toSet());
            queryWrapper.in(CustomerInfoItemTraceDO::getUpdType, types);
        }
        return this.list(queryWrapper);
    }
}
