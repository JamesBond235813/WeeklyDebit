package com.jhl.silver.union.biz.customer.manager.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.cache.LoadingCache;
import com.jhl.silver.union.biz.common.enums.BizDictConfigTypeEnum;
import com.jhl.silver.union.biz.common.enums.CommonStatusEnum;
import com.jhl.silver.union.biz.common.utils.CacheUtils;
import com.jhl.silver.union.biz.customer.dal.entity.BizDictConfigDO;
import com.jhl.silver.union.biz.customer.dal.mapper.BizDictConfigMapper;
import com.jhl.silver.union.biz.customer.manager.BizDictConfigManager;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * <p>
 * 业务字典配置信息。 这里配置的字典信息仅用于标记、展示，不进行业务处理 服务实现类
 * </p>
 *
 * @author Way
 * @since 2025-03-29 00:47:28
 */
@Service
public class BizDictConfigManagerImpl extends ServiceImpl<BizDictConfigMapper, BizDictConfigDO>
        implements BizDictConfigManager {

    private LoadingCache<BizDictConfigTypeEnum, Optional<List<BizDictConfigDO>>> bizConfigCache;
    private volatile boolean inited = false;

    @PostConstruct
    public synchronized void init() {
        if (inited) {
            return;
        }
        bizConfigCache = CacheUtils.buildBasicLoadingCache(60, configType -> listByDictTypeFromDb(configType));
        inited = true;
    }

    @Override
    public List<BizDictConfigDO> listByDictType(BizDictConfigTypeEnum configTypeEnum, boolean forceValid) {
        List<BizDictConfigDO> list = bizConfigCache.getUnchecked(configTypeEnum)
                .orElse(List.of());
        if (!forceValid) {
            return list;
        }
        return list.stream()
                .filter(e -> Objects.equals(e.getStatus(), CommonStatusEnum.OK.status))
                .sorted(Comparator.comparingInt(BizDictConfigDO::getIntValue))
                .collect(Collectors.toList());
    }

    private List<BizDictConfigDO> listByDictTypeFromDb(BizDictConfigTypeEnum configType) {
        if (Objects.isNull(configType)) {
            List.of();
        }
        LambdaQueryWrapper<BizDictConfigDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BizDictConfigDO::getBizType, configType.name())
                .orderByAsc(BizDictConfigDO::getId);
        return this.list(queryWrapper);
    }

    public void clearCache(BizDictConfigTypeEnum configTypeEnum) {
        if (Objects.isNull(configTypeEnum)) {
            bizConfigCache.invalidateAll();
            return;
        }
        bizConfigCache.invalidate(configTypeEnum);
    }

}
