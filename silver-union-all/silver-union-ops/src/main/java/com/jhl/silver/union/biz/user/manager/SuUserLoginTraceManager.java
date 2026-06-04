package com.jhl.silver.union.biz.user.manager;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jhl.silver.union.biz.common.enums.JwtStatusEnum;
import com.jhl.silver.union.biz.user.dal.entity.SuUserLoginTraceDO;

import java.util.List;

/**
 * <p>
 * 用户登录日志 服务类
 * </p>
 *
 * @author Way
 * @since 2025-03-19 17:32:36
 */
public interface SuUserLoginTraceManager extends IService<SuUserLoginTraceDO> {

    /**
     * @param id
     * @param cacheFirst true: 优先走缓存
     * @return
     */
    SuUserLoginTraceDO getById(Long id, boolean cacheFirst);

    /**
     * 根据用户取指定的登录日志
     *
     * @param userId
     * @param jwtStatusEnum
     * @param notExpired
     * @return
     */
    List<SuUserLoginTraceDO> getByUserId(Long userId, JwtStatusEnum jwtStatusEnum, boolean notExpired);

    /**
     * 根据 id 清除登录日志缓存
     *
     * @param ids 若为 null ， 或0长数组， 则表示清空缓存中的所有数据
     */
    void clearCacheBy(Long... ids);

    /**
     * 根据 id 清除登录日志缓存
     *
     * @param ids 若为 null ， 或0长数组， 则表示清空缓存中的所有数据
     */
    void clearCacheBy(List<Long> ids);
}
