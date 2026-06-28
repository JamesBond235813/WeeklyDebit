package com.jhl.silver.union.biz.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.jhl.silver.union.biz.risk.dal.entity.RiskControlReportDO;
import com.jhl.silver.union.biz.risk.integration.PanoramaClient;
import com.jhl.silver.union.biz.risk.service.RiskControlReportService;
import com.jhl.silver.union.biz.risk.service.impl.RiskDispatchAdmissionServiceImpl;
import com.jhl.silver.union.web.data.admin.PushCustInfoItem;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

class RiskDispatchAdmissionServiceImplTest {

    @Test
    void permitsWhenUpstreamZhimaAndPanoramaAllPassWithoutCallingProbeC() throws Exception {
        PanoramaClient panoramaClient = Mockito.mock(PanoramaClient.class);
        RiskControlReportService reportService = Mockito.mock(RiskControlReportService.class);
        RiskDispatchAdmissionServiceImpl service =
                new RiskDispatchAdmissionServiceImpl(panoramaClient, reportService);

        Mockito.when(panoramaClient.isEnabled()).thenReturn(true);
        Mockito.when(reportService.list(ArgumentMatchers.<Wrapper<RiskControlReportDO>>any())).thenReturn(List.of());
        Mockito.when(panoramaClient.fetchProductReport("张三", "110101199001011234", "13800000000",
                        PanoramaClient.PANORAMA_PRODUCT_NO))
                .thenReturn(panoramaResponse("600", "1", "0", "75", "3", "510"));
        Mockito.when(reportService.save(Mockito.any())).thenReturn(true);

        PushCustInfoItem item = new PushCustInfoItem()
                .setName("张三")
                .setMobile("13800000000")
                .setIdCardNo("110101199001011234")
                .setUpstreamZhimaCode(2);

        Assertions.assertTrue(service.shouldAutoDispatch(item));
        Mockito.verify(panoramaClient).fetchProductReport(
                Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
                Mockito.eq(PanoramaClient.PANORAMA_PRODUCT_NO));
        Mockito.verify(panoramaClient, Mockito.never()).fetchProductReport(
                Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
                Mockito.eq(PanoramaClient.PROBE_C_PRODUCT_NO));
    }

    @Test
    void permitsWhenEncodedPanoramaMetricsAllPass() throws Exception {
        PanoramaClient panoramaClient = Mockito.mock(PanoramaClient.class);
        RiskControlReportService reportService = Mockito.mock(RiskControlReportService.class);
        RiskDispatchAdmissionServiceImpl service =
                new RiskDispatchAdmissionServiceImpl(panoramaClient, reportService);

        Mockito.when(panoramaClient.isEnabled()).thenReturn(true);
        Mockito.when(reportService.list(ArgumentMatchers.<Wrapper<RiskControlReportDO>>any())).thenReturn(List.of());
        Mockito.when(panoramaClient.fetchProductReport("张三", "110101199001011234", "13800000000",
                        PanoramaClient.PANORAMA_PRODUCT_NO))
                .thenReturn(encodedPanoramaResponse("510", "500", "1", "0", "75%", "3"));
        Mockito.when(reportService.save(Mockito.any())).thenReturn(true);

        PushCustInfoItem item = new PushCustInfoItem()
                .setName("张三")
                .setMobile("13800000000")
                .setIdCardNo("110101199001011234")
                .setUpstreamZhimaCode(2);

        Assertions.assertTrue(service.shouldAutoDispatch(item));
    }

    @Test
    void permitsWhenUpstreamZhimaIsAbsentCodeAndPanoramaAllPass() throws Exception {
        PanoramaClient panoramaClient = Mockito.mock(PanoramaClient.class);
        RiskControlReportService reportService = Mockito.mock(RiskControlReportService.class);
        RiskDispatchAdmissionServiceImpl service =
                new RiskDispatchAdmissionServiceImpl(panoramaClient, reportService);

        Mockito.when(panoramaClient.isEnabled()).thenReturn(true);
        Mockito.when(reportService.list(ArgumentMatchers.<Wrapper<RiskControlReportDO>>any())).thenReturn(List.of());
        Mockito.when(panoramaClient.fetchProductReport("张三", "110101199001011234", "13800000000",
                        PanoramaClient.PANORAMA_PRODUCT_NO))
                .thenReturn(encodedPanoramaResponse("510", "500", "1", "0", "75%", "3"));
        Mockito.when(reportService.save(Mockito.any())).thenReturn(true);

        Assertions.assertTrue(service.shouldAutoDispatch("张三", "110101199001011234", "13800000000", 5));
    }

    @Test
    void permitsWhenEncodedPanoramaAmountUsesPassingRangeValue() throws Exception {
        PanoramaClient panoramaClient = Mockito.mock(PanoramaClient.class);
        RiskControlReportService reportService = Mockito.mock(RiskControlReportService.class);
        RiskDispatchAdmissionServiceImpl service =
                new RiskDispatchAdmissionServiceImpl(panoramaClient, reportService);

        Mockito.when(panoramaClient.isEnabled()).thenReturn(true);
        Mockito.when(reportService.list(ArgumentMatchers.<Wrapper<RiskControlReportDO>>any())).thenReturn(List.of());
        Mockito.when(panoramaClient.fetchProductReport("张三", "110101199001011234", "13800000000",
                        PanoramaClient.PANORAMA_PRODUCT_NO))
                .thenReturn(encodedPanoramaResponse("510", "[500,1000)", "1", "0", "75%", "3"));
        Mockito.when(reportService.save(Mockito.any())).thenReturn(true);

        Assertions.assertTrue(service.shouldAutoDispatch("张三", "110101199001011234", "13800000000", 2));
    }

    @Test
    void rejectsWhenEncodedPanoramaAmountRangeIsBelowThreshold() throws Exception {
        PanoramaClient panoramaClient = Mockito.mock(PanoramaClient.class);
        RiskControlReportService reportService = Mockito.mock(RiskControlReportService.class);
        RiskDispatchAdmissionServiceImpl service =
                new RiskDispatchAdmissionServiceImpl(panoramaClient, reportService);

        Mockito.when(panoramaClient.isEnabled()).thenReturn(true);
        Mockito.when(reportService.list(ArgumentMatchers.<Wrapper<RiskControlReportDO>>any())).thenReturn(List.of());
        Mockito.when(panoramaClient.fetchProductReport("张三", "110101199001011234", "13800000000",
                        PanoramaClient.PANORAMA_PRODUCT_NO))
                .thenReturn(encodedPanoramaResponse("510", "[0,500)", "1", "0", "75%", "3"));
        Mockito.when(reportService.save(Mockito.any())).thenReturn(true);

        Assertions.assertFalse(service.shouldAutoDispatch("张三", "110101199001011234", "13800000000", 2));
    }

    @Test
    void rejectsWhenAnyPanoramaMetricMissing() throws Exception {
        PanoramaClient panoramaClient = Mockito.mock(PanoramaClient.class);
        RiskControlReportService reportService = Mockito.mock(RiskControlReportService.class);
        RiskDispatchAdmissionServiceImpl service =
                new RiskDispatchAdmissionServiceImpl(panoramaClient, reportService);

        Mockito.when(panoramaClient.isEnabled()).thenReturn(true);
        Mockito.when(reportService.list(ArgumentMatchers.<Wrapper<RiskControlReportDO>>any())).thenReturn(List.of(new RiskControlReportDO()
                .setName("张三")
                .setIdCard("110101199001011234")
                .setPhone("13800000000")
                .setQueryTime(new Date())
                .setReportJson("""
                        {"panorama":{"payload":{"data":{"raw_radar_data":{"申请准入分":"510"}}}},"probe_c":{}}
                        """)));

        PushCustInfoItem item = new PushCustInfoItem()
                .setName("张三")
                .setMobile("13800000000")
                .setIdCardNo("110101199001011234")
                .setUpstreamZhimaCode(2);

        Assertions.assertFalse(service.shouldAutoDispatch(item));
        Mockito.verify(panoramaClient, Mockito.never()).fetchProductReport(
                Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
    }

    @Test
    void rejectsWhenPanoramaCallFails() throws Exception {
        PanoramaClient panoramaClient = Mockito.mock(PanoramaClient.class);
        RiskControlReportService reportService = Mockito.mock(RiskControlReportService.class);
        RiskDispatchAdmissionServiceImpl service =
                new RiskDispatchAdmissionServiceImpl(panoramaClient, reportService);

        Mockito.when(panoramaClient.isEnabled()).thenReturn(true);
        Mockito.when(reportService.list(ArgumentMatchers.<Wrapper<RiskControlReportDO>>any())).thenReturn(List.of());
        Mockito.when(panoramaClient.fetchProductReport(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
                        Mockito.eq(PanoramaClient.PANORAMA_PRODUCT_NO)))
                .thenThrow(new RuntimeException("timeout"));

        PushCustInfoItem item = new PushCustInfoItem()
                .setName("张三")
                .setMobile("13800000000")
                .setIdCardNo("110101199001011234")
                .setUpstreamZhimaCode(2);

        Assertions.assertFalse(service.shouldAutoDispatch(item));
    }

    @Test
    void rejectsWhenUpstreamZhimaBelow600OrInvalid() throws Exception {
        PanoramaClient panoramaClient = Mockito.mock(PanoramaClient.class);
        RiskControlReportService reportService = Mockito.mock(RiskControlReportService.class);
        RiskDispatchAdmissionServiceImpl service =
                new RiskDispatchAdmissionServiceImpl(panoramaClient, reportService);

        Assertions.assertFalse(service.shouldAutoDispatch("张三", "110101199001011234", "13800000000", 1));
        Assertions.assertFalse(service.shouldAutoDispatch("张三", "110101199001011234", "13800000000", null));
        Mockito.verifyNoInteractions(panoramaClient, reportService);
    }

    private String panoramaResponse(String recent1MonthAmount, String recent3MonthLoans, String overdue6Month,
            String normalPayRatio, String closedOrders, String accessScore) {
        return """
                {
                  "code": 0,
                  "data": {
                    "raw_radar_data": {
                      "近1个月_履约贷款总金额": "%s",
                      "近3个月_贷款笔数": "%s",
                      "近6个月MO+逾期贷款笔数": "%s",
                      "正常还款订单占贷款总订单数比例": "%s",
                      "贷款已结清订单数": "%s",
                      "申请准入分": "%s"
                    }
                  }
                }
                """.formatted(recent1MonthAmount, recent3MonthLoans, overdue6Month, normalPayRatio,
                closedOrders, accessScore);
    }

    private String encodedPanoramaResponse(String accessScore, String recent1MonthAmount, String recent3MonthLoans,
            String overdue6Month, String normalPayRatio, String closedOrders) {
        return """
                {
                  "code": 200,
                  "data": {
                    "apply_report_detail": {
                      "A22160001": "%s"
                    },
                    "behavior_report_detail": {
                      "B22170040": "%s",
                      "B22170003": "%s",
                      "B22170025": "%s",
                      "B22170034": "%s",
                      "B22170052": "%s"
                    }
                  }
                }
                """.formatted(accessScore, recent1MonthAmount, recent3MonthLoans, overdue6Month, normalPayRatio,
                closedOrders);
    }
}
