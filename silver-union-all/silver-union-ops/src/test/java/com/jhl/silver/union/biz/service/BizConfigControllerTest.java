package com.jhl.silver.union.biz.service;

import com.jhl.silver.union.biz.common.ResultCode;
import com.jhl.silver.union.biz.data.EnableRecvConfig;
import com.jhl.silver.union.biz.data.UserContext;
import com.jhl.silver.union.biz.sys.service.SysConfigService;
import com.jhl.silver.union.biz.user.dal.entity.SuUserInfoDO;
import com.jhl.silver.union.commons.exception.BizException;
import com.jhl.silver.union.web.controller.admin.BizConfigController;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

class BizConfigControllerTest {
    private SysConfigService sysConfigService;
    private BizConfigController controller;

    @BeforeEach
    void setup() {
        sysConfigService = Mockito.mock(SysConfigService.class);
        controller = new BizConfigController();
        ReflectionTestUtils.setField(controller, "sysConfigService", sysConfigService);
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    void getEnableRecvReturnsValue() {
        Mockito.when(sysConfigService.getEnableRecvConfig())
                .thenReturn(new EnableRecvConfig().setEnable(true));

        var result = controller.getEnableRecv();
        Assertions.assertNotNull(result);
        Assertions.assertNotNull(result.getData());
        Assertions.assertTrue(Boolean.TRUE.equals(result.getData().getEnable()));
    }

    @Test
    void updateEnableRecvRequiresSuperRole() {
        UserContext.setUserInfo(new SuUserInfoDO()
                .setId(10L)
                .setRoles("[\"ROLE_DEPT_INFO_ADMIN\"]"));

        BizException ex = Assertions.assertThrows(BizException.class,
                () -> controller.updateEnableRecv(new EnableRecvConfig().setEnable(false)));
        Assertions.assertEquals(ResultCode.SYS_NO_AUTH.code, ex.getCode());
    }

    @Test
    void updateEnableRecvForSuperRole() {
        UserContext.setUserInfo(new SuUserInfoDO()
                .setId(11L)
                .setRoles("[\"ROLE_SUPPER\"]"));

        controller.updateEnableRecv(new EnableRecvConfig().setEnable(false));
        Mockito.verify(sysConfigService).updateEnableRecvConfig(Mockito.any(EnableRecvConfig.class), Mockito.eq(11L));
    }
}
