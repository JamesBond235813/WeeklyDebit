package com.jhl.silver.union.web.controller.customer;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jhl.silver.union.biz.common.enums.ThirdPlatResultCode;
import com.jhl.silver.union.biz.common.utils.AesUtils;
import com.jhl.silver.union.biz.common.utils.RsaUtils;
import com.jhl.silver.union.biz.customer.dal.entity.CustPushRecordDO;
import com.jhl.silver.union.biz.customer.dal.entity.CustomerInfoItemDO;
import com.jhl.silver.union.biz.customer.manager.CustomerInfoItemManager;
import com.jhl.silver.union.biz.customer.service.CustPushRecordService;
import com.jhl.silver.union.biz.customer.service.ImportCustDataService;
import com.jhl.silver.union.biz.data.EnableRecvConfig;
import com.jhl.silver.union.biz.data.RecvThirdPlatDataConfig;
import com.jhl.silver.union.biz.sys.service.SysConfigService;
import com.jhl.silver.union.commons.gson.GsonHelper;
import com.jhl.silver.union.web.data.admin.PushCustInfoItem;
import com.jhl.silver.union.web.data.customer.recv.RecvThirdPlatAccessCheckRequest;
import com.jhl.silver.union.web.data.customer.recv.RecvThirdPlatAccessCheckResult;
import com.jhl.silver.union.web.data.customer.recv.RecvThirdPlatApplyCreditRequest;
import com.jhl.silver.union.web.data.customer.recv.RecvThirdPlatCommonRequest;
import com.jhl.silver.union.web.data.customer.recv.RecvThirdPlatCommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 接收三方平台推送数据接口
 *
 * @author: qingren
 * @create_time: 2026/1/26
 */
@RestController
@RequestMapping("/public/third")
@RequiredArgsConstructor
@Slf4j
public class RecvThirdPlatDataController {
    private static final int TYPE_ACCESS_CHECK = 0;
    private static final int TYPE_APPLY_CREDIT = 1;
    private static final int EXISTED_FLAG_YES = 1;
    private static final int EXISTED_FLAG_NO = 0;
    private static final int ACCESS_DENIED = 0;
    private static final int ACCESS_ALLOWED = 1;
    private static final long REQUEST_TIME_WINDOW_MILLIS = 10 * 60 * 1000L;

    private final SysConfigService sysConfigService;
    private final CustomerInfoItemManager customerInfoItemManager;
    private final CustPushRecordService custPushRecordService;
    private final ImportCustDataService importCustDataService;

    /**
     * 撞库准入接口
     */
    @PostMapping("/access-check")
    @Operation(summary = "撞库准入接口", description = "不校验用户登录信息")
    public RecvThirdPlatCommonResponse<RecvThirdPlatAccessCheckResult> accessCheck(
            @RequestBody RecvThirdPlatCommonRequest request) {
        RecvThirdPlatCommonResponse<RecvThirdPlatAccessCheckResult> response = new RecvThirdPlatCommonResponse<>();
        EnableRecvConfig enableRecvConfig = sysConfigService.getEnableRecvConfig();
        if (enableRecvConfig != null && Boolean.FALSE.equals(enableRecvConfig.getEnable())) {
            return buildFailedResponse(response, ThirdPlatResultCode.REFUSE_APPLY, null, null);
        }
        CustPushRecordDO record = null;
        String decryptedData = null;
        Integer existedFlag = null;
        String phoneMd5 = null;
        try {
            log.info("recv third plat access-check req: {}", GsonHelper.toJson(request));
            RequestContext context = prepareRequest("access-check", request, TYPE_ACCESS_CHECK, response);
            if (context != null) {
                record = context.record;
                decryptedData = context.decryptedData;
            }
            if (context == null || !context.ok) {
                return response;
            }

            RecvThirdPlatAccessCheckRequest bizReq =
                    GsonHelper.fromJson(decryptedData, RecvThirdPlatAccessCheckRequest.class);
            if (bizReq == null || StringUtils.isBlank(bizReq.getPhoneMd5())) {
                return buildFailedResponse(response, ThirdPlatResultCode.INVALID_PARAMS, "phoneMd5不能为空", record);
            }
            phoneMd5 = StringUtils.lowerCase(StringUtils.trim(bizReq.getPhoneMd5()));
            boolean existed = isMobileMd5Existed(phoneMd5);
            RecvThirdPlatAccessCheckResult data = new RecvThirdPlatAccessCheckResult()
                    .setResult(existed ? ACCESS_DENIED : ACCESS_ALLOWED)
                    .setReason(existed ? "撞库失败，存在相同手机号" : null);
            response = buildSuccessResponse(data);

            existedFlag = existed ? EXISTED_FLAG_YES : EXISTED_FLAG_NO;
            return response;
        } catch (Exception e) {
            log.error("recv third plat access-check failed", e);
            return buildFailedResponse(response, ThirdPlatResultCode.SYSTEM_BUSY, null, record);
        } finally {
            if (record != null && record.getId() != null) {
                custPushRecordService.updateRecord(record.getId(), null, phoneMd5, null, decryptedData,
                        existedFlag, GsonHelper.toJson(response));
            }
            logResponse("access-check", response);
        }
    }

    /**
     * 申请授信接口
     */
    @PostMapping("/apply-credit")
    @Operation(summary = "申请授信接口", description = "不校验用户登录信息")
    public RecvThirdPlatCommonResponse<Void> applyCredit(@RequestBody RecvThirdPlatCommonRequest request) {
        RecvThirdPlatCommonResponse<Void> response = new RecvThirdPlatCommonResponse<>();
        EnableRecvConfig enableRecvConfig = sysConfigService.getEnableRecvConfig();
        if (enableRecvConfig != null && Boolean.FALSE.equals(enableRecvConfig.getEnable())) {
            return buildFailedResponse(response, ThirdPlatResultCode.REFUSE_APPLY, null, null);
        }
        CustPushRecordDO record = null;
        String decryptedData = null;
        String custName = null;
        String mobile = null;
        String orderNo = null;
        try {
            log.info("recv third plat apply-credit req: {}", GsonHelper.toJson(request));
            RequestContext context = prepareRequest("apply-credit", request, TYPE_APPLY_CREDIT, response);
            RecvThirdPlatDataConfig config = null;
            if (context != null) {
                record = context.record;
                decryptedData = context.decryptedData;
                config = context.config;
            }
            if (context == null || !context.ok) {
                return response;
            }

            RecvThirdPlatApplyCreditRequest bizReq =
                    GsonHelper.fromJson(decryptedData, RecvThirdPlatApplyCreditRequest.class);
            String errMsg = validateApplyCreditRequest(bizReq);
            if (StringUtils.isNotBlank(errMsg)) {
                return buildFailedResponse(response, ThirdPlatResultCode.INVALID_PARAMS, errMsg, record);
            }
            custName = bizReq.getUserName();
            mobile = bizReq.getMobile();
            orderNo = bizReq.getOrderNo();
            String mobileMd5 = DigestUtils.md5Hex(StringUtils.trim(mobile));
            if (!custPushRecordService.existsPassedAccessCheck(request.getAppId(), mobileMd5)) {
                return buildFailedResponse(response, ThirdPlatResultCode.INVALID_PARAMS,
                        "该手机号未通过撞库或未先调用access-check", record);
            }
            if (isMobileExisted(mobile)) {
                return buildFailedResponse(response, ThirdPlatResultCode.INVALID_PARAMS,
                        "手机号已存在，请勿重复推送", record);
            }

            PushCustInfoItem item = new PushCustInfoItem()
                    .setName(bizReq.getUserName())
                    .setMobile(bizReq.getMobile())
                    .setIdCardNo(bizReq.getIdNo())
                    .setChannelName(config.getChannelName());
            importCustDataService.addApiPushedCustInfo(List.of(item), 0L, 0L);
            response = buildSuccessResponse(null);

            return response;
        } catch (Exception e) {
            log.error("recv third plat apply-credit failed", e);
            return buildFailedResponse(response, ThirdPlatResultCode.SYSTEM_BUSY, null, record);
        } finally {
            if (record != null && record.getId() != null) {
                custPushRecordService.updateRecord(record.getId(), custName, mobile, orderNo, decryptedData,
                        null, GsonHelper.toJson(response));
            }
            logResponse("apply-credit", response);
        }
    }

    private String validateCommonRequest(RecvThirdPlatCommonRequest request) {
        if (request == null) {
            return "request为空";
        }
        if (StringUtils.isBlank(request.getAppId())) {
            return "appId不能为空";
        }
        if (StringUtils.isBlank(request.getData())) {
            return "data不能为空";
        }
        if (StringUtils.isBlank(request.getRequestId())) {
            return "requestId不能为空";
        }
        if (Objects.isNull(request.getTimestamp())) {
            return "timestamp不能为空";
        }
        long now = System.currentTimeMillis();
        if (Math.abs(now - request.getTimestamp()) > REQUEST_TIME_WINDOW_MILLIS) {
            return "timestamp超过有效期";
        }
        if (StringUtils.isBlank(request.getSecretKey())) {
            return "secretKey不能为空";
        }
        if (StringUtils.isBlank(request.getSign())) {
            return "sign不能为空";
        }
        return null;
    }

    private String validateApplyCreditRequest(RecvThirdPlatApplyCreditRequest request) {
        if (request == null) {
            return "请求参数为空";
        }
        if (StringUtils.isBlank(request.getOrderNo())) {
            return "orderNo不能为空";
        }
        if (StringUtils.isBlank(request.getUserName())) {
            return "userName不能为空";
        }
        if (StringUtils.isBlank(request.getIdNo())) {
            return "idNo不能为空";
        }
        if (StringUtils.isBlank(request.getMobile())) {
            return "mobile不能为空";
        }
        return null;
    }

    private boolean isMobileMd5Existed(String phoneMd5) {
        LambdaQueryWrapper<CustomerInfoItemDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CustomerInfoItemDO::getMobileMd5, phoneMd5);
        return customerInfoItemManager.count(wrapper) > 0;
    }

    private boolean isMobileExisted(String mobile) {
        if (StringUtils.isBlank(mobile)) {
            return false;
        }
        LambdaQueryWrapper<CustomerInfoItemDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CustomerInfoItemDO::getMobile, StringUtils.trim(mobile));
        return customerInfoItemManager.count(wrapper) > 0;
    }

    private String buildSignContent(RecvThirdPlatCommonRequest request) {
        Map<String, Object> map = new TreeMap<>();
        map.put("appId", request.getAppId());
        map.put("data", request.getData());
        map.put("requestId", request.getRequestId());
        map.put("secretKey", request.getSecretKey());
        map.put("timestamp", request.getTimestamp());
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (sb.length() > 0) {
                sb.append("&");
            }
            sb.append(entry.getKey()).append("=").append(entry.getValue());
        }
        return sb.toString();
    }

    private CustPushRecordDO buildPushRecord(RecvThirdPlatDataConfig config, RecvThirdPlatCommonRequest request,
            int type, String custName, String mobile, String orderNo, String receivedReq) {
        CustPushRecordDO record = new CustPushRecordDO()
                .setAppId(StringUtils.trimToEmpty(request.getAppId()))
                .setRequestId(StringUtils.trimToEmpty(request.getRequestId()))
                .setType(type)
                .setCustName(StringUtils.trimToEmpty(custName))
                .setMobile(StringUtils.trimToEmpty(mobile))
                .setOrderNo(StringUtils.trimToEmpty(orderNo))
                .setReceivedReq(receivedReq)
                .setRemark("");
        if (Objects.nonNull(config)) {
            record.setChannelName(config.getChannelName());
            if (Objects.nonNull(config.getChannelId())) {
                record.setChannelId(config.getChannelId().intValue());
            }
        }
        return record;
    }

    private <T> RecvThirdPlatCommonResponse<T> buildSuccessResponse(T data) {
        RecvThirdPlatCommonResponse<T> response = new RecvThirdPlatCommonResponse<>();
        response.setCode(ThirdPlatResultCode.SUCCESS.code);
        response.setMsg(ThirdPlatResultCode.SUCCESS.msg);
        response.setData(data);
        return response;
    }

    private <T> RecvThirdPlatCommonResponse<T> buildFailedResponse(RecvThirdPlatCommonResponse<T> response,
            ThirdPlatResultCode code, String detailMsg, CustPushRecordDO record) {
        response.setCode(code.code);
        response.setMsg(StringUtils.isNotBlank(detailMsg) ? detailMsg : code.msg);
        response.setData(null);
        return response;
    }

    private RequestContext prepareRequest(String apiName, RecvThirdPlatCommonRequest request, int type,
            RecvThirdPlatCommonResponse<?> response) {
        String validateMsg = validateCommonRequest(request);
        if (StringUtils.isNotBlank(validateMsg)) {
            buildFailedResponse(response, ThirdPlatResultCode.INVALID_PARAMS, validateMsg, null);
            return new RequestContext(null, null, null, false);
        }
        RecvThirdPlatDataConfig config = sysConfigService.getRecvThirdPlatDataConfig(request.getAppId());
        if (Objects.isNull(config)) {
            buildFailedResponse(response, ThirdPlatResultCode.APP_ID_MISMATCH, null, null);
            return new RequestContext(null, null, null, false);
        }
        if (custPushRecordService.existsThirdRequest(request.getAppId(), request.getRequestId())) {
            buildFailedResponse(response, ThirdPlatResultCode.INVALID_PARAMS, "requestId重复", null);
            return new RequestContext(config, null, null, false);
        }
        CustPushRecordDO record = buildPushRecord(config, request, type, null, null, null, null);
        try {
            custPushRecordService.saveRecord(record);
        } catch (DuplicateKeyException e) {
            log.warn("recv third plat duplicated request, appId={}, requestId={}",
                    request.getAppId(), request.getRequestId(), e);
            buildFailedResponse(response, ThirdPlatResultCode.INVALID_PARAMS, "requestId重复", null);
            return new RequestContext(config, null, null, false);
        }
        boolean signOk = verifySignature(request, config);
        if (!signOk) {
            buildFailedResponse(response, ThirdPlatResultCode.SIGN_VERIFY_FAILED, null, record);
            return new RequestContext(config, record, null, false);
        }
        String decryptedData = decryptRequestData(apiName, request, config);
        if (decryptedData == null) {
            buildFailedResponse(response, ThirdPlatResultCode.DECRYPT_FAILED, null, record);
            return new RequestContext(config, record, null, false);
        }
        return new RequestContext(config, record, decryptedData, true);
    }

    private boolean verifySignature(RecvThirdPlatCommonRequest request, RecvThirdPlatDataConfig config) {
        try {
            String signContent = buildSignContent(request);
            return RsaUtils.verifySign(request.getSign(), signContent, config.getThirdPlatPublicKey());
        } catch (Exception e) {
            log.warn("recv third plat verify sign failed, appId={}", request == null ? null : request.getAppId(), e);
            return false;
        }
    }

    private String decryptRequestData(String apiName, RecvThirdPlatCommonRequest request,
            RecvThirdPlatDataConfig config) {
        try {
            String plainAesKey = RsaUtils.decryptSecretKeyWithRsa(request.getSecretKey(), config.getPrivateKey());
            String decryptedData = AesUtils.decryptWithAesECB(request.getData(), plainAesKey);
            log.info("recv third plat {} decrypted: {}", apiName, decryptedData);
            return decryptedData;
        } catch (Exception e) {
            log.warn("recv third plat {} decrypt failed", apiName, e);
            return null;
        }
    }

    private void logResponse(String apiName, RecvThirdPlatCommonResponse<?> response) {
        if (response == null) {
            return;
        }
        log.info("recv third plat {} resp: {}", apiName, GsonHelper.toJson(response));
    }

    private static class RequestContext {
        private final RecvThirdPlatDataConfig config;
        private final CustPushRecordDO record;
        private final String decryptedData;
        private final boolean ok;

        private RequestContext(RecvThirdPlatDataConfig config, CustPushRecordDO record, String decryptedData,
                boolean ok) {
            this.config = config;
            this.record = record;
            this.decryptedData = decryptedData;
            this.ok = ok;
        }
    }
}
