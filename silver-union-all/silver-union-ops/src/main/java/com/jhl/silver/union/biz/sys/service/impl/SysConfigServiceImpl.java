package com.jhl.silver.union.biz.sys.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.cache.LoadingCache;
import com.jhl.silver.union.biz.common.BizConstance;
import com.jhl.silver.union.biz.common.enums.BizDictConfigTypeEnum;
import com.jhl.silver.union.biz.common.enums.SysConfigTypeEnum;
import com.jhl.silver.union.biz.common.utils.CacheUtils;
import com.jhl.silver.union.biz.data.EnableRecvConfig;
import com.jhl.silver.union.biz.data.PanoramaConfig;
import com.jhl.silver.union.biz.data.RecvThirdPlatDataConfig;
import com.jhl.silver.union.biz.customer.service.BizConfigService;
import com.jhl.silver.union.biz.sys.SysConfigKeys;
import com.jhl.silver.union.biz.sys.dal.entity.SysConfigDO;
import com.jhl.silver.union.biz.sys.manager.SysConfigManager;
import com.jhl.silver.union.biz.sys.service.SysConfigService;
import com.jhl.silver.union.commons.gson.GsonHelper;
import com.jhl.silver.union.commons.utils.VerifyUtils;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * 平台配置信息 服务实现类
 */
@Service
public class SysConfigServiceImpl implements SysConfigService {
    private static final String CACHE_KEY_SEP = "::";

    @Resource
    private SysConfigManager sysConfigManager;
    @Resource
    private BizConfigService bizConfigService;

    private LoadingCache<String, Optional<SysConfigDO>> configCache;
    private volatile boolean inited = false;

    @PostConstruct
    public synchronized void init() {
        if (inited) {
            return;
        }
        configCache = CacheUtils.buildBasicLoadingCache(5, this::loadConfigFromDb);
        inited = true;
    }

    @Override
    public Long addConfig(SysConfigDO config, Long optUserId) {
        VerifyUtils.notNull(config, "config", "配置信息不能为空", true);
        VerifyUtils.notBlank(config.getCnfType(), "cnfType", "请指定配置类型", true);
        VerifyUtils.notBlank(config.getCnfKey(), "cnfKey", "请指定配置key", true);
        VerifyUtils.notBlank(config.getCnfValue(), "cnfValue", "请指定配置内容", true);
        VerifyUtils.notNull(optUserId, "optUserId", "请指定操作人", true);

        SysConfigTypeEnum typeEnum = SysConfigTypeEnum.findByName(config.getCnfType());
        if (Objects.nonNull(typeEnum) && StringUtils.isBlank(config.getCnfDesc())) {
            config.setCnfDesc(typeEnum.desc);
        }
        config.setId(null);
        config.setDeleteFlag(BizConstance.NOT_DELETED);
        config.setCreatorUid(optUserId);
        config.setModifierUid(optUserId);
        try {
            sysConfigManager.save(config);
        } catch (DuplicateKeyException e) {
            throw new DuplicateKeyException("配置已存在: cnfType=" + config.getCnfType()
                    + ", cnfKey=" + config.getCnfKey(), e);
        }
        clearCache(SysConfigTypeEnum.findByName(config.getCnfType()), config.getCnfKey());
        return config.getId();
    }

    @Override
    public void updateConfig(SysConfigDO config, Long optUserId) {
        VerifyUtils.notNull(config, "config", "配置信息不能为空", true);
        VerifyUtils.notNull(config.getId(), "config.id", "请指定配置ID", true);
        VerifyUtils.notNull(optUserId, "optUserId", "请指定操作人", true);

        SysConfigDO existed = sysConfigManager.getById(config.getId());
        if (Objects.isNull(existed)) {
            return;
        }
        String cnfType = StringUtils.isNotBlank(config.getCnfType()) ? config.getCnfType() : existed.getCnfType();
        String cnfKey = StringUtils.isNotBlank(config.getCnfKey()) ? config.getCnfKey() : existed.getCnfKey();
        String cnfDesc = config.getCnfDesc();
        if (StringUtils.isBlank(cnfDesc)) {
            SysConfigTypeEnum typeEnum = SysConfigTypeEnum.findByName(cnfType);
            if (Objects.nonNull(typeEnum)) {
                cnfDesc = typeEnum.desc;
            }
        }
        VerifyUtils.notBlank(config.getCnfValue(), "cnfValue", "请指定配置内容", true);

        SysConfigDO update = new SysConfigDO()
                .setId(config.getId())
                .setCnfType(cnfType)
                .setCnfKey(cnfKey)
                .setCnfDesc(cnfDesc)
                .setCnfValue(config.getCnfValue())
                .setModifierUid(optUserId);
        sysConfigManager.updateById(update);
        clearCache(SysConfigTypeEnum.findByName(cnfType), cnfKey);
    }

    @Override
    public void deleteConfig(Long id, Long optUserId) {
        VerifyUtils.notNull(optUserId, "optUserId", "请指定操作人", true);
        if (Objects.isNull(id)) {
            return;
        }
        SysConfigDO existed = sysConfigManager.getById(id);
        if (Objects.isNull(existed)) {
            return;
        }
        SysConfigDO update = new SysConfigDO()
                .setId(id)
                .setDeleteFlag(System.currentTimeMillis())
                .setModifierUid(optUserId);
        sysConfigManager.updateById(update);
        clearCache(SysConfigTypeEnum.findByName(existed.getCnfType()), existed.getCnfKey());
    }

    @Override
    public SysConfigDO getConfig(SysConfigTypeEnum typeEnum, String cnfKey) {
        if (Objects.isNull(typeEnum) || StringUtils.isBlank(cnfKey)) {
            return null;
        }
        String cacheKey = buildCacheKey(typeEnum.name(), cnfKey);
        return configCache.getUnchecked(cacheKey).orElse(null);
    }

    @Override
    public List<SysConfigDO> listByType(SysConfigTypeEnum typeEnum) {
        if (Objects.isNull(typeEnum)) {
            return List.of();
        }
        LambdaQueryWrapper<SysConfigDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysConfigDO::getCnfType, typeEnum.name())
                .eq(SysConfigDO::getDeleteFlag, BizConstance.NOT_DELETED)
                .orderByAsc(SysConfigDO::getId);
        return sysConfigManager.list(wrapper);
    }

    @Override
    public void clearCache(SysConfigTypeEnum typeEnum, String cnfKey) {
        if (Objects.isNull(typeEnum)) {
            configCache.invalidateAll();
            return;
        }
        if (StringUtils.isBlank(cnfKey)) {
            configCache.asMap().keySet()
                    .stream()
                    .filter(key -> StringUtils.startsWith(key, typeEnum.name() + CACHE_KEY_SEP))
                    .forEach(configCache::invalidate);
            return;
        }
        configCache.invalidate(buildCacheKey(typeEnum.name(), cnfKey));
    }

    @Override
    public PanoramaConfig getPanoramaConfig() {
        SysConfigDO config = getConfig(SysConfigTypeEnum.RISK_PANORAMA, SysConfigKeys.PANORAMA_CONFIG);
        if (Objects.isNull(config) || StringUtils.isBlank(config.getCnfValue())) {
            return null;
        }
        return GsonHelper.fromJson(config.getCnfValue(), PanoramaConfig.class);
    }

    @Override
    public Long addRecvThirdPlatDataConfig(RecvThirdPlatDataConfig config, Long optUserId) {
        VerifyUtils.notNull(config, "config", "配置信息不能为空", true);
        VerifyUtils.notBlank(config.getAppId(), "appId", "请指定appId", true);
        String channelName = StringUtils.trimToEmpty(config.getChannelName());
        VerifyUtils.notBlank(channelName, "channelName", "请指定渠道名称", true);
        config.setChannelName(channelName);

        bizConfigService.generateChannelDictByNames(Set.of(channelName), 0L);
        com.jhl.silver.union.web.data.BizDictItem item =
                bizConfigService.getSingleBizDictItemByLabel(BizDictConfigTypeEnum.DATA_CHANNEL, channelName);
        VerifyUtils.notNull(item, "channelName", "创建渠道信息失败", true);
        VerifyUtils.notNull(item.getIntValue(), "channelId", "创建渠道信息失败", true);
        config.setChannelId(Long.valueOf(item.getIntValue()));

        SysConfigDO sysConfig = new SysConfigDO()
                .setCnfType(SysConfigTypeEnum.RECV_THIRD_PLAT_DATA.name())
                .setCnfKey(config.getAppId())
                .setCnfDesc(SysConfigTypeEnum.RECV_THIRD_PLAT_DATA.desc)
                .setCnfValue(GsonHelper.toJson(config));
        return addConfig(sysConfig, optUserId);
    }

    @Override
    public RecvThirdPlatDataConfig getRecvThirdPlatDataConfig(String appId) {
        if (StringUtils.isBlank(appId)) {
            return null;
        }
        SysConfigDO config = getConfig(SysConfigTypeEnum.RECV_THIRD_PLAT_DATA, appId);
        if (Objects.isNull(config) || StringUtils.isBlank(config.getCnfValue())) {
            return null;
        }
        return GsonHelper.fromJson(config.getCnfValue(), RecvThirdPlatDataConfig.class);
    }

    @Override
    public EnableRecvConfig getEnableRecvConfig() {
        SysConfigDO config = getConfig(SysConfigTypeEnum.ENABLE_RECV, SysConfigKeys.ENABLE_RECV);
        if (Objects.isNull(config) || StringUtils.isBlank(config.getCnfValue())) {
            EnableRecvConfig defaultConfig = new EnableRecvConfig().setEnable(true);
            try {
                addConfig(new SysConfigDO()
                        .setCnfType(SysConfigTypeEnum.ENABLE_RECV.name())
                        .setCnfKey(SysConfigKeys.ENABLE_RECV)
                        .setCnfDesc(SysConfigTypeEnum.ENABLE_RECV.desc)
                        .setCnfValue(GsonHelper.toJson(defaultConfig)), 0L);
            } catch (DuplicateKeyException ignore) {
                // 并发初始化时可能出现唯一键冲突，忽略后重新读取
            }
            config = getConfig(SysConfigTypeEnum.ENABLE_RECV, SysConfigKeys.ENABLE_RECV);
            if (Objects.isNull(config) || StringUtils.isBlank(config.getCnfValue())) {
                return defaultConfig;
            }
        }
        EnableRecvConfig parsed = GsonHelper.fromJson(config.getCnfValue(), EnableRecvConfig.class);
        if (Objects.isNull(parsed)) {
            return new EnableRecvConfig().setEnable(true);
        }
        if (Objects.isNull(parsed.getEnable())) {
            parsed.setEnable(true);
        }
        return parsed;
    }

    @Override
    public void updateEnableRecvConfig(EnableRecvConfig config, Long optUserId) {
        VerifyUtils.notNull(config, "config", "配置信息不能为空", true);
        VerifyUtils.notNull(config.getEnable(), "config.enable", "请指定enable状态", true);
        VerifyUtils.notNull(optUserId, "optUserId", "请指定操作人", true);

        SysConfigDO existed = getConfig(SysConfigTypeEnum.ENABLE_RECV, SysConfigKeys.ENABLE_RECV);
        String value = GsonHelper.toJson(config);
        if (Objects.isNull(existed)) {
            addConfig(new SysConfigDO()
                    .setCnfType(SysConfigTypeEnum.ENABLE_RECV.name())
                    .setCnfKey(SysConfigKeys.ENABLE_RECV)
                    .setCnfDesc(SysConfigTypeEnum.ENABLE_RECV.desc)
                    .setCnfValue(value), optUserId);
            return;
        }
        updateConfig(new SysConfigDO()
                .setId(existed.getId())
                .setCnfType(SysConfigTypeEnum.ENABLE_RECV.name())
                .setCnfKey(SysConfigKeys.ENABLE_RECV)
                .setCnfDesc(SysConfigTypeEnum.ENABLE_RECV.desc)
                .setCnfValue(value), optUserId);
    }

    private SysConfigDO loadConfigFromDb(String cacheKey) {
        if (StringUtils.isBlank(cacheKey)) {
            return null;
        }
        int idx = cacheKey.indexOf(CACHE_KEY_SEP);
        if (idx <= 0 || idx >= cacheKey.length() - CACHE_KEY_SEP.length()) {
            return null;
        }
        String cnfType = cacheKey.substring(0, idx);
        String cnfKey = cacheKey.substring(idx + CACHE_KEY_SEP.length());
        return sysConfigManager.getByTypeAndKey(cnfType, cnfKey);
    }

    private String buildCacheKey(String cnfType, String cnfKey) {
        return cnfType + CACHE_KEY_SEP + cnfKey;
    }
}
