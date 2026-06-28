package com.jhl.silver.union.biz.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.jhl.silver.union.SilverUnionOpsApplicationTests;
import com.jhl.silver.union.biz.common.enums.UserAuthRoleEnum;
import com.jhl.silver.union.biz.customer.manager.CustomerInfoItemManager;
import com.jhl.silver.union.biz.dept.service.DeptService;
import com.jhl.silver.union.biz.risk.dal.entity.RiskControlReportDO;
import com.jhl.silver.union.biz.risk.integration.PanoramaClient;
import com.jhl.silver.union.biz.risk.service.RiskControlReportService;
import com.jhl.silver.union.biz.risk.service.impl.RiskReportServiceImpl;
import com.jhl.silver.union.commons.exception.BizException;
import java.util.Date;
import java.util.List;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.JdbcTemplate;

import jakarta.annotation.Resource;

@Slf4j
class RiskReportServiceImplTest extends SilverUnionOpsApplicationTests {

    @Resource
    private RiskReportServiceImpl riskReportService;
    @Resource
    private JdbcTemplate jdbcTemplate;

    @MockBean
    private PanoramaClient panoramaClient;
    @MockBean
    private RiskControlReportService riskControlReportService;
    @MockBean
    private CustomerInfoItemManager customerInfoItemManager;
    @MockBean
    private DeptService deptService;

    @Test
    void getReportMockedPanoramaClient() throws Exception {
        String reportJson = jdbcTemplate.queryForObject(
                "select report_json from risk_control_report where report_json is not null limit 1",
                String.class);
        Assumptions.assumeTrue(StringUtils.isNotBlank(reportJson), "缺少可用的 report_json 测试数据");

        Mockito.when(panoramaClient.isEnabled()).thenReturn(true);
        Mockito.when(panoramaClient.fetchProductReport(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
                Mockito.anyString()))
                .thenReturn(reportJson);
        Mockito.when(customerInfoItemManager.count(Mockito.any())).thenReturn(1L);
        Mockito.when(riskControlReportService.list(ArgumentMatchers.<Wrapper<RiskControlReportDO>>any()))
                .thenReturn(List.of());
        Mockito.when(riskControlReportService.save(Mockito.any())).thenReturn(true);

        RiskControlReportDO report = riskReportService.getReport(
                "测试用户",
                "110101199001011234",
                "13800000000",
                1L,
                1L,
                Set.of(UserAuthRoleEnum.ROLE_SUPPER));

        Assertions.assertNotNull(report);
        Assertions.assertTrue(report.getReportJson().contains("榕树花风控报告"));
        Mockito.verify(panoramaClient, Mockito.times(2)).fetchProductReport(Mockito.anyString(), Mockito.anyString(),
                Mockito.anyString(), Mockito.anyString());
    }

    @Test
    void getReportCompletesProbeCOnlyWhenRecentCachedReportHasRadarOnly() throws Exception {
        RiskControlReportDO cached = new RiskControlReportDO()
                .setId(101L)
                .setName("测试用户")
                .setIdCard("110101199001011234")
                .setPhone("13800000000")
                .setQueryTime(new Date())
                .setReportJson("""
                        {
                          "report_type": "RONGSHUHUA_RISK",
                          "title": "榕树花风控报告",
                          "query_time": "2026-06-10 12:00:00",
                          "cache_days": 30,
                          "user_profile": {
                            "name": "测试用户",
                            "phone": "13800000000",
                            "id_card": "110101199001011234"
                          },
                          "panorama": {
                            "source": "PANORAMA",
                            "query_time": "2026-06-10 12:00:00",
                            "payload": {
                              "data": {
                                "apply_report_detail": {
                                  "A22160001": "600"
                                }
                              }
                            }
                          },
                          "probe_c": {}
                        }
                        """);

        Mockito.when(panoramaClient.isEnabled()).thenReturn(true);
        Mockito.when(riskControlReportService.list(ArgumentMatchers.<Wrapper<RiskControlReportDO>>any()))
                .thenReturn(List.of(cached));
        Mockito.when(riskControlReportService.updateById(Mockito.any())).thenReturn(true);
        Mockito.when(panoramaClient.fetchProductReport(
                        Mockito.eq("测试用户"),
                        Mockito.eq("110101199001011234"),
                        Mockito.eq("13800000000"),
                        Mockito.eq(PanoramaClient.PROBE_C_PRODUCT_NO)))
                .thenReturn("""
                        {
                          "code": 0,
                          "data": {
                            "result_code": "2"
                          }
                        }
                        """);

        RiskControlReportDO report = riskReportService.getReport(
                "测试用户",
                "110101199001011234",
                "13800000000",
                1L,
                1L,
                Set.of(UserAuthRoleEnum.ROLE_SUPPER));

        Assertions.assertSame(cached, report);
        Mockito.verify(panoramaClient, Mockito.never()).fetchProductReport(
                Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
                Mockito.eq(PanoramaClient.PANORAMA_PRODUCT_NO));
        Mockito.verify(panoramaClient, Mockito.times(1)).fetchProductReport(
                Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
                Mockito.eq(PanoramaClient.PROBE_C_PRODUCT_NO));

        ArgumentCaptor<RiskControlReportDO> reportCaptor = ArgumentCaptor.forClass(RiskControlReportDO.class);
        Mockito.verify(riskControlReportService).updateById(reportCaptor.capture());
        RiskControlReportDO updated = reportCaptor.getValue();
        Assertions.assertEquals(101L, updated.getId());
        Assertions.assertTrue(updated.getReportJson().contains("\"probe_c\""));
        Assertions.assertTrue(updated.getReportJson().contains("\"result_label\":\"正常履约\""));
        Assertions.assertTrue(updated.getReportJson().contains("\"A22160001\":\"600\""));
    }

    @Test
    void getRadarReportReturnsRecentPanoramaCacheWithoutProbeC() throws Exception {
        RiskControlReportDO cached = new RiskControlReportDO()
                .setId(102L)
                .setName("测试用户")
                .setIdCard("110101199001011234")
                .setPhone("13800000000")
                .setQueryTime(new Date())
                .setReportJson("""
                        {
                          "report_type": "RONGSHUHUA_RISK",
                          "title": "榕树花风控报告",
                          "query_time": "2026-06-10 12:00:00",
                          "cache_days": 30,
                          "user_profile": {
                            "name": "测试用户",
                            "phone": "13800000000",
                            "id_card": "110101199001011234"
                          },
                          "panorama": {
                            "source": "PANORAMA",
                            "query_time": "2026-06-10 12:00:00",
                            "payload": {
                              "data": {
                                "apply_report_detail": {
                                  "A22160001": "600"
                                }
                              }
                            }
                          },
                          "probe_c": {}
                        }
                        """);

        Mockito.when(panoramaClient.isEnabled()).thenReturn(true);
        Mockito.when(riskControlReportService.list(ArgumentMatchers.<Wrapper<RiskControlReportDO>>any()))
                .thenReturn(List.of(cached));

        RiskControlReportDO report = riskReportService.getRadarReport(
                "测试用户",
                "110101199001011234",
                "13800000000",
                1L,
                1L,
                Set.of(UserAuthRoleEnum.ROLE_SUPPER));

        Assertions.assertSame(cached, report);
        Mockito.verify(panoramaClient, Mockito.never()).fetchProductReport(
                Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
                Mockito.eq(PanoramaClient.PROBE_C_PRODUCT_NO));
        Mockito.verify(riskControlReportService, Mockito.never()).updateById(Mockito.any());
        Mockito.verify(riskControlReportService, Mockito.never()).save(Mockito.any());
    }

    @Test
    void getRadarReportFetchesPanoramaOnlyWhenNoCache() throws Exception {
        Mockito.when(panoramaClient.isEnabled()).thenReturn(true);
        Mockito.when(riskControlReportService.list(ArgumentMatchers.<Wrapper<RiskControlReportDO>>any()))
                .thenReturn(List.of());
        Mockito.when(riskControlReportService.save(Mockito.any())).thenReturn(true);
        Mockito.when(panoramaClient.fetchProductReport(
                        Mockito.eq("测试用户"),
                        Mockito.eq("110101199001011234"),
                        Mockito.eq("13800000000"),
                        Mockito.eq(PanoramaClient.PANORAMA_PRODUCT_NO)))
                .thenReturn("""
                        {
                          "code": 0,
                          "data": {
                            "apply_report_detail": {
                              "A22160001": "600"
                            }
                          }
                        }
                        """);

        RiskControlReportDO report = riskReportService.getRadarReport(
                "测试用户",
                "110101199001011234",
                "13800000000",
                1L,
                1L,
                Set.of(UserAuthRoleEnum.ROLE_SUPPER));

        Assertions.assertNotNull(report);
        Assertions.assertTrue(report.getReportJson().contains("\"panorama\""));
        Assertions.assertTrue(report.getReportJson().contains("\"probe_c\":{}"));
        Mockito.verify(panoramaClient, Mockito.times(1)).fetchProductReport(
                Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
                Mockito.eq(PanoramaClient.PANORAMA_PRODUCT_NO));
        Mockito.verify(panoramaClient, Mockito.never()).fetchProductReport(
                Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
                Mockito.eq(PanoramaClient.PROBE_C_PRODUCT_NO));
        Mockito.verify(riskControlReportService, Mockito.times(1)).save(Mockito.any());
    }

    @Test
    void observerRadarReportReturnsCacheOnlyAndDoesNotCallPanoramaWhenMissing() throws Exception {
        Mockito.when(panoramaClient.isEnabled()).thenReturn(true);
        Mockito.when(riskControlReportService.list(ArgumentMatchers.<Wrapper<RiskControlReportDO>>any()))
                .thenReturn(List.of());

        Assertions.assertThrows(BizException.class, () -> riskReportService.getRadarReport(
                "测试用户",
                "110101199001011234",
                "13800000000",
                1L,
                1L,
                Set.of(UserAuthRoleEnum.ROLE_OBSERVER)));

        Mockito.verify(panoramaClient, Mockito.never()).fetchProductReport(
                Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
        Mockito.verify(riskControlReportService, Mockito.never()).save(Mockito.any());
    }

    @Test
    void observerRiskReportDoesNotCompleteProbeCWhenCacheOnlyHasRadar() throws Exception {
        RiskControlReportDO cached = new RiskControlReportDO()
                .setId(103L)
                .setName("测试用户")
                .setIdCard("110101199001011234")
                .setPhone("13800000000")
                .setQueryTime(new Date())
                .setReportJson("""
                        {
                          "report_type": "RONGSHUHUA_RISK",
                          "title": "榕树花风控报告",
                          "query_time": "2026-06-10 12:00:00",
                          "cache_days": 30,
                          "user_profile": {
                            "name": "测试用户",
                            "phone": "13800000000",
                            "id_card": "110101199001011234"
                          },
                          "panorama": {
                            "source": "PANORAMA",
                            "query_time": "2026-06-10 12:00:00",
                            "payload": {
                              "data": {
                                "apply_report_detail": {
                                  "A22160001": "600"
                                }
                              }
                            }
                          },
                          "probe_c": {}
                        }
                        """);

        Mockito.when(panoramaClient.isEnabled()).thenReturn(true);
        Mockito.when(riskControlReportService.list(ArgumentMatchers.<Wrapper<RiskControlReportDO>>any()))
                .thenReturn(List.of(cached));

        Assertions.assertThrows(BizException.class, () -> riskReportService.getReport(
                "测试用户",
                "110101199001011234",
                "13800000000",
                1L,
                1L,
                Set.of(UserAuthRoleEnum.ROLE_OBSERVER)));

        Mockito.verify(panoramaClient, Mockito.never()).fetchProductReport(
                Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
        Mockito.verify(riskControlReportService, Mockito.never()).updateById(Mockito.any());
    }
}
