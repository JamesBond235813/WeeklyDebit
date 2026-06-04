package com.jhl.silver.union.biz.user.manager;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jhl.silver.union.biz.user.dal.entity.SuUserInfoDO;

/**
 * <p>
 * 用户及员工信息 服务类
 * </p>
 *
 * @author Way
 * @since 2025-03-16 20:55:33
 */
public interface SuUserInfoManager extends IService<SuUserInfoDO> {
    /**
     * @param userId
     * @return
     */
    SuUserInfoDO queryUserById(Long userId);

    /**
     * 通过 用户账号，用手机号，邮箱查询用户信息
     *
     * @param username 用户标识。 可为 用户账号，用手机号，邮箱
     * @return 若未找到用户则返回null
     */
    SuUserInfoDO findUserByArbitraryUsername(String username);

    /**
     * 通过用户名查询用户信息
     * @param username
     * @return
     */
    SuUserInfoDO findUserByUsername(String username);

    /**
     * 根据手机号查询用户信息
     *
     * @param phone
     * @return
     */
    Long queryUserIdByPhone(String phone);

    /**
     * 取合法用户信息
     *
     * @param username
     * @return
     */
    SuUserInfoDO getValidUserInfoByUsernameCacheFirst(String username);

    /**
     * 根据用户名取用户信息
     *
     * @param userName
     * @return
     */
    SuUserInfoDO getUserValidInfoByUserName(String userName);

    /**
     * 根据用户名 清除对应的缓存
     * @param usernames 若为 null, 表示清除所有用户缓存
     */
    void clearCacheByUsername(String ... usernames);

    /**
     * 根据用户ID清除对应的缓存
     * @param userIds
     */
    void clearCacheByUserId(Long ... userIds);

    /**
     * 根据用户名,用户 ID 清除对应的缓存
     * @param userId
     * @param userName
     */
    default void clearCacheBy(Long userId,String userName){
        this.clearCacheByUsername(userName);
        this.clearCacheByUserId(userId);
    }
}
