package com.jhl.silver.union.biz.service;

import com.jhl.silver.union.SilverUnionOpsApplicationTests;
import com.jhl.silver.union.biz.common.enums.SysConfigTypeEnum;
import com.jhl.silver.union.biz.data.PanoramaConfig;
import com.jhl.silver.union.biz.sys.SysConfigKeys;
import com.jhl.silver.union.biz.sys.dal.entity.SysConfigDO;
import com.jhl.silver.union.biz.sys.manager.SysConfigManager;
import com.jhl.silver.union.biz.sys.service.SysConfigService;
import com.jhl.silver.union.commons.gson.GsonHelper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author: qingren
 * @create_time: 2026/1/26
 */
@Slf4j
class SysConfigServiceTest extends SilverUnionOpsApplicationTests {
    private static final Long USER_ID = 1L;

    @Resource
    private SysConfigService sysConfigService;
    @Resource
    private SysConfigManager sysConfigManager;

    @Test
    void crudTest() {
        String cnfKey = "TEST_PANORAMA_" + System.currentTimeMillis();
        Long id = null;
        try {
            PanoramaConfig cfg = new PanoramaConfig();
            cfg.setEnabled(true);
            cfg.setApiUrl("http://localhost/panorama");
            cfg.setMerchantNo("merchant-test");
            cfg.setAccessKey("access-key-test");
            cfg.setSecretKey("secret-key-test");

            SysConfigDO config = new SysConfigDO()
                    .setCnfType(SysConfigTypeEnum.RISK_PANORAMA.name())
                    .setCnfKey(cnfKey)
                    .setCnfValue(GsonHelper.toJson(cfg));

            id = sysConfigService.addConfig(config, USER_ID);
            SysConfigDO saved = sysConfigService.getConfig(SysConfigTypeEnum.RISK_PANORAMA, cnfKey);
            Assertions.assertNotNull(saved);

            PanoramaConfig updatedCfg = new PanoramaConfig();
            updatedCfg.setEnabled(false);
            updatedCfg.setApiUrl("http://localhost/panorama-v2");
            updatedCfg.setMerchantNo("merchant-test-v2");
            updatedCfg.setAccessKey("access-key-test-v2");
            updatedCfg.setSecretKey("secret-key-test-v2");

            SysConfigDO update = new SysConfigDO()
                    .setId(id)
                    .setCnfValue(GsonHelper.toJson(updatedCfg));
            sysConfigService.updateConfig(update, USER_ID);

            SysConfigDO updated = sysConfigService.getConfig(SysConfigTypeEnum.RISK_PANORAMA, cnfKey);
            Assertions.assertNotNull(updated);
            PanoramaConfig parsed = GsonHelper.fromJson(updated.getCnfValue(), PanoramaConfig.class);
            Assertions.assertEquals(updatedCfg.getApiUrl(), parsed.getApiUrl());
            Assertions.assertEquals(updatedCfg.getMerchantNo(), parsed.getMerchantNo());
            Assertions.assertEquals(updatedCfg.getAccessKey(), parsed.getAccessKey());
            Assertions.assertEquals(updatedCfg.getSecretKey(), parsed.getSecretKey());
            Assertions.assertEquals(updatedCfg.isEnabled(), parsed.isEnabled());

            sysConfigService.deleteConfig(id, USER_ID);
            SysConfigDO deleted = sysConfigService.getConfig(SysConfigTypeEnum.RISK_PANORAMA, cnfKey);
            Assertions.assertNull(deleted);
        } finally {
            if (id != null) {
                sysConfigManager.removeById(id);
            }
            sysConfigService.clearCache(SysConfigTypeEnum.RISK_PANORAMA, cnfKey);
        }
    }

}
