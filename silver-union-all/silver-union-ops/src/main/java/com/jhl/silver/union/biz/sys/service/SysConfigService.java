package com.jhl.silver.union.biz.sys.service;

import com.jhl.silver.union.biz.common.enums.SysConfigTypeEnum;
import com.jhl.silver.union.biz.data.EnableRecvConfig;
import com.jhl.silver.union.biz.data.PanoramaConfig;
import com.jhl.silver.union.biz.data.RecvThirdPlatDataConfig;
import com.jhl.silver.union.biz.sys.dal.entity.SysConfigDO;

import java.util.List;

/**
 * 平台配置信息 服务
 */
public interface SysConfigService {

    /**
     * 新增配置
     *
     * @param config    配置信息
     * @param optUserId 操作人用户ID
     * @return
     */
    Long addConfig(SysConfigDO config, Long optUserId);

    /**
     * 更新配置
     *
     * @param config    配置信息
     * @param optUserId 操作人用户ID
     */
    void updateConfig(SysConfigDO config, Long optUserId);

    /**
     * 删除配置（逻辑删除）
     *
     * @param id        配置ID
     * @param optUserId 操作人用户ID
     */
    void deleteConfig(Long id, Long optUserId);

    /**
     * 根据配置类型与key查询配置
     *
     * @param typeEnum 配置类型
     * @param cnfKey   配置信息 key
     * @return
     */
    SysConfigDO getConfig(SysConfigTypeEnum typeEnum, String cnfKey);

    /**
     * 根据配置类型查询配置列表
     *
     * @param typeEnum 配置类型
     * @return
     */
    List<SysConfigDO> listByType(SysConfigTypeEnum typeEnum);

    /**
     * 清理缓存
     *
     * @param typeEnum 配置类型，为null表示清理所有缓存
     * @param cnfKey   配置信息 key，为空表示清理指定类型的全部缓存
     */
    void clearCache(SysConfigTypeEnum typeEnum, String cnfKey);

    /**
     * 获取 Panorama 风控配置
     *
     * @return
     */
    PanoramaConfig getPanoramaConfig();

    /**
     * 新增接收三方平台数据的配置信息
     *
     * @param config    配置信息
     * @param optUserId 操作人用户ID
     * @return
     */
    Long addRecvThirdPlatDataConfig(RecvThirdPlatDataConfig config, Long optUserId);

    /**
     * 根据appId获取接收三方平台数据的配置信息
     *
     * @param appId 三方平台appId
     * @return
     */
    RecvThirdPlatDataConfig getRecvThirdPlatDataConfig(String appId);

    /**
     * 获取接收上游流量开关配置。若不存在则初始化为开启状态
     *
     * @return
     */
    EnableRecvConfig getEnableRecvConfig();

    /**
     * 更新接收上游流量开关配置
     *
     * @param config
     * @param optUserId
     */
    void updateEnableRecvConfig(EnableRecvConfig config, Long optUserId);
}
