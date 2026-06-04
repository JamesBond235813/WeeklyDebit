package com.jhl.silver.union.biz.user.manager.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.cache.LoadingCache;
import com.jhl.silver.union.biz.common.enums.JwtStatusEnum;
import com.jhl.silver.union.biz.common.utils.CacheUtils;
import com.jhl.silver.union.biz.user.dal.entity.SuUserLoginTraceDO;
import com.jhl.silver.union.biz.user.dal.mapper.SuUserLoginTraceMapper;
import com.jhl.silver.union.biz.user.manager.SuUserLoginTraceManager;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * <p>
 * 用户登录日志 服务实现类
 * </p>
 *
 * @author Way
 * @since 2025-03-19 17:32:36
 */
@Service
public class SuUserLoginTraceManagerImpl extends ServiceImpl<SuUserLoginTraceMapper, SuUserLoginTraceDO>
        implements SuUserLoginTraceManager {
    private LoadingCache<Long, Optional<SuUserLoginTraceDO>> loginTraceCache;
    private volatile boolean inited = false;

    @PostConstruct
    public synchronized void init() {
        if (inited) {
            return;
        }
        loginTraceCache = CacheUtils.buildBasicLoadingCache(20, loginTraceId -> getById(loginTraceId));
        inited = true;
    }

    @Override
    public SuUserLoginTraceDO getById(Long id, boolean cacheFirst) {
        if (!cacheFirst) {
            return this.getById(id);
        }
        Optional<SuUserLoginTraceDO> opt = loginTraceCache.getUnchecked(id);
        return opt.orElse(null);
    }

    @Override
    public List<SuUserLoginTraceDO> getByUserId(Long userId, JwtStatusEnum jwtStatusEnum, boolean notExpired) {
        if (Objects.isNull(userId)) {
            return List.of();
        }
        LambdaQueryWrapper<SuUserLoginTraceDO> qw = new LambdaQueryWrapper<>();
        qw.eq(SuUserLoginTraceDO::getUserId, userId)
                .ge(notExpired, SuUserLoginTraceDO::getJwtExpiredAt, new Date());
        if (Objects.nonNull(jwtStatusEnum)) {
            qw.eq(SuUserLoginTraceDO::getJwtStatus, jwtStatusEnum.name());
        }
        return this.list(qw);
    }

    @Override
    public void clearCacheBy(Long... ids) {
        if (Objects.isNull(ids) || ids.length == 0) {
            this.clearCacheBy(List.of());
        }
        this.clearCacheBy(List.of(ids));
    }

    @Override
    public void clearCacheBy(List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            loginTraceCache.invalidateAll();
        }
        loginTraceCache.invalidateAll(ids);
    }

}
