package com.jhl.silver.union.biz.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.jhl.silver.union.biz.customer.dal.entity.CustPushRecordDO;
import com.jhl.silver.union.biz.customer.dal.entity.CustomerInfoItemDO;
import com.jhl.silver.union.biz.customer.dal.entity.CustomerInfoItemTraceDO;
import com.jhl.silver.union.biz.customer.manager.CustPushRecordManager;
import com.jhl.silver.union.biz.customer.manager.CustomerInfoItemManager;
import com.jhl.silver.union.biz.customer.manager.CustomerInfoItemTraceManager;
import com.jhl.silver.union.biz.customer.service.impl.CustomerFlowQueryServiceImpl;
import com.jhl.silver.union.biz.dept.service.DeptService;
import com.jhl.silver.union.biz.user.service.UserService;
import com.jhl.silver.union.web.data.customer.CustomerFlowQueryRequest;
import com.jhl.silver.union.web.data.customer.CustomerFlowQueryResult;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.jdbc.core.JdbcTemplate;

class CustomerFlowQueryServiceImplTest {

    @Test
    void describesApiCustomerFlowFromPublicPoolToClaimAndBack() {
        CustomerInfoItemManager customerManager = Mockito.mock(CustomerInfoItemManager.class);
        CustPushRecordManager pushRecordManager = Mockito.mock(CustPushRecordManager.class);
        CustomerInfoItemTraceManager traceManager = Mockito.mock(CustomerInfoItemTraceManager.class);
        UserService userService = Mockito.mock(UserService.class);
        DeptService deptService = Mockito.mock(DeptService.class);
        JdbcTemplate jdbcTemplate = Mockito.mock(JdbcTemplate.class);

        CustomerFlowQueryServiceImpl service = new CustomerFlowQueryServiceImpl(
                customerManager, pushRecordManager, traceManager, userService, deptService, jdbcTemplate);

        Mockito.when(customerManager.list(ArgumentMatchers.<Wrapper<CustomerInfoItemDO>>any()))
                .thenReturn(List.of(new CustomerInfoItemDO()
                        .setId(65879L)
                        .setName("李朝惠")
                        .setMobile("13735775990")
                        .setChannel(1)
                        .setUserSource("n_20123")
                        .setOwnerUserId(0L)
                        .setOwnerDeptId(1L)
                        .setOwnerFavorite(0)
                        .setPublicPoolStarFlag(0)
                        .setApplyDate(Timestamp.valueOf("2026-06-18 09:08:37"))
                        .setGmtCreate(Timestamp.valueOf("2026-06-18 09:08:37"))));
        Mockito.when(pushRecordManager.list(ArgumentMatchers.<Wrapper<CustPushRecordDO>>any()))
                .thenReturn(List.of(new CustPushRecordDO()
                        .setChannelName("花易用")
                        .setType(1)
                        .setExistedFlag(0)
                        .setRemark("{\"code\":0,\"message\":\"success\"}")
                        .setGmtCreate(Timestamp.valueOf("2026-06-18 09:08:37"))));
        Mockito.when(traceManager.list(ArgumentMatchers.<Wrapper<CustomerInfoItemTraceDO>>any()))
                .thenReturn(List.of(
                        trace("2026-06-18 11:58:53", 6L, "yewu003",
                                "[{\"fieldName\":\"ownerUserId\",\"oldValue\":\"0\",\"newValue\":\"6\"},"
                                        + "{\"fieldName\":\"ownerDeptId\",\"oldValue\":\"0\",\"newValue\":\"1\"},"
                                        + "{\"fieldName\":\"publicPoolStarFlag\",\"oldValue\":\"1\",\"newValue\":\"0\"}]"),
                        trace("2026-06-18 12:00:02", 6L, "yewu003",
                                "[{\"fieldName\":\"ownerUserId\",\"oldValue\":\"6\",\"newValue\":\"0\"}]")));
        Mockito.when(jdbcTemplate.queryForList(Mockito.anyString(), Mockito.eq("13735775990"), Mockito.eq(65879L)))
                .thenReturn(List.of(Map.of(
                        "admission_passed", 1,
                        "dispatch_result", "PUBLIC_POOL",
                        "stat_time", Timestamp.valueOf("2026-06-18 09:08:37"),
                        "channel_name", "花易用",
                        "user_source", "n_20123")));
        Mockito.when(userService.getUserRealNames(Mockito.anyCollection()))
                .thenReturn(Map.of(6L, "yewu003"));
        Mockito.when(deptService.getDeptNameByIds(Mockito.anyCollection()))
                .thenReturn(Map.of(1L, "01"));

        CustomerFlowQueryResult result = service.query(new CustomerFlowQueryRequest()
                .setName("李朝惠")
                .setMobile("13735775990"));

        Assertions.assertTrue(result.getSummary().contains("2026-06-18 09:08:37"));
        Assertions.assertTrue(result.getSummary().contains("符合准入"));
        Assertions.assertTrue(result.getSummary().contains("进入公海"));
        Assertions.assertTrue(result.getSummary().contains("yewu003"));
        Assertions.assertEquals(4, result.getNodes().size());
        Assertions.assertEquals("上游入库", result.getNodes().get(0).getTitle());
        Assertions.assertEquals("进入公海", result.getNodes().get(1).getTitle());
        Assertions.assertEquals("业务员领取", result.getNodes().get(2).getTitle());
        Assertions.assertEquals("移回公海", result.getNodes().get(3).getTitle());
    }

    private CustomerInfoItemTraceDO trace(String time, Long optUserId, String optUserName, String json) {
        return new CustomerInfoItemTraceDO()
                .setSourceId(65879L)
                .setUpdType("OTHER")
                .setOptUserId(optUserId)
                .setOptUserRealName(optUserName)
                .setUpdFieldJson(json)
                .setGmtCreate(Date.from(Timestamp.valueOf(time).toInstant()));
    }
}
