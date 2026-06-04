package com.jhl.silver.union.biz.user.manager.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.cache.LoadingCache;
import com.jhl.silver.union.biz.common.BizConstance;
import com.jhl.silver.union.biz.common.enums.CommonStatusEnum;
import com.jhl.silver.union.biz.common.utils.CacheUtils;
import com.jhl.silver.union.biz.data.UserQry;
import com.jhl.silver.union.biz.user.dal.entity.SuUserInfoDO;
import com.jhl.silver.union.biz.user.dal.mapper.SuUserInfoMapper;
import com.jhl.silver.union.biz.user.manager.SuUserInfoManager;
import jakarta.annotation.PostConstruct;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * <p>
 * 用户及员工信息 服务实现类
 * </p>
 *
 * @author Way
 * @since 2025-03-16 20:55:33
 */
@Service
public class SuUserInfoManagerImpl extends ServiceImpl<SuUserInfoMapper, SuUserInfoDO>
        implements SuUserInfoManager {
    private LoadingCache<String, Optional<SuUserInfoDO>> userInfoCache;
    private LoadingCache<Long, Optional<SuUserInfoDO>> userIdInfoCache;
    private volatile boolean inited = false;

    @PostConstruct
    public synchronized void init() {
        if (inited) {
            return;
        }
        userInfoCache = CacheUtils.buildBasicLoadingCache(10, userName -> getUserValidInfoByUserName(userName));
        userIdInfoCache = CacheUtils.buildBasicLoadingCache(10, userId -> getById(userId));
        inited = true;
    }

    @Override
    public SuUserInfoDO queryUserById(Long userId) {
        if (Objects.isNull(userId)) {
            return null;
        }
        // 优先走缓存
        SuUserInfoDO infoDO = userIdInfoCache.getUnchecked(userId).orElse(null);
        if (Objects.equals(infoDO.getStatus(), CommonStatusEnum.OK.status) && Objects.equals(infoDO.getDeleteFlag(),
                BizConstance.NOT_DELETED)) {
            return infoDO;
        }
        return null;
        // UserQry qry = new UserQry()
        //         .setId(userId);
        // return this.getOne(qry.toQryWrapper(true));

    }

    @Override
    public SuUserInfoDO findUserByArbitraryUsername(String username) {
        //依次按照手机号，用户账号 查询用户信息 用户量超过100W 则从es里查询
        if (StringUtils.isBlank(username)) {
            return null;
        }
        String innerUsername = username.trim();
        LambdaQueryWrapper<SuUserInfoDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SuUserInfoDO::getDeleteFlag, 0)
                .and(orQueryWrapper -> orQueryWrapper.eq(SuUserInfoDO::getUserName, innerUsername)
                        .or().eq(SuUserInfoDO::getPhone, innerUsername));
        return this.getOne(queryWrapper, false);

    }

    @Override
    public SuUserInfoDO findUserByUsername(String username) {
        if (StringUtils.isBlank(username)) {
            return null;
        }
        UserQry qry = new UserQry()
                .setUserName(username);
        return this.getOne(qry.toQryWrapper(true), false);
    }

    @Override
    public Long queryUserIdByPhone(String phone) {
        if (StringUtils.isBlank(phone)) {
            return null;
        }
        UserQry qry = new UserQry()
                .setPhone(phone);
        SuUserInfoDO user = this.getOne(qry.toQryWrapper(true));
        if (Objects.isNull(user)) {
            return null;
        }
        return user.getId();
    }

    @Override
    public SuUserInfoDO getValidUserInfoByUsernameCacheFirst(String username) {
        return userInfoCache.getUnchecked(username)
                .orElse(null);
    }

    @Override
    public SuUserInfoDO getUserValidInfoByUserName(String userName) {
        if (Objects.isNull(userName)) {
            return null;
        }
        UserQry qry = new UserQry()
                .setUserName(userName);
        SuUserInfoDO infoDO = this.getOne(qry.toQryWrapper(true));
        if (Objects.nonNull(infoDO) && Objects.nonNull(infoDO.getId())) {
            userIdInfoCache.put(infoDO.getId(), Optional.of(infoDO));
        }
        return infoDO;
    }

    @Override
    public void clearCacheByUsername(String... usernames) {
        if (Objects.isNull(usernames) || usernames.length == 0) {
            userInfoCache.invalidateAll();
            return;
        }
        userInfoCache.invalidateAll(List.of(usernames));
    }

    @Override
    public void clearCacheByUserId(Long... userIds) {
        if (Objects.isNull(userIds) || userIds.length == 0) {
            userIdInfoCache.invalidateAll();
            return;
        }
        userIdInfoCache.invalidateAll(List.of(userIds));
    }

}
