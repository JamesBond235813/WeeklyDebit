package com.jhl.silver.union.biz.service;

import com.jhl.silver.union.biz.common.enums.SysConfigTypeEnum;
import com.jhl.silver.union.biz.customer.service.BizConfigService;
import com.jhl.silver.union.biz.data.EnableRecvConfig;
import com.jhl.silver.union.biz.data.RecvThirdPlatDataConfig;
import com.jhl.silver.union.biz.sys.SysConfigKeys;
import com.jhl.silver.union.biz.sys.dal.entity.SysConfigDO;
import com.jhl.silver.union.biz.sys.manager.SysConfigManager;
import com.jhl.silver.union.biz.sys.service.impl.SysConfigServiceImpl;
import com.jhl.silver.union.commons.gson.GsonHelper;
import com.jhl.silver.union.web.data.BizDictItem;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

class SysConfigServiceImplTest {
    private SysConfigManager sysConfigManager;
    private BizConfigService bizConfigService;
    private SysConfigServiceImpl sysConfigService;

    @BeforeEach
    void setup() {
        sysConfigManager = Mockito.mock(SysConfigManager.class);
        bizConfigService = Mockito.mock(BizConfigService.class);
        sysConfigService = new SysConfigServiceImpl();
        ReflectionTestUtils.setField(sysConfigService, "sysConfigManager", sysConfigManager);
        ReflectionTestUtils.setField(sysConfigService, "bizConfigService", bizConfigService);
        sysConfigService.init();

        Mockito.doAnswer(invocation -> {
            SysConfigDO config = invocation.getArgument(0);
            config.setId(1L);
            return true;
        }).when(sysConfigManager).save(Mockito.any(SysConfigDO.class));
    }

    @Test
    void addRecvThirdPlatDataConfigSetsChannelAndKey() {
        BizDictItem item = new BizDictItem().setIntValue(10).setLabel("渠道A");
        Mockito.when(bizConfigService.getSingleBizDictItemByLabel(Mockito.any(), Mockito.anyString()))
                .thenReturn(item);

        RecvThirdPlatDataConfig config = new RecvThirdPlatDataConfig()
                .setAppId("APP_TEST")
                .setChannelName(" 渠道A ");

        Long id = sysConfigService.addRecvThirdPlatDataConfig(config, 1L);
        Assertions.assertEquals(1L, id);

        ArgumentCaptor<SysConfigDO> captor = ArgumentCaptor.forClass(SysConfigDO.class);
        Mockito.verify(sysConfigManager).save(captor.capture());
        SysConfigDO saved = captor.getValue();
        Assertions.assertEquals(SysConfigTypeEnum.RECV_THIRD_PLAT_DATA.name(), saved.getCnfType());
        Assertions.assertEquals("APP_TEST", saved.getCnfKey());

        RecvThirdPlatDataConfig stored =
                GsonHelper.fromJson(saved.getCnfValue(), RecvThirdPlatDataConfig.class);
        Assertions.assertEquals("渠道A", stored.getChannelName());
        Assertions.assertEquals(10L, stored.getChannelId());
    }

    @Test
    void getEnableRecvConfigCreatesDefaultWhenMissing() {
        SysConfigDO existed = new SysConfigDO()
                .setCnfType(SysConfigTypeEnum.ENABLE_RECV.name())
                .setCnfKey(SysConfigKeys.ENABLE_RECV)
                .setCnfValue("{\"enable\":true}");
        Mockito.when(sysConfigManager.getByTypeAndKey(SysConfigTypeEnum.ENABLE_RECV.name(), SysConfigKeys.ENABLE_RECV))
                .thenReturn(null, existed);

        EnableRecvConfig config = sysConfigService.getEnableRecvConfig();
        Assertions.assertNotNull(config);
        Assertions.assertTrue(Boolean.TRUE.equals(config.getEnable()));

        ArgumentCaptor<SysConfigDO> captor = ArgumentCaptor.forClass(SysConfigDO.class);
        Mockito.verify(sysConfigManager).save(captor.capture());
        SysConfigDO saved = captor.getValue();
        Assertions.assertEquals(SysConfigTypeEnum.ENABLE_RECV.name(), saved.getCnfType());
        Assertions.assertEquals(SysConfigKeys.ENABLE_RECV, saved.getCnfKey());
    }

    @Test
    void updateEnableRecvConfigAddsWhenMissing() {
        Mockito.when(sysConfigManager.getByTypeAndKey(SysConfigTypeEnum.ENABLE_RECV.name(), SysConfigKeys.ENABLE_RECV))
                .thenReturn(null);

        sysConfigService.updateEnableRecvConfig(new EnableRecvConfig().setEnable(false), 7L);

        ArgumentCaptor<SysConfigDO> captor = ArgumentCaptor.forClass(SysConfigDO.class);
        Mockito.verify(sysConfigManager).save(captor.capture());
        SysConfigDO saved = captor.getValue();
        Assertions.assertTrue(saved.getCnfValue().contains("\"enable\":false"));
    }

}
