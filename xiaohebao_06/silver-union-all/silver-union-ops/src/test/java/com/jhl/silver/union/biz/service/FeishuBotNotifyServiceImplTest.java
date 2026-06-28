package com.jhl.silver.union.biz.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.jhl.silver.union.biz.config.BizProperty;
import com.jhl.silver.union.biz.customer.dal.entity.CustNoticeDO;
import com.jhl.silver.union.biz.customer.dal.entity.CustomerInfoItemDO;
import com.jhl.silver.union.biz.customer.manager.CustNoticeManager;
import com.jhl.silver.union.biz.customer.service.impl.FeishuBotNotifyServiceImpl;
import com.jhl.silver.union.biz.user.service.UserService;
import com.jhl.silver.union.commons.gson.GsonHelper;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(OutputCaptureExtension.class)
class FeishuBotNotifyServiceImplTest {

    private HttpServer server;

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void sendsAssignedUserMessageForApiInsertedCustomer() throws Exception {
        List<String> assignedBodies = new CopyOnWriteArrayList<>();
        List<String> publicPoolBodies = new CopyOnWriteArrayList<>();
        Webhooks webhooks = startWebhookServer(assignedBodies, publicPoolBodies);
        BizProperty bizProperty = new BizProperty();
        bizProperty.setFeishuBotWebhook(webhooks.assignedWebhook());
        bizProperty.setFeishuPublicPoolBotWebhook(webhooks.publicPoolWebhook());
        CustNoticeManager noticeManager = Mockito.mock(CustNoticeManager.class);
        UserService userService = Mockito.mock(UserService.class);
        Mockito.when(userService.getUserRealName(9L)).thenReturn("业务员A");
        FeishuBotNotifyServiceImpl service = new FeishuBotNotifyServiceImpl(bizProperty, noticeManager, userService);

        CustomerInfoItemDO customer = new CustomerInfoItemDO()
                .setName("张三")
                .setMobile("13800000000")
                .setIdCardNo("110101199001011234")
                .setOwnerUserId(9L)
                .setOwnerDeptId(2L)
                .setSourceFileName("花易用")
                .setUserSource("n_20123")
                .setApplyDate(new Date(1781510400000L));

        service.notifyApiInsertedCustomers(List.of(customer));

        Assertions.assertEquals(1, assignedBodies.size());
        Assertions.assertEquals(0, publicPoolBodies.size());
        Assertions.assertEquals("""
                新增入库-->业务员A
                --------------
                姓名：张三
                手机号：13800000000
                身份证号：110101199001011234
                来源：n_20123
                申请时间：2026-06-15 16:00:00
                --------------""", extractText(assignedBodies.get(0)));
    }

    @Test
    void sendsPublicPoolLocationMessageToPublicPoolWebhookOnly() throws Exception {
        List<String> assignedBodies = new CopyOnWriteArrayList<>();
        List<String> publicPoolBodies = new CopyOnWriteArrayList<>();
        Webhooks webhooks = startWebhookServer(assignedBodies, publicPoolBodies);
        BizProperty bizProperty = new BizProperty();
        bizProperty.setFeishuBotWebhook(webhooks.assignedWebhook());
        bizProperty.setFeishuPublicPoolBotWebhook(webhooks.publicPoolWebhook());
        CustNoticeManager noticeManager = Mockito.mock(CustNoticeManager.class);
        Mockito.when(noticeManager.list(Mockito.<Wrapper<CustNoticeDO>>any())).thenReturn(List.of(
                new CustNoticeDO().setCustId(101L),
                new CustNoticeDO().setCustId(102L),
                new CustNoticeDO().setCustId(103L),
                new CustNoticeDO().setCustId(103L)));
        UserService userService = Mockito.mock(UserService.class);
        FeishuBotNotifyServiceImpl service = new FeishuBotNotifyServiceImpl(bizProperty, noticeManager, userService);

        CustomerInfoItemDO customer = new CustomerInfoItemDO()
                .setName("李四")
                .setMobile("13900000000")
                .setIdCardNo("110101199002021234")
                .setOwnerUserId(0L)
                .setSourceFileName("花易用")
                .setUserSource("n_20123")
                .setMobileArea("湖北 武汉 电信")
                .setHukouProvince("西藏自治区")
                .setHukouCity("拉萨市")
                .setHukouDistrict("城关区");

        service.notifyApiInsertedCustomers(List.of(customer));

        Assertions.assertEquals(0, assignedBodies.size());
        Assertions.assertEquals(1, publicPoolBodies.size());
        Assertions.assertEquals("""
                -----------------
                进入公海+3
                手机归属地：湖北 武汉
                身份证归属地：西藏自治区 拉萨市
                -----------------""", extractText(publicPoolBodies.get(0)));
    }

    @Test
    void skipsOnlyRouteWhoseWebhookIsBlank() throws Exception {
        List<String> assignedBodies = new CopyOnWriteArrayList<>();
        List<String> publicPoolBodies = new CopyOnWriteArrayList<>();
        Webhooks webhooks = startWebhookServer(assignedBodies, publicPoolBodies);
        BizProperty bizProperty = new BizProperty();
        bizProperty.setFeishuPublicPoolBotWebhook(webhooks.publicPoolWebhook());
        CustNoticeManager noticeManager = Mockito.mock(CustNoticeManager.class);
        Mockito.when(noticeManager.list(Mockito.<Wrapper<CustNoticeDO>>any())).thenReturn(List.of(
                new CustNoticeDO().setCustId(101L)));
        UserService userService = Mockito.mock(UserService.class);
        FeishuBotNotifyServiceImpl service = new FeishuBotNotifyServiceImpl(bizProperty, noticeManager, userService);

        CustomerInfoItemDO assignedCustomer = new CustomerInfoItemDO().setName("赵六").setOwnerUserId(9L);
        CustomerInfoItemDO publicPoolCustomer = new CustomerInfoItemDO().setName("王五").setOwnerUserId(0L);
        service.notifyApiInsertedCustomers(List.of(assignedCustomer, publicPoolCustomer));

        Assertions.assertEquals(0, assignedBodies.size());
        Assertions.assertEquals(1, publicPoolBodies.size());
    }

    @Test
    void logsFeishuSuccessResponseForAssignedCustomer(CapturedOutput output) throws Exception {
        List<String> assignedBodies = new CopyOnWriteArrayList<>();
        List<String> publicPoolBodies = new CopyOnWriteArrayList<>();
        Webhooks webhooks = startWebhookServer(assignedBodies, publicPoolBodies);
        BizProperty bizProperty = new BizProperty();
        bizProperty.setFeishuBotWebhook(webhooks.assignedWebhook());
        CustNoticeManager noticeManager = Mockito.mock(CustNoticeManager.class);
        UserService userService = Mockito.mock(UserService.class);
        Mockito.when(userService.getUserRealName(9L)).thenReturn("业务员A");
        FeishuBotNotifyServiceImpl service = new FeishuBotNotifyServiceImpl(bizProperty, noticeManager, userService);

        CustomerInfoItemDO customer = new CustomerInfoItemDO()
                .setId(65802L)
                .setName("周伟荣")
                .setMobile("13530983517")
                .setOwnerUserId(9L);

        service.notifyApiInsertedCustomers(List.of(customer));

        Assertions.assertTrue(output.getOut().contains(
                "send feishu api customer notice success, route=ASSIGNED, customerId=65802, mobile=13530983517, ownerUserId=9, httpStatus=200, feishuCode=0"));
    }

    @Test
    void logsFeishuBusinessFailureWhenHttpStatusIsOk(CapturedOutput output) throws Exception {
        List<String> assignedBodies = new CopyOnWriteArrayList<>();
        List<String> publicPoolBodies = new CopyOnWriteArrayList<>();
        Webhooks webhooks = startWebhookServer(assignedBodies, publicPoolBodies, "{\"code\":19001,\"msg\":\"keyword mismatch\"}");
        BizProperty bizProperty = new BizProperty();
        bizProperty.setFeishuBotWebhook(webhooks.assignedWebhook());
        CustNoticeManager noticeManager = Mockito.mock(CustNoticeManager.class);
        UserService userService = Mockito.mock(UserService.class);
        FeishuBotNotifyServiceImpl service = new FeishuBotNotifyServiceImpl(bizProperty, noticeManager, userService);

        CustomerInfoItemDO customer = new CustomerInfoItemDO()
                .setId(65803L)
                .setName("李四")
                .setMobile("13900000000")
                .setOwnerUserId(9L);

        service.notifyApiInsertedCustomers(List.of(customer));

        Assertions.assertTrue(output.getOut().contains(
                "send feishu api customer notice failed, route=ASSIGNED, customerId=65803, mobile=13900000000, ownerUserId=9, httpStatus=200, feishuCode=19001, feishuMsg=keyword mismatch"));
    }

    private Webhooks startWebhookServer(List<String> assignedBodies, List<String> publicPoolBodies) throws IOException {
        return startWebhookServer(assignedBodies, publicPoolBodies, "{\"StatusCode\":0}");
    }

    private Webhooks startWebhookServer(List<String> assignedBodies, List<String> publicPoolBodies, String assignedResponse)
            throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/assigned-hook", exchange -> {
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            assignedBodies.add(body);
            byte[] response = assignedResponse.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, response.length);
            exchange.getResponseBody().write(response);
            exchange.close();
        });
        server.createContext("/public-pool-hook", exchange -> {
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            publicPoolBodies.add(body);
            byte[] response = "{\"StatusCode\":0}".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, response.length);
            exchange.getResponseBody().write(response);
            exchange.close();
        });
        server.start();
        String baseUrl = "http://127.0.0.1:" + server.getAddress().getPort();
        return new Webhooks(baseUrl + "/assigned-hook", baseUrl + "/public-pool-hook");
    }

    private record Webhooks(String assignedWebhook, String publicPoolWebhook) {
    }

    @SuppressWarnings("unchecked")
    private String extractText(String body) {
        Map<String, Object> root = GsonHelper.fromJson(body, Map.class);
        Map<String, Object> content = (Map<String, Object>) root.get("content");
        return String.valueOf(content.get("text"));
    }
}
