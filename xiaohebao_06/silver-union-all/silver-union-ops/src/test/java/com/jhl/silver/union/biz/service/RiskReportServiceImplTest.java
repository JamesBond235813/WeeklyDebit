package com.jhl.silver.union.biz.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.jhl.silver.union.SilverUnionOpsApplicationTests;
import com.jhl.silver.union.biz.common.enums.UserAuthRoleEnum;
import com.jhl.silver.union.biz.customer.manager.CustomerInfoItemManager;
import com.jhl.silver.union.biz.dept.service.DeptService;
import com.jhl.silver.union.biz.risk.dal.entity.RiskControlReportDO;
import com.jhl.silver.union.biz.risk.integration.PanoramaClient;
import com.jhl.silver.union.biz.risk.service.RiskControlReportService;
import com.jhl.silver.union.biz.risk.service.RiskReportService;
import java.util.List;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.JdbcTemplate;

import jakarta.annotation.Resource;

@Slf4j
class RiskReportServiceImplTest extends SilverUnionOpsApplicationTests {

    @Resource
    private RiskReportService riskReportService;
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
        Mockito.when(panoramaClient.fetchReport(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
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
        Assertions.assertEquals(reportJson, report.getReportJson());
        Mockito.verify(panoramaClient).fetchReport(Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
    }
}
