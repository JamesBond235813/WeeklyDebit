package com.jhl.silver.union.biz.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.jhl.silver.union.biz.config.BizProperty;
import com.jhl.silver.union.biz.customer.dal.entity.CustomerInfoItemDO;
import com.jhl.silver.union.biz.customer.manager.CustomerInfoItemManager;
import com.jhl.silver.union.biz.customer.service.BizConfigService;
import com.jhl.silver.union.biz.customer.service.CustDispatchService;
import com.jhl.silver.union.biz.customer.service.CustNoticeService;
import com.jhl.silver.union.biz.customer.service.FeishuBotNotifyService;
import com.jhl.silver.union.biz.customer.service.MobileAreaService;
import com.jhl.silver.union.biz.customer.service.impl.ImportCustDataServiceImpl;
import com.jhl.silver.union.biz.data.IdCardAreaInfo;
import com.jhl.silver.union.biz.data.convert.CustImportRecordConvert;
import com.jhl.silver.union.biz.data.excel.CustomerInfoExcelRowInfo;
import com.jhl.silver.union.biz.region.service.IdCardAreaService;
import com.jhl.silver.union.biz.risk.service.RiskDispatchAdmissionService;
import com.jhl.silver.union.commons.CommonConstant;
import com.jhl.silver.union.biz.user.service.UserService;
import com.jhl.silver.union.web.data.admin.PushCustInfoItem;
import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

class ImportCustDataFeishuNotifyTest {

    @Test
    void apiPushedInsertedCustomerTriggersFeishuNotice() {
        ImportCustDataServiceImpl service = buildService();
        FeishuBotNotifyService feishuBotNotifyService =
                (FeishuBotNotifyService) ReflectionTestUtils.getField(service, "feishuBotNotifyService");

        service.addApiPushedCustInfo(List.of(pushItem()), 0L, 0L);

        Mockito.verify(feishuBotNotifyService).notifyApiInsertedCustomers(Mockito.argThat(list ->
                list != null
                        && list.size() == 1
                        && "张三".equals(list.get(0).getName())
                        && "北京市".equals(list.get(0).getHukouProvince())
                        && "北京市".equals(list.get(0).getHukouCity())
                        && "东城区".equals(list.get(0).getHukouDistrict())
                        && "n_20123".equals(list.get(0).getUserSource())));
    }

    @Test
    void manualInsertedCustomerDoesNotTriggerFeishuNotice() {
        ImportCustDataServiceImpl service = buildService();
        FeishuBotNotifyService feishuBotNotifyService =
                (FeishuBotNotifyService) ReflectionTestUtils.getField(service, "feishuBotNotifyService");

        service.addCustInfo(List.of(pushItem()), 0L, 0L);

        Mockito.verify(feishuBotNotifyService, Mockito.never()).notifyApiInsertedCustomers(Mockito.any());
    }

    @Test
    void apiPushedQualifiedCustomerGetsStarWhenNoOnlineAssignee() {
        ImportCustDataServiceImpl service = buildService();
        CustDispatchService custDispatchService =
                (CustDispatchService) ReflectionTestUtils.getField(service, "custDispatchService");
        RiskDispatchAdmissionService admissionService =
                (RiskDispatchAdmissionService) ReflectionTestUtils.getField(service, "riskDispatchAdmissionService");
        FeishuBotNotifyService feishuBotNotifyService =
                (FeishuBotNotifyService) ReflectionTestUtils.getField(service, "feishuBotNotifyService");

        Mockito.when(custDispatchService.isAutoModeActive()).thenReturn(true);
        Mockito.when(custDispatchService.pickAutoAssignee(0L)).thenReturn(java.util.Optional.empty());
        Mockito.when(admissionService.shouldAutoDispatch("张三", "110101199001011234", "13800000000", null))
                .thenReturn(true);

        service.addApiPushedCustInfo(List.of(pushItem()), 0L, 0L);

        Mockito.verify(feishuBotNotifyService).notifyApiInsertedCustomers(Mockito.argThat(list ->
                list != null
                        && list.size() == 1
                        && Objects.equals(CommonConstant.YES, list.get(0).getPublicPoolStarFlag())
                        && Long.valueOf(0L).equals(list.get(0).getOwnerUserId())));
    }

    private ImportCustDataServiceImpl buildService() {
        ImportCustDataServiceImpl service = new ImportCustDataServiceImpl();

        CustImportRecordConvert customerConvert = Mockito.mock(CustImportRecordConvert.class);
        Mockito.when(customerConvert.convert2CustomerInfoExcelRowInfo(Mockito.any()))
                .thenReturn(rowInfo());

        CustomerInfoItemManager customerInfoItemManager = Mockito.mock(CustomerInfoItemManager.class);
        Mockito.when(customerInfoItemManager.list(Mockito.<Wrapper<CustomerInfoItemDO>>any())).thenReturn(List.of());
        Mockito.when(customerInfoItemManager.saveBatch(Mockito.any())).thenReturn(true);

        CustDispatchService custDispatchService = Mockito.mock(CustDispatchService.class);
        Mockito.when(custDispatchService.isAutoModeActive()).thenReturn(false);

        ReflectionTestUtils.setField(service, "bizProperty", new BizProperty());
        ReflectionTestUtils.setField(service, "userService", Mockito.mock(UserService.class));
        ReflectionTestUtils.setField(service, "bizConfigService", Mockito.mock(BizConfigService.class));
        ReflectionTestUtils.setField(service, "custDispatchService", custDispatchService);
        ReflectionTestUtils.setField(service, "custNoticeService", Mockito.mock(CustNoticeService.class));
        ReflectionTestUtils.setField(service, "customerInfoItemManager", customerInfoItemManager);
        ReflectionTestUtils.setField(service, "customerConvert", customerConvert);
        ReflectionTestUtils.setField(service, "mobileAreaService", Mockito.mock(MobileAreaService.class));
        IdCardAreaService idCardAreaService = Mockito.mock(IdCardAreaService.class);
        Mockito.when(idCardAreaService.getAreaInfoByIdCard("110101199001011234"))
                .thenReturn(new IdCardAreaInfo()
                        .setProvince("北京市")
                        .setCity("北京市")
                        .setDistrict("东城区"));
        ReflectionTestUtils.setField(service, "idCardAreaService", idCardAreaService);
        ReflectionTestUtils.setField(service, "riskDispatchAdmissionService",
                Mockito.mock(RiskDispatchAdmissionService.class));
        ReflectionTestUtils.setField(service, "feishuBotNotifyService", Mockito.mock(FeishuBotNotifyService.class));
        return service;
    }

    private PushCustInfoItem pushItem() {
        return new PushCustInfoItem()
                .setName("张三")
                .setMobile("13800000000")
                .setIdCardNo("110101199001011234")
                .setUserSource("n_20123");
    }

    private CustomerInfoExcelRowInfo rowInfo() {
        CustomerInfoExcelRowInfo row = new CustomerInfoExcelRowInfo();
        row.setName("张三");
        row.setMobile("13800000000");
        row.setIdCardNo("110101199001011234");
        row.setUserSource("n_20123");
        return row;
    }
}
