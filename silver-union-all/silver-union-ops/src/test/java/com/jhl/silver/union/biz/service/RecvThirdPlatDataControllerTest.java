package com.jhl.silver.union.biz.service;

import com.jhl.silver.union.biz.common.utils.AesUtils;
import com.jhl.silver.union.biz.customer.service.CustPushRecordService;
import com.jhl.silver.union.biz.customer.manager.CustomerInfoItemManager;
import com.jhl.silver.union.biz.customer.service.ImportCustDataService;
import com.jhl.silver.union.biz.data.RecvThirdPlatDataConfig;
import com.jhl.silver.union.biz.sys.service.SysConfigService;
import com.jhl.silver.union.commons.gson.GsonHelper;
import com.jhl.silver.union.web.controller.customer.RecvThirdPlatDataController;
import com.jhl.silver.union.web.data.admin.PushCustInfoItem;
import com.jhl.silver.union.web.data.customer.recv.RecvThirdPlatAccessCheckRequest;
import com.jhl.silver.union.web.data.customer.recv.RecvThirdPlatAccessCheckResult;
import com.jhl.silver.union.web.data.customer.recv.RecvThirdPlatApplyCreditRequest;
import com.jhl.silver.union.web.data.customer.recv.RecvThirdPlatCommonRequest;
import com.jhl.silver.union.web.data.customer.recv.RecvThirdPlatCommonResponse;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RecvThirdPlatDataControllerTest {
    private SysConfigService sysConfigService;
    private CustomerInfoItemManager customerInfoItemManager;
    private CustPushRecordService custPushRecordService;
    private ImportCustDataService importCustDataService;
    private RecvThirdPlatDataController controller;

    @BeforeEach
    void setup() {
        sysConfigService = Mockito.mock(SysConfigService.class);
        customerInfoItemManager = Mockito.mock(CustomerInfoItemManager.class);
        custPushRecordService = Mockito.mock(CustPushRecordService.class);
        importCustDataService = Mockito.mock(ImportCustDataService.class);
        controller = new RecvThirdPlatDataController(sysConfigService, customerInfoItemManager,
                custPushRecordService, importCustDataService);

        Mockito.lenient().doAnswer(invocation -> {
            Object arg = invocation.getArgument(0);
            if (arg instanceof com.jhl.silver.union.biz.customer.dal.entity.CustPushRecordDO record) {
                record.setId(1L);
                return record;
            }
            return null;
        }).when(custPushRecordService).saveRecord(Mockito.any());
    }

    @Test
    void accessCheckSuccess() throws Exception {
        CryptoMaterial crypto = CryptoMaterial.generate();
        RecvThirdPlatDataConfig config = crypto.toConfig("APP_TEST");
        Mockito.when(sysConfigService.getRecvThirdPlatDataConfig("APP_TEST")).thenReturn(config);
        Mockito.when(customerInfoItemManager.count(Mockito.any())).thenReturn(1L);

        RecvThirdPlatAccessCheckRequest bizReq = new RecvThirdPlatAccessCheckRequest()
                .setPhoneMd5("md5-phone");
        RecvThirdPlatCommonRequest request = crypto.buildCommonRequest("APP_TEST", bizReq);

        RecvThirdPlatCommonResponse<RecvThirdPlatAccessCheckResult> response = controller.accessCheck(request);
        Assertions.assertEquals(200, response.getCode());
        Assertions.assertNotNull(response.getData());
        Assertions.assertEquals(0, response.getData().getResult());
        Assertions.assertEquals("撞库失败，存在相同手机号", response.getData().getReason());
    }

    @Test
    void applyCreditSuccess() throws Exception {
        CryptoMaterial crypto = CryptoMaterial.generate();
        RecvThirdPlatDataConfig config = crypto.toConfig("APP_TEST");
        Mockito.when(sysConfigService.getRecvThirdPlatDataConfig("APP_TEST")).thenReturn(config);

        RecvThirdPlatApplyCreditRequest bizReq = new RecvThirdPlatApplyCreditRequest()
                .setOrderNo("order-001")
                .setUserName("张三")
                .setIdNo("110101199001011234")
                .setMobile("13800000000");
        RecvThirdPlatCommonRequest request = crypto.buildCommonRequest("APP_TEST", bizReq);

        RecvThirdPlatCommonResponse<Void> response = controller.applyCredit(request);
        Assertions.assertEquals(200, response.getCode());

        ArgumentCaptor<List<PushCustInfoItem>> captor = ArgumentCaptor.forClass(List.class);
        Mockito.verify(importCustDataService).addCustInfo(captor.capture(), Mockito.eq(0L), Mockito.eq(0L));
        List<PushCustInfoItem> items = captor.getValue();
        Assertions.assertEquals(1, items.size());
        PushCustInfoItem item = items.get(0);
        Assertions.assertEquals("张三", item.getName());
        Assertions.assertEquals("13800000000", item.getMobile());
        Assertions.assertEquals("110101199001011234", item.getIdCardNo());
        Assertions.assertEquals(config.getChannelName(), item.getChannelName());
    }

    @Test
    void duplicateRequestIdRejected() throws Exception {
        CryptoMaterial crypto = CryptoMaterial.generate();
        RecvThirdPlatDataConfig config = crypto.toConfig("APP_TEST");
        Mockito.when(sysConfigService.getRecvThirdPlatDataConfig("APP_TEST")).thenReturn(config);
        Mockito.when(custPushRecordService.existsThirdRequest("APP_TEST", "REQ-DUP")).thenReturn(true);

        RecvThirdPlatAccessCheckRequest bizReq = new RecvThirdPlatAccessCheckRequest()
                .setPhoneMd5("md5-phone");
        RecvThirdPlatCommonRequest request = crypto.buildCommonRequest("APP_TEST", "REQ-DUP",
                System.currentTimeMillis(), bizReq);

        RecvThirdPlatCommonResponse<RecvThirdPlatAccessCheckResult> response = controller.accessCheck(request);
        Assertions.assertEquals(400, response.getCode());
        Assertions.assertEquals("requestId重复", response.getMsg());
        Mockito.verify(custPushRecordService, Mockito.never()).saveRecord(Mockito.any());
    }

    @Test
    void expiredTimestampRejected() throws Exception {
        CryptoMaterial crypto = CryptoMaterial.generate();
        long expiredTimestamp = System.currentTimeMillis() - 11 * 60 * 1000L;
        RecvThirdPlatApplyCreditRequest bizReq = new RecvThirdPlatApplyCreditRequest()
                .setOrderNo("order-001")
                .setUserName("张三")
                .setIdNo("110101199001011234")
                .setMobile("13800000000");
        RecvThirdPlatCommonRequest request = crypto.buildCommonRequest("APP_TEST", "REQ-EXPIRED",
                expiredTimestamp, bizReq);

        RecvThirdPlatCommonResponse<Void> response = controller.applyCredit(request);
        Assertions.assertEquals(400, response.getCode());
        Assertions.assertEquals("timestamp超过有效期", response.getMsg());
        Mockito.verify(custPushRecordService, Mockito.never()).saveRecord(Mockito.any());
        Mockito.verify(sysConfigService, Mockito.never()).getRecvThirdPlatDataConfig(Mockito.anyString());
    }

    private static class CryptoMaterial {
        private final String receiverPrivateKey;
        private final String receiverPublicKey;
        private final String senderPrivateKey;
        private final String senderPublicKey;
        private final String aesKey;

        private CryptoMaterial(String receiverPrivateKey, String receiverPublicKey, String senderPrivateKey,
                String senderPublicKey, String aesKey) {
            this.receiverPrivateKey = receiverPrivateKey;
            this.receiverPublicKey = receiverPublicKey;
            this.senderPrivateKey = senderPrivateKey;
            this.senderPublicKey = senderPublicKey;
            this.aesKey = aesKey;
        }

        static CryptoMaterial generate() throws Exception {
            KeyPair receiver = generateRsaKeyPair();
            KeyPair sender = generateRsaKeyPair();
            String receiverPrivate = Base64.getEncoder().encodeToString(receiver.getPrivate().getEncoded());
            String receiverPublic = Base64.getEncoder().encodeToString(receiver.getPublic().getEncoded());
            String senderPrivate = Base64.getEncoder().encodeToString(sender.getPrivate().getEncoded());
            String senderPublic = Base64.getEncoder().encodeToString(sender.getPublic().getEncoded());
            String aesKey = "0123456789abcdef0123456789abcdef";
            return new CryptoMaterial(receiverPrivate, receiverPublic, senderPrivate, senderPublic, aesKey);
        }

        RecvThirdPlatDataConfig toConfig(String appId) {
            return new RecvThirdPlatDataConfig()
                    .setAppId(appId)
                    .setPrivateKey(receiverPrivateKey)
                    .setPublicKey(receiverPublicKey)
                    .setThirdPlatPublicKey(senderPublicKey)
                    .setChannelName("测试渠道")
                    .setChannelId(1L);
        }

        RecvThirdPlatCommonRequest buildCommonRequest(String appId, Object bizData) throws Exception {
            return buildCommonRequest(appId, "REQ-" + System.currentTimeMillis(), System.currentTimeMillis(),
                    bizData);
        }

        RecvThirdPlatCommonRequest buildCommonRequest(String appId, String requestId, long timestamp,
                Object bizData) throws Exception {
            String dataJson = GsonHelper.toJson(bizData);
            String encryptedData = AesUtils.encryptWithAesECB(dataJson, aesKey);
            String encryptedSecretKey = encryptWithRsa(aesKey, receiverPublicKey);
            String signContent = buildSignContent(appId, encryptedData, requestId, encryptedSecretKey, timestamp);
            String sign = signWithRsa(signContent, senderPrivateKey);
            return new RecvThirdPlatCommonRequest()
                    .setAppId(appId)
                    .setData(encryptedData)
                    .setRequestId(requestId)
                    .setTimestamp(timestamp)
                    .setSecretKey(encryptedSecretKey)
                    .setSign(sign);
        }

        private static KeyPair generateRsaKeyPair() throws Exception {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            return generator.generateKeyPair();
        }

        private static String encryptWithRsa(String content, String publicKeyBase64) throws Exception {
            PublicKey publicKey = loadPublicKey(publicKeyBase64);
            javax.crypto.Cipher rsaCipher = javax.crypto.Cipher.getInstance("RSA");
            rsaCipher.init(javax.crypto.Cipher.ENCRYPT_MODE, publicKey);
            byte[] encrypted = rsaCipher.doFinal(content.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        }

        private static String signWithRsa(String content, String privateKeyBase64) throws Exception {
            PrivateKey privateKey = loadPrivateKey(privateKeyBase64);
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(privateKey);
            signature.update(content.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(signature.sign());
        }

        private static PublicKey loadPublicKey(String keyBase64) throws Exception {
            byte[] keyBytes = Base64.getDecoder().decode(keyBase64);
            java.security.spec.X509EncodedKeySpec keySpec = new java.security.spec.X509EncodedKeySpec(keyBytes);
            java.security.KeyFactory keyFactory = java.security.KeyFactory.getInstance("RSA");
            return keyFactory.generatePublic(keySpec);
        }

        private static PrivateKey loadPrivateKey(String keyBase64) throws Exception {
            byte[] keyBytes = Base64.getDecoder().decode(keyBase64);
            java.security.spec.PKCS8EncodedKeySpec keySpec = new java.security.spec.PKCS8EncodedKeySpec(keyBytes);
            java.security.KeyFactory keyFactory = java.security.KeyFactory.getInstance("RSA");
            return keyFactory.generatePrivate(keySpec);
        }

        private static String buildSignContent(String appId, String data, String requestId, String secretKey,
                long timestamp) {
            Map<String, Object> map = new TreeMap<>();
            map.put("appId", appId);
            map.put("data", data);
            map.put("requestId", requestId);
            map.put("secretKey", secretKey);
            map.put("timestamp", timestamp);
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                if (sb.length() > 0) {
                    sb.append("&");
                }
                sb.append(entry.getKey()).append("=").append(entry.getValue());
            }
            return sb.toString();
        }
    }
}
